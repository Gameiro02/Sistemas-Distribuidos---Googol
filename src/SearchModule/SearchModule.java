package src.SearchModule;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import src.Configuration;
import src.Downloader;
import src.Barrels.Barrel;
import src.Barrels.BarrelInterface;
import src.AdminPage;

import java.net.*;
import java.io.*;

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
    public List<String> searchForWords(String word, int pageNumber) throws NotBoundException, FileNotFoundException, IOException {
        int randomBarrel = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        BarrelInterface barrel = (BarrelInterface) Naming.lookup("rmi://localhost/Barrel" + randomBarrel);
        List<String> result = barrel.searchForWords(word, pageNumber);

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

        adminPage.updateHashMap(temp);
    }

    @Override
    public List<String> linksToAPage(String word) throws FileNotFoundException, IOException, NotBoundException {
        int randomBarrel = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        BarrelInterface barrel = (BarrelInterface) Naming.lookup("rmi://localhost/Barrel" + randomBarrel);
        List<String> result = barrel.linksToAPage(word);
        return result;
    }

    @Override
    public void IndexarUmNovoUrl(String url) throws RemoteException, IOException, NotBoundException {
        // Send the url to the urlqueue and the downloader will take care of the rest
        // via tcp
        Socket socket = new Socket("localhost", Configuration.PORT_B);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println(url);

        out.close();
        socket.close();
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        SearchModule searchModule = new SearchModule();
        writeCredentials();
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

        searchModule.adminPage = new AdminPage(searchModule.searchDictionary);
        searchModule.adminPage.showMenu();
    }

    private static void writeCredentials() {
        try {
            FileOutputStream fileOut = new FileOutputStream(Configuration.CREDENTIALS_FILE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(Configuration.LOGIN);
            out.writeObject(Configuration.PASSWORD);
            out.close();
            fileOut.close();
            System.out.println("Successfully wrote login credentials to file " + Configuration.CREDENTIALS_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getStringMenu() throws RemoteException {
        return adminPage.getStringMenu();
    }

}
