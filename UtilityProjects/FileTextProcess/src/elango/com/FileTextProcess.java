package elango.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class FileTextProcess {

	private static final int COMPR_EQUAL = 0;
	private static final int COMPR_LESS = 1;
	private static final int COMPR_GREATER = 2;
	private static final int SORT_ASC = 1;
	private static final int SORT_DESC = 2;

	// private static BufferedWriter gLogWriter;
	private static Logger logger;
	private static String LOGFILENAME = "";
	public static final int FILE_SIZE = 1024 * 1024 * 10;
	public static final int NUM_OF_LOG_FILES = 20;
	private static long logLineNum = 0;

	public static void main(String[] args) {

		String projectDir = getProjectDirectory();

		LOGFILENAME = projectDir + "\\log\\"
				+ FileTextProcess.class.getSimpleName() + ".log";

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

		// actual processing
		String fileName = projectDir + "\\res\\content.txt";
		if (args.length > 1 && args[1].isEmpty())
			fileName = args[1];

		File f = new File(fileName);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("File is not exist, Give valid file name");
			System.exit(0);
		}

		while (true) {
			log("used file : " + fileName);
			switch (getMenuOption()) {
			case 0:
				System.exit(0);

			case 1:
				deleteBlankLines(fileName);
				break;
			case 2:
				deleteDuplicateLines(fileName);
				break;
			case 3:
				deleteLinesContaining(fileName);
				break;
			case 4:
				deleteLinesByLength(fileName, COMPR_EQUAL);
				break;
			case 5:
				deleteLinesByLength(fileName, COMPR_GREATER);
				break;
			case 6:
				deleteLinesByLength(fileName, COMPR_LESS);
				break;
			case 7:
				sort(fileName, SORT_ASC);
				break;
			case 8:
				sort(fileName, SORT_DESC);
				break;
			case 9:
				findSimilar(fileName);
				break;
			case 10:
				deleteSimilarLines(fileName);
				break;
			case 11:
				removeDotSpaceAtEnd(fileName);
				break;
			default:
				System.out.println("Invalid option, Try again");
				break;
			}
		}

	}

	public static int getMenuOption() {
		int option = 0;
		try {
			System.out.println();
			System.out.println("========\nMENU\n========");
			System.out.println("0. Exit");
			System.out.println("1. Delete blank lines");
			System.out.println("2. Delete duplicate lines");
			System.out.println("3. Delete lines containing key");
			System.out.println("4. Delete lines having length equal to");
			System.out.println("5. Delete lines having length more than");
			System.out.println("6. Delete lines having length less than");
			System.out.println("7. Sort file in ascending order");
			System.out.println("8. Sort file in descending order");
			System.out.println("9. Find almost similar lines(80% similar)");
			System.out.println("10. Delete almost similar lines(80% similar)");
			System.out.println("11. Remove dot and space at the end");
			System.out.print("Select any option[0-11]: ");

			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			option = Integer.parseInt(br.readLine());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return option;
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

	public static void deleteBlankLines(String fileName) {
		try {
			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename + "_tmp"), "UTF-8"));

			String line;
			int linecount = 0;
			int lineno = 0;
			log("Deleted line numbers : start");
			for (; (line = fReader.readLine()) != null; lineno++) {
				if (isLineEmpty(line)) {
					linecount++;
					log(lineno + ",");
					continue;
				}
				fWriter.write(line.trim());
				fWriter.newLine();
			}
			log("Deleted line numbers : end");
			fWriter.flush();
			fWriter.close();
			fReader.close();
			file.delete();
			File temp_file = new File(filename + "_tmp");
			temp_file.renameTo(file);

			System.out.println(linecount + " blank lines deleted");
			System.out.println("Please refer log file " + LOGFILENAME
					+ " for more details (refresh it).");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteDuplicateLines(String fileName) {
		try {
			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename + "_tmp"), "UTF-8"));
			Vector<String> uniqueLines = new Vector<String>();

			String line;
			int linecount = 0;
			int lineno = 0;
			log("Deleted Duplicate lines : start");
			while ((line = fReader.readLine()) != null) {
				lineno++;
				if (uniqueLines.contains(line.trim())) {
					linecount++;
					log(lineno + ":" + line);
					continue;
				}
				uniqueLines.add(line.trim());
				fWriter.write(line.trim());
				fWriter.newLine();
			}
			log("Deleted Duplicate lines : start");
			fWriter.flush();
			fWriter.close();
			fReader.close();
			file.delete();
			File temp_file = new File(filename + "_tmp");
			temp_file.renameTo(file);

			System.out.println(linecount + " duplicate lines deleted");
			System.out.println("Please refer log file " + LOGFILENAME
					+ " for more details (refresh it).");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteLinesContaining(String fileName) {
		System.out.print("Enter the string to delete the lines consists it: ");
		String key = readString();
		try {
			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename + "_tmp"), "UTF-8"));

			String line;
			int linecount = 0;
			log("Deleted lines containing '" + key + "' : start");
			while ((line = fReader.readLine()) != null) {
				if (!isLineEmpty(line) && line.contains(key)) {
					linecount++;
					log(line);
					continue;
				}
				fWriter.write(line.trim());
				fWriter.newLine();
			}
			log("Deleted lines containing '" + key + "' : end");
			fWriter.flush();
			fWriter.close();
			fReader.close();
			file.delete();
			File temp_file = new File(filename + "_tmp");
			temp_file.renameTo(file);

			System.out.println(linecount + " lines Containing '" + key
					+ "' are removed");
			System.out.println("Please refer log file " + LOGFILENAME
					+ " for more details (refresh it).");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteLinesByLength(String fileName, int compareOption) {

		System.out.print("Enter the length: ");
		int length = readNumber();
		try {
			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename + "_tmp"), "UTF-8"));

			String line;
			int linecount = 0;
			String comparer = "";
			log("Deleting lines based on length '" + length + "' : start");
			switch (compareOption) {
			case COMPR_EQUAL:
				comparer = "equal to";
				while ((line = fReader.readLine()) != null) {
					if (line.length() == length) {
						linecount++;
						log(line);
						continue;
					}
					fWriter.write(line.trim());
					fWriter.newLine();
				}
				break;
			case COMPR_LESS:
				comparer = "less than";
				while ((line = fReader.readLine()) != null) {
					if (line.length() < length) {
						linecount++;
						log(line);
						continue;
					}
					fWriter.write(line.trim());
					fWriter.newLine();
				}
				break;
			case COMPR_GREATER:
				comparer = "greater than";
				while ((line = fReader.readLine()) != null) {
					if (line.length() > length) {
						linecount++;
						log(line);
						continue;
					}
					fWriter.write(line.trim());
					fWriter.newLine();
				}
				break;
			default:
				break;
			}
			log("Deleted lines having length '" + comparer + " " + length
					+ "' : end");
			fWriter.flush();
			fWriter.close();
			fReader.close();
			file.delete();
			File temp_file = new File(filename + "_tmp");
			temp_file.renameTo(file);

			System.out.println(linecount + " lines having length '" + comparer
					+ " " + length + "' are removed");
			System.out.println("Please refer log file " + LOGFILENAME
					+ " for more details (refresh it).");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sort(String fileName, int sortOption) {
		try {
			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename + "_tmp"), "UTF-8"));

			String line;
			String sortType = "";
			Vector<String> lines = new Vector<String>();

			while ((line = fReader.readLine()) != null) {
				if (isLineEmpty(line)) {
					continue;
				}
				lines.add(line.trim());
			}

			switch (sortOption) {
			case SORT_ASC:
				sortType = "Ascending Order";

				quickSort(lines, 0, lines.size() - 1, sortOption);

				for (String sortedline : lines) {
					fWriter.write(sortedline);
					fWriter.newLine();
				}
				break;
			case SORT_DESC:
				sortType = "Descending Order";

				quickSort(lines, 0, lines.size() - 1, sortOption);

				for (String sortedline : lines) {
					fWriter.write(sortedline);
					fWriter.newLine();
				}
				break;
			default:
				break;
			}
			fWriter.flush();
			fWriter.close();
			fReader.close();
			file.delete();
			File temp_file = new File(filename + "_tmp");
			temp_file.renameTo(file);

			System.out.println("The file is sorted in " + sortType);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void quickSort(Vector<String> texts, int lowerIndex,
			int higherIndex, int sortOption) {

		int i = lowerIndex;
		int j = higherIndex;
		String pivot = texts.elementAt(lowerIndex + (higherIndex - lowerIndex)
				/ 2);
		if (sortOption == SORT_ASC)
			while (i <= j) {
				while (texts.elementAt(i).compareTo(pivot) < 0) {
					i++;
				}
				while (texts.elementAt(j).compareTo(pivot) > 0) {
					j--;
				}
				if (i <= j) {
					Collections.swap(texts, i, j);
					i++;
					j--;
				}
			}

		if (sortOption == SORT_DESC)
			while (i <= j) {
				while (texts.elementAt(i).compareTo(pivot) > 0) {
					i++;
				}
				while (texts.elementAt(j).compareTo(pivot) < 0) {
					j--;
				}
				if (i <= j) {
					Collections.swap(texts, i, j);
					i++;
					j--;
				}
			}
		if (lowerIndex < j)
			quickSort(texts, lowerIndex, j, sortOption);
		if (i < higherIndex)
			quickSort(texts, i, higherIndex, sortOption);
	}

	private static class LineDetails {
		int lineNo = 0;
		String line = " ";
	}

	public static Vector<Integer> findSimilar(String fileName) {
		long startTime = System.nanoTime();
		Vector<Integer> similarLinesLineNums = new Vector<Integer>();

		try {

			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			String outputFileName = file.getParent() + "\\" + filename
					+ "_similar.txt";
			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFileName), "UTF-8"));

			System.out.println("OutputFileName: " + outputFileName);

			String lineFromFile;
			int linecount = 0;
			int lineNo = 0;
			Vector<LineDetails> lines = new Vector<LineDetails>();
			Vector<LineDetails> similarLines = new Vector<LineDetails>();

			while ((lineFromFile = fReader.readLine()) != null) {
				if (isLineEmpty(lineFromFile)) {
					continue;
				}
				LineDetails lineDetails = new FileTextProcess.LineDetails();
				lineDetails.lineNo = lineNo;
				lineDetails.line = lineFromFile.trim();
				lines.add(lineDetails);
				lineNo++;
			}

			for (LineDetails currentline : lines) {
				boolean partiallyMatching = matchPartial(currentline, lines,
						similarLines, similarLinesLineNums);

				if (partiallyMatching) {
					linecount++;
					fWriter.write("line " + currentline.lineNo + ": "
							+ currentline.line);
					fWriter.newLine();
					fWriter.write("------------------------");
					fWriter.newLine();

					for (LineDetails similarLineDetails : similarLines) {
						fWriter.write("line " + similarLineDetails.lineNo
								+ ": " + similarLineDetails.line);
						fWriter.newLine();
					}

					fWriter.write("========================");
					fWriter.newLine();
				}
			}
			fWriter.flush();
			fWriter.close();
			fReader.close();

			System.out
					.println(linecount
							+ " lines suspected to be similar. "
							+ "\nPlease refer *_similar.txt file in same directory for more details (refresh it).");
		} catch (IOException e) {
			e.printStackTrace();
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		System.out.println("findSimilar took " + duration / 1000000
				+ " milliseconds");

		return similarLinesLineNums;
	}

	private static void spilitWords(String line, Vector<String> words,
			boolean allowDuplicate) {
		words.clear();
		if (allowDuplicate)
			for (String word : line.split(" ")) {
				words.add(word.toUpperCase());
			}
		else {
			for (String word : line.split(" ")) {
				String wordUpper = word.toUpperCase();
				if (!words.contains(wordUpper))
					words.add(wordUpper);
			}
		}
	}

	private static boolean matchPartial(LineDetails currentLine,
			Vector<LineDetails> lines, Vector<LineDetails> similarLines,
			Vector<Integer> similarLinesLineNums) {

		long startTime = System.nanoTime();

		similarLines.clear();
		Vector<String> words = new Vector<String>();
		spilitWords(currentLine.line, words, false);

		Vector<String> comparedWords = new Vector<String>();

		int similarWordCount = 0;

		for (int i = currentLine.lineNo + 1; i < lines.size(); i++) {
			LineDetails lineDetails = lines.get(i);
			// log("=");
			log("currentLine:-> " + currentLine.line);
			log("lineCompared:-> " + lineDetails.line);

			if (similarLinesLineNums.contains(lineDetails.lineNo))
				continue;
			similarWordCount = 0;

			spilitWords(lineDetails.line, comparedWords, false);
			for (String word : words) {
				if (comparedWords.contains(word))
					similarWordCount++;
			}

			int similarWordCountExpected = comparedWords.size() * 80 / 100;
			log("Expected: " + similarWordCountExpected + " similar:"
					+ similarWordCount);
			if (similarWordCount >= similarWordCountExpected) {
				LineDetails similarLineDetails = new FileTextProcess.LineDetails();
				similarLineDetails.lineNo = lineDetails.lineNo;
				similarLineDetails.line = lineDetails.line;
				similarLines.add(similarLineDetails);
				similarLinesLineNums.add(similarLineDetails.lineNo);
			}
		}

		if (similarLines.size() > 0)
			return true;

		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		// System.out.println("matchPartial took "+ duration/1000000+
		// " milliseconds");
		return false;
	}

	public static void deleteSimilarLines(String fileName) {
		long startTime = System.nanoTime();
		try {
			Vector<Integer> similarLinesLineNums = findSimilar(fileName);

			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename + "_tmp"), "UTF-8"));

			String line;
			int linecount = 0;
			int lineno = 0;
			log("deleting Similar Lines : start");
			for (; (line = fReader.readLine()) != null; lineno++) {
				if (similarLinesLineNums.contains(lineno)) {
					linecount++;
					log(lineno + " " + line);
					continue;
				}
				fWriter.write(line.trim());
				fWriter.newLine();
			}
			log("deleting Similar Lines : end");
			fWriter.flush();
			fWriter.close();
			fReader.close();
			file.delete();
			File temp_file = new File(filename + "_tmp");
			temp_file.renameTo(file);

			System.out.println(linecount + " similar lines deleted");
			System.out.println("Please refer log file " + LOGFILENAME
					+ " for more details (refresh it).");
		} catch (IOException e) {
			e.printStackTrace();
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		System.out.println("deleteSimilarLines took " + duration / 1000000
				+ " milliseconds");
	}

	public static void removeDotSpaceAtEnd(String fileName) {
		try {
			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename + "_tmp"), "UTF-8"));

			String line;
			int linecount = 0;
			int lineno = 0;
			log("Remove Dot Space At End : start");
			for (; (line = fReader.readLine()) != null; lineno++) {
				line = line.trim();
				while (!isLineEmpty(line)
						&& line.charAt(line.length() - 1) == '.') {
					line = line.substring(0,line.length() - 1).trim();
					linecount++;
					log(lineno + ":"+ line);
				}
				fWriter.write(line);
				fWriter.newLine();
				lineno++;
			}
			log("Remove Dot Space At End : end");
			fWriter.flush();
			fWriter.close();
			fReader.close();
			file.delete();
			File temp_file = new File(filename + "_tmp");
			temp_file.renameTo(file);

			System.out.println(linecount + " lines modified to remove dot and space at the end");
			System.out.println("Please refer log file " + LOGFILENAME
					+ " for more details (refresh it).");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProjectDirectory() {
		String projectDir = "D:\\";
		try {
			URL u = FileTextProcess.class.getProtectionDomain().getCodeSource()
					.getLocation();
			File f = new File(u.toURI());
			projectDir = f.getParent().toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return projectDir;
	}
}
