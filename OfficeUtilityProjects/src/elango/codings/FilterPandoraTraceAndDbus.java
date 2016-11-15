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

public class FilterPandoraTraceAndDbus {

	// private static BufferedWriter gLogWriter;
	private static Logger logger;
	private static String LOGFILENAME = "";
	public static final int FILE_SIZE = 1024 * 1024 * 10;
	public static final int NUM_OF_LOG_FILES = 20;
	private static long logLineNum = 0;

	private static boolean REMOVE_TIMESTAMP = true;

	private static String keysToFindPandoraProcess[] = {
			"Initializing PandoraLink Service", "PndrUpdateTrackElapsed",
			"PndrUpdateStationActive", "pndrEventTrackPlay", "PndrUpdateTrackElapsed","Pandora received new request","svcipc_PandoraSvcIpcWrapper_readMsgQueue" };
	
	private static String keysToFindDbusMonitor[] = {"signal sender=:", "method call sender=:"};
	
	private static HashMap<String, String > threadInfo = new HashMap<String, String>(){{
        put("1","Main_thread");
        put("2","Trace_thread");
        put("3","Dbus_thread");
        put("4","Signal_Handler_thread");
        put("5","Request_Handler_thread");
        put("6","AsyncReader_thread");
        put("7","HandleRecievedData_thread");
        put("8","HandleRecievedData_thread");
        put("9","HandleRecievedData_thread");
        put("10","HandleRecievedData_thread");
    }};
    
    
    private static HashMap<String, String > keysToFindDbusProcessNumbers = new HashMap<String, String>(){{
        put("HMI","string \"setPlaybackState\"$$another_string");
        put("PandoraLink","sample_string");
        put("ConnectivityManager","sample_string");
        put("BluetoothService","sample_string");
        put("AudioEntModeManager","sample_string");
        put("Media","sample_string");
        put("MFDService","sample_string");
    }};
    
    private static String serviceNamesDbusFilter[] = {"HMI","PandoraLink","ConnectivityManager","BluetoothService","AudioEntModeManager","Media","MFDService"};
        
    private static Vector<String> signalNames = new Vector<String>();
	private static String[] ignoreSignalsArray = {""};
	private static Vector<String> ignoreSignals = new Vector<String>(Arrays.asList(ignoreSignalsArray));
	
	private static String[] ignoreSignalsFromServiceArray = {""};
	private static Vector<String> ignoreSignalsFromService = new Vector<String>(Arrays.asList(ignoreSignalsFromServiceArray));	
	
