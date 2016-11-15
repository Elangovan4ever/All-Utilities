# AndroidMethodTraceFilter
This tool is to show a hierarchy of function calls from android stack which is obtained via method tracing option in Android.

Author: Elangovan Manickam

To know why and how this tool is used read below:

Android Application Method Trace
-----------------------------------

You can do method tracing in 2 ways.
1) start and stop method tracing inside the android app itself and analyse the generated tracing.
2) start and stop method tracing using the DDMS traceview(Tools->Android->Android Device Monitor and then select DDMS tool button).

We will only see the first option because for second option you can get much information in online and personally I don't find this option much intuitive.



Start and stop method tracing inside the android app itself and analyse the generated tracing.
----------------------------------------------------------------------------------------------
To find what is the trace path in the emulator or device
1) On your onCreate() method of the activity, add the following line.
	Log.d(TAG,"getExternalStorageDirectory: "+Environment.getExternalStorageDirectory().getAbsolutePath())

2) run the app. find the above log line from the logcat. It would be something like the following
	/storage/emulated/0
	
Under this path the trace files will be generated.

To generate the method trace file when you run your app, you need to the following:
1) On your onCreate() method of the activity, add the following line. I used "sampleapp", you can use desired file name. you can add this method anywhere in your activity from where you want to start the method tracing.
	Debug.startMethodTracing("sampleapp");
2) On your onDestroy() method of the activity, add the following line.
	Debug.stopMethodTracing();
3) Run your app (either on emulator or on real device. I used emulator with Nexus_5_API_24). Operate your app which you want to see the method traces.
4) Once you done with your app, press back button until your app terminates. This will invoke the onDestroy() of the activity which invokes stopMethodTracing() which will create the "sampleapp.trace" file in the device (in emulator or phone).
5) Now you need to copy the file to local system, in my case I wanted to copy to windows system.
	(You can omit this step if you are using my "AndroidMethodTraceFilter" java utility program. Please refer "using dmtracedump" section below)
	you can do this using adb command as following
		adb pull /storage/emulated/0/Android/data/com.example.emanickam.sampleapp/files/sampleapp.trace D:\android_trace_files\sampleapp.trace
		
		example:
		C:\Users\emanickam>adb pull /storage/emulated/0/Android/data/com.example.emanickam.sampleapp/files/sampleapp.trace D:\android_trace_files\sampleapp.trace
		2245 KB/s (3575412 bytes in 1.555s)
		
	if you don't know the exact path the use the following which will copy all the files under the directory
		adb pull /storage/emualated/0
		
		example:
		C:\Users\emanickam>adb pull /storage/emulated/0
		pull: building file list...
		pull: /storage/emulated/0/Android/data/com.example.emanickam.sampleapp/files/sampleapp.trace -> ./Android/data/com.example.emanickam.sampleapp/files/sampleapp.trace
		pull: /storage/emulated/0/Android/data/.nomedia -> ./Android/data/.nomedia
		2 files pulled. 0 files skipped.
		1782 KB/s (3575412 bytes in 1.959s)
		

Till now we just copied file to local system. To analyse the filer there are  two ways.
1) dmtracedump - graphical tool, open source, old one, not actively supported on windows. but worth trying.
2) traceview - kind of text view with hierarchical tree, android studio's tool

Tip:
Android Studio installs the platform tools in the following directory. If you don't know where it is , just search "sdk" in C directory.
C:\Users\emanickam\AppData\Local\Android\sdk\platform-tools

