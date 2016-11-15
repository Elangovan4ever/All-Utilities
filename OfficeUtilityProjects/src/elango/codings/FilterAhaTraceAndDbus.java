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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

public class FilterAhaTraceAndDbus {

	// private static BufferedWriter gLogWriter;
	private static Logger logger;
	private static String LOGFILENAME = "";
	public static final int FILE_SIZE = 1024 * 1024 * 10;
	public static final int NUM_OF_LOG_FILES = 20;
	private static long logLineNum = 0;

	private static boolean REMOVE_TIMESTAMP = true;

	private static String keysToFindAhaProcess[] = {
			"Initializing AhaConnect Service. Please wait...", "AhaConnector",
			"AhaSvcIpcClient", "AhaJSONUtils", "NewContentPlaybackState","Enqueueing dbus IPC message request" };
	
	private static String keysToFindDbusMonitor[] = {"signal sender=:", "method call sender=:"};
	
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
    
    private static String serviceNamesDbusFilter[] = {"HMI","HMIGateWay","AhaConnect","ConnectivityManager","BluetoothService","AudioEntModeManager","Media"};
        
    private static Vector<String> signalNames = new Vector<String>();
	
    private static String[] ignoreSignalsArray = {""};
	private static Vector<String> ignoreSignals = new Vector<String>(Arrays.asList(ignoreSignalsArray));
	
	private static String[] ignoreSignalsFromServiceArray = {"HMIGateWay"};
	private static Vector<String> ignoreSignalsFromService = new Vector<String>(Arrays.asList(ignoreSignalsFromServiceArray));
	

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
		
				String ahaProcessId = "";
				ahaProcessId = getProcessId(fileLines, keysToFindAhaProcess);
				if (ahaProcessId.isEmpty()) {
					log("No aha processId found");
					System.out.println("No aha processId found\n");
					continue;
				}
				log("Aha processId found, ahaProcessId = " + ahaProcessId);
		
				String dbusMonitorProcessId = "";
				dbusMonitorProcessId = getDbusMonitorProcessId(fileLines, keysToFindDbusMonitor);
				if (dbusMonitorProcessId.isEmpty()) {
					log("No dbus monitor processId found");
					System.out.println("No dbus monitor processId found\n");
				}
				log("dbusMonitor processId found, dbusMonitorProcessId = " + dbusMonitorProcessId);
				
