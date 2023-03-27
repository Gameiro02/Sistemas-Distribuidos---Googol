package src.SearchModule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SearchModuleInterface extends Remote {
    public List<String> searchForWords(String words)
            throws RemoteException, MalformedURLException, NotBoundException, FileNotFoundException, IOException;

    public List<String> linksToAPage(String word) throws FileNotFoundException, IOException, NotBoundException;
}
