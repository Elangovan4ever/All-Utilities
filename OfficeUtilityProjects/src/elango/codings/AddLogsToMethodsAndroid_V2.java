package elango.codings;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.sound.sampled.Line;

public class AddLogsToMethodsAndroid_V2 {
	
	private static int testLocalFiles = 1;
	private static int enableLogs = 1;
	
	//public static String LOGGER_TYPE_STR = "Log.w";
	public static String LOGGER_TYPE_STR = "Slog.w";
	
	//public static String IMPORT_FOR_LOGGER = "android.util.Log";
	public static String IMPORT_FOR_LOGGER = "android.util.Slog";
	
	//public static final String[] PATHS_TO_ADD_LOGS = {"Z:\\workspace\\ROW_MY18\\frameworks\\base\\services\\java","Z:\\workspace\\ROW_MY18\\frameworks\\base\\core\\java"};
	//public static final String[] PATHS_TO_ADD_LOGS = {"Z:\\workspace\\ROW_MY18\\packages\\inputmethods\\LatinIME\\java\\src\\com\\android\\inputmethod\\annotations"};
	//public static final String[] PATHS_TO_ADD_LOGS = {"/home/emanickam/workspace/ROW_MY18/packages/inputmethods/LatinIME/java"};
	public static final String[] PATHS_TO_ADD_LOGS = {"/data/work/emanickam/workspace/ROW_MY18/frameworks/base/core/java",
			"/data/work/emanickam/workspace/ROW_MY18/frameworks/base/packages",
			"/data/work/emanickam/workspace/ROW_MY18/frameworks/base/services"};
	public static final String[] LOCAL_PATHS_TO_ADD_LOGS = {getProjectDirectory()+ "\\resources"};
	
	public static final String[] validMatcherStringsArr = {};
	public static final String[] validEndMatcherStringsArr = {") {" , "){", ")" };
	public static final String[] nonValidMatcherStringsArr = {";","=","new ","\"","+","?","@interface"};
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
		
		public int singleLineCommentIndex = -1;
		public ArrayList<Pair<Integer,Integer>> multiLineCommentsIndexes = new ArrayList<Pair<Integer,Integer>>();
		
		public ArrayList<Pair<Integer,Integer>> quotationIndexes = new ArrayList<Pair<Integer,Integer>>();
		
		public ArrayList<String> lineToInsertBefore = new ArrayList<String>();
		public ArrayList<String> lineToInsertAfter = new ArrayList<String>();
		public FunctionData functionData = null;
		
		public void print()
		{
			printLogs("\nLineDetails.line: "+lineNum+" "+line);
			printLogs("LineDetails.onlyCommentLine: "+onlyCommentLine);
			printLogs("LineDetails.multiLineCommentOpen: "+multiLineCommentOpen);
			printLogs("LineDetails.singleLineCommentIndex: "+singleLineCommentIndex);
			printLogs("LineDetails.multiLineCommentsIndexes: "+multiLineCommentsIndexes);
			printLogs("LineDetails.quotationIndexes: "+quotationIndexes);
			printLogs("LineDetails.lineToInsertBefore: "+lineToInsertBefore);
			printLogs("LineDetails.lineToInsertAfter: "+lineToInsertAfter);
			printLogs("LineDetails.className: "+className);
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
	
	private static int SPLIT_TO_SEPARATE_LINE = 1;
	private static int DONT_SPLIT_TO_SEPARATE_LINE = 2;
	
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
			countFilesInPath(path,".java");
		}
		
		printLogs("Total files count to be processed: "+totalFilesToProcess,true);
		
