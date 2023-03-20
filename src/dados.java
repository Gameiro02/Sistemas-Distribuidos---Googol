package src;

import java.io.Serializable;
import java.util.HashSet;

public class Dados implements Serializable {
    private String url;
    private HashSet<String> words;

    

    @Override
    public String toString() {
        return "Dados [url=" + url + ", words=" + words + "]";
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public void setWords(HashSet<String> words) {
        this.words = words;
    }

       
}
