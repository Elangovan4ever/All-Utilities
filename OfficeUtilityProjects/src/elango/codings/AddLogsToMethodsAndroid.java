package elango.codings;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.sound.sampled.Line;

public class AddLogsToMethodsAndroid {
	
	private static boolean testLocalFiles = false;
	private static boolean enableLogs = false;
	
	public static int firstFuncLineNum = 0;
	
	public static final String[] PATHS_TO_ADD_LOGS = {"Z:\\workspace\\ROW_MY18\\frameworks\\base\\services\\core\\java\\com\\android\\server"};
	public static final String[] LOCAL_PATHS_TO_ADD_LOGS = {getProjectDirectory()+ "\\resources"};
	
	public static final String[] validMatcherStringsArr = {};
	public static final String[] validEndMatcherStringsArr = {") {" , "){", ")" };
	public static final String[] nonValidMatcherStringsArr = {";","=","new ","\"","+","?","@interface"};
	public static final String[] condStmtMatcherStringsArr = {"if (","if(","while (","while(","for (","for(",
			"switch (","switch(","catch","synchronized"};
	public static final String RETURN_STR="return"; 
	
	private static int openBracesCount = 0;
	private static boolean isInsideFunction = false;
	private static boolean isFunctionReturnAnything = false;
	private static boolean isInfiniteLoop = false;
	
	private static int openBracesCountForLoop = 0;
	private static int openBracketsCountForLoop = 0;
	
	private static int SPLIT_TO_SEPARATE_LINE = 1;
	private static int DONT_SPLIT_TO_SEPARATE_LINE = 2;
	
	private static int filesProcessedCount = 0;
	private static int totalFilesToProcess = 0;
	
	public static void main(String[] args) {
		
		printLogs("Started Program",true);
		
		String[] pathArrToAddLogs = PATHS_TO_ADD_LOGS;
		
		if(testLocalFiles)
			pathArrToAddLogs = LOCAL_PATHS_TO_ADD_LOGS;
		
		for (String pathStr : pathArrToAddLogs)
		{
			File path = new File(pathStr);
			countFilesInPath(path,".java");
		}
		
		printLogs("Total files count to be processed: "+totalFilesToProcess,true);
		
		for (String pathStr : pathArrToAddLogs)
		{
			File path = new File(pathStr);
			processFiles(path);
		}
		
	}
	
	public static void printLogs(String logMsg, boolean isEnabled) 
	{
		if(isEnabled)
			System.out.println(logMsg);
	}
	
	public static void printLogs(String logMsg) 
	{
		printLogs(logMsg, enableLogs);
	}
	
	public static void countFilesInPath(File path, String extension) 
	{
		String filename = path.getName();
		if(path.isDirectory())
		{
			File[] files = path.listFiles();
			for (File file : files) 
			{
				if (file.isDirectory()) 
				{
					countFilesInPath(file,extension);
				} 
				else 
				{
					filename = file.getName();
					if(filename.endsWith(extension))
					{
						totalFilesToProcess++; 
					}
				}
			}
		}
		else
		{
			if(filename.endsWith(extension))
			{
				totalFilesToProcess++; 
			}
		}
	}
	
