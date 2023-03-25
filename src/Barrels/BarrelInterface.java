package src.Barrels;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;

public interface BarrelInterface extends Remote {
    public String searchForWord(String word) throws FileNotFoundException, IOException;
}
