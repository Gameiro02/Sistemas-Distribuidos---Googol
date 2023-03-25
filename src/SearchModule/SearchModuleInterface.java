package src.SearchModule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SearchModuleInterface extends Remote {
    public String searchForWords(String words) throws RemoteException, MalformedURLException, NotBoundException, FileNotFoundException, IOException;
}
