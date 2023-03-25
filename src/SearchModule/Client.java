package src.SearchModule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.List;

public class Client {
    public static void main(String[] args) throws NotBoundException, FileNotFoundException, IOException {
        SearchModuleInterface searchModule = (SearchModuleInterface) Naming.lookup("rmi://localhost/SearchModule");
        List<String> result = searchModule.searchForWords("Hey ola");
        if (result.size() == 0) {
            System.out.println("No results found");
        } else {
            for (String s : result) {
                String[] fields = s.split(";");
                System.out.println("Link: " + fields[0]);
                System.out.println("Title: " + fields[1]);
                System.out.println("Context: " + fields[2]);
            }
        }
    }
}
