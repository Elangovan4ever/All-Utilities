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

public class DbusLogAnalyser
{

	// private static BufferedWriter gLogWriter;
	private static Logger logger;
	private static String LOGFILENAME = "";
	public static final int FILE_SIZE = 1024 * 1024 * 10;
	public static final int NUM_OF_LOG_FILES = 20;
	private static long logLineNum = 0;
	
	private static Vector<String> signalNames = new Vector<String>();
	private static String[] ignoreSignalsArray = {""};
	private static Vector<String> ignoreSignals = new Vector<String>(Arrays.asList(ignoreSignalsArray));
	
	private static String serviceNames[] = {"HMI","AhaConnect","ConnectivityManager",
			"BluetoothService","AudioEntModeManager","Media"};

	public static void main(String[] args) {
		
		String projectDir = getProjectDirectory();

		LOGFILENAME = projectDir + "\\log\\"
				+ DbusLogAnalyser.class.getSimpleName() + ".log";

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

		// actual processing
		String fileName = projectDir + "\\resources\\DbusLogs\\dbuslog1.txt";
		if (args.length > 1 && args[1].isEmpty())
			fileName = args[1];

		File f = new File(fileName);
		if (!f.exists() || f.isDirectory()) {
			log("File is not exist, Give valid file name");
			System.exit(0);
		}

		extractRelatedLogs(fileName);

	}

	private static void log(String msg) {
		try {
			logger.info(logLineNum++ + " || " + msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void log(int num) {
		log(num + "");
	}

	private static boolean isLineEmpty(String line) {
		return (line.isEmpty() || line.trim().equals(""));
	}

	private static String readString() {
		String key = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			key = br.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return key;
	}

	private static int readNumber() {
		int number = 0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			number = Integer.parseInt(br.readLine());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return number;
	}
	
	private static class LineDetails {
		int lineNo = 0;
		String line = " ";
	}

	public static void extractRelatedLogs(String fileName) {
		try {
			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			System.out.println("filename: "+ getProjectDirectory() + "\\results\\DbusLogs\\dbuslog1.txt");
			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(getProjectDirectory() + "\\results\\DbusLogs\\dbuslog1.txt"), "UTF-8"));
			
			int linecount = 0;
			int lineno = 0;
			System.out.println("extract related logs : start");
			String line;				
			Vector<LineDetails> lines = new Vector<LineDetails>();

			while ((line = fReader.readLine()) != null) {
				if (isLineEmpty(line)) {
					continue;
				}
				LineDetails lineDetails = new DbusLogAnalyser.LineDetails();
				lineDetails.lineNo = lineno;
				lineDetails.line = line.trim();
				lines.add(lineDetails);
				lineno++;
			}

			//String serviceNames[] = {"HMI","AhaConnect"};
					
					
			Map<String,String> serviceMap = new HashMap<String,String>();
			
			
			for (String serviceName : serviceNames) {
				log("==>serviceName: "+" "+serviceName);
				
				String serviceUniqueName = "com.harman.service."+serviceName;
				boolean serviceNumFound = false; 
				for (int i = 0; i < lines.size(); i++) 
				{
					line = lines.get(i).line;
					
					if(line.trim().equals("string \""+serviceUniqueName+"\"") && lines.get(i+1).line.trim().equals("string \"\""))
					{
						line = lines.get(i+2).line;
						String serviceNumber = line.substring(line.indexOf(":")+1,line.lastIndexOf("\""));
						log(serviceUniqueName+" "+serviceNumber);
						serviceMap.put(serviceNumber, serviceUniqueName);
						serviceNumFound = true;
						break;
					}
				}
				if(!serviceNumFound)
				{
					log("==>Not Found, serviceName: "+" "+serviceName+" looking for path now");
					for (int i = 0; i < lines.size(); i++) 
					{
						line = lines.get(i).line;
						String serviceUniquePath = "/com/harman/service/"+serviceName;
						
						if(line.trim().startsWith("signal"))
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
			
			for (int i = 0; i < lines.size(); i++) 
			{
				line = lines.get(i).line;
				String nextLine = " ";
				if(i+1 < lines.size())
				{
					nextLine = lines.get(i+1).line;
				}
						
				if(line.startsWith("method") || line.startsWith("signal"))
				{
					log(line);
					log("isCoRelatedToService: "+isCoRelatedToService(line, nextLine, serviceMap));
					if(isCoRelatedToService(line, nextLine, serviceMap))
					{
						fWriter.newLine();
						line = replaceServiceNumWithNames(line,serviceMap);
						fWriter.write(line.trim());
						fWriter.newLine();
						
						for(int j = i+1;  j < lines.size() ; j++)
						{
							String dataLine = lines.get(j).line;
							
							if(dataLine.startsWith("method") || dataLine.startsWith("signal"))
								break;
							
							fWriter.write("  "+dataLine.trim());
							fWriter.newLine();
						}
					}
				}
			}
			
			System.out.println("extract related logs : end");
			fWriter.flush();
			fWriter.close();
			fReader.close();
			//file.delete();
			//File temp_file = new File(filename + "_tmp");
			//temp_file.renameTo(file);

			System.out.println("Please refer log file " + LOGFILENAME
					+ " for more details (refresh it).");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		    line = line.replaceAll(serviceNum, serviceName);
		}	
		return line;
	}

	public static String getProjectDirectory() {
		String projectDir = "D:\\";
		try {
			URL u = DbusLogAnalyser.class.getProtectionDomain().getCodeSource()
					.getLocation();
			File f = new File(u.toURI());
			projectDir = f.getParent().toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return projectDir;
	}
}
