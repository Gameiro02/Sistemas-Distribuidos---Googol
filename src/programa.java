package src;

import java.io.IOException;

import src.Downloader.Downloader;
import src.IndexStorageBarrel.Barrel;

// Install vscode extension: liveserver

public class programa {
    public static void main(String[] args) throws IOException {
        Downloader d = new Downloader();

        d.recursiveDownload("http://127.0.0.1:5500/src/Html/main_page.html", 10);

        // d.printIndex();
    }
}
