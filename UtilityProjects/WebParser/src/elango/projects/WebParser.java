package elango.projects;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebParser {
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

	private static class LineDetails {
		int lineNo = 0;
		String line = " ";
	}

	public static void main(String[] args) throws IOException {

		try {

			String projectDir = getProjectDirectory();

			LOGFILENAME = projectDir + "\\log\\"
					+ WebParser.class.getSimpleName() + ".log";

			logger = Logger.getLogger("MyLog");

			try {

				// This block configure the logger with handler and formatter
				FileHandler fileHandler = new FileHandler(LOGFILENAME,
						FILE_SIZE, NUM_OF_LOG_FILES, false);
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

			String website[] = { "http://www.eegarai.net/t90874-page-3",
					"http://www.eegarai.net/t90874p15-page-3",
					"http://www.eegarai.net/t90874p30-page-3" };

			String charset = "UTF-8";
			int noOfpages = 2;
			int pageNum = 0;
			int serialno = 0;

			Map<String, String> contentMap = new HashMap<String, String>();

			for (int i = 0; i < website.length; i++) {
				Document doc = Jsoup.connect(website[i]).get();

				String webContent = Jsoup.parse(doc.text()).toString();

				String keyContent = "பழமொழி:";
				String keyMeaning = "பொருள்:";
				int index = webContent.indexOf(keyContent);
				while (index >= 0) {
					serialno++;
					int meaningIndex = webContent.indexOf(keyMeaning, index);
					String sentence = webContent.substring(
							index + keyContent.length() + 1, meaningIndex - 1)
							.trim();
					while (sentence.charAt(sentence.length() - 1) == '.') {
						sentence = sentence.substring(0, sentence.length() - 1)
								.trim();
					}
					String meaning = webContent.substring(meaningIndex
							+ keyMeaning.length() + 1,
							webContent.indexOf("விளக்கம்:", meaningIndex) - 1);
					index = webContent.indexOf(keyContent, index + 1);

					System.out.println(serialno + " " + sentence + "#"
							+ meaning);
					contentMap.put(sentence, meaning);
				}
			}

			String fileName = projectDir + "\\res\\content.txt";
			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName + "_tmp"), "UTF-8"));

			String lineFromFile;
			int linecount = 0;
			int lineNo = 0;
			Vector<LineDetails> lines = new Vector<LineDetails>();
			Vector<LineDetails> similarLines = new Vector<LineDetails>();

			while ((lineFromFile = fReader.readLine()) != null) {
				LineDetails lineDetails = new LineDetails();
				lineDetails.lineNo = lineNo;
				if(lineFromFile.indexOf('#') != -1)
					lineFromFile = lineFromFile.substring(0,lineFromFile.indexOf('#')-1);
				lineDetails.line = lineFromFile.trim();
				log(lineFromFile);
				lines.add(lineDetails);
				lineNo++;
			}

			Vector<Integer> similarLinesLineNums = new Vector<Integer>();
			int keyLineNum = 0;

			for (String key : contentMap.keySet()) {
				log("=========================> keyLineNum: "+ keyLineNum++ + key);
				
				similarLines.clear();
				Vector<String> words = new Vector<String>();
				spilitWords(key, words, false);

				Vector<String> comparedWords = new Vector<String>();

				for (LineDetails currentline : lines) {

					int similarWordCount = 0;
					spilitWords(currentline.line, comparedWords, false);
					for (String word : words) {
						if (comparedWords.contains(word))
							similarWordCount++;
					}

					int similarWordCountExpected = comparedWords.size() * 80 / 100;
					log("Expected: " + similarWordCountExpected + " similar:"
							+ similarWordCount);
					if (similarWordCount >= similarWordCountExpected) {
						LineDetails similarLineDetails = new LineDetails();
						similarLineDetails.lineNo = currentline.lineNo ;
						similarLineDetails.line = key;
						similarLines.add(similarLineDetails);
						similarLinesLineNums.add(similarLineDetails.lineNo);
					}
				}
				
				String value = contentMap.get(key);
				String linenumString = "";
				
				for (LineDetails similarLineDetails : similarLines)
				{
					linenumString = "==>"+ (similarLineDetails.lineNo+1) +" : ";
				}
				fWriter.write(linenumString + key+"#"+value);
				fWriter.newLine();
			}
			
			fWriter.flush();
			fWriter.close();
			fReader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

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

	public static String getProjectDirectory() {
		String projectDir = "D:\\";
		try {
			URL u = WebParser.class.getProtectionDomain().getCodeSource()
					.getLocation();
			File f = new File(u.toURI());
			projectDir = f.getParent().toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return projectDir;
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
}