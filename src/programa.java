package src;

import java.io.IOException;
import java.util.HashSet;

import src.Downloader.Downloader;
import src.IndexStorageBarrel.Barrel;

// Install vscode extension: liveserver

public class programa {
    public static void main(String[] args) throws IOException {
        Downloader d = new Downloader();

        for (String link : links) {
            System.out.println(link);
        }

        for (String word : words) {
            System.out.println(word);
        }
    }
}
