package elango.projects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Copy_3_of_WebParser {

	private static int count = 0;
	private static int totalProverbs = 0;
	private static int meaningFoundCount = 0;
	private static int pageNo = 1;

	public static void main(String[] args) {

		try {
			System.out.println("pageNo: " + pageNo);
			getElements("http://tamilnanbargal.com/pazhamozhigal/",
					"views-field views-field-title");

			for (int i = 1; i < 29; i++) {
				pageNo = i + 1;
				System.out.println("pageNo: " + pageNo);
				getElements(
						"http://tamilnanbargal.com/pazhamozhigal?page=" + i,
						"views-field views-field-title");
			}

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
		// System.out.println("webURL: " + webURL);
		Elements elements = new Elements();
		fetchData(webURL, elements, attributeValue);

		if (elements.size() == 0) {
			System.out.println(" elements not found");
		}

		for (Element element : elements) {
			String content = element.text();
			if(content.length() < 10)
				continue;
			if (webURL.equals("http://tamilnanbargal.com/pazhamozhigal/")
					|| webURL
							.contains("http://tamilnanbargal.com/pazhamozhigal?page")) {
				System.out.println("Proverb: " + content);
				totalProverbs++;

				Elements links = element.getElementsByTag("a");
				for (Element link : links) {
					String linkHref = link.attr("href");
					getElements("http://tamilnanbargal.com/" + linkHref,
							"field field--name-body field--type-text-with-summary field--label-hidden");
				}

			} else {
				if (!content.equals("") && !content.isEmpty())
					meaningFoundCount++;
				System.out.println("Meaning: " + content);
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