				filterAhaAndDbuMonitorLogs(listOfFiles[i].getName(), fileLines, ahaProcessId, dbusMonitorProcessId);
			}
	    }
	}

	public static String getProcessId(Vector<String> fileLines,
			String[] keysToFindAhaProcess) {
		String processId = "";
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
	
	public static String getDbusMonitorProcessId(Vector<String> fileLines,
			String[] keysToFindAhaProcess) {
		String processId = "";
		try {

			String line = "";
			Vector<String> spilittedWords = new Vector<String>();
			boolean processIdFound = false;
			
			for (int x = 0; x < keysToFindDbusMonitor.length; x++) {
				
				String key = keysToFindDbusMonitor[x];
				log("searching for key: "+key);
				
				for (int i = 0; i < fileLines.size(); i++) {
					line = fileLines.get(i);
					// log("line "+line);
					if (line.contains(key)) {
						log("DbusMonitor processID extaracting from line:"
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
			log("getDbusMonitorProcessId is done");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return processId;
	}
	
	public static void filterAhaAndDbuMonitorLogs(String fileName, Vector<String> fileLines, String processId, String dbusMonitorProcessId) {
		try {

			String outputFileName = getProjectDirectory()
					+ "\\results\\TraceLogs_Aha\\"+fileName+ "_Filtered";
			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFileName), "UTF-8"));

			fWriter.write("ProcessId for AHA is : " + processId);
			fWriter.newLine();
			
			fWriter.write("ProcessId for DBUS Monitor is : " + dbusMonitorProcessId);
			fWriter.newLine();
			
			Map<String,String> serviceMap = new HashMap<String,String>();
			if(!dbusMonitorProcessId.isEmpty())
			{
				findServiceNames(fileLines , dbusMonitorProcessId, serviceMap);
				for (Map.Entry<String, String> entry : serviceMap.entrySet()) 
				{
				    String serviceNum = entry.getKey();
				    String serviceName = entry.getValue();
				    String servicePath = serviceName.replaceAll("\\.", "/");
				    fWriter.write(serviceNum+" => "+serviceName+" "+servicePath);
					fWriter.newLine();
				}
			}
			
			String line;
			int linecount = 0;
			int lineno = 0;
			String prevThread = "";
			boolean ahaLogsWrittenPrev = true;

			Vector<String> spilittedWords = new Vector<String>();

			for (int j = 0; j < fileLines.size(); j++) {
				line = fileLines.get(j);
				lineno++;
				if ( (!line.contains(processId) && dbusMonitorProcessId.isEmpty())
						|| (!line.contains(processId) && !line.contains(dbusMonitorProcessId))) {
					continue;
				}

				spilitWords(line, spilittedWords, true);
				
				String spaces = getIntents((Integer.parseInt(spilittedWords.get(3))-1)*2);
				String lineToWrite ="";
				
				if(spilittedWords.get(1).equals(processId))
				{
					if(!prevThread.equals(spilittedWords.get(3)))
						fWriter.newLine();
					
					lineToWrite = spaces + threadInfo.get(spilittedWords.get(3))+" "+spilittedWords.get(3) + " " + spilittedWords.get(6) + " "
							+ spilittedWords.get(9);
					for (int k = 10; k < spilittedWords.size(); k++) {
						lineToWrite += " " + spilittedWords.get(k);
					}
					
					fWriter.write(lineToWrite);
					fWriter.newLine();
					
					prevThread = spilittedWords.get(3);
					ahaLogsWrittenPrev = true;
				}
				else if(spilittedWords.get(1).equals(dbusMonitorProcessId))
				{
					String dbusMonitorline = getDbusMonitorLine(spilittedWords);
					String nextLine = " ";
					if(j+1 < fileLines.size())
					{
						spilitWords(fileLines.get(j+1), spilittedWords, true);
						nextLine = getDbusMonitorLine(spilittedWords);
					}
							
					if(dbusMonitorline.startsWith("method") || dbusMonitorline.startsWith("signal"))
					{
						log(dbusMonitorline);
						log("isCoRelatedToService: "+isCoRelatedToService(dbusMonitorline, nextLine, serviceMap));
						if(isCoRelatedToService(dbusMonitorline, nextLine, serviceMap))
						{
							
							spilitWords(line, spilittedWords, true);
							lineToWrite = spaces + "DbusMonitor_Thread" +" "+spilittedWords.get(3) + " " + spilittedWords.get(6) + " "
									+ spilittedWords.get(9);
			
							lineToWrite += " "+replaceServiceNumWithNames(getDbusMonitorLine(spilittedWords),serviceMap);
							
							if(ahaLogsWrittenPrev)
								fWriter.newLine();
							
							fWriter.write(lineToWrite);
							fWriter.newLine();
							
							ahaLogsWrittenPrev = false;
							
							for(int k = j+1;  k < fileLines.size() ; k++)
							{
								
								spilitWords(fileLines.get(k), spilittedWords, true);
								if(!spilittedWords.get(1).equals(dbusMonitorProcessId))
								{
									continue;
								}
								
								String dataLine = getDbusMonitorLine(spilittedWords);
								if(dataLine.startsWith("method") || dataLine.startsWith("signal"))
									break;
								
								lineToWrite = "";
								spilitWords(fileLines.get(k), spilittedWords, true);
								lineToWrite = spaces + "DbusMonitor_Thread" +" "+spilittedWords.get(3) + " " + spilittedWords.get(6) + " "
										+ spilittedWords.get(9);
								lineToWrite += " "+replaceServiceNumWithNames(getDbusMonitorLine(spilittedWords),serviceMap);
								
								fWriter.write(lineToWrite);
								fWriter.newLine();
							}
							
							fWriter.newLine();
						}
					}
				}
				
				
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
	
	private static void findServiceNames(Vector<String> lines, String dbusMonitorProcessId, Map<String,String> serviceMap) {
		String line = "";
		
		Vector<String> spilittedWords = new Vector<String>();
		
		for (String serviceName : serviceNamesDbusFilter) {
			log("==>serviceName: "+" "+serviceName);
			
			String serviceUniqueName = "com.harman.service."+serviceName;
			boolean serviceNumFound = false; 
			for (int i = 0; i < lines.size(); i++) 
			{
				line = lines.get(i);
				spilitWords(line, spilittedWords, true);
				if(spilittedWords.size() <= 1 || !spilittedWords.get(1).equals(dbusMonitorProcessId))
				{
					continue;
				}
				
				String dbusMonitorLine = getDbusMonitorLine(spilittedWords);
				
				if(dbusMonitorLine.trim().equals("string \""+serviceUniqueName+"\""))
				{
					spilitWords(lines.get(i+1),spilittedWords, true);
					if(getDbusMonitorLine(spilittedWords).equals("string \"\""))
					{
						spilitWords(lines.get(i+2),spilittedWords, true);
						dbusMonitorLine = getDbusMonitorLine(spilittedWords);
						String serviceNumber = dbusMonitorLine.substring(dbusMonitorLine.indexOf(":")+1,dbusMonitorLine.lastIndexOf("\""));
						log(serviceUniqueName+" "+serviceNumber);
						serviceMap.put(serviceNumber, serviceUniqueName);
						serviceNumFound = true;
						break;
					}
				}
			}
			if(!serviceNumFound)
			{
				log("==>Not Found, serviceName: "+" "+serviceName+" looking for path now");
				for (int i = 0; i < lines.size(); i++) 
				{
					spilitWords(lines.get(i),spilittedWords, true);
					line = getDbusMonitorLine(spilittedWords);
					String serviceUniquePath = "/com/harman/service/"+serviceName;
					
					if(line.trim().startsWith("signal sender"))
					{
				
						String servicePath = line.substring(line.indexOf("path=")+5,line.indexOf(";"));
						log("servicePath: "+" "+servicePath);
						if(serviceUniquePath.equals(servicePath))
						{
							String serviceNumber = line.substring(line.indexOf(":")+1,line.lastIndexOf("->")-1);
							log("serviceUniquePath: "+serviceUniquePath+", serviceNumber: "+serviceNumber);
							serviceMap.put(serviceNumber, serviceUniqueName);
							serviceNumFound = true;
							break;
						}
					}
				}
			}
		}		
	}
	
	private static String getDbusMonitorLine(Vector<String> spilittedWords) {
		String line = "";
		for (int i = 10; i < spilittedWords.size(); i++) {
			line += spilittedWords.get(i) + " ";
		}
		return line;
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
			URL u = FilterAhaTraceAndDbus.class.getProtectionDomain().getCodeSource()
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
		try {
		
			FileUtils.cleanDirectory(new File(projectDir + "\\log\\"));
		
			LOGFILENAME = projectDir + "\\log\\"
					+ FilterAhaTraceAndDbus.class.getSimpleName() + ".log";
	
			logger = Logger.getLogger("MyLog");

		

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
	
		
		
	private static Boolean isCoRelatedToService(String line, String nextLine, Map<String,String> serviceMap) {
		int count = 0;
		String signalName = "";
		
		line = line.trim();
		if(line.endsWith("Emit"))
		{
			for(int i=0;i<signalNames.size();i++)
			{
				signalName =  signalNames.get(i);
				if(nextLine.trim().equals("string \""+signalName+"\""))
					return true;
			}
		}
				
		
		for (Map.Entry<String, String> entry : serviceMap.entrySet()) {
		    String serviceNum = entry.getKey();
		    String serviceName = entry.getValue();
		    String servicePath = serviceName.replaceAll("\\.", "/");
		    
		    if(!ignoreSignalsFromService.contains(serviceName) && line.startsWith("signal sender=:"+serviceNum+" "))
		    {
		    	return true;
		    }
		    		    
		    log(serviceNum+" "+serviceName+" "+servicePath);
		    if(line.contains(serviceNum) || line.contains(serviceName) || line.contains(servicePath) ||
		    	nextLine.contains(serviceNum) || nextLine.contains(serviceName) || nextLine.contains(servicePath))
		    {
		    	count++;
		    }
		}	
		
		if(count > 1 && line.endsWith("AddMatch"))
    	{
			log("signalName: "+signalName);
    		signalName = nextLine.substring(nextLine.indexOf("arg0='")+6,nextLine.length()-2);
    		if(!ignoreSignals.contains(signalName))
    		{
	    		log("signalName: "+signalName);
	    		if(!signalNames.contains(signalName))
	    			signalNames.add(signalName);
    		}
    	}
		
		if(count > 1)
			return true; 
		
		return false;
	}
		
	private static String replaceServiceNumWithNames(String line, Map<String,String> serviceMap) 
	{
		for (Map.Entry<String, String> entry : serviceMap.entrySet()) {
		    String serviceNum = entry.getKey();
		    String serviceName = entry.getValue();
		    String service = serviceName.substring(serviceName.lastIndexOf("."));
		    line = line.replaceAll(serviceNum+" ", serviceName+" ");
		}	
		return line;
	}


}