    private static HashMap<String, String > opcodeInfo = new HashMap<String, String>(){{
	    put("0X0","PNDR_SESSION_START : HU --> PHONE");
	    put("0X1","PNDR_UPDATE_BRANDING_IMAGE : HU --> PHONE");
	    put("0X2","PNDR_RETURN_BRANDING_IMAGE_SEGMENT : HU --> PHONE");
	    put("0X3","PNDR_GET_STATUS : HU --> PHONE");
	    put("0X5","PNDR_SESSION_TERMINATE : HU --> PHONE");
	    put("0X6","PNDR_GET_LISTENER : HU --> PHONE");
	    put("0X7","PNDR_GET_AUDIO_QUALITY : HU --> PHONE");
	    put("0X8","PNDR_SET_AUDIO_QUALITY : HU --> PHONE");
	    put("0X9","PNDR_GET_EXPLICIT_FILTER : HU --> PHONE");
	    put("0Xa","PNDR_SET_EXPLICIT_FILTER : HU --> PHONE");
	    put("0X10","PNDR_GET_TRACK_INFO : HU --> PHONE");
	    put("0X11","PNDR_GET_TRACK_TITLE : HU --> PHONE");
	    put("0X12","PNDR_GET_TRACK_ARTIST : HU --> PHONE");
	    put("0X13","PNDR_GET_TRACK_ALBUM : HU --> PHONE");
	    put("0X14","PNDR_GET_TRACK_ALBUM_ART : HU --> PHONE");
	    put("0X15","PNDR_SET_TRACK_ELAPSED_POLLING : HU --> PHONE");
	    put("0X16","PNDR_GET_TRACK_INFO_EXTENDED : HU --> PHONE");
	    put("0X30","PNDR_EVENT_TRACK_PLAY : HU --> PHONE");
	    put("0X31","PNDR_EVENT_TRACK_PAUSE : HU --> PHONE");
	    put("0X32","PNDR_EVENT_TRACK_SKIP : HU --> PHONE");
	    put("0X33","PNDR_EVENT_TRACK_RATE_POSITIVE : HU --> PHONE");
	    put("0X34","PNDR_EVENT_TRACK_RATE_NEGATIVE : HU --> PHONE");
	    put("0X35","PNDR_EVENT_TRACK_EXPLAIN : HU --> PHONE");
	    put("0X36","PNDR_GET_TRACK_EXPLAIN : HU --> PHONE");
	    put("0X37","PNDR_EVENT_TRACK_BOOKMARK_TRACK : HU --> PHONE");
	    put("0X38","PNDR_EVENT_TRACK_BOOKMARK_ARTIST : HU --> PHONE");
	    put("0X39","PNDR_EVENT_TRACK_PREVIOUS : HU --> PHONE");
	    put("0X40","PNDR_GET_STATION_ACTIVE : HU --> PHONE");
	    put("0X41","PNDR_GET_STATION_COUNT : HU --> PHONE");
	    put("0X42","PNDR_GET_STATION_TOKENS : HU --> PHONE");
	    put("0X43","PNDR_GET_ALL_STATION_TOKENS : HU --> PHONE");
	    put("0X44","PNDR_GET_STATION_INFO : HU --> PHONE");
	    put("0X45","PNDR_GET_STATIONS_ORDER : HU --> PHONE");
	    put("0X46","PNDR_EVENT_STATIONS_SORT : HU --> PHONE");
	    put("0X47","PNDR_EVENT_STATION_SELECT : HU --> PHONE");
	    put("0X48","PNDR_EVENT_STATION_DELETE : HU --> PHONE");
	    put("0X49","PNDR_EVENT_STATION_CREATE_FROM_CURRENT_ARTIST : HU --> PHONE");
	    put("0X4A","PNDR_EVENT_STATION_CREATE_FROM_CURRENT_TRACK : HU --> PHONE");
	    put("0X4B","PNDR_GET_STATION_ART : HU --> PHONE");
	    put("0X4C","PNDR_EVENT_CANCEL_STATION_ART : HU --> PHONE");
	    put("0X4D","PNDR_GET_GENRE_CATEGORY_COUNT : HU --> PHONE");
	    put("0X4E","PNDR_GET_GENRE_CATEGORY_NAMES : HU --> PHONE");
	    put("0X4F","PNDR_GET_ALL_GENRE_CATEGORY_NAMES : HU --> PHONE");
	    put("0X50","PNDR_GET_GENRE_CATEGORY_STATION_COUNT : HU --> PHONE");
	    put("0X51","PNDR_GET_GENRE_STATION_NAMES : HU --> PHONE");
	    put("0X52","PNDR_EVENT_SELECT_GENRE_STATION : HU --> PHONE");
	    put("0X53","PNDR_GET_GENRE_STATION_ART : HU --> PHONE");
	    put("0X54","PNDR_EVENT_CANCEL_GENRE_STATION_ART : HU --> PHONE");
	    put("0X58","PNDR_GET_RECOMMENDATIONS_COUNT : HU --> PHONE");
	    put("0X59","PNDR_GET_RECOMMENDATIONS_INFO : HU --> PHONE");
	    put("0X5a","PNDR_GET_ALL_RECOMMENDATIONS_INFO : HU --> PHONE");
	    put("0X5b","PNDR_EVENT_RECOMMENDATION_SELECT : HU --> PHONE");
	    put("0X5c","PNDR_GET_RECOMMENDATION_ART : HU --> PHONE");
	    put("0X5d","PNDR_CANCEL_RECOMMENDATION_ART : HU --> PHONE");
	    put("0X60","PNDR_EVENT_SEARCH_AUTO_COMPLETE : HU --> PHONE");
	    put("0X61","PNDR_EVENT_SEARCH_EXTENDED : HU --> PHONE");
	    put("0X62","PNDR_GET_SEARCH_RESULT_INFO : HU --> PHONE");
	    put("0X63","PNDR_EVENT_SEARCH_SELECT : HU --> PHONE");
	    put("0X64","PNDR_EVENT_SEARCH_DISCARD : HU --> PHONE");
	    put("0X70","PNDR_EVENT_OPEN_APP : HU --> PHONE");
	    put("0X80","PNDR_GET_BRANDING_IMAGE : PHONE --> HU");
	    put("0X81","PNDR_UPDATE_STATUS : PHONE --> HU");
	    put("0X82","PNDR_RETURN_STATUS : PHONE --> HU");
	    put("0X83","PNDR_UPDATE_NOTICE : PHONE --> HU");
	    put("0X85","PNDR_RETURN_LISTENER : PHONE --> HU");
	    put("0X86","PNDR_RETURN_AUDIO_QUALITY : PHONE --> HU");
	    put("0X87","PNDR_RETURN_EXPLICIT_FILTER : PHONE --> HU");
	    put("0X90","PNDR_UPDATE_TRACK : PHONE --> HU");
	    put("0X91","PNDR_RETURN_TRACK_INFO : PHONE --> HU");
	    put("0X92","PNDR_RETURN_TRACK_TITLE : PHONE --> HU");
	    put("0X93","PNDR_RETURN_TRACK_ARTIST : PHONE --> HU");
	    put("0X94","PNDR_RETURN_TRACK_ALBUM : PHONE --> HU");
	    put("0X95","PNDR_RETURN_TRACK_ALBUM_ART_SEGMENT : PHONE --> HU");
	    put("0X96","PNDR_UPDATE_TRACK_ALBUM_ART : PHONE --> HU");
	    put("0X97","PNDR_UPDATE_TRACK_ELAPSED : PHONE --> HU");
	    put("0X98","PNDR_UPDATE_TRACK_RATING : PHONE --> HU");
	    put("0X99","PNDR_UPDATE_TRACK_EXPLAIN : PHONE --> HU");
	    put("0X9A","PNDR_RETURN_TRACK_EXPLAIN_SEGMENT : PHONE --> HU");
	    put("0X9B","PNDR_UPDATE_TRACK_BOOKMARK_TRACK : PHONE --> HU");
	    put("0X9C","PNDR_UPDATE_TRACK_BOOKMARK_ARTIST : PHONE --> HU");
	    put("0X9D","PNDR_RETURN_TRACK_INFO_EXTENDED : PHONE --> HU");
	    put("0X9E","PNDR_UPDATE_TRACK_COMPLETED : PHONE --> HU");
	    put("0XB1","PNDR_RETURN_STATION_ACTIVE : PHONE --> HU");
	    put("0XB2","PNDR_RETURN_STATION_COUNT : PHONE --> HU");
	    put("0XB3","PNDR_RETURN_STATION_TOKENS : PHONE --> HU");
	    put("0XB4","PNDR_RETURN_STATION_INFO : PHONE --> HU");
	    put("0XB5","PNDR_RETURN_STATIONS_ORDER : PHONE --> HU");
	    put("0XB6","PNDR_UPDATE_STATIONS_ORDER : PHONE --> HU");
	    put("0XB7","PNDR_UPDATE_STATION_DELETED : PHONE --> HU");
	    put("0XB8","PNDR_RETURN_STATION_ART_SEGMENT : PHONE --> HU");
	    put("0XB9","PNDR_RETURN_GENRE_CATEGORY_COUNT : PHONE --> HU");
	    put("0XBA","PNDR_UPDATE_STATION_ACTIVE : PHONE --> HU");
	    put("0XBB","PNDR_RETURN_GENRE_CATEGORY_NAMES : PHONE --> HU");
	    put("0XBC","PNDR_RETURN_GENRE_CATEGORY_STATION_COUNT : PHONE --> HU");
	    put("0XBD","PNDR_RETURN_GENRE_STATION_NAMES : PHONE --> HU");
	    put("0XC0","PNDR_RETURN_RECOMMENDATION_COUNT : PHONE --> HU");
	    put("0XC1","PNDR_RETURN_RECOMMENDATIONS_INFO : PHONE --> HU");
	    put("0XC2","PNDR_RETURN_RECOMMENDATION_ART_SEGMENT : PHONE --> HU");
	    put("0XD0","PNDR_UPDATE_SEARCH : PHONE --> HU");
	    put("0XD1","PNDR_RETURN_SEARCH_RESULT_INFO : PHONE --> HU");
	    put("0XD2","PNDR_UPDATE_STATION_ADDED : PHONE --> HU");
	    put("0X7F","PNDR_ECHO_REQUEST : PHONE --> HU");
	    put("0XFF","PNDR_ECHO_RESPONSE : PHONE --> HU");
    }};

