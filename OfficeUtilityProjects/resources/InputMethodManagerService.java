package com.android.server;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.InputMethodSubtypeSwitchingController;
import com.android.internal.inputmethod.InputMethodSubtypeSwitchingController.ImeSubtypeListItem;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.inputmethod.InputMethodUtils.InputMethodSettings;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.InputBindResult;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wm.WindowManagerService;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.IUserSwitchObserver;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.Process;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.LruCache;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.view.inputmethod.InputMethodSubtype.InputMethodSubtypeBuilder;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
public class InputMethodManagerService extends IInputMethodManager.Stub
        implements ServiceConnection, Handler.Callback {
    static final boolean DEBUG = false;
    static final String TAG = "InputMethodManagerService";
    static final int MSG_SHOW_IM_PICKER = 1;
    static final int MSG_SHOW_IM_SUBTYPE_PICKER = 2;
    static final int MSG_SHOW_IM_SUBTYPE_ENABLER = 3;
    static final int MSG_SHOW_IM_CONFIG = 4;
    static final int MSG_UNBIND_INPUT = 1000;
    static final int MSG_BIND_INPUT = 1010;
    static final int MSG_SHOW_SOFT_INPUT = 1020;
    static final int MSG_HIDE_SOFT_INPUT = 1030;
    static final int MSG_ATTACH_TOKEN = 1040;
    static final int MSG_CREATE_SESSION = 1050;
    static final int MSG_START_INPUT = 2000;
    static final int MSG_RESTART_INPUT = 2010;
    static final int MSG_UNBIND_METHOD = 3000;
    static final int MSG_BIND_METHOD = 3010;
    static final int MSG_SET_ACTIVE = 3020;
    static final int MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER = 3040;
    static final int MSG_HARD_KEYBOARD_SWITCH_CHANGED = 4000;
    static final long TIME_TO_RECONNECT = 3 * 1000;
    static final int SECURE_SUGGESTION_SPANS_MAX_SIZE = 20;
    private static final int NOT_A_SUBTYPE_ID = InputMethodUtils.NOT_A_SUBTYPE_ID;
    private static final String TAG_TRY_SUPPRESSING_IME_SWITCHER = "TrySuppressingImeSwitcher";
    final Context mContext;
    final Resources mRes;
    final Handler mHandler;
    final InputMethodSettings mSettings;
    final SettingsObserver mSettingsObserver;
    final IWindowManager mIWindowManager;
    final HandlerCaller mCaller;
    final boolean mHasFeature;
    private InputMethodFileManager mFileManager;
    private final HardKeyboardListener mHardKeyboardListener;
    private final WindowManagerService mWindowManagerService;
    private final AppOpsManager mAppOpsManager;
    final InputBindResult mNoBinding = new InputBindResult(null, null, null, -1, -1);
    final ArrayList<InputMethodInfo> mMethodList = new ArrayList<InputMethodInfo>();
    final HashMap<String, InputMethodInfo> mMethodMap = new HashMap<String, InputMethodInfo>();
    private final LruCache<SuggestionSpan, InputMethodInfo> mSecureSuggestionSpans =
            new LruCache<SuggestionSpan, InputMethodInfo>(SECURE_SUGGESTION_SPANS_MAX_SIZE);
    private final InputMethodSubtypeSwitchingController mSwitchingController;
    final ServiceConnection mVisibleConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
    };
    boolean mVisibleBound = false;
    private NotificationManager mNotificationManager;
    private KeyguardManager mKeyguardManager;
    private StatusBarManagerService mStatusBar;
    private Notification mImeSwitcherNotification;
    private PendingIntent mImeSwitchPendingIntent;
    private boolean mShowOngoingImeSwitcherForPhones;
    private boolean mNotificationShown;
    private final boolean mImeSelectedOnBoot;
    static class SessionState {
        final ClientState client;
        final IInputMethod method;
        IInputMethodSession session;
        InputChannel channel;
        @Override
        public String toString() {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return "SessionState{uid " + client.uid + " pid " + client.pid+ " method " + Integer.toHexString(System.identityHashCode(method))+ " session " + Integer.toHexString(System.identityHashCode(session))+ " channel " + channel+ "}";
        }
        SessionState(ClientState _client, IInputMethod _method,
                IInputMethodSession _session, InputChannel _channel) {
                    Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            client = _client;
            method = _method;
            session = _session;
            channel = _channel;
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
    }
    static final class ClientState {
        final IInputMethodClient client;
        final IInputContext inputContext;
        final int uid;
        final int pid;
        final InputBinding binding;
        boolean sessionRequested;
        SessionState curSession;
        @Override
        public String toString() {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return "ClientState{" + Integer.toHexString(System.identityHashCode(this)) + " uid " + uid+ " pid " + pid + "}";
        }
        ClientState(IInputMethodClient _client, IInputContext _inputContext,
                int _uid, int _pid) {
                    Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            client = _client;
            inputContext = _inputContext;
            uid = _uid;
            pid = _pid;
            binding = new InputBinding(null, inputContext.asBinder(), uid, pid);
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
    }
    final HashMap<IBinder, ClientState> mClients
            = new HashMap<IBinder, ClientState>();
    boolean mSystemReady;
    String mCurMethodId;
    int mCurSeq;
    ClientState mCurClient;
    IBinder mCurFocusedWindow;
    IInputContext mCurInputContext;
    EditorInfo mCurAttribute;
    String mCurId;
    private InputMethodSubtype mCurrentSubtype;
    private final HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>>
            mShortcutInputMethodsAndSubtypes =
                new HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>>();
    private boolean mCurClientInKeyguard;
    boolean mHaveConnection;
    boolean mShowRequested;
    boolean mShowExplicitlyRequested;
    boolean mShowForced;
    boolean mInputShown;
    Intent mCurIntent;
    IBinder mCurToken;
    IInputMethod mCurMethod;
    long mLastBindTime;
    boolean mBoundToMethod;
    SessionState mEnabledSession;
    boolean mScreenOn = true;
    int mCurUserActionNotificationSequenceNumber = 0;
    int mBackDisposition = InputMethodService.BACK_DISPOSITION_DEFAULT;
    int mImeWindowVis;
    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mSwitchingDialog;
    private View mSwitchingDialogTitleView;
    private InputMethodInfo[] mIms;
    private int[] mSubtypeIds;
    private Locale mLastSystemLocale;
    private boolean mShowImeWithHardKeyboard;
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    private final IPackageManager mIPackageManager;
    class SettingsObserver extends ContentObserver {
        String mLastEnabled = "";
        SettingsObserver(Handler handler) {
            super(handler);
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.DEFAULT_INPUT_METHOD), false, this);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.ENABLED_INPUT_METHODS), false, this);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE), false, this);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD), false, this);
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        @Override public void onChange(boolean selfChange, Uri uri) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            final Uri showImeUri =
                    Settings.Secure.getUriFor(Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD);
            synchronized (mMethodMap) {
                if (showImeUri.equals(uri)) {
                    updateKeyboardFromSettingsLocked();
                } else {
                    boolean enabledChanged = false;
                    String newEnabled = mSettings.getEnabledInputMethodsStr();
                    if (!mLastEnabled.equals(newEnabled)) {
                        mLastEnabled = newEnabled;
                        enabledChanged = true;
                    }
                    updateInputMethodsFromSettingsLocked(enabledChanged);
                }
            }
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
    }
    class ImmsBroadcastReceiver extends android.content.BroadcastReceiver {
        private void updateActive() {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            if (mCurClient != null && mCurClient.client != null) {
                executeOrSendMessage(mCurClient.client, mCaller.obtainMessageIO(
                        MSG_SET_ACTIVE, mScreenOn ? 1 : 0, mCurClient));
            }
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenOn = true;
                refreshImeWindowVisibilityLocked();
                updateActive();
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenOn = false;
                setImeWindowVisibilityStatusHiddenLocked();
                updateActive();
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                hideInputMethodMenu();
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
            } else if (Intent.ACTION_USER_ADDED.equals(action)|| Intent.ACTION_USER_REMOVED.equals(action)) {
                updateCurrentProfileIds();
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
            } else {
                Slog.w(TAG, "Unexpected intent " + intent);
            }
        }
    }
    class MyPackageMonitor extends PackageMonitor {
        private boolean isChangingPackagesOfCurrentUser() {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            final int userId = getChangingUserId();
            final boolean retval = userId == mSettings.getCurrentUserId();
            if (DEBUG) {
                if (!retval) {
                    Slog.d(TAG, "--- ignore this call back from a background user: " + userId);
                }
            }
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return retval;
        }
        @Override
        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            if (!isChangingPackagesOfCurrentUser()) {
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
            }
            synchronized (mMethodMap) {
                String curInputMethodId = mSettings.getSelectedInputMethod();
                final int N = mMethodList.size();
                if (curInputMethodId != null) {
                    for (int i=0; i<N; i++) {
                        InputMethodInfo imi = mMethodList.get(i);
                        if (imi.getId().equals(curInputMethodId)) {
                            for (String pkg : packages) {
                                if (imi.getPackageName().equals(pkg)) {
                                    if (!doit) {
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                                    return true;
                                    }
                                    resetSelectedInputMethodAndSubtypeLocked("");
                                    chooseNewDefaultIMELocked();
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
        }
        @Override
        public void onSomePackagesChanged() {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            if (!isChangingPackagesOfCurrentUser()) {
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return;
            }
            synchronized (mMethodMap) {
                InputMethodInfo curIm = null;
                String curInputMethodId = mSettings.getSelectedInputMethod();
                final int N = mMethodList.size();
                if (curInputMethodId != null) {
                    for (int i=0; i<N; i++) {
                        InputMethodInfo imi = mMethodList.get(i);
                        final String imiId = imi.getId();
                        if (imiId.equals(curInputMethodId)) {
                            curIm = imi;
                        }
                        int change = isPackageDisappearing(imi.getPackageName());
                        if (isPackageModified(imi.getPackageName())) {
                            mFileManager.deleteAllInputMethodSubtypes(imiId);
                        }
                        if (change == PACKAGE_TEMPORARY_CHANGE|| change == PACKAGE_PERMANENT_CHANGE) {
                            Slog.i(TAG, "Input method uninstalled, disabling: "
                                    + imi.getComponent());
                            setInputMethodEnabledLocked(imi.getId(), false);
                        }
                    }
                }
                buildInputMethodListLocked(
                        mMethodList, mMethodMap, false );
                boolean changed = false;
                if (curIm != null) {
                    int change = isPackageDisappearing(curIm.getPackageName()); 
                    if (change == PACKAGE_TEMPORARY_CHANGE|| change == PACKAGE_PERMANENT_CHANGE) {
                        ServiceInfo si = null;
                        try {
                            si = mIPackageManager.getServiceInfo(
                                    curIm.getComponent(), 0, mSettings.getCurrentUserId());
                        } catch (RemoteException ex) {
                        }
                        if (si == null) {
                            Slog.i(TAG, "Current input method removed: " + curInputMethodId);
                            setImeWindowVisibilityStatusHiddenLocked();
                            if (!chooseNewDefaultIMELocked()) {
                                changed = true;
                                curIm = null;
                                Slog.i(TAG, "Unsetting current input method");
                                resetSelectedInputMethodAndSubtypeLocked("");
                            }
                        }
                    }
                }
                if (curIm == null) {
                    changed = chooseNewDefaultIMELocked();
                }
                if (changed) {
                    updateFromSettingsLocked(false);
                }
            }
        }
    }
    private static final class MethodCallback extends IInputSessionCallback.Stub {
        private final InputMethodManagerService mParentIMMS;
        private final IInputMethod mMethod;
        private final InputChannel mChannel;
        MethodCallback(InputMethodManagerService imms, IInputMethod method,
                InputChannel channel) {
                    Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            mParentIMMS = imms;
            mMethod = method;
            mChannel = channel;
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        @Override
        public void sessionCreated(IInputMethodSession session) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            long ident = Binder.clearCallingIdentity();
            try {
                mParentIMMS.onSessionCreated(mMethod, session, mChannel);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
    }
    private class HardKeyboardListener
            implements WindowManagerService.OnHardKeyboardStatusChangeListener {
        @Override
        public void onHardKeyboardStatusChange(boolean available) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            mHandler.sendMessage(mHandler.obtainMessage(MSG_HARD_KEYBOARD_SWITCH_CHANGED,
                        available ? 1 : 0));
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        public void handleHardKeyboardStatusChange(boolean available) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            if (DEBUG) {
                Slog.w(TAG, "HardKeyboardStatusChanged: available=" + available);
            }
            synchronized(mMethodMap) {
                if (mSwitchingDialog != null && mSwitchingDialogTitleView != null&& mSwitchingDialog.isShowing()) {
                    mSwitchingDialogTitleView.findViewById(
                            com.android.internal.R.id.hard_keyboard_section).setVisibility(
                                    available ? View.VISIBLE : View.GONE);
                }
            }
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
    }
    public InputMethodManagerService(Context context, WindowManagerService windowManager) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        mIPackageManager = AppGlobals.getPackageManager();
        mContext = context;
        mRes = context.getResources();
        mHandler = new Handler(this);
        mIWindowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        mCaller = new HandlerCaller(context, null, new HandlerCaller.Callback() {
            @Override
            public void executeMessage(Message msg) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                handleMessage(msg);
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            }
        }, true );
        mWindowManagerService = windowManager;
        mAppOpsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
        mHardKeyboardListener = new HardKeyboardListener();
        mHasFeature = context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_INPUT_METHODS);
        mImeSwitcherNotification = new Notification();
        mImeSwitcherNotification.icon = com.android.internal.R.drawable.ic_notification_ime_default;
        mImeSwitcherNotification.when = 0;
        mImeSwitcherNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mImeSwitcherNotification.tickerText = null;
        mImeSwitcherNotification.defaults = 0; 
        mImeSwitcherNotification.sound = null;
        mImeSwitcherNotification.vibrate = null;
        mImeSwitcherNotification.extras.putBoolean(Notification.EXTRA_ALLOW_DURING_SETUP, true);
        mImeSwitcherNotification.category = Notification.CATEGORY_SYSTEM;
        Intent intent = new Intent(Settings.ACTION_SHOW_INPUT_METHOD_PICKER);
        mImeSwitchPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        mShowOngoingImeSwitcherForPhones = false;
        final IntentFilter broadcastFilter = new IntentFilter();
        broadcastFilter.addAction(Intent.ACTION_SCREEN_ON);
        broadcastFilter.addAction(Intent.ACTION_SCREEN_OFF);
        broadcastFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        broadcastFilter.addAction(Intent.ACTION_USER_ADDED);
        broadcastFilter.addAction(Intent.ACTION_USER_REMOVED);
        mContext.registerReceiver(new ImmsBroadcastReceiver(), broadcastFilter);
        mNotificationShown = false;
        int userId = 0;
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(
                    new IUserSwitchObserver.Stub() {
                        @Override
                        public void onUserSwitching(int newUserId, IRemoteCallback reply) {
                            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                            synchronized(mMethodMap) {
                                switchUserLocked(newUserId);
                            }
                            if (reply != null) {
                                try {
                                    reply.sendResult(null);
                                } catch (RemoteException e) {
                                }
                            }
                        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        }
                        @Override
                        public void onUserSwitchComplete(int newUserId) throws RemoteException {
                        }
                    });
            userId = ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Couldn't get current user ID; guessing it's 0", e);
        }
        mMyPackageMonitor.register(mContext, null, UserHandle.ALL, true);
        mSettings = new InputMethodSettings(
                mRes, context.getContentResolver(), mMethodMap, mMethodList, userId);
        updateCurrentProfileIds();
        mFileManager = new InputMethodFileManager(mMethodMap, userId);
        synchronized (mMethodMap) {
            mSwitchingController = InputMethodSubtypeSwitchingController.createInstanceLocked(
                    mSettings, context);
        }
        final String defaultImiId = mSettings.getSelectedInputMethod();
        if (DEBUG) {
            Slog.d(TAG, "Initial default ime = " + defaultImiId);
        }
        mImeSelectedOnBoot = !TextUtils.isEmpty(defaultImiId);
        synchronized (mMethodMap) {
            buildInputMethodListLocked(mMethodList, mMethodMap,
                    !mImeSelectedOnBoot );
        }
        mSettings.enableAllIMEsIfThereIsNoEnabledIME();
        if (!mImeSelectedOnBoot) {
            Slog.w(TAG, "No IME selected. Choose the most applicable IME.");
            synchronized (mMethodMap) {
                resetDefaultImeLocked(context);
            }
        }
        mSettingsObserver = new SettingsObserver(mHandler);
        synchronized (mMethodMap) {
            updateFromSettingsLocked(true);
        }
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        synchronized(mMethodMap) {
                            resetStateIfCurrentLocaleChangedLocked();
                        }
                    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    }
                }, filter);
    }
    private void resetDefaultImeLocked(Context context) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mCurMethodId != null&& !InputMethodUtils.isSystemIme(mMethodMap.get(mCurMethodId))) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
        }
        InputMethodInfo defIm = null;
        for (InputMethodInfo imi : mMethodList) {
            if (defIm == null) {
                if (InputMethodUtils.isValidSystemDefaultIme(mSystemReady, imi, context)) {
                    defIm = imi;
                    Slog.i(TAG, "Selected default: " + imi.getId());
                }
            }
        }
        if (defIm == null && mMethodList.size() > 0) {
            defIm = InputMethodUtils.getMostApplicableDefaultIME(
                    mSettings.getEnabledInputMethodListLocked());
            if (defIm != null) {
                Slog.i(TAG, "Default found, using " + defIm.getId());
            } else {
                Slog.i(TAG, "No default found");
            }
        }
        if (defIm != null) {
            setSelectedInputMethodAndSubtypeLocked(defIm, NOT_A_SUBTYPE_ID, false);
        }
    }
    private void resetAllInternalStateLocked(final boolean updateOnlyWhenLocaleChanged,
            final boolean resetDefaultEnabledIme) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!mSystemReady) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        final Locale newLocale = mRes.getConfiguration().locale;
        if (!updateOnlyWhenLocaleChanged|| (newLocale != null && !newLocale.equals(mLastSystemLocale))) {
            if (!updateOnlyWhenLocaleChanged) {
                hideCurrentInputLocked(0, null);
                mCurMethodId = null;
                unbindCurrentMethodLocked(true, false);
            }
            if (DEBUG) {
                Slog.i(TAG, "Locale has been changed to " + newLocale);
            }
            buildInputMethodListLocked(mMethodList, mMethodMap, resetDefaultEnabledIme);
            if (!updateOnlyWhenLocaleChanged) {
                final String selectedImiId = mSettings.getSelectedInputMethod();
                if (TextUtils.isEmpty(selectedImiId)) {
                    resetDefaultImeLocked(mContext);
                }
            } else {
                resetDefaultImeLocked(mContext);
            }
            updateFromSettingsLocked(true);
            mLastSystemLocale = newLocale;
            if (!updateOnlyWhenLocaleChanged) {
                try {
                    startInputInnerLocked();
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Unexpected exception", e);
                }
            }
        }
    }
    private void resetStateIfCurrentLocaleChangedLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        resetAllInternalStateLocked(true ,
                true );
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void switchUserLocked(int newUserId) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        mSettings.setCurrentUserId(newUserId);
        updateCurrentProfileIds();
        mFileManager = new InputMethodFileManager(mMethodMap, newUserId);
        final String defaultImiId = mSettings.getSelectedInputMethod();
        final boolean initialUserSwitch = TextUtils.isEmpty(defaultImiId);
        if (DEBUG) {
            Slog.d(TAG, "Switch user: " + newUserId + " current ime = " + defaultImiId);
        }
        resetAllInternalStateLocked(false  ,
                initialUserSwitch );
        if (initialUserSwitch) {
            InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(mContext.getPackageManager(),
                    mSettings.getEnabledInputMethodListLocked());
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    void updateCurrentProfileIds() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        List<UserInfo> profiles =
                UserManager.get(mContext).getProfiles(mSettings.getCurrentUserId());
        int[] currentProfileIds = new int[profiles.size()]; 
        for (int i = 0; i < currentProfileIds.length; i++) {
            currentProfileIds[i] = profiles.get(i).id;
        }
        mSettings.setCurrentProfileIds(currentProfileIds);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {
        try {
        return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Input Method Manager Crash", e);
            }
            throw e;
        }
    }
    public void systemRunning(StatusBarManagerService statusBar) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        synchronized (mMethodMap) {
            if (DEBUG) {
                Slog.d(TAG, "--- systemReady");
            }
            if (!mSystemReady) {
                mSystemReady = true;
                mKeyguardManager =
                        (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
                mNotificationManager = (NotificationManager)
                        mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                mStatusBar = statusBar;
                statusBar.setIconVisibility("ime", false);
                updateImeWindowStatusLocked();
                mShowOngoingImeSwitcherForPhones = mRes.getBoolean(
                        com.android.internal.R.bool.show_ongoing_ime_switcher);
                if (mShowOngoingImeSwitcherForPhones) {
                    mWindowManagerService.setOnHardKeyboardStatusChangeListener(
                            mHardKeyboardListener);
                }
                buildInputMethodListLocked(mMethodList, mMethodMap,
                        !mImeSelectedOnBoot );
                if (!mImeSelectedOnBoot) {
                    Slog.w(TAG, "Reset the default IME as \"Resource\" is ready here.");
                    resetStateIfCurrentLocaleChangedLocked();
                    InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(
                            mContext.getPackageManager(),
                            mSettings.getEnabledInputMethodListLocked());
                }
                mLastSystemLocale = mRes.getConfiguration().locale;
                try {
                    startInputInnerLocked();
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Unexpected exception", e);
                }
            }
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void setImeWindowVisibilityStatusHiddenLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        mImeWindowVis = 0;
        updateImeWindowStatusLocked();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void refreshImeWindowVisibilityLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        final Configuration conf = mRes.getConfiguration();
        final boolean haveHardKeyboard = conf.keyboard
                != Configuration.KEYBOARD_NOKEYS;
        final boolean hardKeyShown = haveHardKeyboard
                && conf.hardKeyboardHidden
                        != Configuration.HARDKEYBOARDHIDDEN_YES;
        final boolean isScreenLocked = isKeyguardLocked();
        final boolean inputActive = !isScreenLocked && (mInputShown || hardKeyShown);
        final boolean inputVisible = inputActive && !hardKeyShown;
        mImeWindowVis = (inputActive ? InputMethodService.IME_ACTIVE : 0)
                | (inputVisible ? InputMethodService.IME_VISIBLE : 0);
        updateImeWindowStatusLocked();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void updateImeWindowStatusLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        setImeWindowStatus(mCurToken, mImeWindowVis, mBackDisposition);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private boolean calledFromValidUser() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        final int uid = Binder.getCallingUid();
        final int userId = UserHandle.getUserId(uid);
        if (DEBUG) {
            Slog.d(TAG, "--- calledFromForegroundUserOrSystemProcess ? "
                    + "calling uid = " + uid + " system uid = " + Process.SYSTEM_UID
                    + " calling userId = " + userId + ", foreground user id = "
                    + mSettings.getCurrentUserId() + ", calling pid = " + Binder.getCallingPid()
                    + InputMethodUtils.getApiCallStack());
        }
        if (uid == Process.SYSTEM_UID || mSettings.isCurrentProfile(userId)) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return true;
        }
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.INTERACT_ACROSS_USERS_FULL)== PackageManager.PERMISSION_GRANTED) {
            if (DEBUG) {
                Slog.d(TAG, "--- Access granted because the calling process has "
                        + "the INTERACT_ACROSS_USERS_FULL permission");
            }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return true;
        }
        Slog.w(TAG, "--- IPC called from background users. Ignore. \n"
                + InputMethodUtils.getStackTrace());
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return false;
    }
    private boolean calledWithValidToken(IBinder token) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (token == null || mCurToken != token) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return true;
    }
    private boolean bindCurrentInputMethodService(
            Intent service, ServiceConnection conn, int flags) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (service == null || conn == null) {
            Slog.e(TAG, "--- bind failed: service = " + service + ", conn = " + conn);
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
        }
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return mContext.bindServiceAsUser(service, conn, flags,new UserHandle(mSettings.getCurrentUserId()));
    }
    @Override
    public List<InputMethodInfo> getInputMethodList() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return Collections.emptyList();
        }
        synchronized (mMethodMap) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return new ArrayList<InputMethodInfo>(mMethodList);
        }
    }
    @Override
    public List<InputMethodInfo> getEnabledInputMethodList() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return Collections.emptyList();
        }
        synchronized (mMethodMap) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return mSettings.getEnabledInputMethodListLocked();
        }
    }
    @Override
    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId,
            boolean allowsImplicitlySelectedSubtypes) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return Collections.<InputMethodSubtype>emptyList();
        }
        synchronized (mMethodMap) {
            final InputMethodInfo imi;
            if (imiId == null && mCurMethodId != null) {
                imi = mMethodMap.get(mCurMethodId);
            } else {
                imi = mMethodMap.get(imiId);
            }
            if (imi == null) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return Collections.<InputMethodSubtype>emptyList();
            }
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return mSettings.getEnabledInputMethodSubtypeListLocked(mContext, imi, allowsImplicitlySelectedSubtypes);
        }
    }
    @Override
    public void addClient(IInputMethodClient client,
            IInputContext inputContext, int uid, int pid) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        synchronized (mMethodMap) {
            mClients.put(client.asBinder(), new ClientState(client,
                    inputContext, uid, pid));
        }
    }
    @Override
    public void removeClient(IInputMethodClient client) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        synchronized (mMethodMap) {
            ClientState cs = mClients.remove(client.asBinder());
            if (cs != null) {
                clearClientSessionLocked(cs);
            }
        }
    }
    void executeOrSendMessage(IInterface target, Message msg) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
         if (target.asBinder() instanceof Binder) {
             mCaller.sendMessage(msg);
         } else {
             handleMessage(msg);
             msg.recycle();
         }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    void unbindCurrentClientLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mCurClient != null) {
            if (DEBUG) Slog.v(TAG, "unbindCurrentInputLocked: client = "+ mCurClient.client.asBinder());
            if (mBoundToMethod) {
                mBoundToMethod = false;
                if (mCurMethod != null) {
                    executeOrSendMessage(mCurMethod, mCaller.obtainMessageO(
                            MSG_UNBIND_INPUT, mCurMethod));
                }
            }
            executeOrSendMessage(mCurClient.client, mCaller.obtainMessageIO(
                    MSG_SET_ACTIVE, 0, mCurClient));
            executeOrSendMessage(mCurClient.client, mCaller.obtainMessageIO(
                    MSG_UNBIND_METHOD, mCurSeq, mCurClient.client));
            mCurClient.sessionRequested = false;
            mCurClient = null;
            hideInputMethodMenuLocked();
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private int getImeShowFlags() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        int flags = 0;
        if (mShowForced) {
            flags |= InputMethod.SHOW_FORCED
                    | InputMethod.SHOW_EXPLICIT;
        } else if (mShowExplicitlyRequested) {
            flags |= InputMethod.SHOW_EXPLICIT;
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return flags;
    }
    private int getAppShowFlags() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        int flags = 0;
        if (mShowForced) {
            flags |= InputMethodManager.SHOW_FORCED;
        } else if (!mShowExplicitlyRequested) {
            flags |= InputMethodManager.SHOW_IMPLICIT;
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return flags;
    }
    InputBindResult attachNewInputLocked(boolean initial) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!mBoundToMethod) {
            executeOrSendMessage(mCurMethod, mCaller.obtainMessageOO(
                    MSG_BIND_INPUT, mCurMethod, mCurClient.binding));
            mBoundToMethod = true;
        }
        final SessionState session = mCurClient.curSession;
        if (initial) {
            executeOrSendMessage(session.method, mCaller.obtainMessageOOO(
                    MSG_START_INPUT, session, mCurInputContext, mCurAttribute));
        } else {
            executeOrSendMessage(session.method, mCaller.obtainMessageOOO(
                    MSG_RESTART_INPUT, session, mCurInputContext, mCurAttribute));
        }
        if (mShowRequested) {
            if (DEBUG) Slog.v(TAG, "Attach new input asks to show input");
            showCurrentInputLocked(getAppShowFlags(), null);
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return new InputBindResult(session.session,(session.channel != null ? session.channel.dup() : null),mCurId, mCurSeq, mCurUserActionNotificationSequenceNumber);
    }
    InputBindResult startInputLocked(IInputMethodClient client,
            IInputContext inputContext, EditorInfo attribute, int controlFlags) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mCurMethodId == null) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return mNoBinding;
        }
        ClientState cs = mClients.get(client.asBinder());
        if (cs == null) {
            throw new IllegalArgumentException("unknown client "
                    + client.asBinder());
        }
        try {
            if (!mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                Slog.w(TAG, "Starting input on non-focused client " + cs.client
                        + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return null;
            }
        } catch (RemoteException e) {
        }
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return startInputUncheckedLocked(cs, inputContext, attribute, controlFlags);
    }
    InputBindResult startInputUncheckedLocked(ClientState cs,
            IInputContext inputContext, EditorInfo attribute, int controlFlags) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mCurMethodId == null) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return mNoBinding;
        }
        if (mCurClient != cs) {
            mCurClientInKeyguard = isKeyguardLocked();
            unbindCurrentClientLocked();
            if (DEBUG) Slog.v(TAG, "switching to client: client = "+ cs.client.asBinder() + " keyguard=" + mCurClientInKeyguard);
            if (mScreenOn) {
                executeOrSendMessage(cs.client, mCaller.obtainMessageIO(
                        MSG_SET_ACTIVE, mScreenOn ? 1 : 0, cs));
            }
        }
        mCurSeq++;
        if (mCurSeq <= 0) mCurSeq = 1;
        mCurClient = cs;
        mCurInputContext = inputContext;
        mCurAttribute = attribute;
        if (mCurId != null && mCurId.equals(mCurMethodId)) {
            if (cs.curSession != null) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return attachNewInputLocked((controlFlags&InputMethodManager.CONTROL_START_INITIAL) != 0);
            }
            if (mHaveConnection) {
                if (mCurMethod != null) {
                    requestClientSessionLocked(cs);
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return new InputBindResult(null, null, mCurId, mCurSeq,mCurUserActionNotificationSequenceNumber);
                } else if (SystemClock.uptimeMillis()< (mLastBindTime+TIME_TO_RECONNECT)) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return new InputBindResult(null, null, mCurId, mCurSeq,mCurUserActionNotificationSequenceNumber);
                } else {
                    EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME,
                            mCurMethodId, SystemClock.uptimeMillis()-mLastBindTime, 0);
                }
            }
        }
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return startInputInnerLocked();
    }
    InputBindResult startInputInnerLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mCurMethodId == null) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return mNoBinding;
        }
        if (!mSystemReady) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return new InputBindResult(null, null, mCurMethodId, mCurSeq,mCurUserActionNotificationSequenceNumber);
        }
        InputMethodInfo info = mMethodMap.get(mCurMethodId);
        if (info == null) {
            throw new IllegalArgumentException("Unknown id: " + mCurMethodId);
        }
        unbindCurrentMethodLocked(false, true);
        mCurIntent = new Intent(InputMethod.SERVICE_INTERFACE);
        mCurIntent.setComponent(info.getComponent());
        mCurIntent.putExtra(Intent.EXTRA_CLIENT_LABEL,
                com.android.internal.R.string.input_method_binding_label);
        mCurIntent.putExtra(Intent.EXTRA_CLIENT_INTENT, PendingIntent.getActivity(
                mContext, 0, new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0));
        if (bindCurrentInputMethodService(mCurIntent, this, Context.BIND_AUTO_CREATE| Context.BIND_NOT_VISIBLE | Context.BIND_NOT_FOREGROUND| Context.BIND_SHOWING_UI)) {
            mLastBindTime = SystemClock.uptimeMillis();
            mHaveConnection = true;
            mCurId = info.getId();
            mCurToken = new Binder();
            try {
                if (true || DEBUG) Slog.v(TAG, "Adding window token: " + mCurToken);
                mIWindowManager.addWindowToken(mCurToken,
                        WindowManager.LayoutParams.TYPE_INPUT_METHOD);
            } catch (RemoteException e) {
            }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return new InputBindResult(null, null, mCurId, mCurSeq,mCurUserActionNotificationSequenceNumber);
        } else {
            mCurIntent = null;
            Slog.w(TAG, "Failure connecting to input method service: "
                    + mCurIntent);
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return null;
    }
    @Override
    public InputBindResult startInput(IInputMethodClient client,
            IInputContext inputContext, EditorInfo attribute, int controlFlags) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return null;
        }
        synchronized (mMethodMap) {
            final long ident = Binder.clearCallingIdentity();
            try {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return startInputLocked(client, inputContext, attribute, controlFlags);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }
    @Override
    public void finishInput(IInputMethodClient client) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        synchronized (mMethodMap) {
            if (mCurIntent != null && name.equals(mCurIntent.getComponent())) {
                mCurMethod = IInputMethod.Stub.asInterface(service);
                if (mCurToken == null) {
                    Slog.w(TAG, "Service connected without a token!");
                    unbindCurrentMethodLocked(false, false);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return;
                }
                if (DEBUG) Slog.v(TAG, "Initiating attach with token: " + mCurToken);
                executeOrSendMessage(mCurMethod, mCaller.obtainMessageOO(
                        MSG_ATTACH_TOKEN, mCurMethod, mCurToken));
                if (mCurClient != null) {
                    clearClientSessionLocked(mCurClient);
                    requestClientSessionLocked(mCurClient);
                }
            }
        }
    }
    void onSessionCreated(IInputMethod method, IInputMethodSession session,
            InputChannel channel) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        synchronized (mMethodMap) {
            if (mCurMethod != null && method != null&& mCurMethod.asBinder() == method.asBinder()) {
                if (mCurClient != null) {
                    clearClientSessionLocked(mCurClient);
                    mCurClient.curSession = new SessionState(mCurClient,
                            method, session, channel);
                    InputBindResult res = attachNewInputLocked(true);
                    if (res.method != null) {
                        executeOrSendMessage(mCurClient.client, mCaller.obtainMessageOO(
                                MSG_BIND_METHOD, mCurClient.client, res));
                    }
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return;
                }
            }
        }
        channel.dispose();
    }
    void unbindCurrentMethodLocked(boolean reportToClient, boolean savePosition) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mVisibleBound) {
            mContext.unbindService(mVisibleConnection);
            mVisibleBound = false;
        }
        if (mHaveConnection) {
            mContext.unbindService(this);
            mHaveConnection = false;
        }
        if (mCurToken != null) {
            try {
                if (DEBUG) Slog.v(TAG, "Removing window token: " + mCurToken);
                if ((mImeWindowVis & InputMethodService.IME_ACTIVE) != 0 && savePosition) {
                    mWindowManagerService.saveLastInputMethodWindowForTransition();
                }
                mIWindowManager.removeWindowToken(mCurToken);
            } catch (RemoteException e) {
            }
            mCurToken = null;
        }
        mCurId = null;
        clearCurMethodLocked();
        if (reportToClient && mCurClient != null) {
            executeOrSendMessage(mCurClient.client, mCaller.obtainMessageIO(
                    MSG_UNBIND_METHOD, mCurSeq, mCurClient.client));
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    void requestClientSessionLocked(ClientState cs) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!cs.sessionRequested) {
            if (DEBUG) Slog.v(TAG, "Creating new session for client " + cs);
            InputChannel[] channels = InputChannel.openInputChannelPair(cs.toString());
            cs.sessionRequested = true;
            executeOrSendMessage(mCurMethod, mCaller.obtainMessageOOO(
                    MSG_CREATE_SESSION, mCurMethod, channels[1],
                    new MethodCallback(this, mCurMethod, channels[0])));
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    void clearClientSessionLocked(ClientState cs) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        finishSessionLocked(cs.curSession);
        cs.curSession = null;
        cs.sessionRequested = false;
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void finishSessionLocked(SessionState sessionState) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (sessionState != null) {
            if (sessionState.session != null) {
                try {
                    sessionState.session.finishSession();
                } catch (RemoteException e) {
                    Slog.w(TAG, "Session failed to close due to remote exception", e);
                    setImeWindowVisibilityStatusHiddenLocked();
                }
                sessionState.session = null;
            }
            if (sessionState.channel != null) {
                sessionState.channel.dispose();
                sessionState.channel = null;
            }
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    void clearCurMethodLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mCurMethod != null) {
            for (ClientState cs : mClients.values()) {
                clearClientSessionLocked(cs);
            }
            finishSessionLocked(mEnabledSession);
            mEnabledSession = null;
            mCurMethod = null;
        }
        if (mStatusBar != null) {
            mStatusBar.setIconVisibility("ime", false);
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        synchronized (mMethodMap) {
            if (DEBUG) Slog.v(TAG, "Service disconnected: " + name+ " mCurIntent=" + mCurIntent);
            if (mCurMethod != null && mCurIntent != null&& name.equals(mCurIntent.getComponent())) {
                clearCurMethodLocked();
                mLastBindTime = SystemClock.uptimeMillis();
                mShowRequested = mInputShown;
                mInputShown = false;
                if (mCurClient != null) {
                    executeOrSendMessage(mCurClient.client, mCaller.obtainMessageIO(
                            MSG_UNBIND_METHOD, mCurSeq, mCurClient.client));
                }
            }
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    @Override
    public void updateStatusIcon(IBinder token, String packageName, int iconId) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (mMethodMap) {
                if (!calledWithValidToken(token)) {
                    final int uid = Binder.getCallingUid();
                    Slog.e(TAG, "Ignoring updateStatusIcon due to an invalid token. uid:" + uid
                            + " token:" + token);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                            return;
                }
                if (iconId == 0) {
                    if (DEBUG) Slog.d(TAG, "hide the small icon for the input method");
                    if (mStatusBar != null) {
                        mStatusBar.setIconVisibility("ime", false);
                    }
                } else if (packageName != null) {
                    if (DEBUG) Slog.d(TAG, "show a small icon for the input method");
                    CharSequence contentDescription = null;
                    try {
                        final PackageManager packageManager = mContext.getPackageManager();
                        contentDescription = packageManager.getApplicationLabel(
                                mIPackageManager.getApplicationInfo(packageName, 0,
                                        mSettings.getCurrentUserId()));
                    } catch (RemoteException e) {
                    }
                    if (mStatusBar != null) {
                        mStatusBar.setIcon("ime", packageName, iconId, 0,
                                contentDescription  != null
                                        ? contentDescription.toString() : null);
                        mStatusBar.setIconVisibility("ime", true);
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
    private boolean needsToShowImeSwitchOngoingNotification() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    if (!mShowOngoingImeSwitcherForPhones)
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
    }
        if (mSwitchingDialog != null)
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
    }
        if (isScreenLocked())
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
    }
        synchronized (mMethodMap) {
            List<InputMethodInfo> imis = mSettings.getEnabledInputMethodListLocked();
            final int N = imis.size();
            if (N > 2)
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return true;
    }
            if (N < 1)
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
    }
            int nonAuxCount = 0;
            int auxCount = 0;
            InputMethodSubtype nonAuxSubtype = null;
            InputMethodSubtype auxSubtype = null;
            for(int i = 0; i < N; ++i) {
                final InputMethodInfo imi = imis.get(i);
                final List<InputMethodSubtype> subtypes =
                        mSettings.getEnabledInputMethodSubtypeListLocked(mContext, imi, true);
                final int subtypeCount = subtypes.size();
                if (subtypeCount == 0) {
                    ++nonAuxCount;
                } else {
                    for (int j = 0; j < subtypeCount; ++j) {
                        final InputMethodSubtype subtype = subtypes.get(j);
                        if (!subtype.isAuxiliary()) {
                            ++nonAuxCount;
                            nonAuxSubtype = subtype;
                        } else {
                            ++auxCount;
                            auxSubtype = subtype;
                        }
                    }
                }
            }
            if (nonAuxCount > 1 || auxCount > 1) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return true;
            } else if (nonAuxCount == 1 && auxCount == 1) {
                if (nonAuxSubtype != null && auxSubtype != null&& (nonAuxSubtype.getLocale().equals(auxSubtype.getLocale())|| auxSubtype.overridesImplicitlyEnabledSubtype()|| nonAuxSubtype.overridesImplicitlyEnabledSubtype())&& nonAuxSubtype.containsExtraValueKey(TAG_TRY_SUPPRESSING_IME_SWITCHER)) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return false;
                }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
        }
    }
    private boolean isKeyguardLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return mKeyguardManager != null && mKeyguardManager.isKeyguardLocked();
    }
    @SuppressWarnings("deprecation")
    @Override
    public void setImeWindowStatus(IBinder token, int vis, int backDisposition) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        final long ident = Binder.clearCallingIdentity();
        try {
            if (!calledWithValidToken(token)) {
                final int uid = Binder.getCallingUid();
                Slog.e(TAG, "Ignoring setImeWindowStatus due to an invalid token. uid:" + uid
                        + " token:" + token);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return;
            }
            synchronized (mMethodMap) {
                if (vis != 0 && isKeyguardLocked() && !mCurClientInKeyguard) {
                    vis = 0;
                }
                mImeWindowVis = vis;
                mBackDisposition = backDisposition;
                final boolean iconVisibility = ((vis & (InputMethodService.IME_ACTIVE)) != 0)
                        && (mWindowManagerService.isHardKeyboardAvailable()
                                || (vis & (InputMethodService.IME_VISIBLE)) != 0);
                final boolean needsToShowImeSwitcher = iconVisibility
                        && needsToShowImeSwitchOngoingNotification();
                if (mStatusBar != null) {
                    mStatusBar.setImeWindowStatus(token, vis, backDisposition,
                            needsToShowImeSwitcher);
                }
                final InputMethodInfo imi = mMethodMap.get(mCurMethodId);
                if (imi != null && needsToShowImeSwitcher) {
                    final CharSequence title = mRes.getText(
                            com.android.internal.R.string.select_input_method);
                    final CharSequence summary = InputMethodUtils.getImeAndSubtypeDisplayName(
                            mContext, imi, mCurrentSubtype);
                    mImeSwitcherNotification.color = mContext.getResources().getColor(
                            com.android.internal.R.color.system_notification_accent_color);
                    mImeSwitcherNotification.setLatestEventInfo(
                            mContext, title, summary, mImeSwitchPendingIntent);
                    if ((mNotificationManager != null)&& !mWindowManagerService.hasNavigationBar()) {
                        if (DEBUG) {
                            Slog.d(TAG, "--- show notification: label =  " + summary);
                        }
                        mNotificationManager.notifyAsUser(null,
                                com.android.internal.R.string.select_input_method,
                                mImeSwitcherNotification, UserHandle.ALL);
                        mNotificationShown = true;
                    }
                } else {
                    if (mNotificationShown && mNotificationManager != null) {
                        if (DEBUG) {
                            Slog.d(TAG, "--- hide notification");
                        }
                        mNotificationManager.cancelAsUser(null,
                                com.android.internal.R.string.select_input_method, UserHandle.ALL);
                        mNotificationShown = false;
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
    @Override
    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        synchronized (mMethodMap) {
            final InputMethodInfo currentImi = mMethodMap.get(mCurMethodId);
            for (int i = 0; i < spans.length; ++i) {
                SuggestionSpan ss = spans[i];
                if (!TextUtils.isEmpty(ss.getNotificationTargetClassName())) {
                    mSecureSuggestionSpans.put(ss, currentImi);
                }
            }
        }
    }
    @Override
    public boolean notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
        }
        synchronized (mMethodMap) {
            final InputMethodInfo targetImi = mSecureSuggestionSpans.get(span);
            if (targetImi != null) {
                final String[] suggestions = span.getSuggestions();
                if (index < 0 || index >= suggestions.length)
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return false;
    }
                final String className = span.getNotificationTargetClassName();
                final Intent intent = new Intent();
                intent.setClassName(targetImi.getPackageName(), className);
                intent.setAction(SuggestionSpan.ACTION_SUGGESTION_PICKED);
                intent.putExtra(SuggestionSpan.SUGGESTION_SPAN_PICKED_BEFORE, originalString);
                intent.putExtra(SuggestionSpan.SUGGESTION_SPAN_PICKED_AFTER, suggestions[index]);
                intent.putExtra(SuggestionSpan.SUGGESTION_SPAN_PICKED_HASHCODE, span.hashCode());
                final long ident = Binder.clearCallingIdentity();
                try {
                    mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            }
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
    }
    void updateFromSettingsLocked(boolean enabledMayChange) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        updateInputMethodsFromSettingsLocked(enabledMayChange);
        updateKeyboardFromSettingsLocked();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    void updateInputMethodsFromSettingsLocked(boolean enabledMayChange) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (enabledMayChange) {
            List<InputMethodInfo> enabled = mSettings.getEnabledInputMethodListLocked();
            for (int i=0; i<enabled.size(); i++) {
                InputMethodInfo imm = enabled.get(i);
                try {
                    ApplicationInfo ai = mIPackageManager.getApplicationInfo(imm.getPackageName(),
                            PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS,
                            mSettings.getCurrentUserId());
                    if (ai != null && ai.enabledSetting== PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                        if (DEBUG) {
                            Slog.d(TAG, "Update state(" + imm.getId()
                                    + "): DISABLED_UNTIL_USED -> DEFAULT");
                        }
                        mIPackageManager.setApplicationEnabledSetting(imm.getPackageName(),
                                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                                PackageManager.DONT_KILL_APP, mSettings.getCurrentUserId(),
                                mContext.getBasePackageName());
                    }
                } catch (RemoteException e) {
                }
            }
        }
        String id = mSettings.getSelectedInputMethod();
        if (TextUtils.isEmpty(id) && chooseNewDefaultIMELocked()) {
            id = mSettings.getSelectedInputMethod();
        }
        if (!TextUtils.isEmpty(id)) {
            try {
                setInputMethodLocked(id, mSettings.getSelectedInputMethodSubtypeId(id));
            } catch (IllegalArgumentException e) {
                Slog.w(TAG, "Unknown input method from prefs: " + id, e);
                mCurMethodId = null;
                unbindCurrentMethodLocked(true, false);
            }
            mShortcutInputMethodsAndSubtypes.clear();
        } else {
            mCurMethodId = null;
            unbindCurrentMethodLocked(true, false);
        }
        mSwitchingController.resetCircularListLocked(mContext);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    public void updateKeyboardFromSettingsLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        mShowImeWithHardKeyboard = mSettings.isShowImeWithHardKeyboardEnabled();
        if (mSwitchingDialog != null&& mSwitchingDialogTitleView != null&& mSwitchingDialog.isShowing()) {
            final Switch hardKeySwitch = (Switch)mSwitchingDialogTitleView.findViewById(
                    com.android.internal.R.id.hard_keyboard_switch);
            hardKeySwitch.setChecked(mShowImeWithHardKeyboard);
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
     void setInputMethodLocked(String id, int subtypeId) {
         Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        InputMethodInfo info = mMethodMap.get(id);
        if (info == null) {
            throw new IllegalArgumentException("Unknown id: " + id);
        }
        if (mCurClient != null && mCurAttribute != null) {
            final int uid = mCurClient.uid;
            final String packageName = mCurAttribute.packageName;
            if (SystemConfig.getInstance().getFixedImeApps().contains(packageName)) {
                if (InputMethodUtils.checkIfPackageBelongsToUid(mAppOpsManager, uid, packageName)) {
     Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
                }
                Slog.e(TAG, "Ignoring FixedImeApps due to the validation failure. uid=" + uid
                        + " package=" + packageName);
            }
        }
        if (id.equals(mCurMethodId)) {
            final int subtypeCount = info.getSubtypeCount();
            if (subtypeCount <= 0) {
     Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return;
            }
            final InputMethodSubtype oldSubtype = mCurrentSubtype;
            final InputMethodSubtype newSubtype;
            if (subtypeId >= 0 && subtypeId < subtypeCount) {
                newSubtype = info.getSubtypeAt(subtypeId);
            } else {
                newSubtype = getCurrentInputMethodSubtypeLocked();
            }
            if (newSubtype == null || oldSubtype == null) {
                Slog.w(TAG, "Illegal subtype state: old subtype = " + oldSubtype
                        + ", new subtype = " + newSubtype);
     Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return;
            }
            if (newSubtype != oldSubtype) {
                setSelectedInputMethodAndSubtypeLocked(info, subtypeId, true);
                if (mCurMethod != null) {
                    try {
                        refreshImeWindowVisibilityLocked();
                        mCurMethod.changeInputMethodSubtype(newSubtype);
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Failed to call changeInputMethodSubtype");
                    }
                }
            }
     Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return;
        }
        final long ident = Binder.clearCallingIdentity();
        try {
            setSelectedInputMethodAndSubtypeLocked(info, subtypeId, false);
            mCurMethodId = id;
            if (ActivityManagerNative.isSystemReady()) {
                Intent intent = new Intent(Intent.ACTION_INPUT_METHOD_CHANGED);
                intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
                intent.putExtra("input_method_id", id);
                mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
            }
            unbindCurrentClientLocked();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
    @Override
    public boolean showSoftInput(IInputMethodClient client, int flags,
            ResultReceiver resultReceiver) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (mMethodMap) {
                if (mCurClient == null || client == null|| mCurClient.client.asBinder() != client.asBinder()) {
                    try {
                        if (!mIWindowManager.inputMethodClientHasFocus(client)) {
                            Slog.w(TAG, "Ignoring showSoftInput of uid " + uid + ": " + client);
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                            return false;
                        }
                    } catch (RemoteException e) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return false;
                    }
                }
                if (DEBUG) Slog.v(TAG, "Client requesting input be shown");
            {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return showCurrentInputLocked(flags, resultReceiver);
            }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
    boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        mShowRequested = true;
        if ((flags&InputMethodManager.SHOW_IMPLICIT) == 0) {
            mShowExplicitlyRequested = true;
        }
        if ((flags&InputMethodManager.SHOW_FORCED) != 0) {
            mShowExplicitlyRequested = true;
            mShowForced = true;
        }
        if (!mSystemReady) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
        }
        boolean res = false;
        if (mCurMethod != null) {
            if (DEBUG) Slog.d(TAG, "showCurrentInputLocked: mCurToken=" + mCurToken);
            executeOrSendMessage(mCurMethod, mCaller.obtainMessageIOO(
                    MSG_SHOW_SOFT_INPUT, getImeShowFlags(), mCurMethod,
                    resultReceiver));
            mInputShown = true;
            if (mHaveConnection && !mVisibleBound) {
                bindCurrentInputMethodService(
                        mCurIntent, mVisibleConnection, Context.BIND_AUTO_CREATE
                                | Context.BIND_TREAT_LIKE_ACTIVITY);
                mVisibleBound = true;
            }
            res = true;
        } else if (mHaveConnection && SystemClock.uptimeMillis()>= (mLastBindTime+TIME_TO_RECONNECT)) {
            EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME, mCurMethodId,
                    SystemClock.uptimeMillis()-mLastBindTime,1);
            Slog.w(TAG, "Force disconnect/connect to the IME in showCurrentInputLocked()");
            mContext.unbindService(this);
            bindCurrentInputMethodService(mCurIntent, this, Context.BIND_AUTO_CREATE
                    | Context.BIND_NOT_VISIBLE);
        } else {
            if (DEBUG) {
                Slog.d(TAG, "Can't show input: connection = " + mHaveConnection + ", time = "
                        + ((mLastBindTime+TIME_TO_RECONNECT) - SystemClock.uptimeMillis()));
            }
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return res;
    }
    @Override
    public boolean hideSoftInput(IInputMethodClient client, int flags,
            ResultReceiver resultReceiver) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (mMethodMap) {
                if (mCurClient == null || client == null|| mCurClient.client.asBinder() != client.asBinder()) {
                    try {
                        if (!mIWindowManager.inputMethodClientHasFocus(client)) {
                            if (DEBUG) Slog.w(TAG, "Ignoring hideSoftInput of uid "+ uid + ": " + client);
                            setImeWindowVisibilityStatusHiddenLocked();
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                            return false;
                        }
                    } catch (RemoteException e) {
                        setImeWindowVisibilityStatusHiddenLocked();
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return false;
                    }
                }
                if (DEBUG) Slog.v(TAG, "Client requesting input be hidden");
            {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return hideCurrentInputLocked(flags, resultReceiver);
            }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
    boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if ((flags&InputMethodManager.HIDE_IMPLICIT_ONLY) != 0&& (mShowExplicitlyRequested || mShowForced)) {
            if (DEBUG) Slog.v(TAG, "Not hiding: explicit show not cancelled by non-explicit hide");
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
    }
        }
        if (mShowForced && (flags&InputMethodManager.HIDE_NOT_ALWAYS) != 0) {
            if (DEBUG) Slog.v(TAG, "Not hiding: forced show not cancelled by not-always hide");
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
    }
        }
        boolean res;
        if (mInputShown && mCurMethod != null) {
            executeOrSendMessage(mCurMethod, mCaller.obtainMessageOO(
                    MSG_HIDE_SOFT_INPUT, mCurMethod, resultReceiver));
            res = true;
        } else {
            res = false;
        }
        if (mHaveConnection && mVisibleBound) {
            mContext.unbindService(mVisibleConnection);
            mVisibleBound = false;
        }
        mInputShown = false;
        mShowRequested = false;
        mShowExplicitlyRequested = false;
        mShowForced = false;
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return res;
    }
    @Override
    public InputBindResult windowGainedFocus(IInputMethodClient client, IBinder windowToken,
            int controlFlags, int softInputMode, int windowFlags,
            EditorInfo attribute, IInputContext inputContext) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        final boolean calledFromValidUser = calledFromValidUser();
        InputBindResult res = null;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (mMethodMap) {
                if (DEBUG) Slog.v(TAG, "windowGainedFocus: " + client.asBinder()+ " controlFlags=#" + Integer.toHexString(controlFlags)+ " softInputMode=#" + Integer.toHexString(softInputMode)+ " windowFlags=#" + Integer.toHexString(windowFlags));
                ClientState cs = mClients.get(client.asBinder());
                if (cs == null) {
                    throw new IllegalArgumentException("unknown client "
                            + client.asBinder());
                }
                try {
                    if (!mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                        Slog.w(TAG, "Focus gain on non-focused client " + cs.client
                                + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                                return null;
                    }
                } catch (RemoteException e) {
                }
                if (!calledFromValidUser) {
                    Slog.w(TAG, "A background user is requesting window. Hiding IME.");
                    Slog.w(TAG, "If you want to interect with IME, you need "
                            + "android.permission.INTERACT_ACROSS_USERS_FULL");
                    hideCurrentInputLocked(0, null);
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return null;
                }
                if (mCurFocusedWindow == windowToken) {
                    Slog.w(TAG, "Window already focused, ignoring focus gain of: " + client
                            + " attribute=" + attribute + ", token = " + windowToken);
                    if (attribute != null) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return startInputUncheckedLocked(cs, inputContext, attribute,controlFlags);
                    }
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return null;
                }
                mCurFocusedWindow = windowToken;
                final boolean doAutoShow =
                        (softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
                                == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        || mRes.getConfiguration().isLayoutSizeAtLeast(
                                Configuration.SCREENLAYOUT_SIZE_LARGE);
                final boolean isTextEditor =
                        (controlFlags&InputMethodManager.CONTROL_WINDOW_IS_TEXT_EDITOR) != 0;
                boolean didStart = false;
                switch (softInputMode&WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE) {
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED:
                        if (!isTextEditor || !doAutoShow) {
                            if (WindowManager.LayoutParams.mayUseInputMethod(windowFlags)) {
                                if (DEBUG) Slog.v(TAG, "Unspecified window will hide input");
                                hideCurrentInputLocked(InputMethodManager.HIDE_NOT_ALWAYS, null);
                            }
                        } else if (isTextEditor && doAutoShow && (softInputMode &WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION) != 0) {
                            if (DEBUG) Slog.v(TAG, "Unspecified window will show input");
                            if (attribute != null) {
                                res = startInputUncheckedLocked(cs, inputContext, attribute,
                                        controlFlags);
                                didStart = true;
                            }
                            showCurrentInputLocked(InputMethodManager.SHOW_IMPLICIT, null);
                        }
                        break;
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED:
                        break;
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN:
                        if ((softInputMode &WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION) != 0) {
                            if (DEBUG) Slog.v(TAG, "Window asks to hide input going forward");
                            hideCurrentInputLocked(0, null);
                        }
                        break;
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN:
                        if (DEBUG) Slog.v(TAG, "Window asks to hide input");
                        hideCurrentInputLocked(0, null);
                        break;
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE:
                        if ((softInputMode &WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION) != 0) {
                            if (DEBUG) Slog.v(TAG, "Window asks to show input going forward");
                            if (attribute != null) {
                                res = startInputUncheckedLocked(cs, inputContext, attribute,
                                        controlFlags);
                                didStart = true;
                            }
                            showCurrentInputLocked(InputMethodManager.SHOW_IMPLICIT, null);
                        }
                        break;
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE:
                        if (DEBUG) Slog.v(TAG, "Window asks to always show input");
                        if (attribute != null) {
                            res = startInputUncheckedLocked(cs, inputContext, attribute,
                                    controlFlags);
                            didStart = true;
                        }
                        showCurrentInputLocked(InputMethodManager.SHOW_IMPLICIT, null);
                        break;
                }
                if (!didStart && attribute != null) {
                    res = startInputUncheckedLocked(cs, inputContext, attribute,
                            controlFlags);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return res;
    }
    @Override
    public void showInputMethodPickerFromClient(IInputMethodClient client) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        synchronized (mMethodMap) {
            if (mCurClient == null || client == null|| mCurClient.client.asBinder() != client.asBinder()) {
                Slog.w(TAG, "Ignoring showInputMethodPickerFromClient of uid "
                        + Binder.getCallingUid() + ": " + client);
            }
            mHandler.sendEmptyMessage(MSG_SHOW_IM_SUBTYPE_PICKER);
        }
    }
    @Override
    public void setInputMethod(IBinder token, String id) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        setInputMethodWithSubtypeId(token, id, NOT_A_SUBTYPE_ID);
    }
    @Override
    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        synchronized (mMethodMap) {
            if (subtype != null) {
                setInputMethodWithSubtypeIdLocked(token, id,
                        InputMethodUtils.getSubtypeIdFromHashCode(mMethodMap.get(id),
                                subtype.hashCode()));
            } else {
                setInputMethod(token, id);
            }
        }
    }
    @Override
    public void showInputMethodAndSubtypeEnablerFromClient(
            IInputMethodClient client, String inputMethodId) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        synchronized (mMethodMap) {
            if (mCurClient == null || client == null|| mCurClient.client.asBinder() != client.asBinder()) {
                Slog.w(TAG, "Ignoring showInputMethodAndSubtypeEnablerFromClient of: " + client);
            }
            executeOrSendMessage(mCurMethod, mCaller.obtainMessageO(
                    MSG_SHOW_IM_SUBTYPE_ENABLER, inputMethodId));
        }
    }
    @Override
    public boolean switchToLastInputMethod(IBinder token) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
        }
        synchronized (mMethodMap) {
            final Pair<String, String> lastIme = mSettings.getLastInputMethodAndSubtypeLocked();
            final InputMethodInfo lastImi;
            if (lastIme != null) {
                lastImi = mMethodMap.get(lastIme.first);
            } else {
                lastImi = null;
            }
            String targetLastImiId = null;
            int subtypeId = NOT_A_SUBTYPE_ID;
            if (lastIme != null && lastImi != null) {
                final boolean imiIdIsSame = lastImi.getId().equals(mCurMethodId);
                final int lastSubtypeHash = Integer.valueOf(lastIme.second);
                final int currentSubtypeHash = mCurrentSubtype == null ? NOT_A_SUBTYPE_ID
                        : mCurrentSubtype.hashCode();
                if (!imiIdIsSame || lastSubtypeHash != currentSubtypeHash) {
                    targetLastImiId = lastIme.first;
                    subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, lastSubtypeHash);
                }
            }
            if (TextUtils.isEmpty(targetLastImiId)&& !InputMethodUtils.canAddToLastInputMethod(mCurrentSubtype)) {
                final List<InputMethodInfo> enabled = mSettings.getEnabledInputMethodListLocked();
                if (enabled != null) {
                    final int N = enabled.size();
                    final String locale = mCurrentSubtype == null
                            ? mRes.getConfiguration().locale.toString()
                            : mCurrentSubtype.getLocale();
                    for (int i = 0; i < N; ++i) {
                        final InputMethodInfo imi = enabled.get(i);
                        if (imi.getSubtypeCount() > 0 && InputMethodUtils.isSystemIme(imi)) {
                            InputMethodSubtype keyboardSubtype =
                                    InputMethodUtils.findLastResortApplicableSubtypeLocked(mRes,
                                            InputMethodUtils.getSubtypes(imi),
                                            InputMethodUtils.SUBTYPE_MODE_KEYBOARD, locale, true);
                            if (keyboardSubtype != null) {
                                targetLastImiId = imi.getId();
                                subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(
                                        imi, keyboardSubtype.hashCode());
                                if(keyboardSubtype.getLocale().equals(locale)) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(targetLastImiId)) {
                if (DEBUG) {
                    Slog.d(TAG, "Switch to: " + lastImi.getId() + ", " + lastIme.second
                            + ", from: " + mCurMethodId + ", " + subtypeId);
                }
                setInputMethodWithSubtypeIdLocked(token, targetLastImiId, subtypeId);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            } else {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
            }
        }
    }
    @Override
    public boolean switchToNextInputMethod(IBinder token, boolean onlyCurrentIme) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
        }
        synchronized (mMethodMap) {
            if (!calledWithValidToken(token)) {
                final int uid = Binder.getCallingUid();
                Slog.e(TAG, "Ignoring switchToNextInputMethod due to an invalid token. uid:" + uid
                        + " token:" + token);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return false;
            }
            final ImeSubtypeListItem nextSubtype = mSwitchingController.getNextInputMethodLocked(
                    onlyCurrentIme, mMethodMap.get(mCurMethodId), mCurrentSubtype);
            if (nextSubtype == null) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
            }
            setInputMethodWithSubtypeIdLocked(token, nextSubtype.mImi.getId(),
                    nextSubtype.mSubtypeId);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return true;
        }
    }
    @Override
    public boolean shouldOfferSwitchingToNextInputMethod(IBinder token) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
        }
        synchronized (mMethodMap) {
            if (!calledWithValidToken(token)) {
                final int uid = Binder.getCallingUid();
                Slog.e(TAG, "Ignoring shouldOfferSwitchingToNextInputMethod due to an invalid "
                        + "token. uid:" + uid + " token:" + token);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return false;
            }
            final ImeSubtypeListItem nextSubtype = mSwitchingController.getNextInputMethodLocked(
                    false , mMethodMap.get(mCurMethodId), mCurrentSubtype);
            if (nextSubtype == null) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
            }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return true;
        }
    }
    @Override
    public InputMethodSubtype getLastInputMethodSubtype() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return null;
        }
        synchronized (mMethodMap) {
            final Pair<String, String> lastIme = mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme == null || TextUtils.isEmpty(lastIme.first)|| TextUtils.isEmpty(lastIme.second))
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return null;
    }
            final InputMethodInfo lastImi = mMethodMap.get(lastIme.first);
            if (lastImi == null)
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return null;
    }
            try {
                final int lastSubtypeHash = Integer.valueOf(lastIme.second);
                final int lastSubtypeId =
                        InputMethodUtils.getSubtypeIdFromHashCode(lastImi, lastSubtypeHash);
                if (lastSubtypeId < 0 || lastSubtypeId >= lastImi.getSubtypeCount()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return null;
                }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return lastImi.getSubtypeAt(lastSubtypeId);
            } catch (NumberFormatException e) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return null;
            }
        }
    }
    @Override
    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        if (TextUtils.isEmpty(imiId) || subtypes == null || subtypes.length == 0)
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
    }
        synchronized (mMethodMap) {
            final InputMethodInfo imi = mMethodMap.get(imiId);
            if (imi == null)
    {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return;
    }
            final String[] packageInfos;
            try {
                packageInfos = mIPackageManager.getPackagesForUid(Binder.getCallingUid());
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to get package infos");
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
            }
            if (packageInfos != null) {
                final int packageNum = packageInfos.length;
                for (int i = 0; i < packageNum; ++i) {
                    if (packageInfos[i].equals(imi.getPackageName())) {
                        mFileManager.addInputMethodSubtypes(imi, subtypes);
                        final long ident = Binder.clearCallingIdentity();
                        try {
                            buildInputMethodListLocked(mMethodList, mMethodMap,
                                    false );
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return;
                    }
                }
            }
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
    }
    @Override
    public int getInputMethodWindowVisibleHeight() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return mWindowManagerService.getInputMethodWindowVisibleHeight();
    }
    @Override
    public void notifyUserAction(int sequenceNumber) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (DEBUG) {
            Slog.d(TAG, "Got the notification of a user action. sequenceNumber:" + sequenceNumber);
        }
        synchronized (mMethodMap) {
            if (mCurUserActionNotificationSequenceNumber != sequenceNumber) {
                if (DEBUG) {
                    Slog.d(TAG, "Ignoring the user action notification due to the sequence number "
                            + "mismatch. expected:" + mCurUserActionNotificationSequenceNumber
                            + " actual: " + sequenceNumber);
                }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
            }
            final InputMethodInfo imi = mMethodMap.get(mCurMethodId);
            if (imi != null) {
                mSwitchingController.onUserActionLocked(imi, mCurrentSubtype);
            }
        }
    }
    private void setInputMethodWithSubtypeId(IBinder token, String id, int subtypeId) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        synchronized (mMethodMap) {
            setInputMethodWithSubtypeIdLocked(token, id, subtypeId);
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void setInputMethodWithSubtypeIdLocked(IBinder token, String id, int subtypeId) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (token == null) {
            if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS)!= PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException(
                        "Using null token requires permission "
                        + android.Manifest.permission.WRITE_SECURE_SETTINGS);
            }
        } else if (mCurToken != token) {
            Slog.w(TAG, "Ignoring setInputMethod of uid " + Binder.getCallingUid()
                    + " token: " + token);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return;
        }
        final long ident = Binder.clearCallingIdentity();
        try {
            setInputMethodLocked(id, subtypeId);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
    @Override
    public void hideMySoftInput(IBinder token, int flags) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        synchronized (mMethodMap) {
            if (!calledWithValidToken(token)) {
                final int uid = Binder.getCallingUid();
                Slog.e(TAG, "Ignoring hideInputMethod due to an invalid token. uid:"
                        + uid + " token:" + token);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                hideCurrentInputLocked(flags, null);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }
    @Override
    public void showMySoftInput(IBinder token, int flags) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return;
        }
        synchronized (mMethodMap) {
            if (!calledWithValidToken(token)) {
                final int uid = Binder.getCallingUid();
                Slog.e(TAG, "Ignoring showMySoftInput due to an invalid token. uid:"
                        + uid + " token:" + token);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                showCurrentInputLocked(flags, null);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }
    void setEnabledSessionInMainThread(SessionState session) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mEnabledSession != session) {
            if (mEnabledSession != null && mEnabledSession.session != null) {
                try {
                    if (DEBUG) Slog.v(TAG, "Disabling: " + mEnabledSession);
                    mEnabledSession.method.setSessionEnabled(mEnabledSession.session, false);
                } catch (RemoteException e) {
                }
            }
            mEnabledSession = session;
            if (mEnabledSession != null && mEnabledSession.session != null) {
                try {
                    if (DEBUG) Slog.v(TAG, "Enabling: " + mEnabledSession);
                    mEnabledSession.method.setSessionEnabled(mEnabledSession.session, true);
                } catch (RemoteException e) {
                }
            }
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    @Override
    public boolean handleMessage(Message msg) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        SomeArgs args;
        switch (msg.what) {
            case MSG_SHOW_IM_PICKER:
                showInputMethodMenu();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_SHOW_IM_SUBTYPE_PICKER:
                showInputMethodSubtypeMenu();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_SHOW_IM_SUBTYPE_ENABLER:
                args = (SomeArgs)msg.obj;
                showInputMethodAndSubtypeEnabler((String)args.arg1);
                args.recycle();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_SHOW_IM_CONFIG:
                showConfigureInputMethods();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_UNBIND_INPUT:
                try {
                    ((IInputMethod)msg.obj).unbindInput();
                } catch (RemoteException e) {
                }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_BIND_INPUT:
                args = (SomeArgs)msg.obj;
                try {
                    ((IInputMethod)args.arg1).bindInput((InputBinding)args.arg2);
                } catch (RemoteException e) {
                }
                args.recycle();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_SHOW_SOFT_INPUT:
                args = (SomeArgs)msg.obj;
                try {
                    if (DEBUG) Slog.v(TAG, "Calling " + args.arg1 + ".showSoftInput("+ msg.arg1 + ", " + args.arg2 + ")");
                    ((IInputMethod)args.arg1).showSoftInput(msg.arg1, (ResultReceiver)args.arg2);
                } catch (RemoteException e) {
                }
                args.recycle();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_HIDE_SOFT_INPUT:
                args = (SomeArgs)msg.obj;
                try {
                    if (DEBUG) Slog.v(TAG, "Calling " + args.arg1 + ".hideSoftInput(0, "+ args.arg2 + ")");
                    ((IInputMethod)args.arg1).hideSoftInput(0, (ResultReceiver)args.arg2);
                } catch (RemoteException e) {
                }
                args.recycle();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_ATTACH_TOKEN:
                args = (SomeArgs)msg.obj;
                try {
                    if (DEBUG) Slog.v(TAG, "Sending attach of token: " + args.arg2);
                    ((IInputMethod)args.arg1).attachToken((IBinder)args.arg2);
                } catch (RemoteException e) {
                }
                args.recycle();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_CREATE_SESSION: {
                args = (SomeArgs)msg.obj;
                IInputMethod method = (IInputMethod)args.arg1;
                InputChannel channel = (InputChannel)args.arg2;
                try {
                    method.createSession(channel, (IInputSessionCallback)args.arg3);
                } catch (RemoteException e) {
                } finally {
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                }
                args.recycle();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            }
            case MSG_START_INPUT:
                args = (SomeArgs)msg.obj;
                try {
                    SessionState session = (SessionState)args.arg1;
                    setEnabledSessionInMainThread(session);
                    session.method.startInput((IInputContext)args.arg2,
                            (EditorInfo)args.arg3);
                } catch (RemoteException e) {
                }
                args.recycle();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_RESTART_INPUT:
                args = (SomeArgs)msg.obj;
                try {
                    SessionState session = (SessionState)args.arg1;
                    setEnabledSessionInMainThread(session);
                    session.method.restartInput((IInputContext)args.arg2,
                            (EditorInfo)args.arg3);
                } catch (RemoteException e) {
                }
                args.recycle();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_UNBIND_METHOD:
                try {
                    ((IInputMethodClient)msg.obj).onUnbindMethod(msg.arg1);
                } catch (RemoteException e) {
                }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_BIND_METHOD: {
                args = (SomeArgs)msg.obj;
                IInputMethodClient client = (IInputMethodClient)args.arg1;
                InputBindResult res = (InputBindResult)args.arg2;
                try {
                    client.onBindMethod(res);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Client died receiving input method " + args.arg2);
                } finally {
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                }
                args.recycle();
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            }
            case MSG_SET_ACTIVE:
                try {
                    ((ClientState)msg.obj).client.setActive(msg.arg1 != 0);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Got RemoteException sending setActive(false) notification to pid "
                            + ((ClientState)msg.obj).pid + " uid "
                            + ((ClientState)msg.obj).uid);
                }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            case MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER: {
                final int sequenceNumber = msg.arg1;
                final ClientState clientState = (ClientState)msg.obj;
                try {
                    clientState.client.setUserActionNotificationSequenceNumber(sequenceNumber);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Got RemoteException sending "
                            + "setUserActionNotificationSequenceNumber("
                            + sequenceNumber + ") notification to pid "
                            + clientState.pid + " uid "
                            + clientState.uid);
                }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            }
            case MSG_HARD_KEYBOARD_SWITCH_CHANGED:
                mHardKeyboardListener.handleHardKeyboardStatusChange(msg.arg1 == 1);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
    }
    private boolean chooseNewDefaultIMELocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        final InputMethodInfo imi = InputMethodUtils.getMostApplicableDefaultIME(
                mSettings.getEnabledInputMethodListLocked());
        if (imi != null) {
            if (DEBUG) {
                Slog.d(TAG, "New default IME was selected: " + imi.getId());
            }
            resetSelectedInputMethodAndSubtypeLocked(imi.getId());
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return true;
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
    }
    void buildInputMethodListLocked(ArrayList<InputMethodInfo> list,
            HashMap<String, InputMethodInfo> map, boolean resetDefaultEnabledIme) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (DEBUG) {
            Slog.d(TAG, "--- re-buildInputMethodList reset = " + resetDefaultEnabledIme
                    + " \n ------ \n" + InputMethodUtils.getStackTrace());
        }
        list.clear();
        map.clear();
        final PackageManager pm = mContext.getPackageManager();
        String disabledSysImes = mSettings.getDisabledSystemInputMethods();
        if (disabledSysImes == null) disabledSysImes = "";
        final List<ResolveInfo> services = pm.queryIntentServicesAsUser(
                new Intent(InputMethod.SERVICE_INTERFACE),
                PackageManager.GET_META_DATA | PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS,
                mSettings.getCurrentUserId());
        final HashMap<String, List<InputMethodSubtype>> additionalSubtypes =
                mFileManager.getAllAdditionalInputMethodSubtypes();
        for (int i = 0; i < services.size(); ++i) {
            ResolveInfo ri = services.get(i);
            ServiceInfo si = ri.serviceInfo;
            ComponentName compName = new ComponentName(si.packageName, si.name);
            if (!android.Manifest.permission.BIND_INPUT_METHOD.equals(si.permission)) {
                Slog.w(TAG, "Skipping input method " + compName
                        + ": it does not require the permission "
                        + android.Manifest.permission.BIND_INPUT_METHOD);
                continue;
            }
            if (DEBUG) Slog.d(TAG, "Checking " + compName);
            try {
                InputMethodInfo p = new InputMethodInfo(mContext, ri, additionalSubtypes);
                list.add(p);
                final String id = p.getId();
                map.put(id, p);
                if (DEBUG) {
                    Slog.d(TAG, "Found an input method " + p);
                }
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Unable to load input method " + compName, e);
            } catch (IOException e) {
                Slog.w(TAG, "Unable to load input method " + compName, e);
            }
        }
        if (resetDefaultEnabledIme) {
            final ArrayList<InputMethodInfo> defaultEnabledIme =
                    InputMethodUtils.getDefaultEnabledImes(mContext, mSystemReady, list);
            for (int i = 0; i < defaultEnabledIme.size(); ++i) {
                final InputMethodInfo imi =  defaultEnabledIme.get(i);
                if (DEBUG) {
                    Slog.d(TAG, "--- enable ime = " + imi);
                }
                setInputMethodEnabledLocked(imi.getId(), true);
            }
        }
        final String defaultImiId = mSettings.getSelectedInputMethod();
        if (!TextUtils.isEmpty(defaultImiId)) {
            if (!map.containsKey(defaultImiId)) {
                Slog.w(TAG, "Default IME is uninstalled. Choose new default IME.");
                if (chooseNewDefaultIMELocked()) {
                    updateInputMethodsFromSettingsLocked(true);
                }
            } else {
                setInputMethodEnabledLocked(defaultImiId, true);
            }
        }
        mSwitchingController.resetCircularListLocked(mContext);
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void showInputMethodMenu() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        showInputMethodMenuInternal(false);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void showInputMethodSubtypeMenu() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        showInputMethodMenuInternal(true);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void showInputMethodAndSubtypeEnabler(String inputMethodId) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (!TextUtils.isEmpty(inputMethodId)) {
            intent.putExtra(Settings.EXTRA_INPUT_METHOD_ID, inputMethodId);
        }
        mContext.startActivityAsUser(intent, null, UserHandle.CURRENT);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void showConfigureInputMethods() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivityAsUser(intent, null, UserHandle.CURRENT);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private boolean isScreenLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return mKeyguardManager != null&& mKeyguardManager.isKeyguardLocked() && mKeyguardManager.isKeyguardSecure();
    }
    private void showInputMethodMenuInternal(boolean showSubtypes) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (DEBUG) Slog.v(TAG, "Show switching menu");
        final Context context = mContext;
        final boolean isScreenLocked = isScreenLocked();
        final String lastInputMethodId = mSettings.getSelectedInputMethod();
        int lastInputMethodSubtypeId = mSettings.getSelectedInputMethodSubtypeId(lastInputMethodId);
        if (DEBUG) Slog.v(TAG, "Current IME: " + lastInputMethodId);
        synchronized (mMethodMap) {
            final HashMap<InputMethodInfo, List<InputMethodSubtype>> immis =
                    mSettings.getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked(
                            mContext);
            if (immis == null || immis.size() == 0) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return;
            }
            hideInputMethodMenuLocked();
            final List<ImeSubtypeListItem> imList =
                    mSwitchingController.getSortedInputMethodAndSubtypeListLocked(
                            showSubtypes, mInputShown, isScreenLocked);
            if (lastInputMethodSubtypeId == NOT_A_SUBTYPE_ID) {
                final InputMethodSubtype currentSubtype = getCurrentInputMethodSubtypeLocked();
                if (currentSubtype != null) {
                    final InputMethodInfo currentImi = mMethodMap.get(mCurMethodId);
                    lastInputMethodSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(
                            currentImi, currentSubtype.hashCode());
                }
            }
            final int N = imList.size();
            mIms = new InputMethodInfo[N];
            mSubtypeIds = new int[N];
            int checkedItem = 0;
            for (int i = 0; i < N; ++i) {
                final ImeSubtypeListItem item = imList.get(i);
                mIms[i] = item.mImi;
                mSubtypeIds[i] = item.mSubtypeId;
                if (mIms[i].getId().equals(lastInputMethodId)) {
                    int subtypeId = mSubtypeIds[i];
                    if ((subtypeId == NOT_A_SUBTYPE_ID)|| (lastInputMethodSubtypeId == NOT_A_SUBTYPE_ID && subtypeId == 0)|| (subtypeId == lastInputMethodSubtypeId)) {
                        checkedItem = i;
                    }
                }
            }
            final Context settingsContext = new ContextThemeWrapper(context,
                    com.android.internal.R.style.Theme_DeviceDefault_Settings);
            mDialogBuilder = new AlertDialog.Builder(settingsContext);
            mDialogBuilder.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    hideInputMethodMenu();
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                }
            });
            final Context dialogContext = mDialogBuilder.getContext();
            final TypedArray a = dialogContext.obtainStyledAttributes(null,
                    com.android.internal.R.styleable.DialogPreference,
                    com.android.internal.R.attr.alertDialogStyle, 0);
            final Drawable dialogIcon = a.getDrawable(
                    com.android.internal.R.styleable.DialogPreference_dialogIcon);
            a.recycle();
            mDialogBuilder.setIcon(dialogIcon);
            final LayoutInflater inflater = (LayoutInflater) dialogContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            final View tv = inflater.inflate(
                    com.android.internal.R.layout.input_method_switch_dialog_title, null);
            mDialogBuilder.setCustomTitle(tv);
            mSwitchingDialogTitleView = tv;
            mSwitchingDialogTitleView
                    .findViewById(com.android.internal.R.id.hard_keyboard_section)
                    .setVisibility(mWindowManagerService.isHardKeyboardAvailable()
                            ? View.VISIBLE : View.GONE);
            final Switch hardKeySwitch = (Switch) mSwitchingDialogTitleView.findViewById(
                    com.android.internal.R.id.hard_keyboard_switch);
            hardKeySwitch.setChecked(mShowImeWithHardKeyboard);
            hardKeySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    mSettings.setShowImeWithHardKeyboard(isChecked);
                    hideInputMethodMenu();
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                }
            });
            final ImeSubtypeListAdapter adapter = new ImeSubtypeListAdapter(dialogContext,
                    com.android.internal.R.layout.input_method_switch_item, imList, checkedItem);
            final OnClickListener choiceListener = new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    synchronized (mMethodMap) {
                        if (mIms == null || mIms.length <= which || mSubtypeIds == null|| mSubtypeIds.length <= which) {
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                                return;
                        }
                        final InputMethodInfo im = mIms[which];
                        int subtypeId = mSubtypeIds[which];
                        adapter.mCheckedItem = which;
                        adapter.notifyDataSetChanged();
                        hideInputMethodMenu();
                        if (im != null) {
                            if (subtypeId < 0 || subtypeId >= im.getSubtypeCount()) {
                                subtypeId = NOT_A_SUBTYPE_ID;
                            }
                            setInputMethodLocked(im.getId(), subtypeId);
                        }
                    }
                }
            };
            mDialogBuilder.setSingleChoiceItems(adapter, checkedItem, choiceListener);
            if (showSubtypes && !isScreenLocked) {
                final OnClickListener positiveListener = new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                        showConfigureInputMethods();
                    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    }
                };
                mDialogBuilder.setPositiveButton(
                        com.android.internal.R.string.configure_input_methods, positiveListener);
            }
            mSwitchingDialog = mDialogBuilder.create();
            mSwitchingDialog.setCanceledOnTouchOutside(true);
            mSwitchingDialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_INPUT_METHOD_DIALOG);
            mSwitchingDialog.getWindow().getAttributes().privateFlags |=
                    WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;
            mSwitchingDialog.getWindow().getAttributes().setTitle("Select input method");
            updateImeWindowStatusLocked();
            mSwitchingDialog.show();
        }
    }
    private static class ImeSubtypeListAdapter extends ArrayAdapter<ImeSubtypeListItem> {
        private final LayoutInflater mInflater;
        private final int mTextViewResourceId;
        private final List<ImeSubtypeListItem> mItemsList;
        public int mCheckedItem;
        public ImeSubtypeListAdapter(Context context, int textViewResourceId,
                List<ImeSubtypeListItem> itemsList, int checkedItem) {
            super(context, textViewResourceId, itemsList);
                    Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            mTextViewResourceId = textViewResourceId;
            mItemsList = itemsList;
            mCheckedItem = checkedItem;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            final View view = convertView != null ? convertView
                    : mInflater.inflate(mTextViewResourceId, null);
                    if (position < 0 || position >= mItemsList.size())
        {
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return view;
        }
            final ImeSubtypeListItem item = mItemsList.get(position);
            final CharSequence imeName = item.mImeName;
            final CharSequence subtypeName = item.mSubtypeName;
            final TextView firstTextView = (TextView)view.findViewById(android.R.id.text1);
            final TextView secondTextView = (TextView)view.findViewById(android.R.id.text2);
            if (TextUtils.isEmpty(subtypeName)) {
                firstTextView.setText(imeName);
                secondTextView.setVisibility(View.GONE);
            } else {
                firstTextView.setText(subtypeName);
                secondTextView.setText(imeName);
                secondTextView.setVisibility(View.VISIBLE);
            }
            final RadioButton radioButton =
                    (RadioButton)view.findViewById(com.android.internal.R.id.radio);
            radioButton.setChecked(position == mCheckedItem);
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return view;
        }
    }
    void hideInputMethodMenu() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        synchronized (mMethodMap) {
            hideInputMethodMenuLocked();
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    void hideInputMethodMenuLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (DEBUG) Slog.v(TAG, "Hide switching menu");
        if (mSwitchingDialog != null) {
            mSwitchingDialog.dismiss();
            mSwitchingDialog = null;
        }
        updateImeWindowStatusLocked();
        mDialogBuilder = null;
        mIms = null;
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    @Override
    public boolean setInputMethodEnabled(String id, boolean enabled) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
        }
        synchronized (mMethodMap) {
            if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS)!= PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException(
                        "Requires permission "
                        + android.Manifest.permission.WRITE_SECURE_SETTINGS);
            }
            long ident = Binder.clearCallingIdentity();
            try {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return setInputMethodEnabledLocked(id, enabled);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }
    boolean setInputMethodEnabledLocked(String id, boolean enabled) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        InputMethodInfo imm = mMethodMap.get(id);
        if (imm == null) {
            throw new IllegalArgumentException("Unknown id: " + mCurMethodId);
        }
        List<Pair<String, ArrayList<String>>> enabledInputMethodsList = mSettings
                .getEnabledInputMethodsAndSubtypeListLocked();
        if (enabled) {
            for (Pair<String, ArrayList<String>> pair: enabledInputMethodsList) {
                if (pair.first.equals(id)) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
                }
            }
            mSettings.appendAndPutEnabledInputMethodLocked(id, false);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
        } else {
            StringBuilder builder = new StringBuilder();
            if (mSettings.buildAndPutEnabledInputMethodsStrRemovingIdLocked(builder, enabledInputMethodsList, id)) {
                final String selId = mSettings.getSelectedInputMethod();
                if (id.equals(selId) && !chooseNewDefaultIMELocked()) {
                    Slog.i(TAG, "Can't find new IME, unsetting the current input method.");
                    resetSelectedInputMethodAndSubtypeLocked("");
                }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return true;
            } else {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
            }
        }
    }
    private void setSelectedInputMethodAndSubtypeLocked(InputMethodInfo imi, int subtypeId,
            boolean setSubtypeOnly) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        mSettings.saveCurrentInputMethodAndSubtypeToHistory(mCurMethodId, mCurrentSubtype);
        mCurUserActionNotificationSequenceNumber =
                Math.max(mCurUserActionNotificationSequenceNumber + 1, 1);
        if (DEBUG) {
            Slog.d(TAG, "Bump mCurUserActionNotificationSequenceNumber:"
                    + mCurUserActionNotificationSequenceNumber);
        }
        if (mCurClient != null && mCurClient.client != null) {
            executeOrSendMessage(mCurClient.client, mCaller.obtainMessageIO(
                    MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER,
                    mCurUserActionNotificationSequenceNumber, mCurClient));
        }
        if (imi == null || subtypeId < 0) {
            mSettings.putSelectedSubtype(NOT_A_SUBTYPE_ID);
            mCurrentSubtype = null;
        } else {
            if (subtypeId < imi.getSubtypeCount()) {
                InputMethodSubtype subtype = imi.getSubtypeAt(subtypeId);
                mSettings.putSelectedSubtype(subtype.hashCode());
                mCurrentSubtype = subtype;
            } else {
                mSettings.putSelectedSubtype(NOT_A_SUBTYPE_ID);
                mCurrentSubtype = getCurrentInputMethodSubtypeLocked();
            }
        }
        if (mSystemReady && !setSubtypeOnly) {
            mSettings.putSelectedInputMethod(imi != null ? imi.getId() : "");
        }
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private void resetSelectedInputMethodAndSubtypeLocked(String newDefaultIme) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        InputMethodInfo imi = mMethodMap.get(newDefaultIme);
        int lastSubtypeId = NOT_A_SUBTYPE_ID;
        if (imi != null && !TextUtils.isEmpty(newDefaultIme)) {
            String subtypeHashCode = mSettings.getLastSubtypeForInputMethodLocked(newDefaultIme);
            if (subtypeHashCode != null) {
                try {
                    lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(
                            imi, Integer.valueOf(subtypeHashCode));
                } catch (NumberFormatException e) {
                    Slog.w(TAG, "HashCode for subtype looks broken: " + subtypeHashCode, e);
                }
            }
        }
        setSelectedInputMethodAndSubtypeLocked(imi, lastSubtypeId, false);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private Pair<InputMethodInfo, InputMethodSubtype>
            findLastResortApplicableShortcutInputMethodAndSubtypeLocked(String mode) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        List<InputMethodInfo> imis = mSettings.getEnabledInputMethodListLocked();
        InputMethodInfo mostApplicableIMI = null;
        InputMethodSubtype mostApplicableSubtype = null;
        boolean foundInSystemIME = false;
        for (InputMethodInfo imi: imis) {
            final String imiId = imi.getId();
            if (foundInSystemIME && !imiId.equals(mCurMethodId)) {
                continue;
            }
            InputMethodSubtype subtype = null;
            final List<InputMethodSubtype> enabledSubtypes =
                    mSettings.getEnabledInputMethodSubtypeListLocked(mContext, imi, true);
            if (mCurrentSubtype != null) {
                subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(
                        mRes, enabledSubtypes, mode, mCurrentSubtype.getLocale(), false);
            }
            if (subtype == null) {
                subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(
                        mRes, enabledSubtypes, mode, null, true);
            }
            final ArrayList<InputMethodSubtype> overridingImplicitlyEnabledSubtypes =
                    InputMethodUtils.getOverridingImplicitlyEnabledSubtypes(imi, mode);
            final ArrayList<InputMethodSubtype> subtypesForSearch =
                    overridingImplicitlyEnabledSubtypes.isEmpty()
                            ? InputMethodUtils.getSubtypes(imi)
                            : overridingImplicitlyEnabledSubtypes;
            if (subtype == null && mCurrentSubtype != null) {
                subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(
                        mRes, subtypesForSearch, mode, mCurrentSubtype.getLocale(), false);
            }
            if (subtype == null) {
                subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(
                        mRes, subtypesForSearch, mode, null, true);
            }
            if (subtype != null) {
                if (imiId.equals(mCurMethodId)) {
                    mostApplicableIMI = imi;
                    mostApplicableSubtype = subtype;
                    break;
                } else if (!foundInSystemIME) {
                    mostApplicableIMI = imi;
                    mostApplicableSubtype = subtype;
                    if ((imi.getServiceInfo().applicationInfo.flags& ApplicationInfo.FLAG_SYSTEM) != 0) {
                        foundInSystemIME = true;
                    }
                }
            }
        }
        if (DEBUG) {
            if (mostApplicableIMI != null) {
                Slog.w(TAG, "Most applicable shortcut input method was:"
                        + mostApplicableIMI.getId());
                if (mostApplicableSubtype != null) {
                    Slog.w(TAG, "Most applicable shortcut input method subtype was:"
                            + "," + mostApplicableSubtype.getMode() + ","
                            + mostApplicableSubtype.getLocale());
                }
            }
        }
        if (mostApplicableIMI != null) {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return new Pair<InputMethodInfo, InputMethodSubtype> (mostApplicableIMI,mostApplicableSubtype);
        } else {
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return null;
        }
    }
    @Override
    public InputMethodSubtype getCurrentInputMethodSubtype() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return null;
        }
        synchronized (mMethodMap) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return getCurrentInputMethodSubtypeLocked();
        }
    }
    private InputMethodSubtype getCurrentInputMethodSubtypeLocked() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mCurMethodId == null) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return null;
        }
        final boolean subtypeIsSelected = mSettings.isSubtypeSelected();
        final InputMethodInfo imi = mMethodMap.get(mCurMethodId);
        if (imi == null || imi.getSubtypeCount() == 0) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return null;
        }
        if (!subtypeIsSelected || mCurrentSubtype == null|| !InputMethodUtils.isValidSubtypeId(imi, mCurrentSubtype.hashCode())) {
            int subtypeId = mSettings.getSelectedInputMethodSubtypeId(mCurMethodId);
            if (subtypeId == NOT_A_SUBTYPE_ID) {
                List<InputMethodSubtype> explicitlyOrImplicitlyEnabledSubtypes =
                        mSettings.getEnabledInputMethodSubtypeListLocked(mContext, imi, true);
                if (explicitlyOrImplicitlyEnabledSubtypes.size() == 1) {
                    mCurrentSubtype = explicitlyOrImplicitlyEnabledSubtypes.get(0);
                } else if (explicitlyOrImplicitlyEnabledSubtypes.size() > 1) {
                    mCurrentSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(
                            mRes, explicitlyOrImplicitlyEnabledSubtypes,
                            InputMethodUtils.SUBTYPE_MODE_KEYBOARD, null, true);
                    if (mCurrentSubtype == null) {
                        mCurrentSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(
                                mRes, explicitlyOrImplicitlyEnabledSubtypes, null, null,
                                true);
                    }
                }
            } else {
                mCurrentSubtype = InputMethodUtils.getSubtypes(imi).get(subtypeId);
            }
        }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return mCurrentSubtype;
    }
    private void addShortcutInputMethodAndSubtypes(InputMethodInfo imi,
            InputMethodSubtype subtype) {
                Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mShortcutInputMethodsAndSubtypes.containsKey(imi)) {
            mShortcutInputMethodsAndSubtypes.get(imi).add(subtype);
        } else {
            ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
            subtypes.add(subtype);
            mShortcutInputMethodsAndSubtypes.put(imi, subtypes);
        }
            Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    @SuppressWarnings("rawtypes")
    @Override
    public List getShortcutInputMethodsAndSubtypes() {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        synchronized (mMethodMap) {
            ArrayList<Object> ret = new ArrayList<Object>();
            if (mShortcutInputMethodsAndSubtypes.size() == 0) {
                Pair<InputMethodInfo, InputMethodSubtype> info =
                    findLastResortApplicableShortcutInputMethodAndSubtypeLocked(
                            InputMethodUtils.SUBTYPE_MODE_VOICE);
                if (info != null) {
                    ret.add(info.first);
                    ret.add(info.second);
                }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return ret;
            }
            for (InputMethodInfo imi: mShortcutInputMethodsAndSubtypes.keySet()) {
                ret.add(imi);
                for (InputMethodSubtype subtype: mShortcutInputMethodsAndSubtypes.get(imi)) {
                    ret.add(subtype);
                }
            }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return ret;
        }
    }
    @Override
    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (!calledFromValidUser()) {
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return false;
        }
        synchronized (mMethodMap) {
            if (subtype != null && mCurMethodId != null) {
                InputMethodInfo imi = mMethodMap.get(mCurMethodId);
                int subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(imi, subtype.hashCode());
                if (subtypeId != NOT_A_SUBTYPE_ID) {
                    setInputMethodLocked(mCurMethodId, subtypeId);
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return true;
                }
            }
    Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return false;
        }
    }
    private static class InputMethodFileManager {
        private static final String SYSTEM_PATH = "system";
        private static final String INPUT_METHOD_PATH = "inputmethod";
        private static final String ADDITIONAL_SUBTYPES_FILE_NAME = "subtypes.xml";
        private static final String NODE_SUBTYPES = "subtypes";
        private static final String NODE_SUBTYPE = "subtype";
        private static final String NODE_IMI = "imi";
        private static final String ATTR_ID = "id";
        private static final String ATTR_LABEL = "label";
        private static final String ATTR_ICON = "icon";
        private static final String ATTR_IME_SUBTYPE_LOCALE = "imeSubtypeLocale";
        private static final String ATTR_IME_SUBTYPE_MODE = "imeSubtypeMode";
        private static final String ATTR_IME_SUBTYPE_EXTRA_VALUE = "imeSubtypeExtraValue";
        private static final String ATTR_IS_AUXILIARY = "isAuxiliary";
        private final AtomicFile mAdditionalInputMethodSubtypeFile;
        private final HashMap<String, InputMethodInfo> mMethodMap;
        private final HashMap<String, List<InputMethodSubtype>> mAdditionalSubtypesMap =
                new HashMap<String, List<InputMethodSubtype>>();
        public InputMethodFileManager(HashMap<String, InputMethodInfo> methodMap, int userId) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            if (methodMap == null) {
                throw new NullPointerException("methodMap is null");
            }
            mMethodMap = methodMap;
            final File systemDir = userId == UserHandle.USER_OWNER
                    ? new File(Environment.getDataDirectory(), SYSTEM_PATH)
                    : Environment.getUserSystemDirectory(userId);
            final File inputMethodDir = new File(systemDir, INPUT_METHOD_PATH);
            if (!inputMethodDir.mkdirs()) {
                Slog.w(TAG, "Couldn't create dir.: " + inputMethodDir.getAbsolutePath());
            }
            final File subtypeFile = new File(inputMethodDir, ADDITIONAL_SUBTYPES_FILE_NAME);
            mAdditionalInputMethodSubtypeFile = new AtomicFile(subtypeFile);
            if (!subtypeFile.exists()) {
                writeAdditionalInputMethodSubtypes(
                        mAdditionalSubtypesMap, mAdditionalInputMethodSubtypeFile, methodMap);
            } else {
                readAdditionalInputMethodSubtypes(
                        mAdditionalSubtypesMap, mAdditionalInputMethodSubtypeFile);
            }
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        private void deleteAllInputMethodSubtypes(String imiId) {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            synchronized (mMethodMap) {
                mAdditionalSubtypesMap.remove(imiId);
                writeAdditionalInputMethodSubtypes(
                        mAdditionalSubtypesMap, mAdditionalInputMethodSubtypeFile, mMethodMap);
            }
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        public void addInputMethodSubtypes(
                InputMethodInfo imi, InputMethodSubtype[] additionalSubtypes) {
                    Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            synchronized (mMethodMap) {
                final ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
                final int N = additionalSubtypes.length;
                for (int i = 0; i < N; ++i) {
                    final InputMethodSubtype subtype = additionalSubtypes[i];
                    if (!subtypes.contains(subtype)) {
                        subtypes.add(subtype);
                    } else {
                        Slog.w(TAG, "Duplicated subtype definition found: "
                                + subtype.getLocale() + ", " + subtype.getMode());
                    }
                }
                mAdditionalSubtypesMap.put(imi.getId(), subtypes);
                writeAdditionalInputMethodSubtypes(
                        mAdditionalSubtypesMap, mAdditionalInputMethodSubtypeFile, mMethodMap);
            }
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        public HashMap<String, List<InputMethodSubtype>> getAllAdditionalInputMethodSubtypes() {
            Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            synchronized (mMethodMap) {
        Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return mAdditionalSubtypesMap;
            }
        }
        private static void writeAdditionalInputMethodSubtypes(
                HashMap<String, List<InputMethodSubtype>> allSubtypes, AtomicFile subtypesFile,
                HashMap<String, InputMethodInfo> methodMap) {
                    Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            final boolean isSetMethodMap = methodMap != null && methodMap.size() > 0;
            FileOutputStream fos = null;
            try {
                fos = subtypesFile.startWrite();
                final XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fos, "utf-8");
                out.startDocument(null, true);
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, NODE_SUBTYPES);
                for (String imiId : allSubtypes.keySet()) {
                    if (isSetMethodMap && !methodMap.containsKey(imiId)) {
                        Slog.w(TAG, "IME uninstalled or not valid.: " + imiId);
                        continue;
                    }
                    out.startTag(null, NODE_IMI);
                    out.attribute(null, ATTR_ID, imiId);
                    final List<InputMethodSubtype> subtypesList = allSubtypes.get(imiId);
                    final int N = subtypesList.size();
                    for (int i = 0; i < N; ++i) {
                        final InputMethodSubtype subtype = subtypesList.get(i);
                        out.startTag(null, NODE_SUBTYPE);
                        out.attribute(null, ATTR_ICON, String.valueOf(subtype.getIconResId()));
                        out.attribute(null, ATTR_LABEL, String.valueOf(subtype.getNameResId()));
                        out.attribute(null, ATTR_IME_SUBTYPE_LOCALE, subtype.getLocale());
                        out.attribute(null, ATTR_IME_SUBTYPE_MODE, subtype.getMode());
                        out.attribute(null, ATTR_IME_SUBTYPE_EXTRA_VALUE, subtype.getExtraValue());
                        out.attribute(null, ATTR_IS_AUXILIARY,
                                String.valueOf(subtype.isAuxiliary() ? 1 : 0));
                        out.endTag(null, NODE_SUBTYPE);
                    }
                    out.endTag(null, NODE_IMI);
                }
                out.endTag(null, NODE_SUBTYPES);
                out.endDocument();
                subtypesFile.finishWrite(fos);
            } catch (java.io.IOException e) {
                Slog.w(TAG, "Error writing subtypes", e);
                if (fos != null) {
                    subtypesFile.failWrite(fos);
                }
            }
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        private static void readAdditionalInputMethodSubtypes(
                HashMap<String, List<InputMethodSubtype>> allSubtypes, AtomicFile subtypesFile) {
                    Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                if (allSubtypes == null || subtypesFile == null)
                {
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
                }
            allSubtypes.clear();
            FileInputStream fis = null;
            try {
                fis = subtypesFile.openRead();
                final XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fis, null);
                int type = parser.getEventType();
                while ((type = parser.next()) != XmlPullParser.START_TAG&& type != XmlPullParser.END_DOCUMENT) {}
                String firstNodeName = parser.getName();
                if (!NODE_SUBTYPES.equals(firstNodeName)) {
                    throw new XmlPullParserException("Xml doesn't start with subtypes");
                }
                final int depth =parser.getDepth();
                String currentImiId = null;
                ArrayList<InputMethodSubtype> tempSubtypesArray = null;
                while (((type = parser.next()) != XmlPullParser.END_TAG|| parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                    if (type != XmlPullParser.START_TAG)
                        continue;
                    final String nodeName = parser.getName();
                    if (NODE_IMI.equals(nodeName)) {
                        currentImiId = parser.getAttributeValue(null, ATTR_ID);
                        if (TextUtils.isEmpty(currentImiId)) {
                            Slog.w(TAG, "Invalid imi id found in subtypes.xml");
                            continue;
                        }
                        tempSubtypesArray = new ArrayList<InputMethodSubtype>();
                        allSubtypes.put(currentImiId, tempSubtypesArray);
                    } else if (NODE_SUBTYPE.equals(nodeName)) {
                        if (TextUtils.isEmpty(currentImiId) || tempSubtypesArray == null) {
                            Slog.w(TAG, "IME uninstalled or not valid.: " + currentImiId);
                            continue;
                        }
                        final int icon = Integer.valueOf(
                                parser.getAttributeValue(null, ATTR_ICON));
                        final int label = Integer.valueOf(
                                parser.getAttributeValue(null, ATTR_LABEL));
                        final String imeSubtypeLocale =
                                parser.getAttributeValue(null, ATTR_IME_SUBTYPE_LOCALE);
                        final String imeSubtypeMode =
                                parser.getAttributeValue(null, ATTR_IME_SUBTYPE_MODE);
                        final String imeSubtypeExtraValue =
                                parser.getAttributeValue(null, ATTR_IME_SUBTYPE_EXTRA_VALUE);
                        final boolean isAuxiliary = "1".equals(String.valueOf(
                                parser.getAttributeValue(null, ATTR_IS_AUXILIARY)));
                        final InputMethodSubtype subtype = new InputMethodSubtypeBuilder()
                                .setSubtypeNameResId(label)
                                .setSubtypeIconResId(icon)
                                .setSubtypeLocale(imeSubtypeLocale)
                                .setSubtypeMode(imeSubtypeMode)
                                .setSubtypeExtraValue(imeSubtypeExtraValue)
                                .setIsAuxiliary(isAuxiliary)
                                .build();
                        tempSubtypesArray.add(subtype);
                    }
                }
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Error reading subtypes: " + e);
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
            } catch (java.io.IOException e) {
                Slog.w(TAG, "Error reading subtypes: " + e);
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
            } catch (NumberFormatException e) {
                Slog.w(TAG, "Error reading subtypes: " + e);
                Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                return;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (java.io.IOException e1) {
                        Slog.w(TAG, "Failed to close.");
                    }
                }
            }
        }
    }
    @Override
protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
     Slog.w(TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)!= PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump InputMethodManager from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
 Slog.w(TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                    return;
        }
        IInputMethod method;
        ClientState client;
        final Printer p = new PrintWriterPrinter(pw);
        synchronized (mMethodMap) {
            p.println("Current Input Method Manager state:");
            int N = mMethodList.size();
            p.println("  Input Methods:");
            for (int i=0; i<N; i++) {
                InputMethodInfo info = mMethodList.get(i);
                p.println("  InputMethod #" + i + ":");
                info.dump(p, "    ");
            }
            p.println("  Clients:");
            for (ClientState ci : mClients.values()) {
                p.println("  Client " + ci + ":");
                p.println("    client=" + ci.client);
                p.println("    inputContext=" + ci.inputContext);
                p.println("    sessionRequested=" + ci.sessionRequested);
                p.println("    curSession=" + ci.curSession);
            }
            p.println("  mCurMethodId=" + mCurMethodId);
            client = mCurClient;
            p.println("  mCurClient=" + client + " mCurSeq=" + mCurSeq);
            p.println("  mCurFocusedWindow=" + mCurFocusedWindow);
            p.println("  mCurId=" + mCurId + " mHaveConnect=" + mHaveConnection
                    + " mBoundToMethod=" + mBoundToMethod);
            p.println("  mCurToken=" + mCurToken);
            p.println("  mCurIntent=" + mCurIntent);
            method = mCurMethod;
            p.println("  mCurMethod=" + mCurMethod);
            p.println("  mEnabledSession=" + mEnabledSession);
            p.println("  mShowRequested=" + mShowRequested
                    + " mShowExplicitlyRequested=" + mShowExplicitlyRequested
                    + " mShowForced=" + mShowForced
                    + " mInputShown=" + mInputShown);
            p.println("  mCurUserActionNotificationSequenceNumber="
                    + mCurUserActionNotificationSequenceNumber);
            p.println("  mSystemReady=" + mSystemReady + " mInteractive=" + mScreenOn);
        }
        p.println(" ");
        if (client != null) {
            pw.flush();
            try {
                client.client.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                p.println("Input method client dead: " + e);
            }
        } else {
            p.println("No input method client.");
        }
        p.println(" ");
        if (method != null) {
            pw.flush();
            try {
                method.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                p.println("Input method service dead: " + e);
            }
        } else {
            p.println("No input method service.");
        }
    }
}
