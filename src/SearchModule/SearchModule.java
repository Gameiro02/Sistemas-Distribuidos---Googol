package src.SearchModule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import src.Configuration;
import src.Downloader;
import src.Barrels.Barrel;
import src.Barrels.BarrelInterface;
import src.AdminPage;

import java.util.*;

public class SearchModule extends UnicastRemoteObject implements SearchModuleInterface {
    private AdminPage adminPage;
    private HashMap<String, Integer> searchDictionary;

    public SearchModule() throws RemoteException {
        super();
        this.searchDictionary = new HashMap<String, Integer>();
        adminPage = new AdminPage(searchDictionary);
    }

    @Override
    public List<String> searchForWords(String word) throws NotBoundException, FileNotFoundException, IOException {
        int randomBarrel = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        BarrelInterface barrel = (BarrelInterface) Naming.lookup("rmi://localhost/Barrel" + randomBarrel);
        List<String> result = barrel.searchForWords(word);

        if (this.searchDictionary.containsKey(word)) {
            this.searchDictionary.put(word, this.searchDictionary.get(word) + 1);
        } else {
            this.searchDictionary.put(word, 1);
        }

        sortSearchDictionary();

        return result;
    }

    private void sortSearchDictionary() {
        // Sort the search dictionary by the number of times a word has been searched
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(
                this.searchDictionary.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        this.searchDictionary = temp;
    }

    @Override
    public List<String> linksToAPage(String word) throws FileNotFoundException, IOException, NotBoundException {
        int randomBarrel = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        BarrelInterface barrel = (BarrelInterface) Naming.lookup("rmi://localhost/Barrel" + randomBarrel);
        List<String> result = barrel.linksToAPage(word);
        return result;
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        SearchModule searchModule = new SearchModule();
        LocateRegistry.createRegistry(1099);
        Naming.rebind("SearchModule", searchModule);

        for (int i = 1; i <= Configuration.NUM_BARRELS; i++) {
            Barrel b = new Barrel(i);
            b.start();

            // System.out.println("Barrel " + i + " is ready.");
        }

        for (int i = 1; i <= Configuration.NUM_DOWNLOADERS; i++) {
            Downloader d = new Downloader(i);
            try {
                d.start();
            } catch (Exception e) {
                // Enviar o url para o outro downloader
                e.printStackTrace();
            }
            // System.out.println("Downloader " + i + " is ready.");
        }

        searchModule.adminPage.showMenu();
    }

    @Override
    public String getStringMenu() throws RemoteException {
        return adminPage.getStringMenu();
    }

}
