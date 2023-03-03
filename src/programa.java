package src;

import java.io.IOException;

import src.Downloader.Downloader;

public class programa {
    public static void main(String[] args) throws IOException {
        Downloader d = new Downloader();

        d.recursiveDownload("https://www.google.com", 10, 0);

        // d.printIndex();
    }
}
