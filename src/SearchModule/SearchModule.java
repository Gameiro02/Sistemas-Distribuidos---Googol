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

public class SearchModule extends UnicastRemoteObject implements SearchModuleInterface{

    public SearchModule() throws RemoteException {
		super();
	}    

    @Override
    public List<String> searchForWords(String word) throws NotBoundException, FileNotFoundException, IOException {
        int randomBarrel = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        BarrelInterface barrel = (BarrelInterface) Naming.lookup("rmi://localhost/Barrel"+randomBarrel);
        List<String> result = barrel.searchForWords(word);
        return result;
    }

    @Override
    public List<String> linksToAPage(String word) throws FileNotFoundException, IOException, NotBoundException {
        int randomBarrel = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        BarrelInterface barrel = (BarrelInterface) Naming.lookup("rmi://localhost/Barrel"+randomBarrel);
        List<String> result = barrel.linksToAPage(word);
        return result;
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        SearchModule searchModule = new SearchModule();
        LocateRegistry.createRegistry(1099);
        Naming.rebind("SearchModule", searchModule);
        
        for (int i = 1; i <= Configuration.NUM_DOWNLOADERS; i++) {
            Downloader d = new Downloader(i);
            d.start();
        }

        for (int i = 1; i <= Configuration.NUM_BARRELS; i++) {
            Barrel b = new Barrel(i);
            b.start();
        }

    }
}
