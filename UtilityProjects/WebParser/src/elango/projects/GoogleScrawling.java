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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class LinkInfo {
	public String title;
	public String url;
}

public class GoogleScrawling {
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

	public static void main(String[] args) throws IOException {

		try {

			String projectDir = getProjectDirectory();

			LOGFILENAME = projectDir + "\\log\\"
					+ GoogleScrawling.class.getSimpleName() + ".log";

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

			// actual processing
			String fileName = projectDir + "\\res\\content.txt";
			if (args.length > 1 && args[1].isEmpty())
				fileName = args[1];

			File f = new File(fileName);
			if (!f.exists() || f.isDirectory()) {
				System.out.println("File is not exist, Give valid file name");
				System.exit(0);
			}

			File file = new File(fileName);
			String filename = file.getName();
			BufferedReader fReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			String line;
			int linecount = 0;
			int lineno = 0;

			String google = "http://www.google.com/search?q=";
			String charset = "UTF-8";
			String userAgent = "Mozilla/5.0";
			Queue<LinkInfo> linkQueue = new LinkedList<LinkInfo>();
			int noOfpages = 2;
			int pageNum = 0;

			for (; (line = fReader.readLine()) != null; lineno++) {
				for (int pageno = 0; pageno < (noOfpages*10); pageno += 10) {
					Document doc = Jsoup
							.connect(
									google + URLEncoder.encode(line, charset)
											+ "&start=" + pageno)
							.userAgent(userAgent).get();

					Elements links = doc.select("li.g>h3>a");

					for (Element link : links) {
						LinkInfo linkInfo = new LinkInfo();
						linkInfo.title = line;
						linkInfo.url = link.absUrl("href");

						linkInfo.url = URLDecoder.decode(linkInfo.url
								.substring(linkInfo.url.indexOf('=') + 1,
										linkInfo.url.indexOf('&')), "UTF-8");

						if (!linkInfo.url.startsWith("http")) {
							continue;
						}

						System.out.println(lineno + ": Title: "
								+ linkInfo.title);
						System.out.println("url: " + linkInfo.url);
						linkQueue.add(linkInfo);

						/*
						 * Elements contents =
						 * link.parent().parent().select("span.st");
						 * System.out.println("link: " + link.toString());
						 * System.out.println("contents: " +
						 * contents.toString()); for (Element content :
						 * contents) { System.out.println("content: " +
						 * content); }
						 */
					}
				}
			}

			int titleNo = 0;
			BufferedWriter fWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("abc"), "UTF-8"));
			while (!linkQueue.isEmpty()) {
				LinkInfo linkInfo = linkQueue.remove();

				try {

					Document webdoc = Jsoup.connect(linkInfo.url).get();
					String contentFileName = linkInfo.title;
					contentFileName = contentFileName.replaceAll("\\s", "");
					System.out.println("replaced space contentFileName: "
							+ contentFileName);
					if (contentFileName.length() > 20)
						contentFileName = contentFileName.substring(0, 20);
					contentFileName = projectDir + "\\res\\" + contentFileName;
					if (titleNo % 10 == 0) {
						fWriter.close();
						fWriter = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream(contentFileName), "UTF-8"));
					}

					System.out.println("contentFileName: " + contentFileName);

					fWriter.write(Jsoup.parse(webdoc.toString()).text());

					titleNo++;

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
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
			URL u = GoogleScrawling.class.getProtectionDomain().getCodeSource()
					.getLocation();
			File f = new File(u.toURI());
			projectDir = f.getParent().toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return projectDir;
	}
}