Lets explore both the ways.
1) using dmtracedump
   -------------
	1) 	dmtracedump comes as part of android platform tools. Make sure the platform-tools path is added to the "PATH" environment variable.
			C:\Users\emanickam\AppData\Local\Android\sdk\platform-tools\dmtracedump.exe
	2) 	The dmtracedump tool require the graphviz tool to be installed. Download the graphviz from the follwing link for windows.
			http://www.graphviz.org/Download_windows.php
		I downloaded "graphviz-2.38.msi" and installed in following path. Make sure you add graphviz\bin path also to the "PATH" environment variable.
		example path: C:\Program Files (x86)\Graphviz2.38\bin\
	3)  Skip to step 4. DO this step only if you want to manually go through the fucntion call stack.
		Open command prompt in windows, run the following command to generate a readable trace text file.
			dmtracedump -ho D:\android_trace_files\sampleapp.trace > D:\android_trace_files\methodtrace.txt
		This will generate a text file which will have each function calls. This is not very easy to see the hierarchy.
	4)  I have written a small utility java program to make our task easy from this point. Download the project from following git repository
			https://github.com/Elangovan4ever/AndroidMethodTraceFilter
	5)	Once downloaded, import this project in eclipse. This project is created using eclipse Kepler version with JRE1.7.
			File->Import->Existing project into workspace
	6)	Once imported into eclipse, open file named "MethodTraceFilter.java".
		Change the trace file path to point to the directory where you copied from device or emulator using adb pull command earlier.
			private static String remoteTraceFile = "/storage/emulated/0/Android/data/com.example.emanickam.sampleapp/files/sampleapp.trace";
	7) 	Run the program from eclipse.  menu: Run->Run
		This application will pull the trace file from connected android emulator or phone, so make sure the device is still connected.
		After that it will run dmtracedump command to generate readable text file.
		After that it will process the generated text file.
	8)  Once the application is done with loading all the function data, then it will prompt you to enter the function name and class name that you want to see the whole hierarchy. class name is optional.
		if you enter function name as "oncreateview" and class name as "myfragment1", then it will search for "myfragment1.oncreateview" function. this is not case sensitive.
		Press enter. You will get all matches to the function you searched and it's ancestor functions up to level 0 (this level is limited/mapped to stack present in the pulled trace file)
		
		
		Sample output form this utility application run:
		------------------------------------------------		
			Starting program
			[  1%] /storage/emulated/0/Android/data/com.example.emanickam.sampleapp/files/sampleapp.trace
			[100%] /storage/emulated/0/Android/data/com.example.emanickam.sampleapp/files/sampleapp.trace
			fileName: E:\Git_Repository\AndroidMethodTraceFilter\resources\methodtrace.txt
			========
			Method Tracer
			========
			Please enter the function name: oncreateview
			Please enter the class name (Optional, press Enter to ignore): myfragment1

			android.app.BackStackRecord.run                                            level: 0, lineNo: 37454, thread: 2221
			 android.app.FragmentManagerImpl.moveToState                               level: 1, lineNo: 37797, thread: 2221
			  android.app.FragmentManagerImpl.moveToState                              level: 2, lineNo: 37802, thread: 2221
				android.app.Fragment.performCreateView                                  level: 3, lineNo: 37901, thread: 2221
				 com.example.emanickam.sampleapp.MyFragment1.onCreateView               level: 4, lineNo: 37904, thread: 2221

			com.example.emanickam.sampleapp.MainActivity.onCreate                      level: 0, lineNo: 163674, thread: 2221
			 android.app.Activity.setContentView                                       level: 1, lineNo: 164449, thread: 2221
			  com.android.internal.policy.PhoneWindow.setContentView                   level: 2, lineNo: 164452, thread: 2221
				android.view.LayoutInflater.inflate                                     level: 3, lineNo: 190041, thread: 2221
				 android.view.LayoutInflater.inflate                                    level: 4, lineNo: 190042, thread: 2221
				  android.view.LayoutInflater.inflate                                   level: 5, lineNo: 190143, thread: 2221
				   android.view.LayoutInflater.rInflateChildren                         level: 6, lineNo: 190738, thread: 2221
					android.view.LayoutInflater.rInflate                                level: 7, lineNo: 190741, thread: 2221
					 android.view.LayoutInflater.createViewFromTag                      level: 8, lineNo: 190768, thread: 2221
					  android.view.LayoutInflater.createViewFromTag                     level: 9, lineNo: 190769, thread: 2221
					   android.app.Activity.onCreateView                                level: 10, lineNo: 190806, thread: 2221
						android.app.FragmentController.onCreateView                     level: 11, lineNo: 190841, thread: 2221
						 android.app.FragmentManagerImpl.onCreateView                   level: 12, lineNo: 190842, thread: 2221
						  android.app.FragmentManagerImpl.addFragment                   level: 13, lineNo: 191297, thread: 2221
						   android.app.FragmentManagerImpl.moveToState                  level: 14, lineNo: 191386, thread: 2221
							android.app.FragmentManagerImpl.moveToState                 level: 15, lineNo: 191387, thread: 2221
							 android.app.Fragment.performCreateView                     level: 16, lineNo: 191508, thread: 2221
							  com.example.emanickam.sampleapp.MyFragment1.onCreateView  level: 17, lineNo: 191511, thread: 2221

			android.app.Activity.performCreateCommon                                   level: 0, lineNo: 205026, thread: 2221
			 android.app.FragmentController.dispatchActivityCreated                    level: 1, lineNo: 205031, thread: 2221
			  android.app.FragmentManagerImpl.dispatchActivityCreated                  level: 2, lineNo: 205032, thread: 2221
				android.app.FragmentManagerImpl.moveToState                             level: 3, lineNo: 205033, thread: 2221
				 android.app.FragmentManagerImpl.moveToState                            level: 4, lineNo: 205034, thread: 2221
				  android.app.FragmentManagerImpl.moveToState                           level: 5, lineNo: 205039, thread: 2221
				   android.app.Fragment.performCreateView                               level: 6, lineNo: 205202, thread: 2221
					com.example.emanickam.sampleapp.MyFragment1.onCreateView            level: 7, lineNo: 205205, thread: 2221

			========
			Method Tracer
			========
			Please enter the function name:


2) Traceview
   -----------
   This is different from the DDMS traceview we discussed initially though it is almost similar. This means that Android studio is pulling this feature into the IDE itself and it is still in progress.
   1) Open android studio
   2) Go to File->open and select the .trace file you pulled.
   3) Android studio will open the file in traceview.
   I find difficulties to analyse the data presented by this tool. So the TraceView tool is not my choice.


 =====================



