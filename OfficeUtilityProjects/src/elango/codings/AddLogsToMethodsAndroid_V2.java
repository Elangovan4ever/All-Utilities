package elango.codings;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

public class AddLogsToMethodsAndroid_V2 {
	
	private static int testLocalFiles = 0;
	private static int enableLogs = 0;
	
	public static String LOGGER_TYPE_STR = "Log.w";
	//public static String LOGGER_TYPE_STR = "Slog.w";
	
	public static String IMPORT_FOR_LOGGER = "android.util.Log";
	//public static String IMPORT_FOR_LOGGER = "android.util.Slog";
	
	//====== From here combine for fresh setup =============
	
	/*public static final String[] PATHS_TO_ADD_LOGS = {"/data/work/emanickam/workspace/ROW_MY18/frameworks/base/services",
			"/data/work/emanickam/workspace/ROW_MY18/frameworks/base/core/java/android/os/SystemService.java",
			"/data/work/emanickam/workspace/ROW_MY18/frameworks/base/core/java/com/android/internal/inputmethod/"};*/
	//public static final String[] PATHS_TO_ADD_LOGS = {"/data/work/emanickam/workspace/ROW_MY18/frameworks/base/core/java/android/view/inputmethod/"};
	//public static final String[] PATHS_TO_ADD_LOGS = {"/data/work/emanickam/workspace/ROW_MY18/frameworks/base/core/java/com/android/internal/inputmethod/InputMethodSubtypeSwitchingController.java"};
	public static final String[] PATHS_TO_ADD_LOGS = {"/home/emanickam/workspace/ROW_MY18/packages/inputmethods/LatinIME/java"};

	public static final String[] LOCAL_PATHS_TO_ADD_LOGS = {getProjectDirectory()+ "\\resources"};
	
	public static final String[] VALID_FILE_SHOULD_CONTAINS = {"input","systemserver","*"};
	public static final String[] VALID_FILE_SHOULD_NOT_CONTAINS = {"test"};
	public static final String[] VALID_FILE_SHOULD_END_WITH = {".java"};
	
	public static final String[] validMatcherStringsArr = {};
	public static final String[] validEndMatcherStringsArr = {") {" , "){", ")" };
	public static final String[] nonValidMatcherStringsArr = {";","=","new ","\"","+","?","@",":","."};
	public static final String[] nonValidEndMatcherStringsArr = {"{}","{ }"};
	public static final String[] condStmtMatcherStringsArr = {"if (","if(","while (","while(","for (","for(",
			"switch (","switch(","catch","synchronized"};
	
	//since java don't have pair option, our custom one
	public class Pair<First,Second> {
	    private First first;
	    private Second second;
	    public Pair(){ }
	    public Pair(First first, Second second){
	        this.first = first;
	        this.second = second;
	    }
	    public First getFirst(){ return first; }
	    public Second getSecond(){ return second; }
	    public void setFirst(First first){ this.first = first; }
	    public void setSecond(Second second){ this.second = second; }
	    
	    public String toString()
	    {
	    	return first + " " + second;
	    }
	}
	
	private class LineDetails
	{
		public String className = "";
		
		public String line = "";
		public int lineNum = 0;
		
		public boolean onlyCommentLine = false;
		public boolean multiLineCommentOpen = false;
		
		public boolean isConditionStmt = false;
		
		public int singleLineCommentIndex = -1;
		public ArrayList<Pair<Integer,Integer>> multiLineCommentsIndexes = new ArrayList<Pair<Integer,Integer>>();
		
		public ArrayList<Pair<Integer,Integer>> quotationIndexes = new ArrayList<Pair<Integer,Integer>>();
		
		public ArrayList<String> lineToInsertBefore = new ArrayList<String>();
		public ArrayList<String> lineToInsertAfter = new ArrayList<String>();
		public FunctionData functionData = null;
		
		public boolean isEntryLogAdded = false;
		public boolean isExitLogAdded = false;
		
		public void print()
		{
			printLogs("\nLineDetails.line: "+lineNum+" "+line);
			printLogs("LineDetails.onlyCommentLine: "+onlyCommentLine);
			printLogs("LineDetails.multiLineCommentOpen: "+multiLineCommentOpen);
			printLogs("LineDetails.isConditionStmt: "+isConditionStmt);
			printLogs("LineDetails.singleLineCommentIndex: "+singleLineCommentIndex);
			printLogs("LineDetails.multiLineCommentsIndexes: "+multiLineCommentsIndexes);
			printLogs("LineDetails.quotationIndexes: "+quotationIndexes);
			printLogs("LineDetails.lineToInsertBefore: "+lineToInsertBefore);
			printLogs("LineDetails.lineToInsertAfter: "+lineToInsertAfter);
			printLogs("LineDetails.className: "+className);
			printLogs("LineDetails.isEntryLogAdded: "+isEntryLogAdded);
			printLogs("LineDetails.isExitLogAdded: "+isExitLogAdded);
			if(functionData != null)
				functionData.print();
		}
	}
	
	private class FunctionData
	{
		public String functionName = "";
		public int openBracesCount = 0;
		public boolean isFunctionReturnAnything = false;
		public boolean isInfiniteLoop = false;
		public boolean isAlwaysExpThrownOut = false;
		public boolean isInsideSwitch = false;	
		public int switchStmtOpenBracesCount = 0;
		public boolean wasLastStmtSwitch = false;
		
		public void print()
		{
			printLogs("FunctionData.functionName: "+functionName);
			printLogs("FunctionData.openBracesCount: "+openBracesCount);
			printLogs("FunctionData.isFunctionReturnAnything: "+isFunctionReturnAnything);
			printLogs("FunctionData.isInfiniteLoop: "+isInfiniteLoop);
			printLogs("FunctionData.isInsideSwitch: "+isInsideSwitch);
			printLogs("FunctionData.switchStmtOpenBracesCount: "+switchStmtOpenBracesCount);
			printLogs("FunctionData.wasLastStmtSwitch: "+wasLastStmtSwitch);
			
		}
	}
	
	private static Stack<FunctionData> functionDataStack = new Stack<FunctionData>();
	
	private static int filesProcessedCount = 0;
	private static int totalFilesToProcess = 0;
	
	private static AddLogsToMethodsAndroid_V2 thisObject = new AddLogsToMethodsAndroid_V2();
	
	public static void main(String[] args) {
		
		printLogs("Started Program",true);
		
		String[] pathArrToAddLogs = PATHS_TO_ADD_LOGS;
		
		if(testLocalFiles == 1)
			pathArrToAddLogs = LOCAL_PATHS_TO_ADD_LOGS;
		
		for (String pathStr : pathArrToAddLogs)
		{
			File path = new File(pathStr);
			if(path.exists())
				countFilesInPath(path,true);
			else
				System.out.println("countFilesInPath : path does not exist, path: "+path);
		}
		
		printLogs("Total files count to be processed: "+totalFilesToProcess,true);
		
		for (String pathStr : pathArrToAddLogs)
		{
			File path = new File(pathStr);
			if(path.exists())
				processFiles(path,true);
			else
				System.out.println("processFilespath does not exist, path: "+path);
		}
		
	}
	
	public static void countFilesInPath(File path, boolean fromPathArray) 
	{
		if(path.isDirectory())
		{
			File[] files = path.listFiles();
			for (File file : files) 
			{
				if (file.isDirectory()) 
				{
					countFilesInPath(file, false);
				} 
				else 
				{
					if(isValidFile(file, false))
					{
						totalFilesToProcess++; 
					}
				}
			}
		}
		else
		{
			if(isValidFile(path, fromPathArray))
			{
				totalFilesToProcess++; 
			}
		}
	}
	
	public static boolean isValidFile(File file, boolean fromPathArray)
	{
		if(fromPathArray == true)
			return true;
		
		boolean isFileEndsAsExpected = false;
		boolean isFileContainsExpected = false;
		
		String filename = file.getName().trim().toLowerCase();
		String fileAbsolutePath = file.getAbsolutePath().trim().toLowerCase();
		
		for (String validFileNotToContain : VALID_FILE_SHOULD_NOT_CONTAINS)
		{
			if(fileAbsolutePath.contains(validFileNotToContain))
			{
				return false;
			}
		}
		
		if(VALID_FILE_SHOULD_END_WITH.length == 0)
			isFileEndsAsExpected = true;
		
		for (String validFileEndsWith : VALID_FILE_SHOULD_END_WITH)
		{
			if(filename.endsWith(validFileEndsWith))
			{
				isFileEndsAsExpected = true;
				break;
			}
		}
		
		if(VALID_FILE_SHOULD_END_WITH.length == 0)
			isFileEndsAsExpected = true;
		
		if(VALID_FILE_SHOULD_CONTAINS.length == 0)
			isFileContainsExpected = true;
		else if(isArrayContains(VALID_FILE_SHOULD_CONTAINS, "*"))
		{
			isFileContainsExpected = true;
		}
		else
		{
			for (String validFileContains : VALID_FILE_SHOULD_CONTAINS)
			{
				if(filename.contains(validFileContains))
				{
					isFileContainsExpected = true;
					break;
				}
			}
		}
		
		/*printLogs("filename: "+filename,true);
		printLogs("isFileEndsAsExpected: "+isFileEndsAsExpected+" isFileContainsExpected: "+isFileContainsExpected,true);*/
		
		return (isFileEndsAsExpected && isFileContainsExpected) ;
	}
	
