package elango.projects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CopyOfWebParser {

	private static int count = 0;
	private static int totalProverbs = 0;
	private static int meaningFoundCount = 0;
	private static int pageNo = 1;

	public static void main(String[] args) {

		try {

			getElements("http://tamilnanbargal.com/pazhamozhigal/",
					"views-field views-field-title");
			
			/*for (int i = 1; i < 29; i++) {
				pageNo = i + 1;
				getElements(
						"http://tamilnanbargal.com/pazhamozhigal?page=" + i,
						"views-field views-field-title");
			}*/


			System.out.println("totalProverbs:  " + totalProverbs);
			System.out.println("meaningFoundCount:  " + meaningFoundCount);
			System.out.println("No Meanings count:  "
					+ (totalProverbs - meaningFoundCount));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean getElements(String webURL, String attributeValue) {
		webURL = webURL.trim();
		System.out.println("webURL: " + webURL);
		Elements elements = new Elements();
		fetchData(webURL, elements, attributeValue);

		if (elements.size() == 0) {

			if (!webURL.isEmpty()) {
				if (webURL.length() > 77) {
					webURL = webURL.substring(0, 76);
				} else if (webURL.charAt(webURL.length() - 1) == '.') {
					webURL = webURL.substring(0, webURL.length() - 1);
				} else if (webURL.charAt(webURL.length() - 1) == '?') {
					webURL = webURL.substring(0, webURL.length() - 1);
				} else {
					return false;
				}
				return getElements(webURL, attributeValue);
			}
			return false;
		}

		for (Element element : elements) {
			String content = element.text();
			if (webURL.equals("http://tamilnanbargal.com/pazhamozhigal/")
					|| webURL
							.contains("http://tamilnanbargal.com/pazhamozhigal?page")) {
				System.out.println(pageNo + ": proverb: " + content);
				totalProverbs++;
				content = content.trim();
				if (content.charAt(content.length() - 2) == ' ')
					content = content.substring(0, content.length() - 2);
				String proverbText = content.replaceAll(" ", "-")
						.replaceAll(";", "-").replaceAll(",", "-")
						.replaceAll("---", "-").replaceAll("--", "-");
				;
				count = 0;
				boolean gotMeaning = getElements(
						"http://tamilnanbargal.com/tamil/proverbs/"
								+ proverbText,
						"field field--name-body field--type-text-with-summary field--label-hidden");
				if (!gotMeaning) {
					count = 0;
					gotMeaning = getElements(
							"http://tamilnanbargal.com/pazhamozhigal/"
									+ proverbText,
							"field field--name-body field--type-text-with-summary field--label-hidden");
					if (!gotMeaning) {
						count = 0;
						gotMeaning = getElements("http://tamilnanbargal.com/"
								+ proverbText,
								"field field--name-body field--type-text-with-summary field--label-hidden");
						if (!gotMeaning) {
							count = 0;
							gotMeaning = getElements(
									"http://tamilnanbargal.com/tamil/proverbs/ "
											+ proverbText,
									"field field--name-body field--type-text-with-summary field--label-hidden");
						}
					}
				}
			} else {
				if (!content.equals("") && !content.isEmpty())
					meaningFoundCount++;
				System.out.println("proverb meaning: " + content);
			}
		}
		return true;
	}

	public static void fetchData(String webURL, Elements elements,
			String attributeValue) {
		try {
			Document documentExplain = Jsoup.connect(webURL).get();
			Elements elementsTemp = documentExplain
					.getElementsByAttributeValueContaining("class",
							attributeValue);

			elements.clear();
			elements.addAll(0, elementsTemp);

		} catch (Exception e) {
			// System.out.println("Not valid URL:  " + webURL);
		}
	}

}
