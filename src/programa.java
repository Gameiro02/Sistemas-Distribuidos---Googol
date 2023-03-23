package src;

import java.io.IOException;

public class Programa {
    public static void main(String[] args) throws IOException {

        for (int i = 1; i <= Configuration.NUM_DOWNLOADERS; i++) {
            Downloader d = new Downloader(i);
            d.start();
        }

        for (int i = 1; i <= Configuration.NUM_BARRELS; i++) {
            Barrel b = new Barrel(i);
            b.run();
        }
    }
}
