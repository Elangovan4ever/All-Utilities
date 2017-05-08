package elango.codings;
import java.io.*;
import java.net.URL;
import java.util.*;

public class AddLogsToMethodsAndroid {
	public static int firstFuncLineNum = 0;
	
	public static final String[] PATHS_TO_ADD_LOGS = {"Z:\\workspace\\ROW_MY18\\frameworks\\base\\services\\core\\java"};
	
	public static final String ENTRY_LOG_STR = "Slog.w(TAG,\"entry: \" + "
			+ "Thread.currentThread().getStackTrace()[2].getMethodName()+\"() \"+"
			+ "Thread.currentThread().getStackTrace()[2].getClassName()+\":\"+"
			+ "Thread.currentThread().getStackTrace()[2].getLineNumber() );";
	
	public static final String EXIT_LOG_STR = "Slog.w(TAG,\"exit: \" + "
			+ "Thread.currentThread().getStackTrace()[2].getMethodName()+\"() \"+"
			+ "Thread.currentThread().getStackTrace()[2].getClassName()+\":\"+"
			+ "Thread.currentThread().getStackTrace()[2].getLineNumber() );";
	
	public static final String[] validMatcherStringsArr = {};
	public static final String[] validEndMatcherStringsArr = {") {" , "){" };
	public static final String[] nonValidMatcherStringsArr = {";","=","new "};
	public static final String[] condStmtMatcherStringsArr = {"if (","if(","while (","while(","for (","for(",
			"switch (","switch(","catch","synchronized"};
	public static final String RETURNSTR="return"; 
	
	private static int openBracesCount = 0;
	private static boolean isInsideFunction = false;
	private static boolean isFunctionReturnAnything = false;
	

	public static void main(String[] args) {
		
		//String projectDir = getProjectDirectory();
		//File sourceFolder = new File(projectDir + "\\resources");
		
		for (String pathStr : PATHS_TO_ADD_LOGS)
		{
			File path = new File(pathStr);
			processFiles(path);
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
					//System.out.println("directory:" + file.getCanonicalPath());
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
			System.out.println("Modifying file :" + filename);
			removeComments(file);
			bringBracketAndBracesTogether(file);
			makeReturnStmtsSingleLine(file);
			makeConditionalStmtsSingleLine(file);
			updateFile(file);
			insertImportStmts(file);
			System.out.println("Modified file :" + filename);
		}					
	}
	
