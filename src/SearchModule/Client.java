package src.SearchModule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

public class Client {
    public static void main(String[] args) throws NotBoundException, FileNotFoundException, IOException {
        SearchModuleInterface searchModule = (SearchModuleInterface) Naming.lookup("rmi://localhost/SearchModule");
        System.out.println(searchModule.searchForWords("Test Ola Hey Document"));
    }
}