	public static boolean isArrayContains(String[] arr, String str)
	{
		for (String arrStr : arr)
		{
			if(arrStr.equals(str))
			{
				return true;
			}
		}
		return false;
	}
	
	public static void addLogsToFile(File file,boolean fromPathArray)
	{
		String filename = file.getName();
		if(isValidFile(file, fromPathArray))
		{
			filesProcessedCount++;
			printLogs("Processing "+filesProcessedCount + " files of "+totalFilesToProcess+", file name: "+file.getAbsolutePath(),true);
			
			ArrayList<LineDetails> fileContentOriginal = new ArrayList<LineDetails>();
			ArrayList<LineDetails> fileContentModified = new ArrayList<LineDetails>();
			
			loadFileContentIntoArrayList(file, fileContentOriginal);
			
			//displayListContent(fileContentOriginal);
			
			addMethodLogs(file, fileContentOriginal, fileContentModified);
			
			//displayListContent(fileContentModified);
			
			rewriteFile(file,fileContentModified); 
			
			printLogs("Processed "+filesProcessedCount + " files of "+totalFilesToProcess+", file name: "+file.getAbsolutePath(),true);
		}					
	}
	
	public static void rewriteFile(File file, ArrayList<LineDetails> fileContent)
	{
		try
		{
			String fileName = file.getName();
			
			BufferedWriter fWriter = new BufferedWriter(new FileWriter(fileName+"_tmp.java"));
			
			ListIterator<LineDetails> iter = fileContent.listIterator();

			while (iter.hasNext())
			{
				LineDetails lineDetails = iter.next();
				
				for(String previousLine : lineDetails.lineToInsertBefore)
				{
					fWriter.write(previousLine);
					fWriter.newLine();
				}
				
				fWriter.write(lineDetails.line);
				fWriter.newLine();
				
				for(String nextLine : lineDetails.lineToInsertAfter)
				{
					fWriter.write(nextLine);
					fWriter.newLine();
				}
				
			}
			
			fWriter.close();
			
			if(testLocalFiles != 1)
			{
				file.delete();
				File temp_file = new File(fileName+"_tmp.java"); 
				temp_file.renameTo(file);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void displayListContent(ArrayList<LineDetails> fileContent)
	{
		int totalLines = fileContent.size();
		
		LineDetails lineDetails = null;
		
		for(int lineNo=0; lineNo < totalLines; lineNo++)
		{
			lineDetails = fileContent.get(lineNo);
			
			lineDetails.print();
		}
	}
	
	public static void addMethodLogs(File file, ArrayList<LineDetails> fileContentOriginal, ArrayList<LineDetails> fileContentModified )
	{
		functionDataStack.clear();
		
		ListIterator<LineDetails> iter = fileContentOriginal.listIterator();
		
		String fileName = file.getName();
		String className = fileName.replace(".java", "");

		while (iter.hasNext())
		{
			LineDetails lineDetails = iter.next();
			
			printLogs("\naddMethodLogs: Before Modifying line, the details are:");
			lineDetails.print();
			
			LineDetails modifiedlineDetails = modifyLineWithFunctionData(lineDetails, fileContentOriginal, className);
			
			printLogs("\naddMethodLogs: After Modifying line, the details are:");
			modifiedlineDetails.print();
			
			fileContentModified.add(modifiedlineDetails);
		}
		
	}
	
	public static int getNextNonSpaceValidIndex(LineDetails lineDetails, ArrayList<LineDetails> fileContent, int fromIndex)
	{
		if(lineDetails.onlyCommentLine || fromIndex >= lineDetails.line.length())
			return -1;
		
		if(lineDetails.singleLineCommentIndex != -1 && fromIndex >= lineDetails.singleLineCommentIndex )
			return -1;
		
		String line = lineDetails.line;
		
		for(int i = fromIndex + 1; i < line.length(); i++)
		{
			if(line.charAt(i) != ' ' && line.charAt(i) != '\t' && line.charAt(i) != '\n')
			{
				boolean foundInsideComment =  false;
				
				for(Pair<Integer,Integer> multiLineCommentsIndexPair : lineDetails.multiLineCommentsIndexes)
				{
					if(i < multiLineCommentsIndexPair.first) //multiline indexes crossed our index, so no need to check more.
						break;
					else if(i >= multiLineCommentsIndexPair.first && i <= ((multiLineCommentsIndexPair.second != -1)? multiLineCommentsIndexPair.second+1 : line.length()-1)) 
					{
						foundInsideComment = true;
						break;
					}
					 
				}

				if(!foundInsideComment)
				{
					return i;
				}
			}
		}
		
		return -1;
	}
	
	public static int getPrevNonSpaceValidIndex(LineDetails lineDetails, ArrayList<LineDetails> fileContent, int fromIndex)
	{
		if(lineDetails.onlyCommentLine || fromIndex <= 0)
			return -1;
		
		if(lineDetails.singleLineCommentIndex != -1 && fromIndex >= lineDetails.singleLineCommentIndex )
			fromIndex = lineDetails.singleLineCommentIndex;
		
		String line = lineDetails.line;
		
		for(int i = fromIndex - 1 ; i >= 0; i--)
		{
			if(line.charAt(i) != ' ' && line.charAt(i) != '\t')
			{
				boolean foundInsideComment =  false;
				
				ListIterator<Pair<Integer, Integer>>  multiLineCommentsIndexesIter = lineDetails.multiLineCommentsIndexes.listIterator();
				
				while(multiLineCommentsIndexesIter.hasPrevious())
				{
					Pair<Integer,Integer> multiLineCommentsIndexPair = multiLineCommentsIndexesIter.previous();
					
					if(i > multiLineCommentsIndexPair.second+1) //multiline indexes crossed our index, so no need to check more.
						break;
					else if(i >= multiLineCommentsIndexPair.first && i <= ((multiLineCommentsIndexPair.second != -1)? multiLineCommentsIndexPair.second+1 : line.length()-1)) 
					{
						foundInsideComment = true;
						break;
					}
				}

				if(!foundInsideComment)
				{
					return i;
				}
			}
		}
		
		return -1;
	}
	
	public static Pair<LineDetails, Integer> getNextValidStrIndexEqualsTo(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr, int fromIndex)
	{
		for(int i=lineDetails.lineNum; i < fileContent.size(); i++)
		{
			lineDetails = fileContent.get(i);
			
			int nextValidStrIndexEqualsTo = getValidIndexOf(lineDetails, fileContent, keyStr, fromIndex);
			
			if(nextValidStrIndexEqualsTo != -1)
			{
				return thisObject.new Pair<LineDetails, Integer>(lineDetails, nextValidStrIndexEqualsTo);
			}		
			
			fromIndex = 0;
		}
		
		return null;
	}
	
	public static Pair<LineDetails, Integer> getPrevValidStrIndexEqualsTo(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr, int fromIndex)
	{
		boolean firstTime = true;
		
		for(int i=lineDetails.lineNum; i >= 0; i--)
		{
			lineDetails = fileContent.get(i);
			
			if(!firstTime)
			{
				fromIndex = lineDetails.line.length() - 1;
			}
			
			int prevValidStrIndexEqualsTo = getLastValidIndexOf(lineDetails, fileContent, keyStr, fromIndex);
			
			if(prevValidStrIndexEqualsTo != -1)
			{
				return thisObject.new Pair<LineDetails, Integer>(lineDetails, prevValidStrIndexEqualsTo);
			}		
			
			firstTime = false;			
		}
		
		return null;
	}
	
	public static boolean isLineEndsWith(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr)
	{
		ArrayList<Integer> keyStrIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails, fileContent, keyStr);
		
		if(keyStrIndexes.isEmpty())
			return false;
		
		int keyStrLastIndex = keyStrIndexes.get(keyStrIndexes.size() - 1);
		int lineLastIndex = lineDetails.line.length() - 1;
		
		if(lineDetails.singleLineCommentIndex != -1)
			lineLastIndex = lineDetails.singleLineCommentIndex - 1;
		
		for(int i = lineDetails.multiLineCommentsIndexes.size() - 1; i >= 0; i--)
		{
			if(keyStrLastIndex < lineDetails.multiLineCommentsIndexes.get(i).first)
			{
				lineLastIndex = lineDetails.multiLineCommentsIndexes.get(i).first - 1;
			}
			else
			{
				break;
			}
		}
		
		//exclude whitespaces in reverse
		while(lineLastIndex >= 0)
		{
			if(lineDetails.line.charAt(lineLastIndex) == ' ' || lineDetails.line.charAt(lineLastIndex) == '\t' )
				lineLastIndex--;
			else
				break;
		}
		
		if((keyStrLastIndex + keyStr.length() - 1) == lineLastIndex ) //need to include it
		{
			return true;
		}
		
		return false;
	}
	
	public static boolean isLineStartsWith(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr)
	{
		ArrayList<Integer> keyStrIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails, fileContent, keyStr);
		
		if(keyStrIndexes.isEmpty())
			return false;
		
		int keyStrFirstIndex = keyStrIndexes.get(0);
		int lineFirstIndex = 0;
		
		for(int i = 0; i < lineDetails.multiLineCommentsIndexes.size(); i++)
		{
			if(keyStrFirstIndex > lineDetails.multiLineCommentsIndexes.get(i).first)
			{
				lineFirstIndex = lineDetails.multiLineCommentsIndexes.get(i).first + 1;
			}
			else
			{
				break;
			}
		}
		
		//exclude whitespaces in reverse
		while(lineDetails.line.charAt(lineFirstIndex) == ' ' || lineDetails.line.charAt(lineFirstIndex) == '\t' )
		{
				lineFirstIndex++;
		}
		
		if(keyStrFirstIndex  == lineFirstIndex)
		{
			return true;
		}
		
		return false;
	}
	
	public static Pair<LineDetails, Integer> getMatchingPair(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr, int currentIndex)
	{
		String oppositePairStr = "(";
		
		if(keyStr.equals("("))
			 oppositePairStr = ")";
		else if(keyStr.equals("{"))
			 oppositePairStr = "}";
		else if(keyStr.equals("}"))
				oppositePairStr = "{";
		else
			oppositePairStr = "(";
		
		printLogs("getMatchingPair trying to get "+oppositePairStr+" pair for "+keyStr+ " from line: "+lineDetails.line+ " from Index: "+currentIndex);
		
		
		Pair<LineDetails, Integer> matchingPair = thisObject.new Pair<LineDetails, Integer>();
		
		if(fileContent.isEmpty())
			return null;
		
		Stack<String> stack = new Stack<String>();
		
		if(keyStr.equals("(") || keyStr.equals("{"))
			currentIndex += 1;
		else if(keyStr.equals(")") || keyStr.equals("}")) 
			currentIndex -= 1;
		
		stack.push(keyStr);
		
		//printLogs("pushing ( from index: "+currentIndex);
		
		int i=0;
		boolean firstTime = true;
		
		for( i = lineDetails.lineNum ; !stack.empty() ; )
		{
			if((keyStr.equals("(") || keyStr.equals("{")) && i >= fileContent.size() )
					break;
			else if((keyStr.equals(")") || keyStr.equals("}")) && i < 0) 	break;
			
			lineDetails = fileContent.get(i);
			
			if(!firstTime)
			{
				if(keyStr.equals("(") || keyStr.equals("{") )
				{
					currentIndex = 0;
				}
				else if(keyStr.equals(")") || keyStr.equals("}") ) 
				{
					currentIndex = lineDetails.line.length() - 1 ;
				}
			}
			firstTime = false;
			
			printLogs("getMatchingPair trying to find matching "+oppositePairStr+" for "+keyStr+" in line: "+lineDetails.line);
			for(int k=0;k<lineDetails.line.length();k++)
			{
				printLogsNoNewLine(k+"="+lineDetails.line.charAt(k)+",");
			}
			printLogs("");

			ArrayList<Pair<Integer,String>> bracketIndexes = new ArrayList<Pair<Integer,String>>();
			
			ArrayList<Integer> oppositePairStrIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails, fileContent, oppositePairStr);
			ArrayList<Integer> keyStrIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails, fileContent, keyStr);
			
			printLogs("oppositePairStrIndexes: "+oppositePairStrIndexes);
			printLogs("keyStrIndexes: "+keyStrIndexes);
			
			for(int j=0; j < oppositePairStrIndexes.size();j++)
			{
				if((keyStr.equals("(") || keyStr.equals("{")) && oppositePairStrIndexes.get(j) < currentIndex)
					continue;
				else if((keyStr.equals(")") || keyStr.equals("}")) && oppositePairStrIndexes.get(j) > currentIndex)
					break;
				
				Pair<Integer,String> pair = thisObject.new Pair<Integer,String>(oppositePairStrIndexes.get(j),oppositePairStr);
				bracketIndexes.add(pair);
			}
			
			for(int j=0;j < keyStrIndexes.size();j++)
			{
				if((keyStr.equals("(") || keyStr.equals("{")) && keyStrIndexes.get(j) < currentIndex)
					continue;
				else if((keyStr.equals(")") || keyStr.equals("}")) && keyStrIndexes.get(j) > currentIndex)
					break;
				
				Pair<Integer,String> pair = thisObject.new Pair<Integer,String>(keyStrIndexes.get(j),keyStr);
				bracketIndexes.add(pair);
			}
			
			Collections.sort(bracketIndexes, new Comparator<Pair<Integer,String>>() {
		        @Override public int compare(Pair<Integer,String> p1, Pair<Integer,String> p2) {
		            return p1.first - p2.first;
		        }
		    });
			
			printLogs("bracketIndexes: "+bracketIndexes);
			
			int j = 0;
			if(keyStr.equals("(") || keyStr.equals("{"))
				j=0;
			else if(keyStr.equals(")") || keyStr.equals("}"))
				j=bracketIndexes.size()-1;
			
			for( ; ; )
			{
				if((keyStr.equals("(") || keyStr.equals("{")) && j >= bracketIndexes.size())
					break;
				else if((keyStr.equals(")") || keyStr.equals("}")) && j < 0) 	break;
				
				if(bracketIndexes.get(j).second.equals(stack.peek()))
				{
					stack.push(bracketIndexes.get(j).second);
					
					/*printLogs("pushing "+bracketIndexes.get(j).second+" from index: "+bracketIndexes.get(j).first);
					printLogs("stack size : "+stack.size());*/
				}
				else
				{
				 	String poppedStr = stack.pop();
				 	
				 	//printLogs("poppedStr: "+poppedStr);
					
					if(stack.empty())
					{
						//printLogs("stack become empty on : "+bracketIndexes.get(j).first);
						matchingPair.second = bracketIndexes.get(j).first;
						matchingPair.first = lineDetails;
						
						return matchingPair;
					}
				}
				if(keyStr.equals("(") || keyStr.equals("{") )
					j++;
				else if(keyStr.equals(")") || keyStr.equals("}") )
					j--;
			}
			
			if(keyStr.equals("(") || keyStr.equals("{") )
			{
				i++;
			}
			else if(keyStr.equals(")") || keyStr.equals("}") ) 
			{
				i--;
			}
			
			//printLogs("stack size: "+stack.size());
		}
			
