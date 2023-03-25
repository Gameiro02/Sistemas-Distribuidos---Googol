package src;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;

import src.Barrels.Barrel;

import src.SearchModule.SearchModule;

public class Programa {
    public static void main(String[] args) throws IOException, NotBoundException {

        SearchModule searchModule = new SearchModule();
        LocateRegistry.createRegistry(1099);
        Naming.rebind("SearchModule", searchModule);
        System.out.println("SearchModule is ready.");

        // for (int i = 1; i <= Configuration.NUM_DOWNLOADERS; i++) {
        // Downloader d = new Downloader(i);
        // d.start();

        // for (int i = 1; i <= Configuration.NUM_BARRELS; i++) {
        // Barrel b = new Barrel(i);
        // b.start();
        // }
    }
}
