package src.Barrels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import src.Configuration;

public class Barrel extends Thread implements BarrelInterface, Serializable {
    private String INDEXFILE;
    private String LINKSFILE;

    public Barrel(int index) throws IOException, RemoteException {
        this.INDEXFILE = "src\\Barrels\\BarrelFiles\\Barrel" + index + ".txt";
        this.LINKSFILE = "src\\Barrels\\BarrelFiles\\Links" + index + ".txt";

        File f = new File(INDEXFILE);

        if (f.exists()) {
            FileWriter writer = new FileWriter(INDEXFILE);
            writer.write("");
            writer.close();
        } else {
            f.createNewFile();
        }

        f = new File(LINKSFILE);

        if (f.exists()) {
            FileWriter writer = new FileWriter(LINKSFILE);
            writer.write("");
            writer.close();
        } else {
            f.createNewFile();
        }

        Naming.rebind("rmi://localhost/Barrel" + index, this);
    }

    public void run() {
        receive_multicast();
    }

    private void receive_multicast() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(Configuration.MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
            socket.joinGroup(group);

            while (true) {
                byte[] buffer = new byte[16384];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());

                ArrayList<String> data;
                data = textParser(received);
                writeToFile(data);
                writeToLinksFile(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private ArrayList<String> textParser(String received) {
        String[] fields = received.split(";");
        ArrayList<String> data = new ArrayList<String>();

        data.add(fields[0]); // urls "url|referencedUrl1|referencedUrl2|..."
        data.add(fields[1]); // title

        for (int i = 2; i < fields.length; i++) {
            data.add(fields[i]);
        }

        return data;
    }

    private void writeToLinksFile(ArrayList<String> data) throws IOException {

        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(LINKSFILE));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();

        String[] firstElement = data.get(0).split("\\|"); // url|referencedUrl1|referencedUrl2|...
        String url = firstElement[0];
        String otherUrls = "";
        for (int i = 1; i < firstElement.length; i++) {
            if (i != firstElement.length - 1)
                otherUrls += firstElement[i] + "|";
            else
                otherUrls += firstElement[i];
        }

        boolean found = false;
        for (String linha : lines) {
            if (linha.equals(url)) {
                found = true;
            }
        }

        if (!found) {
            String context = "";
            for (int i = 2; i < data.size() && i < Configuration.CONTEXT_SIZE+2; i++) {
                context += data.get(i) + " ";
            }
            String linha;
            if (!otherUrls.equals(""))
                linha = url + "|" + otherUrls + ";" + data.get(1) + ";" + context;
            else
                linha = url + ";" + data.get(1) + ";" + context;

            FileWriter writer = new FileWriter(LINKSFILE, true);
            writer.write(linha);
            writer.write(System.getProperty("line.separator"));
            writer.close();
        }
    }

    private void writeToFile(ArrayList<String> data) throws IOException {   
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(INDEXFILE));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();

        String[] firstElement = data.get(0).split("\\|"); // url|referencedUrl1|referencedUrl2|...
        String url = firstElement[0];

        for (int i = 2; i < data.size(); i++) {
            boolean found = false;
            for (String linha : lines) {
                String[] parts = linha.split(";");
                String word = parts[0];
                List<String> links = Arrays.asList(parts).subList(1, parts.length);
                if (word.equals(data.get(i))) {
                    if (!links.contains(url)) {
                        lines.set(lines.indexOf(linha), linha + ";" + url);
                    }
                    found = true;
                }
            }
            if (!found) {
                lines.add(data.get(i) + ";" + url);
            }
        }

        FileWriter writer = new FileWriter(INDEXFILE);
        for (String linha : lines) {
            writer.write(linha);
            writer.write(System.getProperty("line.separator"));
        }
        writer.close();
    }

    @Override
    public List<String> searchForWords(String word) throws FileNotFoundException, IOException {
        // Search for the word in the barrel
        List<String> lines = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(INDEXFILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        String words[] = word.split(" ");
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (String palavra : words) {
            for (String linha : lines) {
                String[] parts = linha.split(";");
                String wordInBarrel = parts[0];
                List<String> links = Arrays.asList(parts).subList(1, parts.length);
                if (wordInBarrel.toLowerCase().equals(palavra.toLowerCase())) {
                    for (String link : links) {
                        if (map.containsKey(link)) {
                            map.put(link, map.get(link) + 1);
                        } else {
                            map.put(link, 1);
                        }
                    }
                }
            }
        }

        // Remove links from the map that don't have all the words
        for (String key : map.keySet()) {
            if (map.get(key) != words.length) {
                map.remove(key);
            }
        }
        
        // Each string has the format "url;title;context"
        List<String> result = new ArrayList<String>();
        for (String key : map.keySet()) {
            String aux = "";
            try (BufferedReader reader2 = new BufferedReader(new FileReader(LINKSFILE))) {
                String line2;
                while ((line2 = reader2.readLine()) != null) {
                    String[] parts = line2.split(";");
                    String url = parts[0].split("\\|")[0];

                    String title = parts[1];
                    String context = parts[2];
                    if (url.equals(key)) {
                        aux = url + ";" + title + ";" + context;
                    }
                }
            }
            result.add(aux);
        }

        List<String> links_lines = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LINKSFILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                links_lines.add(line);
            }
        }

        // Count the number of times each link appears in the barrel
        // and sort the list by the number of times the link appears
        Collections.sort(result, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] parts1 = o1.split(";");
                String[] parts2 = o2.split(";");
                int count1 = 0;
                int count2 = 0;
                for (String linha : links_lines) {
                    // count the number of times parts1 and parts2 appear in the barrel
                    String[] parts = linha.split(";");
                    String[] links = parts[0].split("\\|");
                    for (String link : links) {
                        if (link.equals(parts1[0])) {
                            count1++;
                        }
                        if (link.equals(parts2[0])) {
                            count2++;
                        }
                    }
                }
                return count2 - count1;
            }
        });

        return result;
    }

    // Find every link that points to a page
    @Override
    public List<String> linksToAPage(String word) throws FileNotFoundException, IOException {
        List<String> lines = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LINKSFILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        List<String> result = new ArrayList<String>();
        for (String linha : lines) {
            String[] parts = linha.split(";");
            String[] links = parts[0].split("\\|");
            for (int i = 1; i < links.length; i++) {
                if (links[i].equals(word)) {
                    result.add(parts[0].split("\\|")[0]);
                }
            }
        }
        return result;
    }

}
