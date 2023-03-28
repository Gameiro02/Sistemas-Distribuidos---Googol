package src.Barrels;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.util.List;

public interface BarrelInterface extends Remote {
    public List<String> searchForWords(String word, int pageNumber) throws FileNotFoundException, IOException;
    public List<String> linksToAPage(String word) throws FileNotFoundException, IOException;
}
