package elango.codings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class FilterAhaLogs {

	// private static BufferedWriter gLogWriter;
	private static Logger logger;
	private static String LOGFILENAME = "";
	public static final int FILE_SIZE = 1024 * 1024 * 10;
	public static final int NUM_OF_LOG_FILES = 20;
	private static long logLineNum = 0;

	private static boolean REMOVE_TIMESTAMP = true;

	private static String keysToFindAhaProcess[] = {
			"Initializing AhaConnect Service. Please wait...", "AhaConnector",
			"AhaSvcIpcClient", "AhaJSONUtils","NewContentPlaybackState" };
	
	private static HashMap<String, String > threadInfo = new HashMap<String, String>(){{
        put("1","Main_thread");
        put("2","Trace_thread");
        put("3","Dbus_thread");
        put("4","Signal_Handler_thread");
        put("5","File_Transfer_thread");
        put("6","Request_Handler_thread");
        put("7","AsyncReader_thread");
        put("8","HandleRecievedData_thread");
        put("9","HandleRecievedData_thread");
        put("10","HandleRecievedData_thread");
    }};

	public static void main(String[] args) {

		initializeLogger();

		String projectDir = getProjectDirectory();

		//process all the trace file names
		File folder = new File(projectDir + "\\resources\\TraceLogs_Aha");
		File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) 
	    {
			if (listOfFiles[i].isDirectory()) 
			{
			    System.out.println("Not a file. It is a directory " + listOfFiles[i].getName());
			}
			else if (listOfFiles[i].isFile()) 
			{
				System.out.println("processing file: "+listOfFiles[i].getName());
				String fileName = projectDir + "\\resources\\TraceLogs_Aha\\"+listOfFiles[i].getName();
				if (args.length > 1 && args[1].isEmpty())
					fileName = args[1];

				File f = new File(fileName);
				if (!f.exists() || f.isDirectory()) {
					log("File is not exist, Give valid file name");
					System.out.println("exiting");
					System.exit(0);
				}

				Vector<String> fileLines = new Vector<String>();
				readFileLinesIntoVector(fileName, fileLines);

				String processId = "";
				processId = getProcessId(fileLines, keysToFindAhaProcess);
				if (processId == "NONE") {
					log("No aha processId found");
					return;
				}
				
				log("Aha processId found, processId = " + processId);
				filterAhaLogs(listOfFiles[i].getName(), fileLines, processId);
			}	 
	    }
	}

	public static String getProcessId(Vector<String> fileLines,
			String[] keysToFindAhaProcess) {
		String processId = "NONE";
		try {

			String line = "";
			Vector<String> spilittedWords = new Vector<String>();
			boolean processIdFound = false;
			
			for (int x = 0; x < keysToFindAhaProcess.length; x++) {
				
				String key = keysToFindAhaProcess[x];
				log("searching for key: "+key);
				
				for (int i = 0; i < fileLines.size(); i++) {
					line = fileLines.get(i);
					// log("line "+line);
					if (line.contains(key)) {
						log("AhaConnect processID extaracting from line:"
								+ line);
						spilitWords(line, spilittedWords, true);
						processId = spilittedWords.get(1);
						processIdFound = true;
						break;
					}
				}
				if (processIdFound)
					break;
			}
			log("getProcessId is done");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return processId;
	}

	public static void filterAhaLogs(String fileName, Vector<String> fileLines, String processId) {
		try {

			String outputFileName = getProjectDirectory()
					+ "\\results\\TraceLogs_Aha\\"+fileName+ "_Filtered";
			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFileName), "UTF-8"));

			fWriter.write("ProcessId for AHA is : " + processId);
			fWriter.newLine();

			String line;
			int linecount = 0;
			int lineno = 0;
			String prevThread = "";

			Vector<String> spilittedWords = new Vector<String>();

			for (int j = 0; j < fileLines.size(); j++) {
				line = fileLines.get(j);
				lineno++;
				if (isLineEmpty(line) || !line.contains(processId)) {
					continue;
				}

				spilitWords(line, spilittedWords, true);
				
				if(!prevThread.equals(spilittedWords.get(3)))
					fWriter.newLine();
				
				String spaces = getIntents((Integer.parseInt(spilittedWords.get(3))-1)*2);

				String lineToWrite = spaces + threadInfo.get(spilittedWords.get(3))+" "+spilittedWords.get(3) + " " + spilittedWords.get(6) + " "
						+ spilittedWords.get(9);
				for (int k = 10; k < spilittedWords.size(); k++) {
					lineToWrite += " " + spilittedWords.get(k);
				}
				fWriter.write(lineToWrite);
				fWriter.newLine();
				
				prevThread = spilittedWords.get(3);
			}
			fWriter.flush();
			fWriter.close();

			System.out
					.println("Filtering process is completed. please check file: "
							+ outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean isLineEmpty(String line) {
		return (line.isEmpty() || line.trim().equals(""));
	}

	private static void spilitWords(String line, Vector<String> words,
			boolean allowDuplicate) {
		line = line.trim();
		words.clear();
		if (allowDuplicate)
			for (String word : line.split("\\s+")) {
				words.add(word);
			}
		else {
			for (String word : line.split("\\s+")) {
				if (!words.contains(word))
					words.add(word);
			}
		}
	}

	private static String getIntents(int noOfSpaces) {
		String str = "";
		for (int i = 0; i < noOfSpaces - 1; i++)
			str += " ";
		return str;
	}

	private static void readFileLinesIntoVector(String fileName,
			Vector<String> fileLines) {
		try {
			System.out.println("fileName: " + fileName);
			File file = new File(fileName);
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			String line = "";
			int doublePercentLinesCount = 0;

			while ((line = fReader.readLine()) != null) {
				fileLines.add(line);
				if (line.trim().equals("%%"))
					doublePercentLinesCount++;
			}
			fReader.close();
			int i = 0;
			for (i = 0; i < fileLines.size() && doublePercentLinesCount != 0; i++) {
				String lineText = fileLines.get(i);
				if (lineText.trim().equals("%%")) {
					doublePercentLinesCount--;
				}
			}

			if (i < fileLines.size()) {
				fileLines.subList(0, i).clear();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProjectDirectory() {
		String projectDir = "D:\\";
		try {
			URL u = FilterAhaLogs.class.getProtectionDomain().getCodeSource()
					.getLocation();
			File f = new File(u.toURI());
			projectDir = f.getParent().toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return projectDir;
	}

	public static void initializeLogger() {
		String projectDir = getProjectDirectory();
		LOGFILENAME = projectDir + "\\log\\"
				+ FilterAhaLogs.class.getSimpleName() + ".log";

		logger = Logger.getLogger("MyLog");

		try {

			// This block configure the logger with handler and formatter
			FileHandler fileHandler = new FileHandler(LOGFILENAME, FILE_SIZE,
					NUM_OF_LOG_FILES, false);
			fileHandler.setEncoding("UTF-8");
			logger.addHandler(fileHandler);
			logger.setUseParentHandlers(false);

			fileHandler.setFormatter(new Formatter() {
				@Override
				public String format(LogRecord record) {
					return record.getMessage() + "\n";
				}
			});

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void log(String msg) {
		try {
			logger.info(logLineNum++ + " || " + msg);
		} catch (Exception e) {
			System.out.println("got exception while logging");
			e.printStackTrace();
		}
	}

	private static void log(int num) {
		log(num + "");
	}

}