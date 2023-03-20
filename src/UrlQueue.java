package src;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import src.Downloader.Downloader;

public class UrlQueue {
    private static Queue<String> links = new LinkedList<String>();
    public static void main(String[] args) throws IOException {
        while (!links.isEmpty()) {
            Downloader.download(links.remove());
        }
    }

    public static void add(String link) {
        links.add(link);
    }
}
