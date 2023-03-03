package src.Downloader;

import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {

    private static ArrayList<String> links = new ArrayList<String>();
    private static HashMap<String, HashSet<String>> index = new HashMap<String, HashSet<String>>();

    public void download(String url) throws IOException {
        // Download the webpage and parse its content using Jsoup
        Document doc = Jsoup.connect(url).get();

        // Extract the title of the webpage
        String title = doc.title();

        if (index.containsKey(title)) {
            index.get(title).add(url);
        } else {
            index.put(title, new HashSet<String>());
            index.get(title).add(url);
        }

        // Extract the text of the webpage
        String text = doc.text();

        String[] words = text.split(" ");

        for (String word : words) {
            if (index.containsKey(word) && !index.get(word).contains(url)) {
                index.get(word).add(url);
            } else {
                index.put(word, new HashSet<String>());
                index.get(word).add(url);
            }
        }

        // Extract the links of the webpage and add them to the list of links
        Elements linksOnPage = doc.select("a[href]");
        for (Element page : linksOnPage) {
            // if the link is not in the list of links, add it
            if (!links.contains(page.attr("abs:href"))) {
                links.add(page.attr("abs:href"));
                // System.out.println("New link = " + page.attr("abs:href"));
            }
        }

        // System.out.println("Size of the list of links: " + links.size()); // Print
        // the size of the list of links

    }

    // Recursive method to download the webpages of the list of links
    public void recursiveDownload(String url, int depth, int index) throws IOException {
        if (depth > 0) {

            try {
                download(url);
            } catch (Exception e) {
                // if we can't download the webpage, we remove it from the list of links
                links.remove(url);
            }

            // if the list of links is not empty, we download the first link
            if (!links.isEmpty()) {
                recursiveDownload(links.get(0), depth - 1, index + 1);
            }

        }
    }

    public void printIndex() throws IOException {
        for (String key : index.keySet()) {
            System.out.println(key + " : " + index.get(key));
        }
    }

    public void printLinks() throws IOException {
        for (String link : links) {
            System.out.println(link);
        }
    }
}
