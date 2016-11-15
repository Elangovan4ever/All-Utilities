package elango.android.trace.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Vector;
import java.util.function.Function;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class MethodTraceFilter {

	private static String remoteTraceFile = "/storage/emulated/0/Android/data/com.example.emanickam.sampleapp/files/sampleapp.trace";

	private static Logger logger;
	private static String LOGFILENAME = "";
	private static final int FILE_SIZE = 1024 * 1024 * 10;
	private static final int NUM_OF_LOG_FILES = 20;
	
	public static void main(String args[]) {
		
		System.out.println("Starting program");
		// For logger
		String projectDir = getProjectDirectory();

		LOGFILENAME = projectDir + "\\log\\"
				+ MethodTraceFilter.class.getSimpleName() + ".log";

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
					/*
					 * SimpleDateFormat logTime = new SimpleDateFormat(
					 * "MM-dd-yyyy HH:mm:ss"); Calendar cal = new
					 * GregorianCalendar();
					 * cal.setTimeInMillis(record.getMillis()); return
					 * logTime.format(cal.getTime()) + " || "+
					 * record.getMessage() + "\n";
					 */
					return record.getMessage() + "\n";
				}
			});

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Starting the actual task
		
		String command = "adb pull " + remoteTraceFile + " " + projectDir + "\\resources\\methodtrace.trace";

		boolean isSuccess = executeCommand(command);
		
		if(!isSuccess)
		{
			System.out.print("Command excution failed for:"+command+"\nTerminating the application.");
			System.exit(0);
		}

		command = "dmtracedump -ho " + projectDir + "\\resources\\methodtrace.trace > " + projectDir + "\\resources\\methodtrace.txt";
		executeCommand(command);
		
		if(!isSuccess)
		{
			System.out.print("Command excution failed for:"+command+"\nTerminating the application.");
			System.exit(0);
		}

		String fileName = projectDir + "\\resources\\methodtrace.txt";
		File file = new File(fileName);
		if (!file.exists() || file.isDirectory()) {
			System.out.println("File is not exist, Give valid file name");
			System.exit(0);
		}

		try
		{
			Map<Integer, ThreadData> allNodesFromFile = new HashMap<Integer, ThreadData>();
			readAllNodes(fileName, allNodesFromFile);
			
			for (Map.Entry<Integer, ThreadData> entry : allNodesFromFile.entrySet()) 
			{
				Integer key = entry.getKey();
				ThreadData threadData = entry.getValue();
				Vector<FunctionData> nodesInThread = threadData.getNodesInThread();
				
				FunctionData rootNode = null;
				Stack<FunctionData> callStack = new Stack<FunctionData>();
				List<FunctionData> rootNodes = new ArrayList<FunctionData>();
							
				for(FunctionData currentNode : nodesInThread)
				{	
					int level =  currentNode.level;
					
					if(level == 0)
					{
						rootNodes.add(currentNode);
						callStack.push(currentNode);
					}
					else if(level > 0 && rootNode == null)
					{
						FunctionData previousDummyNode = null;
						for(int i=0;i<level;i++)
						{
							FunctionData dummyNode = new FunctionData("Dummy function since this level not present in the original trace", 
									"DummyClass", "DummyReturn", i, currentNode.traceLineNum, currentNode.threadId);
							
							if(i == 0)
							{
								rootNode = dummyNode;
								rootNodes.add(rootNode);
							}
							
							if(previousDummyNode != null)
							{
								dummyNode.parent = previousDummyNode;
								previousDummyNode.addChild(dummyNode);
							}
							callStack.push(dummyNode);
							previousDummyNode = dummyNode;
						}
						
						currentNode.parent = previousDummyNode;
						previousDummyNode.addChild(currentNode);
						callStack.push(currentNode);
					}
					else
					{	
						FunctionData last = (FunctionData) callStack.peek();
				
						if(last.level < currentNode.level)
						{
							currentNode.parent = last;
							
						}
						else if(last.level == currentNode.level)
						{
							callStack.pop();
							currentNode.parent = last.parent;
						}
						else if(last.level > currentNode.level)
						{
							do
							{
								last = (FunctionData) callStack.peek();
								callStack.pop();
							}while(!callStack.isEmpty() && last.level > currentNode.level);
							
							currentNode.parent = last.parent;
						}
						
						last.addChild(currentNode);
						callStack.push(currentNode);
					}
					
					logger.info("\nprocessed currentNode: "+currentNode.functionName+ ", level: "+currentNode.level+ " lineNo: "+currentNode.traceLineNum+", thread: "+currentNode.threadId);
					if(currentNode.parent != null)
						logger.info("    Parent Node : "+currentNode.parent.functionName+ ", level: "+currentNode.parent.level+ " lineNo: "+currentNode.parent.traceLineNum+", thread: "+currentNode.parent.threadId);
				}
				
				threadData.setRootNodes(rootNodes);
			}
			
			while (true) {
				
				System.out.println("\n========\nMethod Tracer\n========");
				System.out.println("Please enter the function name: ");
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				String functionName = br.readLine();
				if(functionName.isEmpty())
				{
					continue;
				}
				
				System.out.println("Please enter the class name (Optional, press Enter to ignore): ");
				String className = br.readLine();
				
				if(!className.isEmpty())
					functionName = className +"."+ functionName;
				
				if(!findFunctionInAllNodes(allNodesFromFile, functionName) == true)
				{
					System.out.println("functionName: "+functionName+ " is not found");
					//System.out.println("functionName: oncreateview is not found");
				}
				
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean findFunctionInAllNodes(Map<Integer, ThreadData> allNodesFromFile, String functionName) 
	{
		boolean functionFound = false;
		for (Map.Entry<Integer, ThreadData> entry : allNodesFromFile.entrySet())
		{
			if(findInEachRootNodes(entry.getValue().getRootNodes(), functionName) == true)
			{
				functionFound = true;
				//return true;
			}
		}
		
		return functionFound;
	}
	
	public static boolean findInEachRootNodes(List<FunctionData> rootNodes, String functionName) 
	{
		boolean functionFound = false;
		for (FunctionData currentRootNode : rootNodes)
		{
			if(findFunctionData(currentRootNode, functionName) == true)
			{
				functionFound = true;
				//return true;
			}
		}
		
		return functionFound;
	}

	public static boolean findFunctionData(FunctionData functionDataNode, String functionName) 
	{
		boolean functionFound = false;
		
		logger.info("\nSearching in currentNode: "+functionDataNode.functionName+ ", level: "+functionDataNode.level+ " lineNo: "+functionDataNode.traceLineNum+", thread: "+functionDataNode.threadId);
		if(functionDataNode.parent != null)
			logger.info("    Parent Node : "+functionDataNode.parent.functionName+ ", level: "+functionDataNode.parent.level+ " lineNo: "+functionDataNode.parent.traceLineNum+", thread: "+functionDataNode.threadId);
		
		
		if(functionDataNode.functionName.toLowerCase().endsWith(functionName.toLowerCase()))
		{
			System.out.println("\n");
			printTraceUp(functionDataNode);
			return true;
		}
		
		for (FunctionData currentNode : functionDataNode.children)
		{
	        if(findFunctionData(currentNode, functionName))
	        {
	        	functionFound = true;
	        	//return true;
	        }
		}
	
		return functionFound;
	}

	public static boolean executeCommand(String command) {
		try {
			List<String> commandList = new ArrayList<String>();
			commandList.add("cmd");
			commandList.add("/c");
			Collections.addAll(commandList, command.split("\\s+"));

			Process p = new ProcessBuilder(commandList).start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				if(line.contains("adb: error"))
					return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;			
		}
		return true;
	}

	public static String getProjectDirectory() {
		String projectDir = "D:\\";
		try {
			URL u = MethodTraceFilter.class.getProtectionDomain()
					.getCodeSource().getLocation();
			File f = new File(u.toURI());
			projectDir = f.getParent().toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return projectDir;
	}

	private static boolean isLineEmpty(String line) {
		return (line.isEmpty() || line.trim().equals(""));
	}
	
	private static void printTraceUp(FunctionData functionData)
	{
		if(functionData == null)
			return;
		
		printTraceUp(functionData.parent);
		
		String function = String.format("%"+(functionData.level+1)+"s", "") + functionData.functionName;
		String relatedInfo = " level: "+ functionData.level+ ", lineNo: "+functionData.traceLineNum+", thread: "+functionData.threadId;
		
		System.out.printf("%-100.100s %-100.100s\n", function, relatedInfo);
		
	}
	
	private static void readAllNodes(String fileName,
			Map<Integer, ThreadData> allNodesFromFile) {
		try 
		{
			System.out.println("fileName: " + fileName);
			
			File file = new File(fileName);
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			String line = "";
			
			int traceLineNum = 0;
			while ((line = fReader.readLine()).startsWith("Trace") == false)
			{
				traceLineNum++;
			}			
			traceLineNum++;
			
			while ((line = fReader.readLine()) != null) 
			{
				traceLineNum++;
				
				if (isLineEmpty(line))
					continue;
				
				boolean isLevelZero = false;
				if(Pattern.compile("[0-9]-").matcher(line).find())
				{
					isLevelZero = true;
				}
				
				line = line.replaceAll("[0-9]-", " ");
				
				String[] wordsInLine = line.split("\\s+");
				
				if(wordsInLine[1].equals("xit"))
				{
					continue;
				}
				
				int threadId = Integer.parseInt(wordsInLine[0]);
				ThreadData threadData = new ThreadData();
				
				if(!allNodesFromFile.containsKey(threadId))
				{
					allNodesFromFile.put(threadId,threadData);
				}
				else
				{
					threadData = allNodesFromFile.get(threadId);
				}
				
				Vector<FunctionData> nodesUnderThread = threadData.getNodesInThread();
				
				int level = 0;
				String functionName = wordsInLine[3];
				int functionNameLength = functionName.length();
				int dotsIndex = 0;
				for(; dotsIndex <functionNameLength && functionName.charAt(dotsIndex) == '.' ; dotsIndex++)
				{}
				
				if(!isLevelZero)
					level = dotsIndex + 1; //means num of dots plus one. because there is -1 level in the trace file.
				
				functionName = functionName.substring(dotsIndex,functionNameLength);
				
				String returnType = wordsInLine[4];
				String className = wordsInLine[5].substring(0,wordsInLine[5].length()-5);
				
				FunctionData currentNode = new FunctionData(functionName, className, returnType, level, traceLineNum, threadId);
				
				nodesUnderThread.add(currentNode);
			}
			fReader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
