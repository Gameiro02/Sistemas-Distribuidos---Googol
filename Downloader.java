import java.io.IOException;
import java.util.HashSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {
    
    public static void download(String url) throws IOException {
        // Download the webpage and parse its content using Jsoup
        Document doc = Jsoup.connect(url).get();

        // Extract the title of the webpage
        String title = doc.title();

        // Extract the text of the webpage
        String text = doc.text();

        // Extract the links of the webpage
        HashSet<String> links = new HashSet<>();
        Elements linkElements = doc.select("a[href]");
        for (Element linkElement : linkElements) {
            String link = linkElement.absUrl("href");
            if (!link.isEmpty()) {
                links.add(link);
            }
        }

        // Show the results
        System.out.println("Title: " + title);
        System.out.println("Text: " + text);
        for (String link : links) {
            System.out.println(link);
        }
    }
}
