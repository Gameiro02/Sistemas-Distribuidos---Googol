package src;

import java.io.IOException;
import java.util.HashSet;

import src.Downloader.Downloader;

public class programa {
    public static void main(String[] args) throws IOException, InterruptedException {

        Downloader download = new Downloader("https://www.google.com");
        download.start();

        download.join();

        HashSet<String> links = download.getLinks();
        HashSet<String> words = download.getWords();

        for (String link : links) {
            System.out.println(link);
        }

        for (String word : words) {
            System.out.println(word);
        }
    }
}
