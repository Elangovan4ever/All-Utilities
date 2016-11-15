package elango.projects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Copy_2_of_WebParser {

	private static int count = 0;
	private static int totalProverbs = 0;
	private static int meaningFoundCount = 0;
	private static int pageNo = 1;

	public static void main(String[] args) {

		try {

			for (int i = 6280; i < 6985; i++) {
				pageNo = i + 1;
				String webURL = "http://tamilnanbargal.com/node/" + i;
				Document doc = Jsoup.connect(webURL).get();
				Elements elements = doc
						.getElementsByAttributeValueContaining("class",
								"title gutter h1-quote");
				if(elements.size() > 1)
				{
					System.out.println("multiple proverb presents . check "+webURL);
					return;
				}
				for (Element element : elements) {
					totalProverbs++;
					String content = element.text();
					System.out.println("Proverb: " + content);
				}
				
				elements = doc
						.getElementsByAttributeValueContaining("class",
								"field field--name-body field--type-text-with-summary field--label-hidden");
				
				if(elements.size() > 1)
				{
					System.out.println("multiple proverb meaning presents . check "+webURL);
					return;
				}
				for (Element element : elements) {
					meaningFoundCount++;
					String content = element.text();
					System.out.println("Proverb Meaning: " + content);
				}
			}

			System.out.println("totalProverbs:  " + totalProverbs);
			System.out.println("meaningFoundCount:  " + meaningFoundCount);
			System.out.println("No Meanings count:  "
					+ (totalProverbs - meaningFoundCount));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
