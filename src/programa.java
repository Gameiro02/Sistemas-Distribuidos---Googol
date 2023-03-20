package src;

import java.io.IOException;
import java.util.HashSet;

import src.Downloader.Downloader;
import src.IndexStorageBarrel.Barrel;

// Install vscode extension: liveserver

public class programa {
    public static void main(String[] args) throws IOException {
        Downloader d = new Downloader("https://www.google.com/");
        d.start();

        Barrel b = new Barrel(1);
        b.run();
    }
}