	public static String getProjectDirectory() {
		String projectDir = "D:\\";
		try {
			URL u = AddLogsToMethodsAndroid.class.getProtectionDomain().getCodeSource()
					.getLocation();
			File f = new File(u.toURI());
			projectDir = f.getParent().toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return projectDir;
	}

	public static void processFiles(File path) {
		
		if(path.isDirectory())
		{
			File[] files = path.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					//printLogs("directory:" + file.getCanonicalPath());
					processFiles(file);
				} else {
					addLogsToFile(file);
				}
			}
		}
		else
		{
			addLogsToFile(path);
		}
	}
	
	public static void addLogsToFile(File file)
	{
		String filename = file.getName();
		if(filename.endsWith(".java"))
		{
			filesProcessedCount++;
			printLogs("Processing "+filesProcessedCount + " files of "+totalFilesToProcess+", file name: "+filename,true);
			removeComments(file);
			bringBracketAndBracesTogether(file);
			makeReturnStmtsSingleLine(file);
			makeConditionalStmtsSingleLine(file);
			makeStmtsSignleLineByKey(file,"new ");
			makeStmtsSignleLineByKey(file,"super");
			makeStmtsSignleLineByKey(file,"this");
			makeStmtsSignleLineByKey(file,"throw ");
			makeStmtsSignleLineByKey(file,"Slog.");			
			makeStmtsSignleLineByKey(file,"print");
			makeStmtsSignleLineByKey(file,"throws ",SPLIT_TO_SEPARATE_LINE);			
			updateFile(file);
			insertImportStmts(file);
			insertLogTag(file);
			printLogs("Processed "+filesProcessedCount + " files of "+totalFilesToProcess+", file name: "+filename,true);
		}					
	}
	
	public static String getEntryLogStr()
	{
		return getEntryLogStr("Slog.w");
	}
	
	public static String getEntryLogStr(String loggerType)
	{
		return loggerType +"(ENTRY_EXIT_TAG,\"entry: \" + "
				+ "Thread.currentThread().getStackTrace()[2].getMethodName()+\"() \"+"
				+ "Thread.currentThread().getStackTrace()[2].getClassName()+\":\"+"
				+ "Thread.currentThread().getStackTrace()[2].getLineNumber() );";
	}
	
	public static String getExitLogStr()
	{
		return getExitLogStr("Slog.w");
	}
	
	public static String getExitLogStr(String loggerType)
	{
		return loggerType +"(ENTRY_EXIT_TAG,\"exit: \" + "
				+ "Thread.currentThread().getStackTrace()[2].getMethodName()+\"() \"+"
				+ "Thread.currentThread().getStackTrace()[2].getClassName()+\":\"+"
				+ "Thread.currentThread().getStackTrace()[2].getLineNumber() );";
	}
	
	public static int countOccurances(String str, String keyStr)
	{ 
		return countOccurances(str, keyStr, 0);
	}
	
	public static int countOccurances(String str, String key, int fromIndex)
	{ 
		if(fromIndex >= str.length())
		{
			return 0;
		}
		
		int lastIndex = fromIndex;
		int count = 0;
		
		while (lastIndex != -1) {
			
		    lastIndex = str.indexOf(key, lastIndex);
		    
		    if (lastIndex != -1) {
		    	
		        count++;
		        lastIndex += key.length();
		    }
		    
		}
		return count;
	}
	
	public static int countOccurancesOutsideLiteral(String str, String keyStr)
	{ 
		return countOccurancesOutsideLiteral(str, keyStr, 0);
	}
	
	public static int countOccurancesOutsideLiteral(String str, String keyStr, int fromIndex)
	{ 
		int lastIndex = fromIndex;
		int count = 0;
		
		while (lastIndex != -1) {
			
			lastIndex = indexOutsideStringLiteral(str, keyStr, lastIndex);
		    
		    if (lastIndex != -1) {
		    	
		        count++;
		        lastIndex += keyStr.length();
		    }
		    
		}
		return count;
	}
	
	public static int indexOutsideStringLiteral(String line, String keyStr)
	{
		return indexOutsideStringLiteral(line, keyStr, 0);
	}
	
	public static int indexOutsideStringLiteral(String line, String keyStr, int fromIndex)
	{
		if(fromIndex >= line.length())
			return -1;
		
		int quotationCount = countOccurances(line, "\"", fromIndex);
		if(quotationCount > 0)
		{
			int keyStrOccurances = countOccurances(line, keyStr, fromIndex);
			int keyStrIndex = fromIndex;				
			int closeQuoteIndex = -1;
			
			for(int i=0; i<keyStrOccurances ; i++)
			{				
				keyStrIndex =  line.indexOf(keyStr, keyStrIndex); //each time check for next index of keyStr
				
				closeQuoteIndex = (fromIndex == 0)? -1 : fromIndex;
				
				int j = 0;
				for(; j < quotationCount/2; j++){
					
					int openQuoteIndex = line.indexOf("\"",closeQuoteIndex + 1);
					closeQuoteIndex = line.indexOf("\"",openQuoteIndex+1);
					
					if(keyStrIndex > openQuoteIndex && keyStrIndex < closeQuoteIndex)
					{
						break;
					}
				}
				
				if(j >= quotationCount/2 )
				{
					return keyStrIndex;
				}
				
				keyStrIndex += keyStr.length();
			}
			
			return -1;
		}
		else
		{
			return line.indexOf(keyStr, fromIndex);
		}
	}
	
	public static String removeSignleLineCommentsFromLine(String line)
	{
		String singleCommentStr = "//";
		if(line.trim().startsWith(singleCommentStr))
			line = "";
		else
		{
			int quotationCount = countOccurances(line,"\"");
			if(quotationCount > 0)
			{
				int commentStrCount = countOccurances(line,singleCommentStr);
				int commentStrIndex = 0;				
				int closeQuoteStrIndex = -1;
				
				for(int i=0; i<commentStrCount; i++){
					
					commentStrIndex =  line.indexOf(singleCommentStr, commentStrIndex); //each time check for next index of '//'
					
					closeQuoteStrIndex = -1;
					
					int j = 0;
					for(; j < quotationCount/2; j++){
						
						int openQuoteStrIndex = line.indexOf("\"",closeQuoteStrIndex + 1);
						closeQuoteStrIndex = line.indexOf("\"",openQuoteStrIndex+1);
						
						if(commentStrIndex > openQuoteStrIndex && commentStrIndex < closeQuoteStrIndex)
						{
							break;
						}
					}
					if(j >= quotationCount/2 )
					{
						line = line.replace( line.substring(line.indexOf(singleCommentStr, commentStrIndex) ) , "");
						break;
					}
					
					commentStrIndex += singleCommentStr.length();
				}
			}
			else
			{
				line = line.replace( line.substring(line.indexOf(singleCommentStr) ) , "");
			}
		}
		return line;
	}
	
	public static String removeClosedCommentsFromLine(String line)
	{
		int closeCommentsCount = countOccurancesOutsideLiteral(line,"*/");
		for(int i=0; i< closeCommentsCount; i++)
		{
			int openCommentIndex =  indexOutsideStringLiteral(line, "/*");
			if(openCommentIndex != -1)
			{
				int closeCommentIndex = indexOutsideStringLiteral(line, "*/", openCommentIndex);
				if(closeCommentIndex != -1)
				{
					line = line.replace(line.substring(openCommentIndex, closeCommentIndex+2),"");
				}
			}
		}
		return line;
	}
	
	public static void removeComments(File file)
	{
		try
		{
			
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			BufferedWriter fWriter = new BufferedWriter(new FileWriter(filename+"_tmp"));
			String line;
	
			while ((line = fReader.readLine()) != null) {
				
				if(line != null && line.trim().isEmpty())
				{
					continue;
				}
				
				if(line.contains("//"))
				{
					line = removeSignleLineCommentsFromLine(line); // remove single line '//' comments
				}
				
				if(line.contains("/*"))
				{
					line = removeClosedCommentsFromLine(line); // remove closed comments
					if(indexOutsideStringLiteral(line, "/*") != -1)
					{
						if(!line.startsWith("/*")) //only one /* is present, means the last part is comment only
						{
							line = line.substring(0, line.indexOf("/*")); // since only one open comment is left, we take the uncommented string and write
							if(!line.trim().isEmpty())
							{
								fWriter.write(line);
								fWriter.newLine();
							}
						}
						
						while((line!= null) && !line.contains("*/")) //skip the lines until we find */
						{
							line = fReader.readLine();
						}
						
						if(line!= null)
						{
							line = line.substring(line.indexOf("*/")+2);
							if(!line.trim().isEmpty())
							{
								line = removeClosedCommentsFromLine(line);
							}
						}
					}
				}	
				if(line != null && !line.trim().isEmpty())
				{
					fWriter.write(line);
					fWriter.newLine();
				}
			}
			
			fWriter.close();
			fReader.close();
			
			file.delete();
			File temp_file = new File(filename+"_tmp"); 
			temp_file.renameTo(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void bringBracketAndBracesTogether(File file)
	{
		try
		{
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			BufferedWriter fWriter = new BufferedWriter(new FileWriter(filename+"_tmp"));
			String line;
	
			while ((line = fReader.readLine()) != null) {
				
				if(line != null && line.trim().isEmpty())
				{
					continue;
				}
				
				if(line.trim().endsWith(")"))
				{
					String nextLine = fReader.readLine();
					while(nextLine != null && nextLine.trim().isEmpty()) //remove empty lines
					{
						nextLine = fReader.readLine();
					}
					
					if(nextLine != null && !nextLine.trim().isEmpty())
					{
						if( nextLine.trim().startsWith("{"))
						{
							fWriter.write(line.trim()+" "+nextLine.trim());
							fWriter.newLine();
						}
						else
						{
							fWriter.write(line);
							fWriter.newLine();
							fWriter.write(nextLine);
							fWriter.newLine();
						}
					}
				}
				else
				{
					fWriter.write(line);
					fWriter.newLine();
				}
			}
			
			fWriter.close();
			fReader.close();
			
			file.delete();
			File temp_file = new File(filename+"_tmp"); 
			temp_file.renameTo(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public static void makeReturnStmtsSingleLine(File file)
	{
		try
		{
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			BufferedWriter fWriter = new BufferedWriter(new FileWriter(filename+"_tmp"));
			String line = "";
			String previousLine = "";
			String singleReturnStmt = "";
	
			while ((line = fReader.readLine()) != null) {
				
				singleReturnStmt = "";
				
				if(line != null && line.trim().isEmpty())
				{
					continue;
				}
				
				if(isFoundOutsideLiteral(line, "return;") || isFoundOutsideLiteral(line, "return "))
				{
					if(isFoundOutsideLiteral(line, " new "))
					{
						singleReturnStmt = line;
					}
					else
					{
						do
						{
							singleReturnStmt += " " + line.trim();
						}while(!line.trim().endsWith(";") && ((line = fReader.readLine()) != null));
						
						if(!singleReturnStmt.trim().startsWith(RETURN_STR)){
							
							fWriter.write(createIndentFromLine(previousLine) + singleReturnStmt.substring(0, singleReturnStmt.indexOf(RETURN_STR)));
							fWriter.newLine();
							
							singleReturnStmt = singleReturnStmt.substring(singleReturnStmt.indexOf(RETURN_STR));
						}
					}
					
					fWriter.write(createIndentFromLine(previousLine) + singleReturnStmt);
					fWriter.newLine();
				}
				else
				{
					fWriter.write(line);
					fWriter.newLine();
				}
				
				previousLine = line;
			}
			
			fWriter.close();
			fReader.close();
			
			file.delete();
			File temp_file = new File(filename+"_tmp"); 
			temp_file.renameTo(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void makeConditionalStmtsSingleLine(File file)
	{
		
		if(condStmtMatcherStringsArr.length == 0)
			return;
		
		for (String condStmtMatcher : condStmtMatcherStringsArr)
		{
			makeStmtsSignleLineByKey(file, condStmtMatcher );
		}
	}
	
	public static void makeStmtsSignleLineByKey(File file, String keyStr)
	{
		makeStmtsSignleLineByKey(file, keyStr, DONT_SPLIT_TO_SEPARATE_LINE );
	}
	
	public static void makeStmtsSignleLineByKey(File file, String keyStr, int flag)
	{
		try
		{
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			BufferedWriter fWriter = new BufferedWriter(new FileWriter(filename+"_tmp"));
			String line = "";
			String singleStmt = "";
			int openBracketCount = 0;
	
			while ((line = fReader.readLine()) != null) {
				
				singleStmt = "";
				openBracketCount = 0;
				
				if(line != null && line.trim().isEmpty())
				{
					continue;
				}
				
				if(line.contains(keyStr))
				{		
					String spaces = createIndentFromLine(line);
					String fromKeyStr = line.substring(line.indexOf(keyStr));
					
					openBracketCount += countOccurancesOutsideLiteral(fromKeyStr,"(");
					openBracketCount -= countOccurancesOutsideLiteral(fromKeyStr,")");
					
					if(openBracketCount <= 0 || fromKeyStr.endsWith("{") || fromKeyStr.endsWith("}") )
					{
						singleStmt += " " +line.trim();
					}
					else
					{
						singleStmt += " " + line.trim();
						line = fReader.readLine();
						do
						{
							singleStmt += " " + line.trim();
							openBracketCount += countOccurancesOutsideLiteral(line,"(");
							openBracketCount -= countOccurancesOutsideLiteral(line,")");
							
							if(openBracketCount <= 0 || line.endsWith("{") || line.endsWith("}"))
							{
								break;
							}
							
						}while((line = fReader.readLine() ) != null);
						
					}
					
					if(flag == SPLIT_TO_SEPARATE_LINE && !singleStmt.trim().startsWith(keyStr)){
						
						fWriter.write(spaces + singleStmt.substring(0, singleStmt.indexOf(keyStr)));
						fWriter.newLine();
						
						singleStmt = singleStmt.substring(singleStmt.indexOf(keyStr));
					}
					
					line = spaces + singleStmt;
				}				
				
				fWriter.write(line);
				fWriter.newLine();
			}
			
			fWriter.close();
			fReader.close();
			
			file.delete();
			File temp_file = new File(filename+"_tmp"); 
			temp_file.renameTo(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
		
	public static String createIndentFromLine(String line)
	{
		int counter = 0;
		String indentString = "";
	    for( int i = 0; i < line.length(); i++)
	    {
	      if( line.charAt(i) == ' ')
	      {
	    	  counter++;
	    	  indentString += " ";
	      }
	      else if(counter > 0) 
	    	  break;
	    }
	    
		return indentString;
	}
	
	public static boolean shouldContainToBeFunction(String line)
	{
		if(validMatcherStringsArr.length == 0)
			return true;
		
		for (String vaildMatchers : validMatcherStringsArr)
		{
			if(line.contains(vaildMatchers))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean shouldEndWithToBeFunction(String line)
	{
		if(validEndMatcherStringsArr.length == 0)
			return true;
		
		for (String validEndMatcher : validEndMatcherStringsArr)
		{
			if(line.trim().endsWith(validEndMatcher))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean mustNotContainToBeFunction(String line)
	{
		if(nonValidMatcherStringsArr.length == 0)
			return true;
		
		for (String nonVaildMatchers : nonValidMatcherStringsArr)
		{
			if(line.contains(nonVaildMatchers))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean mustNotContainCondToBeFunction(String line)
	{
		if(condStmtMatcherStringsArr.length == 0)
			return true;
		
		for (String condStmtMatcher : condStmtMatcherStringsArr)
		{
			if(line.contains(condStmtMatcher))
			{
				return false;
			}
		}
		return true;
	}
	
	
	public static boolean canBeAFunction(String line)
	{
		/*printLogs("shouldContainToBeFunction(line): "+shouldContainToBeFunction(line));
		printLogs("shouldEndWithToBeFunction(line): "+shouldEndWithToBeFunction(line));
		printLogs("mustNotContainToBeFunction(line): "+mustNotContainToBeFunction(line));
		printLogs("mustNotContainCondToBeFunction(line): "+mustNotContainCondToBeFunction(line));*/
		return shouldContainToBeFunction(line) && shouldEndWithToBeFunction(line) && 
				mustNotContainToBeFunction(line) && mustNotContainCondToBeFunction(line);
	}
	
	public static void updateFile(File file)
	{
		try
		{
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			String filename = file.getName();
			BufferedWriter fWriter = new BufferedWriter(new FileWriter(filename+"_tmp")); 
			String line;
			int lineno=0;
			
			int openBracketCount = 0;			
			boolean isCondStmt = false;
			
			openBracesCount = 0;
			isInsideFunction = false;
			isFunctionReturnAnything = false;
			
			String previousLine = "";
			String indentationSpaces = "";

			while ((line = fReader.readLine()) != null) {
				++lineno;
				printLogs("updateFile lineno: "+lineno+", line ==> " + line);
				
				if(line != null && line.trim().isEmpty()) //to avoid empty lines
				{
					continue;
				}
				
				if(isFoundOutsideLiteral(line, "while (true)") || isFoundOutsideLiteral(line,"while (true)")) //will never come out of loop so dont add exit log
				{
					isInfiniteLoop = true;
				}
				
				//to add the exit log
				printLogs("updateFile 1calling checkAndAddExitLog when isInfiniteLoop "+isInfiniteLoop);
				boolean openBracesAdded = checkAndAddExitLog(fWriter, line, previousLine, indentationSpaces);
				
				fWriter.write(line);
				fWriter.newLine();
				
				if(openBracesAdded){
					fWriter.write(indentationSpaces+"}");
					fWriter.newLine();
				}
				
				previousLine = line;
				
				//to identify cond statement
				if(!mustNotContainCondToBeFunction(line))
				{
					isCondStmt = true;
					openBracketCount = countOccurancesOutsideLiteral(line,"(");
				}
				
				if(openBracketCount > 0)
				{
					openBracketCount -= countOccurancesOutsideLiteral(line,")");
					if(openBracketCount > 0)
					{
						continue;
					}
					else if(isCondStmt == true)
					{
						isCondStmt = false;
						continue;
					}
				}
				
				//printLogs(" canBeAFunction(line): " + canBeAFunction(line));
				if (canBeAFunction(line))
				{
					//printLogs(" shouldEndWithToBeFunction(line): " + shouldEndWithToBeFunction(line));
					if(shouldEndWithToBeFunction(line))
					{
						String nextLine = fReader.readLine(); //need to add log only after the super class method call
						lineno++;
						printLogs("updateFile lineno: "+lineno+", nextLine ==> " + nextLine);
						
						
						if(line.trim().endsWith(")"))
						{
							//if the end of the statment is ; then it cannot be a function. so write and continue to next line.
							if(nextLine.trim().contains(";") )
							{
								fWriter.write(nextLine);
								fWriter.newLine();
								continue;
							}
														
							//check for end of the function line
							if(nextLine != null )
							{
								String functionLine = line.trim();
								
								do
								{
									printLogs("updateFile lineno: "+lineno+", nextLine ==> " + nextLine);
									fWriter.write(nextLine);
									fWriter.newLine();
									functionLine += " " + nextLine.trim();
									
								}while(!nextLine.trim().endsWith("{") && !nextLine.trim().endsWith(";") && (nextLine = fReader.readLine()) != null);
								
								if(!mustNotContainToBeFunction(functionLine) || nextLine.trim().endsWith(";") || functionLine.contains("@"))
								{
									continue;
								}
								
								//post reading one line after {
								nextLine = fReader.readLine();
								printLogs("updateFile lineno: "+lineno+", nextLine ==> " + nextLine);
								
							}
						}
						
						if(nextLine != null && (nextLine.trim().startsWith("super") || nextLine.trim().startsWith("this")
								&& !nextLine.trim().startsWith("this.")))
						{
							fWriter.write(nextLine);
							fWriter.newLine();
						}						
						
						openBracesCount = 1;
						isInsideFunction = true;
						isFunctionReturnAnything = false;
						isInfiniteLoop = false;
						indentationSpaces = createIndentFromLine(line);
						String entryLogStr = indentationSpaces + "    " + getEntryLogStr();
						fWriter.write(entryLogStr);
						fWriter.newLine();
						printLogs("updateFile Entry log is added");
						
						if(isFoundOutsideLiteral(nextLine, "while (true)") || isFoundOutsideLiteral(nextLine,"while (true)")) //will never come out of loop so dont add exit log
						{
							isInfiniteLoop = true;
						}
						
						printLogs("updateFile 2calling checkAndAddExitLog when isInfiniteLoop "+isInfiniteLoop+" openBracesCount: "+openBracesCount);
						openBracesAdded = checkAndAddExitLog(fWriter, nextLine, previousLine, indentationSpaces);
						previousLine = nextLine;
						
						if(nextLine != null &&  ( (!nextLine.trim().startsWith("super") && !nextLine.trim().startsWith("this")) || nextLine.trim().startsWith("this.")))
						{
							fWriter.write(nextLine);
							fWriter.newLine();
						}
						
						if(openBracesAdded == true) // if extra braces added to cover single line if statements, we need to close it
						{
							fWriter.write(indentationSpaces+"}");
							fWriter.newLine();
						}
					}					
				}
			}
			
			fWriter.flush();
			fWriter.close();
			fReader.close();
			file.delete();
			File temp_file = new File(filename+"_tmp"); 
			temp_file.renameTo(file);			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean isFoundOutsideLiteral(String line,String keyStr)
	{
		if(line.contains(keyStr) && indexOutsideStringLiteral(line, keyStr) != -1)
			return true;
			
		return false;
	}
	
	public static boolean checkAndAddExitLog(BufferedWriter fWriter, String line, String previousLine, String indentationSpaces)
	{
		boolean openBracesAdded = false;
		try
		{
			if(isInsideFunction == true && openBracesCount > 0)
			{
				openBracesCount += countOccurancesOutsideLiteral(line,"{");
				openBracesCount -= countOccurancesOutsideLiteral(line,"}");
				if(openBracesCount > 0) //function not yet done
				{
					//this is for return in between functions, based on some conditions
					if( isFoundOutsideLiteral(line, "return;") || isFoundOutsideLiteral(line, "return ") || isFoundOutsideLiteral(line, "throw ") ) 
					{
						if(previousLine.trim().startsWith("if") && !previousLine.trim().endsWith("{")) //we need to add braces for 'if' stmts without braces
						{
							fWriter.write(indentationSpaces+"{");
							fWriter.newLine();
							openBracesAdded = true;
						}
						
						printLogs("Exit log is added, in between function");
						fWriter.write(indentationSpaces + getExitLogStr());
						fWriter.newLine();
						
						if(isFoundOutsideLiteral(line, "return "))
						{
							isFunctionReturnAnything = true;		
						}
						
					}
				}
				
				printLogs("\ncheckAndAddExitLog: line: "+line);
				printLogs("checkAndAddExitLog: previousLine: "+previousLine);
				printLogs("checkAndAddExitLog: openBracesCount: "+openBracesCount+"\n");
				if(openBracesCount == 0) //last closing brace met for the function, so function is done
				{
					isInsideFunction = false;
					
					if( !isFoundOutsideLiteral(previousLine, "return;") && !isFoundOutsideLiteral(previousLine, "return ") 
							&& !isFoundOutsideLiteral(previousLine, "throw ") && !isFunctionReturnAnything &&  !isInfiniteLoop) //dont add exit log after the return statement
					{
						printLogs("Exit log is added, at the end of the function");
						fWriter.write(indentationSpaces + getExitLogStr());
						fWriter.newLine();
					}
					
					isFunctionReturnAnything = false;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return openBracesAdded;
	}
	
	public static void insertImportStmts(File file){
		
		String importPackage = "android.util.Slog";
		
		if(!isImportedAlready(file,importPackage))	
		{
			insertLineToFile(file,2,"import "+importPackage+";");
		}
	}
	
	public static void insertLogTag(File file){
		try
		{
			String filename = file.getName();
			String className = filename.replace(".java", "");
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			String line;
			int lineNo=0;
			boolean classFound = false;

			while ((line = fReader.readLine()) != null) {
				lineNo++;
				if(line.contains("class "+className+" "))
				{
					while(line != null && !line.trim().endsWith("{"))
					{
						line = fReader.readLine();
						lineNo++;
					}
					classFound = true;
					break;
				}
			}
			
			fReader.close();
			if(classFound)
				insertLineToFile(file,lineNo + 1 ,"public static final String ENTRY_EXIT_TAG = \""+ className +"\";");
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		String importPackage = "android.util.Slog";
		
		if(!isImportedAlready(file,importPackage))	
		{
			insertLineToFile(file,2,"import "+importPackage+";");
		}
	}
		
	public static boolean isImportedAlready(File file, String importPackage){
		try
		{
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			String line;
			int lineNum = 0;
			while ((line = fReader.readLine()) != null && lineNum < 200) {
				lineNum++;
				if(line.contains(importPackage))
				{
					fReader.close();
					return true;
				}
			}
			
			fReader.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
			
	//give line no and line text to insert into the file
	public static void insertLineToFile(File file,int lineNo,String newLine)
	{ 
		try
		{
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			BufferedWriter fWriter = new BufferedWriter(new FileWriter(filename+"_tmp"));
			int lineNum = 0;
			String line;

			while ((line = fReader.readLine()) != null) {
				lineNum++;
				if(lineNo == lineNum )
				{
					fWriter.write(newLine);
					fWriter.newLine();
				}
				fWriter.write(line);
				fWriter.newLine();
			}
			
			fWriter.close();
			fReader.close();
			
			file.delete();
			File temp_file = new File(filename+"_tmp"); 
			temp_file.renameTo(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}