		return null;
	}
	
	public static void checkAndModifyConditionalStmts(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr)
	{
		int keyStrIndex = getValidIndexOf(lineDetails, fileContent, keyStr);
		printLogs("checkAndModifyConditionalStmts keyStrIndex: "+keyStrIndex);
		if(keyStrIndex == -1)
			return;
		
		if(keyStrIndex >= 0 && keyStrIndex - 1 == getPrevNonSpaceValidIndex(lineDetails, fileContent, keyStrIndex))
			return;
		
		if(keyStrIndex < lineDetails.line.length() -1  && keyStrIndex + 1 == getNextNonSpaceValidIndex(lineDetails, fileContent, keyStrIndex + keyStr.length() - 1))
			return;	
		
		//ignore single line if stmts having try stmts, very difficult to parse it
		if(isLineContainsValidStr(lineDetails, fileContent, "try"))
			return;
		
		int openBracketIndex = getValidIndexOf(lineDetails, fileContent, "(", keyStrIndex+1);
		if(openBracketIndex == -1)
			return;
		
		Pair<LineDetails, Integer> closeBracketDetails = getMatchingPair(lineDetails, fileContent, "(", openBracketIndex);
		
		if(closeBracketDetails != null)
		{
			LineDetails tempLineDetails = lineDetails;
			while(tempLineDetails.lineNum <= closeBracketDetails.first.lineNum)
			{
				tempLineDetails.isConditionStmt = true;
				
				if(tempLineDetails.lineNum < fileContent.size() - 1)
					tempLineDetails = fileContent.get(tempLineDetails.lineNum+1);
				else
					break;
			}
			
			printLogs("elango closeBracketDetails.first.line: "+closeBracketDetails.first.line);
			printLogs("elango closeBracketDetails.second: "+closeBracketDetails.second);
			
			for(int i=0;i<closeBracketDetails.first.line.length();i++)
			{
				printLogsNoNewLine(i+"="+closeBracketDetails.first.line.charAt(i)+",");
			}
			printLogs("");
						
			int nextNonSpaceValidIndex = getNextNonSpaceValidIndex(closeBracketDetails.first, fileContent, closeBracketDetails.second);

			printLogs("elango nextNonSpaceValidIndex: "+nextNonSpaceValidIndex);
			if(nextNonSpaceValidIndex != -1)
			printLogs("elango char forund on that: "+closeBracketDetails.first.line.charAt(nextNonSpaceValidIndex));
			
			if(nextNonSpaceValidIndex != -1 && closeBracketDetails.first.line.charAt(nextNonSpaceValidIndex) == '{') //ends with {, no need to do anything
			{
				closeBracketDetails.first.isConditionStmt = true;
				return;
			}
			else if(nextNonSpaceValidIndex != -1 && closeBracketDetails.first.line.charAt(nextNonSpaceValidIndex) != '{') //does not end with { and something is there after if cond
			{
				closeBracketDetails.first.isConditionStmt = true;
				
				tempLineDetails = closeBracketDetails.first;
				int condCloseBracketIndex = closeBracketDetails.second;
				
				String tempLine = tempLineDetails.line;
				String spaces = createIndentFromLine(tempLine);
				
				printLogs("elango tempLineDetails.line"+tempLineDetails.line);
				
				tempLineDetails.lineToInsertAfter.add(spaces+"{");
				if(!isLineContainsValidStr(tempLineDetails, fileContent, ";"))
				{
					tempLineDetails.lineToInsertAfter.add(spaces + "    " +tempLine.substring(condCloseBracketIndex+1));
				}
				else
				{
					int colonStrIndex = getValidIndexOf(tempLineDetails, fileContent, ";");
					tempLineDetails.lineToInsertAfter.add(spaces + "    " +tempLineDetails.line.substring(condCloseBracketIndex+1, colonStrIndex + 1));
				}
				
				printLogs("1 elango is line contains ; "+isLineContainsValidStr(tempLineDetails, fileContent, ";") );
				
				while(!isLineContainsValidStr(tempLineDetails, fileContent, ";"))
				{
					if(tempLineDetails.lineNum > fileContent.size() - 1)
						break;
					tempLineDetails = fileContent.get(tempLineDetails.lineNum + 1);
					
					printLogs("2 elango is line contains ; "+isLineContainsValidStr(tempLineDetails, fileContent, ";") );
				}
				
				tempLineDetails.lineToInsertAfter.add(spaces+"}");
				
				if(!isLineEndsWith(tempLineDetails, fileContent, ";"))
				{
					int colonStrIndex = getValidIndexOf(tempLineDetails, fileContent, ";");
					tempLineDetails.lineToInsertAfter.add(spaces + "    " +tempLineDetails.line.substring(colonStrIndex + 1));
					if(tempLineDetails.lineNum != closeBracketDetails.first.lineNum)
						tempLineDetails.line = tempLineDetails.line.substring(0,colonStrIndex+1);
				}	
						
				closeBracketDetails.first.line = tempLine.substring(0, closeBracketDetails.second+1 );
				
				printLogs("elango added close bracket");
			}
			else //does not end {, but nothing is there after if, so in next line we will add brace
			{
				if(closeBracketDetails.first.lineNum < fileContent.size() - 1)
				{
					LineDetails startLine = fileContent.get(closeBracketDetails.first.lineNum+1);
					LineDetails nextLine = fileContent.get(closeBracketDetails.first.lineNum+1);
					
					printLogs("startLine: "+startLine.line);
					printLogs("1 nextLine: "+nextLine.line);
					
					while( nextLine.onlyCommentLine && nextLine.lineNum < fileContent.size() - 1) //bypass if any comment lines
					{
						nextLine = fileContent.get(nextLine.lineNum+1);
						nextLine.isConditionStmt = true;
					}
					
					printLogs("2 nextLine: "+nextLine.line);
					
					while(!isLineContainsValidStr(nextLine, fileContent, ";")) //go till the stmt contains ;
					{
						if(nextLine.lineNum > fileContent.size() - 1)
							break;
						nextLine = fileContent.get(nextLine.lineNum + 1);
					}
					
					printLogs("3 nextLine: "+nextLine.line);
					
					if(!isLineStartsWith(startLine, fileContent, "{"))
					{
						nextLine.isConditionStmt = true;
						startLine.isConditionStmt = true;
						String spaces = createIndentFromLine(lineDetails.line);
						startLine.lineToInsertBefore.add(spaces+"{");
						
						if(!isLineEndsWith(nextLine, fileContent, ";")) //if ; comes in between line. mean if cond finished there.
						{
							int colonStrIndex = getValidIndexOf(nextLine, fileContent, ";");
							nextLine.lineToInsertAfter.add(spaces+"}");
							nextLine.lineToInsertAfter.add(spaces + "    " +nextLine.line.substring(colonStrIndex + 1));
							nextLine.line = nextLine.line.substring(0,colonStrIndex+1);
						}	
						else
							nextLine.lineToInsertAfter.add(spaces+"}");
						
					}
				}
			}
		}
	}
	
	public static void checkTryCatchFinally(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		int finalizeStrIndex = getValidIndexOf(lineDetails, fileContent, "finally");
		
		//printLogs("elango1 finalizeStrIndex: "+finalizeStrIndex);
		
		if(finalizeStrIndex == -1)
			return;
		
		Pair<LineDetails, Integer> finallyCloseBracesPair = getPrevValidStrIndexEqualsTo(lineDetails, fileContent, "}", finalizeStrIndex);
		
		//printLogs("elango1 finallyCloseBracesPair.first: "+finallyCloseBracesPair.first.line);
		//printLogs("elango1 finallyCloseBracesPair.second: "+finallyCloseBracesPair.second);
		
		Pair<LineDetails, Integer> tryStartingPair = getMatchingPair(lineDetails, fileContent, "}", finallyCloseBracesPair.second);
		
		//printLogs("elango1 tryStartingPair.first: "+tryStartingPair.first.line);
		//printLogs("elango1 tryStartingPair.second: "+tryStartingPair.second);
		
		LineDetails tempLineDetails = finallyCloseBracesPair.first;
		
		for(int k=tempLineDetails.lineNum; k >= tryStartingPair.first.lineNum; k--)
		{
			tempLineDetails = fileContent.get(k);
			
			//printLogs("elango1 tempLine: "+tempLineDetails.line);
			
			int throwIndex =  getValidIndexOf(tempLineDetails, fileContent, "throw ");
			
			//printLogs("elango1 throwIndex: "+throwIndex);
			
			if(throwIndex != -1)
			{
				Pair<LineDetails, Integer> lastThrowStmtEndPair = getNextValidStrIndexEqualsTo(tempLineDetails, fileContent, ";", throwIndex);
				
				//printLogs("elango1 lastThrowStmtEndPair.first: "+lastThrowStmtEndPair.first.line);
				//printLogs("elango1 lastThrowStmtEndPair.second: "+lastThrowStmtEndPair.second);
				
				int throwStmtLineNum = lastThrowStmtEndPair.first.lineNum;
				
				//printLogs("elango1 throwStmtLineNum: "+throwStmtLineNum);
				
				while( throwStmtLineNum < finallyCloseBracesPair.first.lineNum && 
						(lastThrowStmtEndPair.first.line.trim().isEmpty() 
						|| lastThrowStmtEndPair.first.onlyCommentLine))
				{
					throwStmtLineNum++;
				}
				
				//printLogs("elango1 throwStmtLineNum: "+throwStmtLineNum);
				//printLogs("elango1 finallyCloseBracesPair.first.lineNum: "+finallyCloseBracesPair.first.lineNum);
				
				if(throwStmtLineNum + 1 ==  finallyCloseBracesPair.first.lineNum)
				{
					if(!functionDataStack.isEmpty())
					{
						FunctionData functionData = functionDataStack.peek();
						functionData.isAlwaysExpThrownOut = true;
					}
					break;
				}
				
				break;
			}
			
		}
	}
	
	public static LineDetails modifyLineWithFunctionData(LineDetails lineDetails, ArrayList<LineDetails> fileContentOriginal, String className)
	{
		String line = lineDetails.line;
		
		LineDetails previousLineDetails = null;
		if(lineDetails.lineNum > 0)
			previousLineDetails = fileContentOriginal.get(lineDetails.lineNum - 1);
		
		if(!functionDataStack.isEmpty())
		{
			lineDetails.functionData = functionDataStack.peek();
		}
		
		if(previousLineDetails != null)
		{
			lineDetails.className = previousLineDetails.className;
		}
		
		if(line == null || (line != null && line.trim().isEmpty()) ) //empty line
		{
			return lineDetails;
		}
		
		//check only comment line presents, no executable line
		if(lineDetails.onlyCommentLine)
		{
			return lineDetails;
		}
		
		if(isLineStartsWith(lineDetails, fileContentOriginal, "package "))
			lineDetails.lineToInsertAfter.add("import "+IMPORT_FOR_LOGGER+";");
		
		if(isLineContainsValidStr(lineDetails, fileContentOriginal, "DEBUG = false") 
				|| isLineContainsValidStr(lineDetails, fileContentOriginal, "DEBUG =false") 
				|| isLineContainsValidStr(lineDetails, fileContentOriginal, "DEBUG= false") 
				|| isLineContainsValidStr(lineDetails, fileContentOriginal, "DEBUG=false") )
		{
			lineDetails.line = lineDetails.line.replace("false", "true");
			return lineDetails;
		}
		
		/*if(testLocalFiles == 0)
			return lineDetails;*/
			
		updateWasLastStmtSwitch(lineDetails, fileContentOriginal);
		
		if(!mustNotContainCondToBeFunction(lineDetails, fileContentOriginal)) //some conditional statements are there in the line
		{
			checkAndModifyConditionalStmts(lineDetails, fileContentOriginal,"if");
		}
		
		checkTryCatchFinally(lineDetails, fileContentOriginal);
		
		checkAndAddExitLog(lineDetails, fileContentOriginal);
		
		if(lineDetails.isConditionStmt)
			return lineDetails;
		
		if(lineDetails.isEntryLogAdded)
			return lineDetails;		
		
		if(isLineContainsValidStr(lineDetails, fileContentOriginal, "while (true)") || isLineContainsValidStr(lineDetails, fileContentOriginal, "while(true)")
				|| isLineContainsValidStr(lineDetails, fileContentOriginal, "for (;;)") || isLineContainsValidStr(lineDetails, fileContentOriginal, "for(;;)") ) //will never come out of loop so dont add exit log
		{
			if(!functionDataStack.isEmpty())
			{
				FunctionData functionData = functionDataStack.peek();
				functionData.isInfiniteLoop = true;
			}
		}
		
		if(lineDetails.className.isEmpty() &&
				(line.contains("class "+className+" ") || line.endsWith("class "+className) || line.contains("class "+className+"<")) ||
				(line.contains("interface "+className+" ") || line.endsWith("interface "+className) || line.contains("interface "+className+"<")) )
		{
			ListIterator<LineDetails> tempIter = fileContentOriginal.listIterator();
			LineDetails tempLineDetails = lineDetails;
			
			while(!isLineEndsWith(tempLineDetails, fileContentOriginal, "{") && tempIter.hasNext())
			{
				tempLineDetails = tempIter.next();
			}
			
			tempLineDetails.className = className; //to avoid further entering this condition
			tempLineDetails.lineToInsertAfter.add(createIndentFromLine(tempLineDetails.line) + "    " + "public static final String ENTRY_EXIT_TAG = \""+ className +"\";");
		}
		
		/*printLogs("elango1 checking line: "+lineDetails.line);
		printLogs("mustContainToBeFunction(lineDetails, fileContentOriginal) : "+mustContainToBeFunction(lineDetails, fileContentOriginal));
		printLogs("mustEndWithToBeFunction(lineDetails, fileContentOriginal): "+mustEndWithToBeFunction(lineDetails, fileContentOriginal));
		printLogs("mustNotContainToBeFunction(lineDetails, fileContentOriginal): "+mustNotContainToBeFunction(lineDetails, fileContentOriginal));
		printLogs("mustNotContainCondToBeFunction(lineDetails, fileContentOriginal): "+mustNotContainCondToBeFunction(lineDetails, fileContentOriginal));*/
		
		if((mustContainToBeFunction(lineDetails, fileContentOriginal) && mustEndWithToBeFunction(lineDetails, fileContentOriginal)
				&& mustNotContainToBeFunction(lineDetails, fileContentOriginal) && mustNotContainCondToBeFunction(lineDetails, fileContentOriginal)
				|| isLineContainsValidStr(lineDetails, fileContentOriginal, "throws ") ) && mustNotEndWithToBeFunction(lineDetails, fileContentOriginal))
		{		
			LineDetails openBraceLineDetails = lineDetails;
			
			if(isLineContainsValidStr(lineDetails, fileContentOriginal, ") {") && isLineContainsValidStr(lineDetails, fileContentOriginal, "}"))
			{
				return lineDetails;
			}
			
			if(isLineEndsWith(lineDetails, fileContentOriginal, ")"))
			{		
				if(isLineStartsWith(lineDetails, fileContentOriginal, "("))
				{
					if(previousLineDetails != null && (
							!mustNotContainToBeFunction(previousLineDetails, fileContentOriginal) 
							|| !mustNotContainCondToBeFunction(previousLineDetails, fileContentOriginal)))
					{
						return lineDetails;
					}
							
				}
				
				for(int j= openBraceLineDetails.lineNum+1 ; j<fileContentOriginal.size(); j++)
				{
					if(isLineContainsValidStr(openBraceLineDetails, fileContentOriginal, ";"))
						return lineDetails;
					
					openBraceLineDetails = fileContentOriginal.get(j);
					if(isLineContainsValidStr(openBraceLineDetails, fileContentOriginal, "{"))
					{
						break;
					}
				}
			}
			
			Pair<String, LineDetails> functionNameInfo = null;
			
			if(isLineContainsValidStr(lineDetails, fileContentOriginal, "throws "))
			{
				if(lineDetails.isEntryLogAdded)
					return lineDetails;
				
				if(lineDetails.line.endsWith(";"))
					return lineDetails;
				
				for(int j= openBraceLineDetails.lineNum ; j<fileContentOriginal.size(); j++)
				{
					openBraceLineDetails = fileContentOriginal.get(j);
					if(isLineContainsValidStr(openBraceLineDetails, fileContentOriginal, "{"))
					{
						break;
					}
				}
				
				int throwsStrIndex = getValidIndexOf(lineDetails, fileContentOriginal, "throws");
			
				printLogs("elango  throwsStrIndex: "+throwsStrIndex);
				Pair<LineDetails, Integer> closeBracketFuncPair = getPrevValidStrIndexEqualsTo(lineDetails, fileContentOriginal, ")", throwsStrIndex);
				printLogs("elango closeBracketFuncPair.first.line: "+closeBracketFuncPair.first.line);
				printLogs("elango closeBracketFuncPair.second: "+closeBracketFuncPair.second);
				
				functionNameInfo = getFunctionName(closeBracketFuncPair.first, fileContentOriginal);
				printLogs("elango 1 functionName: "+functionNameInfo.first);
			}
			else
			{
				int closeBracketIndex = getLastValidIndexOf(lineDetails, fileContentOriginal, ")");
				Pair<LineDetails, Integer> matchingPairData = getMatchingPair(lineDetails, fileContentOriginal,")", closeBracketIndex);
				
				if(!mustNotContainCondToBeFunction(matchingPairData.first, fileContentOriginal) || !mustNotContainToBeFunction(matchingPairData.first, fileContentOriginal) )
				{
					return lineDetails;
				}
				
				functionNameInfo = getFunctionName(lineDetails, fileContentOriginal);
				printLogs("elango 2 functionName: "+functionNameInfo.first);
			}
			
			if(lineDetails.lineNum != openBraceLineDetails.lineNum) //since it wont be counted later
			{
				if(!functionDataStack.isEmpty())
				{
					FunctionData lastfunctionData = functionDataStack.peek();
					lastfunctionData.openBracesCount += 1;
				}
			}
			
			if(openBraceLineDetails.isEntryLogAdded)
				return lineDetails;
			
			if(!functionDataStack.isEmpty())
			{
				FunctionData lastfunctionData = functionDataStack.peek();
				lastfunctionData.openBracesCount = (lastfunctionData.openBracesCount > 0)? lastfunctionData.openBracesCount - 1: lastfunctionData.openBracesCount ;
			}
			
			openBraceLineDetails.isEntryLogAdded = true;
			
			if(openBraceLineDetails.lineNum + 1 < fileContentOriginal.size())
			{
				LineDetails nextLineDetails =  fileContentOriginal.get(openBraceLineDetails.lineNum + 1 );
				
				while( nextLineDetails.onlyCommentLine && nextLineDetails.lineNum < fileContentOriginal.size() - 1) //skip if any comment lines
				{
					nextLineDetails = fileContentOriginal.get(nextLineDetails.lineNum+1);
				}
				
				if( isLineStartsWith(nextLineDetails, fileContentOriginal, "super") ||
						((isLineContainsValidStr(nextLineDetails, fileContentOriginal, "super") || isLineContainsValidStr(nextLineDetails, fileContentOriginal, "this"))
						&& (!isLineStartsWith(nextLineDetails, fileContentOriginal, "this.") 
							&& !isLineContainsValidStr(nextLineDetails, fileContentOriginal, "this)") 
							&& !isLineContainsValidStr(nextLineDetails, fileContentOriginal, "this,") 
							&& !(isLineContainsValidStr(nextLineDetails, fileContentOriginal, "return") && isLineContainsValidStr(nextLineDetails, fileContentOriginal, "super"))
							&& !(isLineContainsValidStr(nextLineDetails, fileContentOriginal, "return") && isLineContainsValidStr(nextLineDetails, fileContentOriginal, "this")) 
							&& !(isLineContainsValidStr(nextLineDetails, fileContentOriginal, "synchronized") && isLineContainsValidStr(nextLineDetails, fileContentOriginal, "this")))))
				{
					
					while(!isLineEndsWith(nextLineDetails, fileContentOriginal, ";"))
					{
						if(nextLineDetails.lineNum > fileContentOriginal.size() - 1)
							break;
						
						nextLineDetails = fileContentOriginal.get(nextLineDetails.lineNum + 1);
					}
					
					openBraceLineDetails = nextLineDetails;
				}
			}
				
			FunctionData functionData = thisObject.new FunctionData();
			functionData.functionName = functionNameInfo.first;
			functionData.openBracesCount = 1;
			functionData.isFunctionReturnAnything = false;
			functionData.isInfiniteLoop = false;
			
			functionDataStack.push(functionData);
			
			lineDetails.functionData = functionData;
			
			String spaces = createIndentFromLine(functionNameInfo.second.line);
			
			openBraceLineDetails.lineToInsertAfter.add(spaces + "    " + getEntryLogStr(lineDetails.functionData.functionName));
			
			printLogs("\n================== Entry log is added on line: "+openBraceLineDetails.line);
			
		}
		
		return lineDetails;
	}
	
	public static void loadFileContentIntoArrayList(File file, ArrayList<LineDetails> fileContent)
	{
		try
		{
			BufferedReader fReader = new BufferedReader(new FileReader(file)); //jhello //awds
			String line = ""; 
			int lineNo = -1;
			
			while ((line = fReader.readLine()) != null) {
				
				LineDetails lineDetails = thisObject.new LineDetails();
				
				printLogs("\n\nRead from file - line: "+line);
				
				lineDetails.lineNum = ++lineNo;
				lineDetails.line = line;
				
				fileContent.add(lineDetails);
				
				if(line.trim().startsWith("//") || (line.trim().startsWith("/*") && line.trim().indexOf("*/", 2) == -1))
				{
					lineDetails.onlyCommentLine = true;
				}
				
				LineDetails previousLineDetails = null;
				
				if(lineDetails.lineNum > 0)
					previousLineDetails = fileContent.get(lineDetails.lineNum - 1);
				
				ArrayList<Integer> singleLineCommentIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails,fileContent,"//");
				if(!singleLineCommentIndexes.isEmpty())
					lineDetails.singleLineCommentIndex =  singleLineCommentIndexes.get(0);
				
				ArrayList<Integer> multiLineCommentOpenIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails,fileContent,"/*");
				printLogs("multiLineCommentOpenIndexes: "+multiLineCommentOpenIndexes);
				
				ArrayList<Integer> multiLineCommentCloseIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails,fileContent,"*/");
				printLogs("multiLineCommentCloseIndexes: "+multiLineCommentCloseIndexes);
				
				ArrayList<Integer> quotationIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails,fileContent,"\"");
				printLogs("quotationIndexes: "+quotationIndexes);
				
				if(!multiLineCommentOpenIndexes.isEmpty() || !multiLineCommentCloseIndexes.isEmpty() )
				{
					int i=0;
					int j=0;				
					for(i=0, j=0; i < multiLineCommentOpenIndexes.size() &&  j < multiLineCommentCloseIndexes.size(); i++, j++)
					{
						int openCommentStrIndex = multiLineCommentOpenIndexes.get(i);
						int closeCommentStrIndex = multiLineCommentCloseIndexes.get(j);
						
						if(i == 0 && openCommentStrIndex > closeCommentStrIndex )
						{
							openCommentStrIndex = -1;
							i--;
						}
						
						Pair<Integer,Integer> pairOfCommentIndexes = thisObject.new Pair<Integer,Integer>(openCommentStrIndex, closeCommentStrIndex);
						lineDetails.multiLineCommentsIndexes.add(pairOfCommentIndexes);	
					}
					
					if(i < multiLineCommentOpenIndexes.size()) //to check if open comment is left
					{
						Pair<Integer,Integer> pairOfCommentIndexes = thisObject.new Pair<Integer,Integer>(multiLineCommentOpenIndexes.get(i), -1);
						lineDetails.multiLineCommentsIndexes.add(pairOfCommentIndexes);	
						lineDetails.multiLineCommentOpen = true;
					}
				}
				else if(previousLineDetails !=null && previousLineDetails.multiLineCommentOpen) //both list are empty and previous line was not closed
				{
					lineDetails.onlyCommentLine = true;
					lineDetails.multiLineCommentOpen = true;
				}
				
				for(int i=0;i<quotationIndexes.size();i++)
				{
					Pair<Integer,Integer> pairOfQuotationIndexes = thisObject.new Pair<Integer,Integer>(quotationIndexes.get(i), quotationIndexes.get(++i));
					lineDetails.quotationIndexes.add(pairOfQuotationIndexes);
				}		
								
				//lineDetails.print();
			}
				
			fReader.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void updateWasLastStmtSwitch(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		if(functionDataStack.isEmpty())
		{
			return;
		}
		
		FunctionData functionData = functionDataStack.peek();
		
		if(isLineContainsValidStr(lineDetails, fileContent, "switch (") ||isLineContainsValidStr(lineDetails, fileContent, "switch(") && !functionData.isInsideSwitch) 
		{
			functionData.isInsideSwitch = true;
		}
		
		printLogs("isInsideSwitch: "+functionData.isInsideSwitch);
		
		if(functionData.isInsideSwitch)
		{
			if(isLineContainsValidStr(lineDetails, fileContent, "break;"))
			{
				functionData.wasLastStmtSwitch = false;
				functionData.isInsideSwitch = false;
			}
			else
			{
				functionData.switchStmtOpenBracesCount += getValidOccurancesCount(lineDetails, fileContent, "{");
				functionData.switchStmtOpenBracesCount -= getValidOccurancesCount(lineDetails, fileContent, "}");
				
				printLogs("updateFile switchStmtOpenBracesCount : "+functionData.switchStmtOpenBracesCount);
				
				if(functionData.switchStmtOpenBracesCount == 0 )
				{
					functionData.wasLastStmtSwitch = true;
					functionData.isInsideSwitch = false;
				}
			}
		}
		
		if(functionData.wasLastStmtSwitch && !lineDetails.line.trim().equals("}"))
		{
			functionData.wasLastStmtSwitch = false;
		}
		
		printLogs("wasLastStmtSwitch: "+functionData.wasLastStmtSwitch);
	}
	
	public static ArrayList<Pair<Integer,String>> getOccurancesIndexes(String line, String keyStr)
	{
		return getOccurancesIndexes(line, keyStr, 0);
	}
	
	public static ArrayList<Pair<Integer,String>> getOccurancesIndexes(String line, String keyStr, int index)
	{
		ArrayList<Pair<Integer,String>> keyStrIndexes = new ArrayList<Pair<Integer,String>>();
		
		int keyStrIndex = index;		
		while((keyStrIndex = line.indexOf(keyStr, keyStrIndex)) != -1)
		{
			if(keyStr.equals("\""))
			{
				if(keyStrIndex < line.length() - 2 && line.charAt(keyStrIndex-1) == '\'' && line.charAt(keyStrIndex+1) == '\'')
				{
					keyStrIndex += keyStr.length();
					continue;
				}
				
				if(keyStrIndex > 0 && line.charAt(keyStrIndex-1) == '\\')
				{
					int slashCount = 0;
					for(int i=keyStrIndex - 1; i > 0 && keyStrIndex >= 0 && line.charAt(i) == '\\' ; i--)
					{
						slashCount++;
					}
					
					if(slashCount % 2 != 0)
					{
						keyStrIndex += keyStr.length();
						continue;
					}
				}
			}

			if(keyStr.equals("}") || keyStr.equals("{") )
			{
				if((keyStrIndex > 0 && line.charAt(keyStrIndex-1) == '\'') && (keyStrIndex < line.length() - 1 && line.charAt(keyStrIndex+1) == '\''))
				{
					keyStrIndex += keyStr.length();
					continue;
				}
				
			/*	if(keyStrIndex > 0 && line.charAt(keyStrIndex-1) == '\\')
				{
					int slashCount = 0;
					for(int i=keyStrIndex - 1; i > 0 && keyStrIndex >= 0 && line.charAt(i) == '\\' ; i--)
					{
						slashCount++;
					}
					
					if(slashCount % 2 != 0)
					{
						keyStrIndex += keyStr.length();
						continue;
					}
				}*/
			}
			
			Pair<Integer,String> pair = thisObject.new Pair<Integer,String>(keyStrIndex, keyStr);
			keyStrIndexes.add(pair);
			
			keyStrIndex += keyStr.length();
		}
		
		return keyStrIndexes;
	}
	
	public static ArrayList<Integer> getIndexesOutMultiLineCmntQuotes(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr)
	{
		ArrayList<Integer> resultedKeyStrIndexes = new ArrayList<Integer>();
		
		if(lineDetails == null)
			return resultedKeyStrIndexes;
		
		String line = lineDetails.line;
		
		LineDetails previousLineDetails = null;
		if(lineDetails.lineNum > 0)
			previousLineDetails = fileContent.get(lineDetails.lineNum - 1);
		
		int index = 0;
		if(previousLineDetails !=null && previousLineDetails.multiLineCommentOpen)
		{
			if(line.indexOf("*/") == -1)
				return resultedKeyStrIndexes;
			else
				index = line.indexOf("*/",index);
		}
		
		ArrayList<Pair<Integer,String>> keyStrIndexes = getOccurancesIndexes(line, keyStr, index);
		
		//printLogs("getIndexesOutMultiLineCmntQuotes keyStrIndexes: "+keyStrIndexes);
		
		if(!keyStrIndexes.isEmpty())
		{
			ArrayList<Pair<Integer,String>> indexesOfExcluders = new ArrayList<Pair<Integer,String>>();
			
			indexesOfExcluders.addAll(getOccurancesIndexes(line, "//", index));
			indexesOfExcluders.addAll(getOccurancesIndexes(line, "/*", index));
			
			//printLogs("open bracket occurances: "+getOccurancesIndexes(line, "/*", index));
			
			indexesOfExcluders.addAll(getOccurancesIndexes(line, "*/", index));
			
			//printLogs("close bracket occurances: "+getOccurancesIndexes(line, "*/", index));
			
			indexesOfExcluders.addAll(getOccurancesIndexes(line, "\"", index));
			
			Collections.sort(indexesOfExcluders, new Comparator<Pair<Integer,String>>() {
		        @Override public int compare(Pair<Integer,String> p1, Pair<Integer,String> p2) {
		            return p1.first - p2.first;
		        }
		    });
			
			printLogs("getIndexesOutMultiLineCmntQuotes: before - indexesOfExcluders: "+indexesOfExcluders);
			//Keep only valid excluders like outside of each other.
			ListIterator<Pair<Integer,String>> iter = indexesOfExcluders.listIterator();
			int pos = 0;
			Pair<Integer,String> indexOfExluderPairPrev = null;
			while (iter.hasNext())  //DO NOT CHANGE THE ORDER OF THESE CONDITIONS
			{
				if(previousLineDetails != null && previousLineDetails.multiLineCommentOpen) //skip comment lines
				{
					while(iter.hasNext() && !iter.next().second.equals("*/"))
			    	{
			    		iter.remove();
			    	}
				}				
				
				if(iter.hasNext())
				{
					Pair<Integer,String> indexOfExluderPair = iter.next();
					
					if(!keyStr.equals("\"") && indexOfExluderPairPrev != null)
					{
						if(!indexOfExluderPairPrev.second.equals("\"") && indexOfExluderPairPrev.second.equals(indexOfExluderPair.second))
						{
							iter.remove();
							break;
						}
					}
				    
				    if(indexOfExluderPair.second.equals("\""))
				    {
				    	while(iter.hasNext())
				    	{
				    		indexOfExluderPair = iter.next();
				    		if(!indexOfExluderPair.second.equals("\""))
				    		{
				    			iter.remove();
				    		}
				    		else
				    			break;
				    	}
				    	
				    	//while(iter.hasNext() && !iter.next().second.equals("\""))
				    	//{
				    	//	iter.remove();
				    	//}
				    }
				    
				    if(indexOfExluderPair.second.equals("//"))
				    {
 				    	indexesOfExcluders.subList(pos + 1, indexesOfExcluders.size()).clear();
				    	break;
				    }
				    
				    if(indexOfExluderPair.second.equals("/*"))
				    {
				    	while(iter.hasNext())
				    	{
				    		indexOfExluderPair = iter.next();
				    		if(!indexOfExluderPair.second.equals("*/"))
				    		{
				    			iter.remove();
				    		}
				    		else
				    			break;
				    	}
				    	//while(iter.hasNext() && !iter.next().second.equals("*/"))
				    	//{
				    	//	printLogs("removing */");
				    	//	iter.remove();
				    	//}
				    }
				    
				    pos++;
				    indexOfExluderPairPrev = indexOfExluderPair;
				}
			}
			
			printLogs("getIndexesOutMultiLineCmntQuotes: after - indexesOfExcluders: "+indexesOfExcluders);
			
			//finding valid keyStr indexes who stays outside excluders
			for(Pair<Integer,String> keyStrIndexPair : keyStrIndexes)
			{
				boolean validKeyStrIdex = true;
				
				iter = indexesOfExcluders.listIterator();
				
				while (iter.hasNext())
				{
					Pair<Integer,String> indexOfExcluder = iter.next();
					
					if(indexOfExcluder.second.equals("*/") && !keyStr.equals("/*") && keyStrIndexPair.first < indexOfExcluder.first)
					{
						validKeyStrIdex = false;
						break;
					}
					else if(indexOfExcluder.second.equals("//") && keyStrIndexPair.first > indexOfExcluder.first)
					{
						validKeyStrIdex = false;
						break;
					}				
					else if(!indexOfExcluder.second.equals("*/") && !indexOfExcluder.second.equals("//"))
					{
						if(iter.hasNext())
						{
							Pair<Integer,String> indexOfExcluderSecond = iter.next();
							
							if(keyStrIndexPair.first > indexOfExcluder.first && keyStrIndexPair.first < indexOfExcluderSecond.first)
							{
								validKeyStrIdex = false;
								break;
							}
						}
					}
					
				}
				
				if(validKeyStrIdex)
				{
					resultedKeyStrIndexes.add(keyStrIndexPair.first);
				}
			}
		}
		
		return resultedKeyStrIndexes;
		
	}
	
	
	public static void printLogs(String logMsg, boolean isEnabled) 
	{
		if(isEnabled)
			System.out.println(logMsg);
	}
	
	public static void printLogs(String logMsg) 
	{
		printLogs(logMsg, enableLogs == 1);
	}
	
	public static void printLogsNoNewLine(String logMsg, boolean isEnabled) 
	{
		if(isEnabled)
			System.out.print(logMsg);
	}
	
	public static void printLogsNoNewLine(String logMsg) 
	{
		printLogsNoNewLine(logMsg, enableLogs == 1);
	}
	
	public static String getProjectDirectory() {
		String projectDir = "D:\\";
		try {
			URL u = AddLogsToMethodsAndroid_V2.class.getProtectionDomain().getCodeSource()
					.getLocation();
			File f = new File(u.toURI());
			projectDir = f.getParent().toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return projectDir;
	}

	public static void processFiles(File path, boolean fromPathArray) {
		try
		{
			if(path.isDirectory())
			{
				printLogs("Processing files from directory and its subdirectories : " + path.getCanonicalPath());
				File[] files = path.listFiles();
				for (File file : files) {
					if (file.isDirectory()) {
						processFiles(file,false);
					} else {
						addLogsToFile(file,false);
					}
				}
				printLogs("Completed files from directory and its subdirectories : " + path.getCanonicalPath());
			}
			else
			{
				addLogsToFile(path,fromPathArray);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public static String getEntryLogStr(String methodName)
	{
		return getEntryLogStr(methodName, LOGGER_TYPE_STR);
	}
	
	public static String getEntryLogStr(String methodName, String loggerType)
	{
		return loggerType +"(ENTRY_EXIT_TAG,\"entry: "+ methodName + "\");";
	}
	
	public static String getExitLogStr(String methodName)
	{
		return getExitLogStr(methodName, LOGGER_TYPE_STR);
	}
	
	public static String getExitLogStr(String methodName, String loggerType)
	{
		return loggerType +"(ENTRY_EXIT_TAG,\"exit: "+ methodName + "\");";
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
	
	public static int getValidOccurancesCount(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr)
	{ 
		return getValidOccurancesCount(lineDetails,fileContent, keyStr, 0);
	}
	
	public static int getValidOccurancesCount(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr, int fromIndex)
	{ 
		ArrayList<Integer> keyStrIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails, fileContent, keyStr);
		
		ListIterator<Integer> keyIndexIter = keyStrIndexes.listIterator();
		while(keyIndexIter.hasNext())
		{
			if(keyIndexIter.next() < fromIndex)
			{
				keyIndexIter.remove();
			}
		}	
		
		return keyStrIndexes.size();
	}
	
	public static int getValidIndexOf(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr)
	{
		return getValidIndexOf(lineDetails, fileContent, keyStr,0);
	}
	
	public static int getValidIndexOf(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr, int fromIndex)
	{
		if(fromIndex >= lineDetails.line.length())
			return -1;
		
		ArrayList<Integer> keyStrIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails, fileContent, keyStr);
		
		for(int i=0; i<keyStrIndexes.size(); i++)
		{
			if(keyStrIndexes.get(i) > fromIndex )
			{
				return keyStrIndexes.get(i);
			}
		}
		
		return -1;
	}
		
	public static int getLastValidIndexOf(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr)
	{
		return getLastValidIndexOf(lineDetails, fileContent, keyStr, lineDetails.line.length()-1);
	}
	
	public static int getLastValidIndexOf(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr, int fromIndex)
	{
		if(fromIndex < 0)
			return -1;
		
		if(fromIndex > lineDetails.line.length())
			fromIndex = lineDetails.line.length();
		
		ArrayList<Integer> keyStrIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails, fileContent, keyStr);
		
		for(int i=keyStrIndexes.size()-1; i>=0; i--)
		{
			if(keyStrIndexes.get(i) <= fromIndex )
			{
				return keyStrIndexes.get(i);
			}
		}		
		
		return -1;
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
	
	public static String createIndentFromLine(String line)
	{
		int i = 0;
		for( ; i < line.length(); i++)
	    {
	      if( line.charAt(i) != ' ' && line.charAt(i) != '\t')
	      {
	    	  break;
	      }
	    }
		
		return line.substring(0,i);
	}
	
	public static boolean mustContainToBeFunction(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		if(validMatcherStringsArr.length == 0)
			return true;
		
		for (String vaildMatchers : validMatcherStringsArr)
		{
			ArrayList<Integer> keyIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails, fileContent, vaildMatchers);
			if(!keyIndexes.isEmpty())
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean mustEndWithToBeFunction(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		if(validEndMatcherStringsArr.length == 0)
			return true;
		
		for (String validEndMatcher : validEndMatcherStringsArr)
		{
			if(isLineEndsWith(lineDetails, fileContent, validEndMatcher))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean mustNotContainToBeFunction(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		if(nonValidMatcherStringsArr.length == 0)
			return true;
		
		for (String nonVaildMatcher : nonValidMatcherStringsArr)
		{
			if(getValidOccurancesCount(lineDetails, fileContent, nonVaildMatcher) != 0)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean mustNotEndWithToBeFunction(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		if(nonValidEndMatcherStringsArr.length == 0)
			return true;
		
		for (String nonVaildEndMatcher : nonValidEndMatcherStringsArr)
		{
			if(isLineEndsWith(lineDetails, fileContent, nonVaildEndMatcher))
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	
	public static boolean mustNotContainCondToBeFunction(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		if(condStmtMatcherStringsArr.length == 0)
			return true;
		
		for (String condStmtMatcher : condStmtMatcherStringsArr)
		{
			if(getValidOccurancesCount(lineDetails, fileContent, condStmtMatcher) != 0)
			{
				return false;
			}
		}
		return true;
	}
	
	public static Pair<String, LineDetails> getFunctionName(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		String functionName = "";
		
		if(fileContent.isEmpty())
			return null;
		
		int currentIndex = getLastValidIndexOf(lineDetails, fileContent, ")");
		
		printLogs("elango currentIndex: "+currentIndex);
		
		Pair<LineDetails, Integer> matchingPairData = getMatchingPair(lineDetails,fileContent,")", currentIndex);
		
		if(matchingPairData == null)
		{
			return null;
		}
		
		printLogs("elango matchingPairData.first.line: "+matchingPairData.first.line);
		printLogs("elango matchingPairData.second: "+matchingPairData.second);
		
		int lastNonSpaceIndex = matchingPairData.second;
		lineDetails = matchingPairData.first;
		
		while((--lastNonSpaceIndex) >= 0 && (lineDetails.line.charAt(lastNonSpaceIndex) == ' ' || lineDetails.line.charAt(lastNonSpaceIndex) == '\t'))
		{
			//loop till we get a non space char or start of the line
		}
		
		int lastIndexOfSpace = getLastValidIndexOf(lineDetails, fileContent, " ",lastNonSpaceIndex);
		
		if(lastIndexOfSpace == -1)
		{
			lastIndexOfSpace = 0;	
		}
		
		if(lastNonSpaceIndex == -1)
		{
			lastNonSpaceIndex = lineDetails.line.length()-1;
		}

		printLogs("elango lastIndexOfSpace: "+lastIndexOfSpace);
		printLogs("elango lastNonSpaceIndex: "+lastNonSpaceIndex);
		functionName = lineDetails.line.substring(lastIndexOfSpace+1, lastNonSpaceIndex+1);
		
		Pair<String, LineDetails> functionNameInfo = thisObject.new Pair<String, LineDetails>(functionName, lineDetails);
		
		return functionNameInfo;		
	}
	
	public static void checkAndAddExitLog(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		if(functionDataStack.isEmpty())
		{
			return;
		}
		
		if(lineDetails.isEntryLogAdded)
			return;
		
		LineDetails previousLineDetails = null;
		if(lineDetails.lineNum > 0)
			previousLineDetails = fileContent.get(lineDetails.lineNum - 1);
		
		FunctionData functionData = functionDataStack.peek();
		
		printLogs("\ncheckAndAddExitLog lineDetails.line: "+lineDetails.line);
		
		if(functionData.openBracesCount > 0)
		{
			functionData.openBracesCount += getValidOccurancesCount(lineDetails,fileContent,"{",0);
			functionData.openBracesCount -= getValidOccurancesCount(lineDetails,fileContent,"}",0);
			
			if(functionData.openBracesCount > 0) //function not yet done
			{
				//this is for return in between functions, based on some conditions. Or when throw happens
				if( isLineContainsValidStr(lineDetails, fileContent, "return;") || isLineContainsValidStr(lineDetails, fileContent, "return ") 
						|| isLineContainsValidStr(lineDetails, fileContent, "throw ") 
						|| lineDetails.line.trim().equals("return") ) 
				{
					int returnStrIndex = getValidIndexOf(lineDetails,fileContent,"return");
					int nextNonSpaceIndex = getNextNonSpaceValidIndex(lineDetails, fileContent, returnStrIndex+5);
					int prevNonSpaceIndex = getPrevNonSpaceValidIndex(lineDetails, fileContent, returnStrIndex);
					
					if((nextNonSpaceIndex != -1 && nextNonSpaceIndex == returnStrIndex+6 && lineDetails.line.charAt(nextNonSpaceIndex) != ';') || 
							(prevNonSpaceIndex != -1 && lineDetails.line.charAt(prevNonSpaceIndex) != '{')) //return word is part of some other identifier
					{
						return;
					}
					
					String exitExtraMetaData = "";
					if(isLineContainsValidStr(lineDetails, fileContent, "return"))
						exitExtraMetaData = "(reason: return) ";
					else if (isLineContainsValidStr(lineDetails, fileContent, "throw"))
						exitExtraMetaData = "(reason: throw) ";
					
					printLogs("\n==================== Exit log is added == in between function ======================\n");
					lineDetails.lineToInsertBefore.add(createIndentFromLine(lineDetails.line) + getExitLogStr(exitExtraMetaData+functionData.functionName));
					
					if(getValidOccurancesCount(lineDetails, fileContent, "return ") != 0)
					{
						functionData.isFunctionReturnAnything = true;		
					}
					
				}
				
				for(int i=0;i<lineDetails.lineToInsertAfter.size();i++)
				{
					String lineToAddAfter = lineDetails.lineToInsertAfter.get(i);
					
					String exitExtraMetaData = "";
					if(lineToAddAfter.trim().contains("return"))
						exitExtraMetaData = "(reason: return) ";
					else if (lineToAddAfter.trim().contains("throw"))
						exitExtraMetaData = "(reason: throw) ";
					
					if(lineToAddAfter.trim().startsWith("return;") || lineToAddAfter.trim().startsWith("return ") || lineToAddAfter.trim().startsWith("throw ")
							|| lineDetails.line.trim().equals("return")) 
					{
						printLogs("\n==================== Exit log is added == in between function, inside lineToInsertAfter ======================\n");
						
						lineDetails.lineToInsertAfter.add(i, createIndentFromLine(lineToAddAfter) + getExitLogStr(exitExtraMetaData+functionData.functionName));
						
						if(!lineToAddAfter.trim().startsWith("throw "))
							functionData.isFunctionReturnAnything = true;
						
						i++;
					}
				}
			}
			
			/*if(previousLineDetails!=null)
				printLogs("checkAndAddExitLog: previousLine: "+previousLineDetails.line);
			printLogs("checkAndAddExitLog: openBracesCount: "+functionData.openBracesCount + "\n");*/
			if(functionData.openBracesCount == 0) //last closing brace met for the function, so function is done
			{
				FunctionData tempFunctionData = functionDataStack.pop();
				printLogs("openBracesCount become 0, so popped function: '"+tempFunctionData.functionName+"' from function data stack");
				
				lineDetails.functionData = null;
				printLogs("openBracesCount become 0, so made functiondata null in line: "+lineDetails.line);
				
				//TODO add to go till last throw or return
				
				printLogs("checkAndAddExitLog wasLastStmtSwitch: "+functionData.wasLastStmtSwitch);
				
				ArrayList<LineDetails> fullStatementLines = getFullStatement(previousLineDetails, fileContent);
				
				boolean isValidEndOfFunction = true;
				
				for(LineDetails fullStatementLine : fullStatementLines)
				{
					if(!isValidLastFunctionLine(fullStatementLine, fileContent, functionData))
					{
						isValidEndOfFunction = false;
						break;
					}
				}
				
				if(!isValidLastFunctionLine(previousLineDetails, fileContent, functionData)) 
				{
					isValidEndOfFunction = false;
				}
				
				if(isValidEndOfFunction) 
				{
					printLogs("\n===================== Exit log is added, at the end of the function==========================\n");
					
					lineDetails.lineToInsertBefore.add(createIndentFromLine(lineDetails.line) + "    " + getExitLogStr(functionData.functionName));
				}
				
			}
		}
	}
	
	public static boolean isValidLastFunctionLine(LineDetails lineDetails, ArrayList<LineDetails> fileContent,FunctionData functionData)
	{
		if( !isLineContainsValidStr(lineDetails,fileContent, "return;") 
				&& !isLineContainsValidStr(lineDetails,fileContent, "return ")   //dont add exit log after the return statement
				&& !isLineContainsValidStr(lineDetails,fileContent, "throw ") //dont add exit log if the function throws at last line
				&& !functionData.isInfiniteLoop //dont add exit log if the function has infinite loop
				&& !functionData.isFunctionReturnAnything //dont add exit log if the function must return something
				&& !functionData.wasLastStmtSwitch  //dont add exit log if the function has switch and doesn't not break
				&& !functionData.isAlwaysExpThrownOut
				&& !lineDetails.line.trim().equals("return"))
		{
			return true;
		}
		return false;
	}
		
	public static ArrayList<LineDetails> getFullStatement(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		if(lineDetails == null)
			return null;
		
		printLogs("getFullStatement starts");
		
		ArrayList<LineDetails> fullStatementLines = new ArrayList<LineDetails>();
		
		fullStatementLines.add(lineDetails);
		
		printLogs("added line to fullStatementLines: "+lineDetails.line);
		
		if(lineDetails.lineNum > 0)
		{
			do
			{
				lineDetails = fileContent.get(lineDetails.lineNum-1);
				
				if(isLineContainsValidStr(lineDetails, fileContent, ";") 
						|| lineDetails.isEntryLogAdded 
						|| lineDetails.isExitLogAdded)
					break;
				
				if(isLineContainsValidStr(lineDetails, fileContent, "throw") || isLineContainsValidStr(lineDetails, fileContent, "return"))
				{
					fullStatementLines.add(lineDetails);
					printLogs("added line to fullStatementLines: "+lineDetails.line);
					break;
				}
				
				fullStatementLines.add(lineDetails);
				printLogs("added line to fullStatementLines: "+lineDetails.line);
				
			}while(lineDetails.lineNum > 0);
			
		}
		
		Collections.reverse(fullStatementLines);
		
		printLogs("\n\n !!!!!!!!!!!! fullStatementLines: "+fullStatementLines);
		
		return fullStatementLines;
	}
	
	public static boolean isLineContainsValidStr(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr)
	{
		return getValidOccurancesCount(lineDetails, fileContent, keyStr) > 0;
	}
	
	public static boolean insertLogTag(File file){
		boolean classFound = false;
		try
		{
			String filename = file.getName();
			String className = filename.replace(".java", "");
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			String line;
			int lineNo=0;

			while ((line = fReader.readLine()) != null) {
				lineNo++;
				if(line.contains("class "+className+" ") || line.endsWith("class "+className) || line.contains("class "+className+"<"))
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
			{
				insertLineToFile(file,lineNo + 1 ,"public static final String ENTRY_EXIT_TAG = \""+ className +"\";");
				insertLineToFile(file,2,"import "+IMPORT_FOR_LOGGER+";");
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return classFound;
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