	public static int countOccurances(String str, String key)
	{ 
		int lastIndex = 0;
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
				boolean notAComment = false;
				
				for(int i=0; i<commentStrCount && !notAComment; i++){
					
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
		int closeCommentsCount = countOccurances(line,"*/");
		
		for(int i=0; i< closeCommentsCount; i++)
		{
			int openCommentIndex =  line.indexOf("/*");
			if(openCommentIndex != -1)
			{
				int closeCommentIndex =  line.indexOf("*/", openCommentIndex);
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
					if(line.contains("/*"))
					{
						if(!line.startsWith("/*")) //only one /* is there and it is starting, this means no uncomment text present
						{
							line = line.substring(0, line.indexOf("/*") - 1); // since only one open comment is left, we take the uncommented string and write
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
				
				if(line.contains(RETURNSTR))
				{
					do
					{
						singleReturnStmt += line.trim();
					}while(!line.trim().endsWith(";") && ((line = fReader.readLine()) != null));
					
					if(!singleReturnStmt.trim().startsWith(RETURNSTR)){
						
						fWriter.write(createIndentFromLine(previousLine) + singleReturnStmt.substring(0, singleReturnStmt.indexOf(RETURNSTR)-1));
						fWriter.newLine();
						
						singleReturnStmt = singleReturnStmt.substring(singleReturnStmt.indexOf(RETURNSTR));
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
		try
		{
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			BufferedWriter fWriter = new BufferedWriter(new FileWriter(filename+"_tmp"));
			String line = "";
			String singleCondStmt = "";
			int openBracketCount = 0;
	
			while ((line = fReader.readLine()) != null) {
				
				singleCondStmt = "";
				openBracketCount = 0;
				
				if(line != null && line.trim().isEmpty())
				{
					continue;
				}
				
				if(!mustNotContainCondToBeFunction(line))
				{
					String spaces = createIndentFromLine(line);
					do
					{
						singleCondStmt += line.trim();
						openBracketCount += countOccurances(line,"(");
						openBracketCount -= countOccurances(line,")");
						
						if(openBracketCount == 0 )
						{
							break;
						}
						
					}while((line = fReader.readLine() ) != null);
					
					line = spaces + singleCondStmt;
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
		/*System.out.println("shouldContainToBeFunction(line): "+shouldContainToBeFunction(line));
		System.out.println("shouldEndWithToBeFunction(line): "+shouldEndWithToBeFunction(line));
		System.out.println("mustNotContainToBeFunction(line): "+mustNotContainToBeFunction(line));
		System.out.println("mustNotContainCondToBeFunction(line): "+mustNotContainCondToBeFunction(line));*/
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
				//System.out.println("lineno: "+lineno+", line ==> " + line);
				
				if(line != null && line.trim().isEmpty()) //to avoid empty lines
				{
					continue;
				}
				
				//to add the exit log
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
					openBracketCount = countOccurances(line,"(");
				}
				
				if(openBracketCount > 0)
				{
					openBracketCount -= countOccurances(line,")");
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
				
				//System.out.println(" canBeAFunction(line): " + canBeAFunction(line));
				if (canBeAFunction(line))
				{
					//System.out.println(" shouldEndWithToBeFunction(line): " + shouldEndWithToBeFunction(line));
					if(shouldEndWithToBeFunction(line))
					{
						String nextLine = fReader.readLine(); //need to add log only after the super class method call
						lineno++;
						//System.out.println("lineno: "+lineno+", nextLine ==> " + nextLine);
						if(nextLine != null && nextLine.trim().startsWith("super") )
						{
							fWriter.write(nextLine);
							fWriter.newLine();
						}
						openBracesCount = 1;
						isInsideFunction = true;
						isFunctionReturnAnything = false;
						indentationSpaces = createIndentFromLine(line);
						String entryLogStr = indentationSpaces + "    " + ENTRY_LOG_STR;
						fWriter.write(entryLogStr);
						fWriter.newLine();
						
						openBracesAdded = checkAndAddExitLog(fWriter, nextLine, previousLine, indentationSpaces);
						previousLine = nextLine;
						
						if(nextLine != null && !nextLine.trim().startsWith("super") )
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
	
	public static boolean checkAndAddExitLog(BufferedWriter fWriter, String line, String previousLine, String indentationSpaces)
	{
		boolean openBracesAdded = false;
		try
		{
			if(isInsideFunction == true && openBracesCount > 0)
			{
				openBracesCount += countOccurances(line,"{");
				openBracesCount -= countOccurances(line,"}");
				if(openBracesCount > 0) //function not yet done
				{
					if(line.contains(RETURNSTR)) //this is for return in between functions, based on some conditions
					{
						if(previousLine.trim().startsWith("if") && !previousLine.trim().endsWith("{")) //we need to add braces for 'if' stmts without braces
						{
							fWriter.write(indentationSpaces+"{");
							fWriter.newLine();
							openBracesAdded = true;
						}
						
						fWriter.write(indentationSpaces + EXIT_LOG_STR);
						fWriter.newLine();
						
						isFunctionReturnAnything = true;
					}
				}
				if(openBracesCount == 0) //last closing brace met for the function, so function is done
				{
					isInsideFunction = false;
					
					if(!previousLine.contains(RETURNSTR) && !isFunctionReturnAnything) //dont add exit log after the return statement
					{
						fWriter.write(indentationSpaces + EXIT_LOG_STR);
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
			insertLineToFile(file,2,"import "+importPackage);
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