package src;

import java.io.IOException;

// Install vscode extension: liveserver

public class programa {
    public static void main(String[] args) throws IOException {
        Downloader d = new Downloader(1);
        Downloader d2 = new Downloader(2);

        d.start();
        d2.start();

        Barrel b = new Barrel(1);
        b.run();
    }
}
