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

public class BulkRenameFiles {

	private static String containerFolders[] = {"D:\\References\\Video Tutorials - from linked in","D:\\References\\Android"};

	private static Logger logger;
	private static String LOGFILENAME = "";
	private static final int FILE_SIZE = 1024 * 1024 * 10;
	private static final int NUM_OF_LOG_FILES = 20;

	public static void main(String args[]) {

		System.out.println("Starting program");
		// For logger
		String projectDir = getProjectDirectory();

		LOGFILENAME = projectDir + "\\log\\"
				+ BulkRenameFiles.class.getSimpleName() + ".log";

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

		// process all the trace file names
		
		for(int i=0;i<containerFolders.length;i++)
		{
			processDirectory(containerFolders[i]);
		}

	}

	public static void processDirectory(String directory) {
		String command = "dir \"" + directory + "\" /od";

		try {
			
			Vector<String> commandOutput = new Vector<>();
			boolean isSuccess = executeCommand(command, commandOutput);
			
			if(!isSuccess)
			{
				System.out.print("Command excution failed for:"+command);
				return;
			}
			
			String line;
			int fileNo = 1;
			for(int i=0; i<commandOutput.size(); i++)
			{
				line = commandOutput.get(i);
				if (line.endsWith("mp4")) {
					String[] wordsInLine = line.split("\\s+");
					String fileName = "";
					for (int j = 5; j < wordsInLine.length; j++) {
						fileName += wordsInLine[j];
						if (j != wordsInLine.length - 1)
							fileName += " ";
					}
					String renameCommand = "ren \"" + directory + "\\" + fileName + "\"  \"" + fileNo + "_" + fileName + "\"";
					Vector<String> renameCommandOutput = new Vector<>();
					isSuccess = executeCommand(renameCommand, renameCommandOutput);
					if(!isSuccess)
					{
						System.out.print("Command excution failed for:"+command);
						continue;
					}
					fileNo++;
				} else {
					if(line.contains("<DIR>"))
					{
						String[] wordsInLine = line.split("\\s+");
						if(!(wordsInLine[5].equals(".") || wordsInLine[5].equals("..")))
						{
							String folderName = "";
							for (int j = 5; j < wordsInLine.length; j++) {
								folderName += wordsInLine[j];
								if (j != wordsInLine.length - 1)
									folderName += " ";
							}
							processDirectory(directory +"\\"+ folderName);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean executeCommand(String command, Vector<String> commandOutput) {
		try {
			
			System.out.println("executing command: " + command);
			
			List<String> commandList = new ArrayList<String>();
			commandList.add("cmd");
			commandList.add("/c");
			Collections.addAll(commandList, command.split("\\s+"));

			Process p = new ProcessBuilder(commandList).start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				commandOutput.add(line);
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
			URL u = BulkRenameFiles.class.getProtectionDomain().getCodeSource()
					.getLocation();
			File f = new File(u.toURI());
			projectDir = f.getParent().toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return projectDir;
	}

}
