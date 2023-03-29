package src.SearchModule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SearchModuleInterface extends Remote {
    public List<String> searchForWords(String words, int pageNumber)
            throws RemoteException, MalformedURLException, NotBoundException, FileNotFoundException, IOException;

    public List<String> linksToAPage(String word) throws FileNotFoundException, IOException, NotBoundException;

    public String getStringMenu() throws RemoteException;

    public void IndexarUmNovoUrl(String url) throws RemoteException, IOException, NotBoundException;

    public boolean login(String username, String password) throws RemoteException;

}
