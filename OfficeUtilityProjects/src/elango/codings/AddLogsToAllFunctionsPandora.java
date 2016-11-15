package elango.codings;
import java.io.*;
import java.io.IOException;
import java.util.*;

public class AddLogsToAllFunctionsPandora {
	public static int firstFuncLineNum = 0;

	public static void main(String[] args) {
		File currentDir = new File("D:\\PandoraLink\\workspace\\src\\src"); 
		
		processFiles(currentDir);
	}

	public static void processFiles(File dir) {
			String[] filesToAddHeadersArr = {"Event.cpp","EventController.cpp","PandoraLinkAPI.cpp","PresentationControllerAPI.cpp"};
			Vector<String> fileNamesToAddHeaders = new Vector<String>(Arrays.asList(filesToAddHeadersArr));
			
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					//System.out.println("directory:" + file.getCanonicalPath());
					processFiles(file);
				} else {
					String filename = file.getName();
					if(filename.endsWith(".cpp"))
					{
						System.out.println("Modifying file :" + filename);
						updateFile(file);
						//System.exit(1);
					}
					
					System.out.println("file name is:" + filename);
					
					if(fileNamesToAddHeaders.contains(filename))
					{
						System.out.println("Modifying file :" + filename);
						insertLineToFile(file,1,"#include \"api/sys/tracesrv/pf/trace/src/private/HBTraceMacros.hpp\"");
						insertLineToFile(file,2,"#include \"api/sys/tracesrv/pf/trace/src/CTraceThread.hpp\"");
						insertLineToFile(file,3,"#include \"api/sys/tracesrv/pf/trace/src/CHBNullTracePersistence.hpp\"");
					}
				}
			}
	}
	
	public static void updateFile(File file)
	{
		try
		{
			firstFuncLineNum = 0;
			Vector<String> functionNames = new Vector<String>();
			BufferedReader fReader = new BufferedReader(new FileReader(file));
			String filename = file.getName();
			BufferedWriter fWriter = new BufferedWriter(new FileWriter(filename+"_tmp")); 
			String line,nextline,fullLine="";
			String scopedefStr="";
			int lineno=0;
			while ((line = fReader.readLine()) != null) {
				++lineno;
				//System.out.println("line ==> " + line);
				if(line.contains("TRC_SCOPE_DEF"))
				{
					firstFuncLineNum = (firstFuncLineNum == 0)?lineno:firstFuncLineNum;
					addScopeDefFunTofunctionNames(functionNames,line);
				}
				fWriter.write(line);
				fWriter.newLine();
				String fileNameWithoutExtension = filename.substring(0,filename.indexOf("."));
				if((line.contains(") {") || line.contains("){") || line.contains("::") ||  line.contains(fileNameWithoutExtension+"::"+fileNameWithoutExtension))
				&& !line.contains(";") && !line.contains("main") 
				&& !line.contains("if (") && !line.contains("while (") && !line.contains("for (") && !line.contains("switch (")
				&& !line.contains("if(") && !line.contains("while(") && !line.contains("for(") && !line.contains("switch(") 
				&& !line.contains("~") && !line.contains("#define") && !line.contains("catch") && !line.contains("ConnectionManager::connectToApp") &&
				!line.contains("PandoraLinkConnector::crc16_ccitt_table")
				)
				{
					firstFuncLineNum = (firstFuncLineNum == 0)?lineno:firstFuncLineNum;
					if(!line.endsWith(")") && !line.endsWith("{"))
					{
						fullLine +=line;
						continue;
					}
					if(line.endsWith(")"))
					{
						nextline = fReader.readLine();
						while(!nextline.startsWith("{"))
						{
							fWriter.write(nextline);
							fWriter.newLine();
							fullLine +=nextline;
							nextline = fReader.readLine();
						}
						if(nextline.startsWith("{"))
						{
							fWriter.write(nextline);
							fWriter.newLine();
						}
					}
					fullLine +=line;
					while ((nextline = fReader.readLine()) != null) {
						if(nextline != null && !nextline.isEmpty())
						{							
							if(nextline.startsWith("{"))
							{
								fWriter.write(nextline);
								fWriter.newLine();
								continue;
							}
							else if(nextline.contains("/*"))
							{
								while(!nextline.contains("*/"))
								{
									fWriter.write(nextline);
									fWriter.newLine();
									nextline = fReader.readLine();
								}
								fWriter.write(nextline);
								fWriter.newLine();
								continue;
							}
							else if(nextline.contains("TRC_SCOPE"))
							{
								fWriter.write(nextline);
								fWriter.newLine();
								break;
							}
							else
							{
								System.out.println("fullLine ==> " + fullLine);
								if(fullLine.startsWith("//") || fullLine.startsWith("/*") )
									break;
								int openBracketIndex = fullLine.lastIndexOf("(");
								if(openBracketIndex == -1)
									break;
								int scopeOperIndex = fullLine.lastIndexOf("::");
								while( scopeOperIndex > -1 && scopeOperIndex > openBracketIndex)
								{
									scopeOperIndex = fullLine.substring(0,scopeOperIndex-2).lastIndexOf("::");
								}
								String functionName;
								if(fullLine.contains(fileNameWithoutExtension+"::"+fileNameWithoutExtension))
									functionName = fileNameWithoutExtension;
								else
									functionName = (scopeOperIndex > -1)?(fullLine.substring(scopeOperIndex+2,openBracketIndex)):(fullLine.substring(getFunStart(fullLine,openBracketIndex),openBracketIndex));
								
								if(functionName.equals("operator="))
									functionName="operatorequal";
								
								String foldername = file.getParentFile().getName();
								System.out.println("functionName: "+functionName+" "+foldername);
								String scopestr = foldername.trim()+", "+fileNameWithoutExtension.trim()+", "+functionName.trim();
								fWriter.write("    TRC_SCOPE("+scopestr+");"); 
								fWriter.newLine();
								
								/* 
							   Enumeration e=functionNames.elements();
							   System.out.println("Function names present already :- "); 
							   while (e.hasMoreElements()) {         
									System.out.println("Number = " + e.nextElement());
							   }*/
							   //String defString = foldername+":"+fileNameWithoutExtension+":"+functionName;
								if(!functionNames.contains(scopestr))
								{
									functionNames.addElement(scopestr);
									scopedefStr += "TRC_SCOPE_DEF("+scopestr+");"+System.lineSeparator();
								}
								
								fWriter.write(nextline);
								fWriter.newLine();
								break;
							}
						}
					}
					fullLine="";
				}
			}
			fWriter.flush();
			fWriter.close();
			fReader.close();
			file.delete();
			File temp_file = new File(filename+"_tmp"); 
			boolean result = temp_file.renameTo(file);			
			//System.out.println("renamed: "+result);
			writeScopeDefToFile(file,scopedefStr);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static int getFunStart(String line,int openBracketIndex)
	{ 
		int startIndex = openBracketIndex;
		while(startIndex >= 1  && line.charAt(startIndex--) != ' ')
		{}
	
		return startIndex+2;
	}
	
	public static void writeScopeDefToFile(File file,String scopedefStr)
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
				if(firstFuncLineNum == lineNum )
				{
					fWriter.write(scopedefStr);
					fWriter.newLine();
				}
				fWriter.write(line);
				fWriter.newLine();
			}
			
			fWriter.close();
			fReader.close();
			
			file.delete();
			File temp_file = new File(filename+"_tmp"); 
			boolean result = temp_file.renameTo(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void addScopeDefFunTofunctionNames(Vector<String> functionNames,String line)
	{ 
		String scopedefStr = line.substring(line.indexOf("(")+1,line.lastIndexOf(")"));
				//System.out.println("functionName: "+functionName);
		functionNames.addElement(scopedefStr.trim());
	}
	
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
			boolean result = temp_file.renameTo(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}