		for (String pathStr : pathArrToAddLogs)
		{
			File path = new File(pathStr);
			processFiles(path);
		}
		
	}
	
	public static void addLogsToFile(File file)
	{
		String filename = file.getName();
		if(filename.endsWith(".java"))
		{
			filesProcessedCount++;
			printLogs("Processing "+filesProcessedCount + " files of "+totalFilesToProcess+", file name: "+filename,true);
			
			ArrayList<LineDetails> fileContentOriginal = new ArrayList<LineDetails>();
			ArrayList<LineDetails> fileContentModified = new ArrayList<LineDetails>();
			
			loadFileContentIntoArrayList(file, fileContentOriginal);
			
			//displayListContent(fileContentOriginal);
			
			addMethodLogs(file, fileContentOriginal, fileContentModified);
			
			//displayListContent(fileContentModified);
			
			rewriteFile(file,fileContentModified); 
			
			printLogs("Processed "+filesProcessedCount + " files of "+totalFilesToProcess+", file name: "+filename,true);
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
			
			/*file.delete();
			File temp_file = new File(filename+"_tmp"); 
			temp_file.renameTo(file);*/
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
			if(line.charAt(i) != ' ' || line.charAt(i) != '\t')
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
		
		if(keyStrLastIndex  == lineLastIndex)
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
		
		Pair<LineDetails, Integer> matchingPair = thisObject.new Pair<LineDetails, Integer>();
		
		if(fileContent.isEmpty())
			return null;
		
		Stack<String> stack = new Stack<String>();
		
		if(keyStr.equals("(") || keyStr.equals("{"))
			currentIndex += 1;
		else if(keyStr.equals(")") || keyStr.equals("}")) 
			currentIndex -= 1;
		
		stack.push(keyStr);
		
		int i=0;
		
		for( i = lineDetails.lineNum ; !stack.empty() ; )
		{
			if((keyStr.equals("(") || keyStr.equals("{")) && i >= fileContent.size() )
					break;
			else if((keyStr.equals(")") || keyStr.equals("}")) && i < 0) 	break;
			
			lineDetails = fileContent.get(i);		
			printLogs("getMatchingPair trying to find matching "+oppositePairStr+" for "+keyStr+" in line: "+lineDetails.line);

			ArrayList<Pair<Integer,String>> bracketIndexes = new ArrayList<Pair<Integer,String>>();
			
			ArrayList<Integer> oppositePairStrIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails, fileContent, oppositePairStr);
			ArrayList<Integer> keyStrIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails, fileContent, keyStr);
			
			printLogs("getMatchingPair oppositePairStrIndexes: "+oppositePairStrIndexes);
			printLogs("getMatchingPair keyStrIndexes: "+keyStrIndexes);
			
			for(int j=0; j < oppositePairStrIndexes.size();j++)
			{
				if((keyStr.equals("(") || keyStr.equals("{")) && oppositePairStrIndexes.get(j) <= currentIndex)
					continue;
				else if((keyStr.equals(")") || keyStr.equals("}")) && oppositePairStrIndexes.get(j) >= currentIndex)
					break;
				
				Pair<Integer,String> pair = thisObject.new Pair<Integer,String>(oppositePairStrIndexes.get(j),oppositePairStr);
				bracketIndexes.add(pair);
			}
			
			for(int j=0;j < keyStrIndexes.size();j++)
			{
				if((keyStr.equals("(") || keyStr.equals("{")) && keyStrIndexes.get(j) <= currentIndex)
					continue;
				else if((keyStr.equals(")") || keyStr.equals("}")) && keyStrIndexes.get(j) >= currentIndex)
					break;
				
				Pair<Integer,String> pair = thisObject.new Pair<Integer,String>(keyStrIndexes.get(j),keyStr);
				bracketIndexes.add(pair);
			}
			
			Collections.sort(bracketIndexes, new Comparator<Pair<Integer,String>>() {
		        @Override public int compare(Pair<Integer,String> p1, Pair<Integer,String> p2) {
		            return p1.first - p2.first;
		        }
		    });
			
			printLogs("getMatchingPair bracketIndexes: "+bracketIndexes);
			
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
					printLogs("getMatchingPair pushing "+bracketIndexes.get(j).second+" from index: "+bracketIndexes.get(j).first);
				}
				else
				{
					stack.pop();
					if(stack.empty())
					{
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
			
			currentIndex = lineDetails.line.length();
			
			if(keyStr.equals("(") || keyStr.equals("{") )
				i++;
			else if(keyStr.equals(")") || keyStr.equals("}") ) 
				i--;
		}
			
		return null;
	}
	
	public static void checkAndModifyConditionalStmts(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr)
	{
		printLogs("elango checkAndModifyConditionalStmts called: ");
		int openStrIndex = getValidIndexOf(lineDetails, fileContent, keyStr);
		if(openStrIndex == -1)
			return;
		
		printLogs("elango openStrIndex: "+openStrIndex);
		
		int openBracketIndex = getValidIndexOf(lineDetails, fileContent, "(", openStrIndex+1);
		if(openBracketIndex == -1)
			return;
		
		printLogs("elango openBracketIndex: "+openBracketIndex);
		
		Pair<LineDetails, Integer> closeBracketDetails = getMatchingPair(lineDetails, fileContent, "(", openBracketIndex);
		
		printLogs("elango closeBracketDetails.first.line: "+closeBracketDetails.first.line);
		
		if(closeBracketDetails != null)
		{
			printLogs("elango nextNonSpaceValidIndex"+getNextNonSpaceValidIndex(lineDetails, fileContent, closeBracketDetails.second));
			
			if(getNextNonSpaceValidIndex(lineDetails, fileContent, closeBracketDetails.second) != -1)
			{
				String tempLine = closeBracketDetails.first.line;
				closeBracketDetails.first.line = tempLine.substring(0, closeBracketDetails.second+1 );
				closeBracketDetails.first.lineToInsertAfter.add("{");
				closeBracketDetails.first.lineToInsertAfter.add(tempLine.substring(closeBracketDetails.second+1));
				closeBracketDetails.first.lineToInsertAfter.add("}");
				printLogs("elango closeBracketDetails.first.line: "+closeBracketDetails.first.line);
			}
			else
			{
				if(closeBracketDetails.first.lineNum < fileContent.size() - 1)
				{
					LineDetails nextLine = fileContent.get(closeBracketDetails.first.lineNum+1);
					
					printLogs("before 1 elango nextLine: "+nextLine);
					
					while( nextLine.onlyCommentLine && nextLine.lineNum < fileContent.size() - 1) //bypass if any comment lines
					{
						nextLine = fileContent.get(nextLine.lineNum+1);
					}
					
					printLogs("before 2 elango nextLine: "+nextLine);
					
					if(!isLineStartsWith(nextLine, fileContent, "{"))
					{
						nextLine.lineToInsertBefore.add("{");
						nextLine.lineToInsertAfter.add("}");
					}
					
					printLogs("after 2 elango nextLine: "+nextLine);
				}
			}
			
		}
		
	}
	
	public static LineDetails modifyLineWithFunctionData(LineDetails lineDetails, ArrayList<LineDetails> fileContentOriginal, String className)
	{
		String line = lineDetails.line;
		
		LineDetails previousLineDetails = null;
		if(lineDetails.lineNum > 0)
			previousLineDetails = fileContentOriginal.get(lineDetails.lineNum - 1);
		
		if(previousLineDetails != null)
		{
			lineDetails.functionData = previousLineDetails.functionData;
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
		
		if(!mustNotContainCondToBeFunction(lineDetails, fileContentOriginal)) //some conditional statements are there in the line
		{
			checkAndModifyConditionalStmts(lineDetails, fileContentOriginal,"if");
		}
		
		checkAndAddExitLog(lineDetails, fileContentOriginal);
		
		if(lineDetails.className.isEmpty() && line.contains("class "+className+" ") || line.endsWith("class "+className) || line.contains("class "+className+"<"))
		{
			ListIterator<LineDetails> tempIter = fileContentOriginal.listIterator();
			LineDetails tempLineDetails = lineDetails;
			
			while(!isLineEndsWith(tempLineDetails, fileContentOriginal, "{") && tempIter.hasNext())
			{
				tempLineDetails = tempIter.next();
			}
			
			tempLineDetails.className = className; //to avoid further entering this condition
			tempLineDetails.lineToInsertAfter.add("public static final String ENTRY_EXIT_TAG = \""+ className +"\";");
		}
		
		
		
		if(mustContainToBeFunction(lineDetails, fileContentOriginal) && mustEndWithToBeFunction(lineDetails, fileContentOriginal)
				&& mustNotContainToBeFunction(lineDetails, fileContentOriginal) && mustNotContainCondToBeFunction(lineDetails, fileContentOriginal))
		{		
			printLogs("\n/*== may be function ==*/");
			LineDetails openBraceLineDetails = lineDetails;
			
			if(isLineEndsWith(lineDetails, fileContentOriginal, ")"))
			{				
				printLogs("line ends with close bracket");
				for(int j= openBraceLineDetails.lineNum+1 ; j<fileContentOriginal.size(); j++)
				{
					openBraceLineDetails = fileContentOriginal.get(j);
					if(isLineStartsWith(openBraceLineDetails, fileContentOriginal, "{"))
					{
						break;
					}
				}
			}
			
			if(!functionDataStack.isEmpty())
			{
				FunctionData lastfunctionData = functionDataStack.peek();
				
				lastfunctionData.openBracesCount = (lastfunctionData.openBracesCount > 0)? lastfunctionData.openBracesCount - 1: lastfunctionData.openBracesCount ;
				
				printLogs("Storing last functional data's openBracesCount as : "+lastfunctionData.openBracesCount);
			}
				
			String functionName = getFunctionName(lineDetails, fileContentOriginal);
			
			printLogs("extracted functionName: "+functionName);
			
			FunctionData functionData = thisObject.new FunctionData();
			functionData.functionName = functionName;
			functionData.openBracesCount = 1;
			functionData.isFunctionReturnAnything = false;
			functionData.isInfiniteLoop = false;
			
			functionDataStack.push(functionData);
			
			lineDetails.functionData = functionData;
			
			openBraceLineDetails.lineToInsertAfter.add(getEntryLogStr(lineDetails.functionData.functionName));
			
			printLogs("\n Entry log is added");
			
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
				
				//printLogs("\n\nRead from file - line: "+line);
				
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
				ArrayList<Integer> multiLineCommentCloseIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails,fileContent,"*/");
				ArrayList<Integer> quotationIndexes = getIndexesOutMultiLineCmntQuotes(lineDetails,fileContent,"\"");
				
				/*printLogs("multiLineCommentOpenIndexes: "+multiLineCommentOpenIndexes);
				printLogs("multiLineCommentCloseIndexes: "+multiLineCommentCloseIndexes);
				printLogs("quotationIndexes: "+quotationIndexes);*/
				
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
			if(!(keyStr.equals("\"") && line.charAt(keyStrIndex-1) == '\\' ))
			{
				Pair<Integer,String> pair = thisObject.new Pair<Integer,String>(keyStrIndex, keyStr);
				keyStrIndexes.add(pair);
			}
			keyStrIndex += keyStr.length();
		}
		
		return keyStrIndexes;
	}
	
	public static ArrayList<Integer> getIndexesOutMultiLineCmntQuotes(LineDetails lineDetails, ArrayList<LineDetails> fileContent, String keyStr)
	{
		ArrayList<Integer> resultedKeyStrIndexes = new ArrayList<Integer>();
		
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
			indexesOfExcluders.addAll(getOccurancesIndexes(line, "*/", index));
			indexesOfExcluders.addAll(getOccurancesIndexes(line, "\"", index));
			
			Collections.sort(indexesOfExcluders, new Comparator<Pair<Integer,String>>() {
		        @Override public int compare(Pair<Integer,String> p1, Pair<Integer,String> p2) {
		            return p1.first - p2.first;
		        }
		    });
			
			//printLogs("getIndexesOutMultiLineCmntQuotes: before - indexesOfExcluders: "+indexesOfExcluders);
			//Keep only valid excluders like outside of each other.
			ListIterator<Pair<Integer,String>> iter = indexesOfExcluders.listIterator();
			int pos = 0;
			Pair<Integer,String> indexOfExluderPairPrev = null;
			while (iter.hasNext())  //DO NOT CHANGE THE ORDER OF THESE CONDITIONS
			{
				if(previousLineDetails != null && previousLineDetails.multiLineCommentOpen) 
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
						if(indexOfExluderPairPrev.second.equals(indexOfExluderPair.second))
						{
							iter.remove();
							break;
						}
					}
				    
				    if(indexOfExluderPair.second.equals("\""))
				    {
				    	while(iter.hasNext() && !iter.next().second.equals("\""))
				    	{
				    		iter.remove();
				    	}
				    }
				    
				    if(indexOfExluderPair.second.equals("//"))
				    {
 				    	indexesOfExcluders.subList(pos + 1, indexesOfExcluders.size()).clear();
				    	break;
				    }
				    
				    if(indexOfExluderPair.second.equals("/*"))
				    {
				    	while(iter.hasNext() && !iter.next().second.equals("*/"))
				    	{
				    		iter.remove();
				    	}
				    }
				    
				    pos++;
				    indexOfExluderPairPrev = indexOfExluderPair;
				}
			}
			
			//printLogs("getIndexesOutMultiLineCmntQuotes: after - indexesOfExcluders: "+indexesOfExcluders);
			
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
			URL u = AddLogsToMethodsAndroid_V2.class.getProtectionDomain().getCodeSource()
					.getLocation();
			File f = new File(u.toURI());
			projectDir = f.getParent().toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return projectDir;
	}

	public static void processFiles(File path) {
		try
		{
			if(path.isDirectory())
			{
				printLogs("Processing files from directory and its subdirectories : " + path.getCanonicalPath());
				File[] files = path.listFiles();
				for (File file : files) {
					if (file.isDirectory()) {
						processFiles(file);
					} else {
						addLogsToFile(file);
					}
				}
				printLogs("Completed files from directory and its subdirectories : " + path.getCanonicalPath());
			}
			else
			{
				addLogsToFile(path);
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
			if(keyStrIndexes.get(i) < fromIndex )
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
	
	public static String getFunctionName(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		String functionName = "";
		
		if(fileContent.isEmpty())
			return "";
		
		int currentIndex = getLastValidIndexOf(lineDetails, fileContent, ")");
		
		Pair<LineDetails, Integer> matchingPairData = getMatchingPair(lineDetails,fileContent,")", currentIndex);
		
		if(matchingPairData == null)
		{
			return "";
		}
		
		int lastNonSpaceIndex = matchingPairData.second;
		lineDetails = matchingPairData.first;
		
		printLogs("getFunctionName 1lastNonSpaceIndex: "+lastNonSpaceIndex);
		
		while((--lastNonSpaceIndex) >= 0 && (lineDetails.line.charAt(lastNonSpaceIndex) == ' ' || lineDetails.line.charAt(lastNonSpaceIndex) == '\t'))
		{
			//loop till we get a non space char or start of the line
		}
		
		printLogs("getFunctionName 2lastNonSpaceIndex: "+lastNonSpaceIndex);
		
		int lastIndexOfSpace = getLastValidIndexOf(lineDetails, fileContent, " ",lastNonSpaceIndex);
		
		printLogs("getFunctionName lastIndexOfSpace: "+lastIndexOfSpace);
		
		if(lastIndexOfSpace == -1)
		{
			lastIndexOfSpace = 0;	
		}

		functionName = lineDetails.line.substring(lastIndexOfSpace+1, lastNonSpaceIndex+1);
		printLogs("getFunctionName functionName: "+functionName);
		
		return functionName;		
	}
	
	public static void checkAndAddExitLog(LineDetails lineDetails, ArrayList<LineDetails> fileContent)
	{
		if(functionDataStack.isEmpty())
		{
			return;
		}
		
		LineDetails previousLineDetails = null;
		if(lineDetails.lineNum > 0)
			previousLineDetails = fileContent.get(lineDetails.lineNum - 1);
		
		FunctionData functionData = functionDataStack.peek();
		
		if(functionData.openBracesCount > 0)
		{
			functionData.openBracesCount += getValidOccurancesCount(lineDetails,fileContent,"{",0);
			functionData.openBracesCount -= getValidOccurancesCount(lineDetails,fileContent,"}",0);
			if(functionData.openBracesCount > 0) //function not yet done
			{
				//this is for return in between functions, based on some conditions. Or when throw happens
				if( isLineContainsValidStr(lineDetails, fileContent, "return;") || isLineContainsValidStr(lineDetails, fileContent, "return ") || isLineContainsValidStr(lineDetails, fileContent, "throw ") ) 
				{
					if((isLineContainsValidStr(lineDetails, fileContent, "return;") || isLineContainsValidStr(lineDetails, fileContent, "return ") ) 
							&& !isLineStartsWith(lineDetails, fileContent, "return"))
					{							
						int returnStrIndex = getValidIndexOf(lineDetails, fileContent, "return");
						if(!(lineDetails.line.charAt(returnStrIndex - 1) == ' ' || lineDetails.line.charAt(returnStrIndex - 1) == ' ' 
								|| lineDetails.line.charAt(returnStrIndex - 1) == '{' || lineDetails.line.charAt(returnStrIndex - 1) == '}' 
								|| lineDetails.line.charAt(returnStrIndex - 1) == ')'))
						{
							return;
						}
					}
					
					printLogs("\n--\nExit log is added, in between function\n--\n");
					lineDetails.lineToInsertAfter.add(getExitLogStr(functionData.functionName));
					
					if(getValidOccurancesCount(lineDetails, fileContent, "return ") != 0)
					{
						functionData.isFunctionReturnAnything = true;		
					}
					
				}
			}
			
			printLogs("\ncheckAndAddExitLog: line: "+lineDetails.line);
			if(previousLineDetails!=null)
				printLogs("checkAndAddExitLog: previousLine: "+previousLineDetails.line);
			printLogs("checkAndAddExitLog: openBracesCount: "+functionData.openBracesCount + "\n");
			if(functionData.openBracesCount == 0) //last closing brace met for the function, so function is done
			{
				printLogs("checkAndAddExitLog wasLastStmtSwitch: "+functionData.wasLastStmtSwitch);
				
				if( !isLineContainsValidStr(previousLineDetails,fileContent, "return;") && !isLineContainsValidStr(previousLineDetails,fileContent, "return ")   //dont add exit log after the return statement
						&& !isLineContainsValidStr(previousLineDetails,fileContent, "throw ") //dont add exit log if the function throws at last line
						&& !functionData.isInfiniteLoop //dont add exit log if the function has infinite loop
						&& !functionData.isFunctionReturnAnything //dont add exit log if the function must return something
						&& !functionData.isInfiniteLoop //dont add exit log if the function has infinite loop
						&& !functionData.wasLastStmtSwitch)  //dont add exit log if the function has switch and doesn't not break
				{
					printLogs("\n--\nExit log is added, at the end of the function\n--\n");
					lineDetails.lineToInsertAfter.add(getExitLogStr(functionData.functionName));
				}
				
				functionDataStack.pop();
			}
		}
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