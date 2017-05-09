package com.android.server;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import android.net.Uri;
import android.util.FastImmutableArraySet;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.MutableInt;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.LogPrinter;
import android.util.Printer;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.internal.util.FastPrintWriter;
public abstract class IntentResolver<F extends IntentFilter, R extends Object> {
public static final String ENTRY_EXIT_TAG = "IntentResolver";
public static final String ENTRY_EXIT_TAG = "IntentResolver";
    final private static String TAG = "IntentResolver";
    final private static boolean DEBUG = false;
    final private static boolean localLOGV = DEBUG || false;
    public void addFilter(F f) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (localLOGV) {
            Slog.v(TAG, "Adding filter: " + f);
            f.dump(new LogPrinter(Log.VERBOSE, TAG, Log.LOG_ID_SYSTEM), "      ");
            Slog.v(TAG, "    Building Lookup Maps:");
        }
        mFilters.add(f);
        int numS = register_intent_filter(f, f.schemesIterator(),
                mSchemeToFilter, "      Scheme: ");
        int numT = register_mime_types(f, "      Type: ");
        if (numS == 0 && numT == 0) {
            register_intent_filter(f, f.actionsIterator(),
                    mActionToFilter, "      Action: ");
        }
        if (numT != 0) {
            register_intent_filter(f, f.actionsIterator(),
                    mTypedActionToFilter, "      TypedAction: ");
        }
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private boolean filterEquals(IntentFilter f1, IntentFilter f2) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        int s1 = f1.countActions();
        int s2 = f2.countActions();
        if (s1 != s2) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
        }
        for (int i=0; i<s1; i++) {
            if (!f2.hasAction(f1.getAction(i))) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
            }
        }
        s1 = f1.countCategories();
        s2 = f2.countCategories();
        if (s1 != s2) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
        }
        for (int i=0; i<s1; i++) {
            if (!f2.hasCategory(f1.getCategory(i))) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
            }
        }
        s1 = f1.countDataTypes();
        s2 = f2.countDataTypes();
        if (s1 != s2) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
        }
        for (int i=0; i<s1; i++) {
            if (!f2.hasExactDataType(f1.getDataType(i))) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
            }
        }
        s1 = f1.countDataSchemes();
        s2 = f2.countDataSchemes();
        if (s1 != s2) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
        }
        for (int i=0; i<s1; i++) {
            if (!f2.hasDataScheme(f1.getDataScheme(i))) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
            }
        }
        s1 = f1.countDataAuthorities();
        s2 = f2.countDataAuthorities();
        if (s1 != s2) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
        }
        for (int i=0; i<s1; i++) {
            if (!f2.hasDataAuthority(f1.getDataAuthority(i))) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
            }
        }
        s1 = f1.countDataPaths();
        s2 = f2.countDataPaths();
        if (s1 != s2) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
        }
        for (int i=0; i<s1; i++) {
            if (!f2.hasDataPath(f1.getDataPath(i))) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
            }
        }
        s1 = f1.countDataSchemeSpecificParts();
        s2 = f2.countDataSchemeSpecificParts();
        if (s1 != s2) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
        }
        for (int i=0; i<s1; i++) {
            if (!f2.hasDataSchemeSpecificPart(f1.getDataSchemeSpecificPart(i))) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
            }
        }
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return true;
    }
    private ArrayList<F> collectFilters(F[] array, IntentFilter matching) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        ArrayList<F> res = null;
        if (array != null) {
            for (int i=0; i<array.length; i++) {
                F cur = array[i];
                if (cur == null) {
                    break;
                }
                if (filterEquals(cur, matching)) {
                    if (res == null) {
                        res = new ArrayList<>();
                    }
                    res.add(cur);
                }
            }
        }
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return res;
    }
    public ArrayList<F> findFilters(IntentFilter matching) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (matching.countDataSchemes() == 1) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return collectFilters(mSchemeToFilter.get(matching.getDataScheme(0)), matching);
        } else if (matching.countDataTypes() != 0 && matching.countActions() == 1) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return collectFilters(mTypedActionToFilter.get(matching.getAction(0)), matching);
        } else if (matching.countDataTypes() == 0 && matching.countDataSchemes() == 0&& matching.countActions() == 1) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return collectFilters(mActionToFilter.get(matching.getAction(0)), matching);
        } else {
            ArrayList<F> res = null;
            for (F cur : mFilters) {
                if (filterEquals(cur, matching)) {
                    if (res == null) {
                        res = new ArrayList<>();
                    }
                    res.add(cur);
                }
            }
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return res;
        }
    }
    public void removeFilter(F f) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        removeFilterInternal(f);
        mFilters.remove(f);
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    void removeFilterInternal(F f) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        if (localLOGV) {
            Slog.v(TAG, "Removing filter: " + f);
            f.dump(new LogPrinter(Log.VERBOSE, TAG, Log.LOG_ID_SYSTEM), "      ");
            Slog.v(TAG, "    Cleaning Lookup Maps:");
        }
        int numS = unregister_intent_filter(f, f.schemesIterator(),
                mSchemeToFilter, "      Scheme: ");
        int numT = unregister_mime_types(f, "      Type: ");
        if (numS == 0 && numT == 0) {
            unregister_intent_filter(f, f.actionsIterator(),
                    mActionToFilter, "      Action: ");
        }
        if (numT != 0) {
            unregister_intent_filter(f, f.actionsIterator(),
                    mTypedActionToFilter, "      TypedAction: ");
        }
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    boolean dumpMap(PrintWriter out, String titlePrefix, String title,
            String prefix, ArrayMap<String, F[]> map, String packageName,
            boolean printFilter, boolean collapseDuplicates) {
                Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        final String eprefix = prefix + "  ";
        final String fprefix = prefix + "    ";
        final ArrayMap<Object, MutableInt> found = new ArrayMap<>();
        boolean printedSomething = false;
        Printer printer = null;
        for (int mapi=0; mapi<map.size(); mapi++) {
            F[] a = map.valueAt(mapi);
            final int N = a.length;
            boolean printedHeader = false;
            F filter;
            if (collapseDuplicates) {
                found.clear();
                for (int i=0; i<N && (filter=a[i]) != null; i++) {
                    if (packageName != null && !isPackageForFilter(packageName, filter)) {
                        continue;
                    }
                    Object label = filterToLabel(filter);
                    int index = found.indexOfKey(label);
                    if (index < 0) {
                        found.put(label, new MutableInt(1));
                    } else {
                        found.valueAt(index).value++;
                    }
                }
                for (int i=0; i<found.size(); i++) {
                    if (title != null) {
                        out.print(titlePrefix); out.println(title);
                        title = null;
                    }
                    if (!printedHeader) {
                        out.print(eprefix); out.print(map.keyAt(mapi)); out.println(":");
                        printedHeader = true;
                    }
                    printedSomething = true;
                    dumpFilterLabel(out, fprefix, found.keyAt(i), found.valueAt(i).value);
                }
            } else {
                for (int i=0; i<N && (filter=a[i]) != null; i++) {
                    if (packageName != null && !isPackageForFilter(packageName, filter)) {
                        continue;
                    }
                    if (title != null) {
                        out.print(titlePrefix); out.println(title);
                        title = null;
                    }
                    if (!printedHeader) {
                        out.print(eprefix); out.print(map.keyAt(mapi)); out.println(":");
                        printedHeader = true;
                    }
                    printedSomething = true;
                    dumpFilter(out, fprefix, filter);
                    if (printFilter) {
                        if (printer == null) {
                            printer = new PrintWriterPrinter(out);
                        }
                        filter.dump(printer, fprefix + "  ");
                    }
                }
            }
        }
            Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return printedSomething;
    }
    public boolean dump(PrintWriter out, String title, String prefix, String packageName,
            boolean printFilter, boolean collapseDuplicates) {
                Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        String innerPrefix = prefix + "  ";
        String sepPrefix = "\n" + prefix;
        String curPrefix = title + "\n" + prefix;
        if (dumpMap(out, curPrefix, "Full MIME Types:", innerPrefix,mTypeToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Base MIME Types:", innerPrefix,mBaseTypeToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Wild MIME Types:", innerPrefix,mWildTypeToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Schemes:", innerPrefix,mSchemeToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Non-Data Actions:", innerPrefix,mActionToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "MIME Typed Actions:", innerPrefix,mTypedActionToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
            Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return curPrefix == sepPrefix;
    }
    private class IteratorWrapper implements Iterator<F> {
        private final Iterator<F> mI;
        private F mCur;
        IteratorWrapper(Iterator<F> it) {
            Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            mI = it;
        Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
        public boolean hasNext() {
            Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return mI.hasNext();
        }
        public F next() {
            Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        return (mCur = mI.next());
        }
        public void remove() {
            Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            if (mCur != null) {
                removeFilterInternal(mCur);
            }
            mI.remove();
        Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        }
    }
    public Iterator<F> filterIterator() {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return new IteratorWrapper(mFilters.iterator());
    }
    public Set<F> filterSet() {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return Collections.unmodifiableSet(mFilters);
    }
    public List<R> queryIntentFromList(Intent intent, String resolvedType, 
            boolean defaultOnly, ArrayList<F[]> listCut, int userId) {
                Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        ArrayList<R> resultList = new ArrayList<R>();
        final boolean debug = localLOGV ||
                ((intent.getFlags() & Intent.FLAG_DEBUG_LOG_RESOLUTION) != 0);
        FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
        final String scheme = intent.getScheme();
        int N = listCut.size();
        for (int i = 0; i < N; ++i) {
            buildResolveList(intent, categories, debug, defaultOnly,
                    resolvedType, scheme, listCut.get(i), resultList, userId);
        }
        sortResults(resultList);
            Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return resultList;
    }
    public List<R> queryIntent(Intent intent, String resolvedType, boolean defaultOnly,
            int userId) {
                Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
                Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        String scheme = intent.getScheme();
        ArrayList<R> finalList = new ArrayList<R>();
        final boolean debug = localLOGV ||
                ((intent.getFlags() & Intent.FLAG_DEBUG_LOG_RESOLUTION) != 0);
        if (debug) Slog.v(TAG, "Resolving type=" + resolvedType + " scheme=" + scheme+ " defaultOnly=" + defaultOnly + " userId=" + userId + " of " + intent);
        F[] firstTypeCut = null;
        F[] secondTypeCut = null;
        F[] thirdTypeCut = null;
        F[] schemeCut = null;
        if (resolvedType != null) {
            int slashpos = resolvedType.indexOf('/');
            if (slashpos > 0) {
                final String baseType = resolvedType.substring(0, slashpos);
                if (!baseType.equals("*")) {
                    if (resolvedType.length() != slashpos+2|| resolvedType.charAt(slashpos+1) != '*') {
                        firstTypeCut = mTypeToFilter.get(resolvedType);
                        if (debug) Slog.v(TAG, "First type cut: " + Arrays.toString(firstTypeCut));
                        secondTypeCut = mWildTypeToFilter.get(baseType);
                        if (debug) Slog.v(TAG, "Second type cut: "+ Arrays.toString(secondTypeCut));
                    } else {
                        firstTypeCut = mBaseTypeToFilter.get(baseType);
                        if (debug) Slog.v(TAG, "First type cut: " + Arrays.toString(firstTypeCut));
                        secondTypeCut = mWildTypeToFilter.get(baseType);
                        if (debug) Slog.v(TAG, "Second type cut: "+ Arrays.toString(secondTypeCut));
                    }
                    thirdTypeCut = mWildTypeToFilter.get("*");
                    if (debug) Slog.v(TAG, "Third type cut: " + Arrays.toString(thirdTypeCut));
                } else if (intent.getAction() != null) {
                    firstTypeCut = mTypedActionToFilter.get(intent.getAction());
                    if (debug) Slog.v(TAG, "Typed Action list: " + Arrays.toString(firstTypeCut));
                }
            }
        }
        if (scheme != null) {
            schemeCut = mSchemeToFilter.get(scheme);
            if (debug) Slog.v(TAG, "Scheme list: " + Arrays.toString(schemeCut));
        }
        if (resolvedType == null && scheme == null && intent.getAction() != null) {
            firstTypeCut = mActionToFilter.get(intent.getAction());
            if (debug) Slog.v(TAG, "Action list: " + Arrays.toString(firstTypeCut));
        }
        FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
        if (firstTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly,
                    resolvedType, scheme, firstTypeCut, finalList, userId);
        }
        if (secondTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly,
                    resolvedType, scheme, secondTypeCut, finalList, userId);
        }
        if (thirdTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly,
                    resolvedType, scheme, thirdTypeCut, finalList, userId);
        }
        if (schemeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly,
                    resolvedType, scheme, schemeCut, finalList, userId);
        }
        sortResults(finalList);
        if (debug) {
            Slog.v(TAG, "Final result list:");
            for (int i=0; i<finalList.size(); i++) {
                Slog.v(TAG, "  " + finalList.get(i));
            }
        }
            Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
            return finalList;
    }
    protected boolean allowFilterResult(F filter, List<R> dest) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return true;
    }
    protected boolean isFilterStopped(F filter, int userId) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return false;
    }
    protected abstract boolean isPackageForFilter(String packageName, F filter);
    protected abstract F[] newArray(int size);
    @SuppressWarnings("unchecked")
    protected R newResult(F filter, int match, int userId) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return (R)filter;
    }
    @SuppressWarnings("unchecked")
    protected void sortResults(List<R> results) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Collections.sort(results, mResolvePrioritySorter);
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    protected void dumpFilter(PrintWriter out, String prefix, F filter) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        out.print(prefix); out.println(filter);
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    protected Object filterToLabel(F filter) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return "IntentFilter";
    }
    protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        out.print(prefix); out.print(label); out.print(": "); out.println(count);
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private final void addFilter(ArrayMap<String, F[]> map, String name, F filter) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        F[] array = map.get(name);
        if (array == null) {
            array = newArray(2);
            map.put(name,  array);
            array[0] = filter;
        } else {
            final int N = array.length;
            int i = N;
            while (i > 0 && array[i-1] == null) {
                i--;
            }
            if (i < N) {
                array[i] = filter;
            } else {
                F[] newa = newArray((N*3)/2);
                System.arraycopy(array, 0, newa, 0, N);
                newa[N] = filter;
                map.put(name, newa);
            }
        }
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    }
    private final int register_mime_types(F filter, String prefix) {
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        Slog.w(ENTRY_EXIT_TAG,"entry: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
        final Iterator<String> i = filter.typesIterator();
        if (i == null) {
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    Slog.w(ENTRY_EXIT_TAG,"exit: " + Thread.currentThread().getStackTrace()[2].getMethodName()+"() "+Thread.currentThread().getStackTrace()[2].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber() );
    return 0;
        }
        int num = 0;
        while (i.hasNext()) {
            String name = i.next();
            num++;
            if (localLOGV) Slog.v(TAG, prefix + name);
            String baseName = name;
            final int slashpos = name.indexOf('/');
            if (slashpos > 0) {
                baseName = name.substring(0, slashpos).intern();
            } else {
                name = name + 
    private final ArraySet<F> mFilters = new ArraySet<F>();
    private final ArrayMap<String, F[]> mTypeToFilter = new ArrayMap<String, F[]>();
    private final ArrayMap<String, F[]> mBaseTypeToFilter = new ArrayMap<String, F[]>();
    private final ArrayMap<String, F[]> mWildTypeToFilter = new ArrayMap<String, F[]>();
    private final ArrayMap<String, F[]> mSchemeToFilter = new ArrayMap<String, F[]>();
    private final ArrayMap<String, F[]> mActionToFilter = new ArrayMap<String, F[]>();
    private final ArrayMap<String, F[]> mTypedActionToFilter = new ArrayMap<String, F[]>();
}
