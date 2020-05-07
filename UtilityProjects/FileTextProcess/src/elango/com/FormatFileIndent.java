package elan.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class FormatFileIndent {

	public static void main(String[] args) {
		FormatFileIndent obj = new FormatFileIndent();
		obj.formatIndent();
	}

	public void formatIndent() {
		try {

			String fileName = "D:\\Ongoing\\Unity tutorial index\\unityIndex_test.txt";

			File f = new File(fileName);
			if (!f.exists() || f.isDirectory()) {
				System.out.println("File is not exist, Give valid file name");
				System.out.println("exiting");
				System.exit(0);
			}

			Vector<LineDetails> fileLines = new Vector<LineDetails>();
			readFileLinesIntoVector(fileName, fileLines);

			String outputFileName = "D:\\Ongoing\\Unity tutorial index\\unityIndex_out.txt";
			BufferedWriter fWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFileName), "UTF-8"));
			
			
			Integer totalLines = fileLines.size();
			for (int i=1; i<totalLines; i++){
				Integer difference = fileLines.get(i).orignalLevel - fileLines.get(i-1).orignalLevel;
				System.out.println("checking line: "+fileLines.get(i).line+", difference: "+difference);
				if (difference > 1){
					for ( int j=i; (j<totalLines) && (fileLines.get(j).orignalLevel >= fileLines.get(i).orignalLevel); j++){
						fileLines.get(j).modifiedLevel = fileLines.get(j).modifiedLevel - (difference-1);
						System.out.println("modified line: line: "+fileLines.get(j).line +", modified orignalLevel: "+fileLines.get(j).orignalLevel);
					}
				}
			}

			String line;
			for (int i=0; i<totalLines; i++){				
				line = fileLines.get(i).line;
				line = line.replaceAll("\t", "");
				String tabs = "";
				for(int j=0; j<fileLines.get(i).modifiedLevel; j++) {
					tabs += "\t";
				}
				
				line = tabs.concat(line);
				System.out.println("writing line: "+line);
				fWriter.write(line);
				fWriter.newLine();				
			}
			
			fWriter.flush();
			fWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	class LineDetails{
		String line;
		Integer orignalLevel;
		Integer modifiedLevel;
		
	}

	private boolean isLineEmpty(String line) {
		return (line.isEmpty() || line.trim().equals(""));
	}

	private void readFileLinesIntoVector(String fileName, Vector<LineDetails> fileLines) {
		try {
			System.out.println("fileName: " + fileName);
			File file = new File(fileName);
			BufferedReader fReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = "";
			while ((line = fReader.readLine()) != null) {
				if(isLineEmpty(line))
					continue;
				LineDetails lineDetails = new LineDetails();
				lineDetails.line = line;
				lineDetails.modifiedLevel = lineDetails.orignalLevel = line.length() - line.replaceAll("\t", "").length();
				fileLines.add(lineDetails);
			}
			fReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//asdadas
	
	/*
	 * 
	D:\Ongoing\Unity tutorial index\ unityIndex_test.txt
		Unity's interface
				The Project window
				The Scene view
						Scene view navigation
							Picking and selecting GameObjects
								Positioning GameObjects
									Grid snapping
				Some sub section
					View section
						Run with me
			Sample 2nd level
		Sample 1st level
			another sub heading

	D:\Ongoing\Unity tutorial index\ unityIndex_out.txt
		Unity's interface
			The Project window
			The Scene view
				Scene view navigation
					Picking and selecting GameObjects
						Positioning GameObjects
							Grid snapping
			Some sub section
				View section
					Run with me
			Sample 2nd level
		Sample 1st level
			another sub heading

	*/
}