	public static void main(String[] args) {

		initializeLogger();

		String projectDir = getProjectDirectory();

		//process all the trace file names
		File folder = new File(projectDir + "\\resources\\TraceLogs_Pandora");
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
				String fileName = projectDir + "\\resources\\TraceLogs_Pandora\\"+listOfFiles[i].getName();
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
				
				
				String pandoraProcessId = "";
				pandoraProcessId = getProcessId(fileLines, keysToFindPandoraProcess);
				if (pandoraProcessId.isEmpty()) {
					log("No pandora processId found");
					System.out.println("No pandora processId found\n");
					continue;
				}
				log("Pandora processId found, pandoraProcessId = " + pandoraProcessId);
		
				String dbusMonitorProcessId = "";
				dbusMonitorProcessId = getDbusMonitorProcessId(fileLines, keysToFindDbusMonitor);
				if (dbusMonitorProcessId.isEmpty()) {
					log("No dbus monitor processId found");
					System.out.println("No dbus monitor processId found");
				}
				log("dbusMonitor processId found, dbusMonitorProcessId = " + dbusMonitorProcessId);
				
				filterPandoraAndDbuMonitorLogs(listOfFiles[i].getName(), fileLines, pandoraProcessId, dbusMonitorProcessId);
				System.out.println();
			}
	    }
	}

	public static String getProcessId(Vector<String> fileLines,
			String[] keysToFindPandoraProcess) {
		String processId = "";
		try {

			String line = "";
			Vector<String> spilittedWords = new Vector<String>();
			boolean processIdFound = false;
			
			for (int x = 0; x < keysToFindPandoraProcess.length; x++) {
				
				String key = keysToFindPandoraProcess[x];
				log("searching for key: "+key);
				
				for (int i = 0; i < fileLines.size(); i++) {
					line = fileLines.get(i);
					//log("line "+line);
					if (line.contains(key)) {
						log("PandoraConnect processID extaracting from line:"
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
			String[] keysToFindPandoraProcess) {
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
						//log("DbusMonitor processID extaracting from line:"
						//		+ line);
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
	
	public static void filterPandoraAndDbuMonitorLogs(String fileName, Vector<String> fileLines, String processId, String dbusMonitorProcessId) {
		try {

			String outputFileName = getProjectDirectory()
					+ "\\results\\TraceLogs_Pandora\\"+fileName+ "_Filtered";
			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFileName), "UTF-8"));

			fWriter.write("ProcessId for PANDORA is : " + processId);
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
			boolean pandoraLogsWrittenPrev = true;

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
					
					if(lineToWrite.contains("opCode : 0x"))
					{
						lineToWrite += " ==> " +opcodeInfo.get(spilittedWords.get(12).toUpperCase());
					}
					
					fWriter.write(lineToWrite);
					fWriter.newLine();
					
					prevThread = spilittedWords.get(3);
					pandoraLogsWrittenPrev = true;
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
							
							if(pandoraLogsWrittenPrev)
								fWriter.newLine();
							
							fWriter.write(lineToWrite);
							fWriter.newLine();
							
							pandoraLogsWrittenPrev = false;
							
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
		
		for (Map.Entry<String, String> entry : keysToFindDbusProcessNumbers.entrySet())
		{
			String serviceName = entry.getKey();
			String serviceFinderValue = entry.getValue();

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
				log(serviceName+"==> Not Found by registration, continue searching via path");
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
			if(!serviceNumFound)
			{
				log(serviceName+"==> Not Found by path also, continue searching via dbus method names");
				Vector<String> serviceFinderValues = new Vector<>(); 
				for (String word : serviceFinderValue.split("\\$\\$"))
				{
					serviceFinderValues.add(word);
				}
				
				for (int i = 0; i < lines.size(); i++) 
				{
					spilitWords(lines.get(i),spilittedWords, true);
					line = getDbusMonitorLine(spilittedWords);
					
					if(line.trim().startsWith("method call sender"))
					{
						String nextLine = " ";
						for (int j = i+1; j < lines.size(); j++)
						{
							spilitWords(lines.get(j), spilittedWords, true);
							nextLine = getDbusMonitorLine(spilittedWords);
							if(nextLine.startsWith("string \"") )
							{
								for (int k = 0; k < serviceFinderValues.size(); k++) 
								{
									if(nextLine.contains(serviceFinderValues.get(k)))
									{
										String serviceNumber = line.substring(line.indexOf(":")+1,line.lastIndexOf("->")-1);
										log("Service Name: "+serviceName+", serviceNumber: "+serviceNumber);
										serviceMap.put(serviceNumber, serviceUniqueName);
										serviceNumFound = true;
										break;
									}
								}
								if(serviceNumFound)
									break;
								
							}
							if(nextLine.trim().startsWith("signal sender") || nextLine.trim().startsWith("method call sender"))
								break;
						}
					}
				}
			}
			if(!serviceNumFound)
			{
				log("$$$$$$$$$$$$$$$ Service Name: "+serviceName+" Not found in the DBUS LOGS $$$$$$$$$$$$$$$$$$$$$$");
			}
			else
			{
				log("$$$$$$$$$$$$$$$ Service Name: "+serviceName+" succesfully found in the DBUS LOGS $$$$$$$$$$$$$$$$$$$$$$");
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
			URL u = FilterPandoraTraceAndDbus.class.getProtectionDomain().getCodeSource()
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
				+ FilterPandoraTraceAndDbus.class.getSimpleName() + ".log";

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
		    log(serviceNum+" "+serviceName+" "+servicePath);
		    
		    if(!ignoreSignalsFromService.contains(serviceName) && line.startsWith("signal sender=:"+serviceNum+" "))
		    {
		    	return true;
		    }
		    
		    if(line.contains(serviceNum) || line.contains(serviceName) || line.contains(servicePath) ||
		    	nextLine.contains(serviceNum) || nextLine.contains(serviceName) || nextLine.contains(servicePath))
		    {
		    	count++;
		    }
		}	
		
		if(count > 1 && line.endsWith("AddMatch"))
    	{
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