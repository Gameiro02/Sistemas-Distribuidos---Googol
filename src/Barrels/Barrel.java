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
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import src.Configuration;

public class Barrel extends Thread implements BarrelInterface, Serializable {
    private String INDEXFILE;
    private String LINKSFILE;
    private int index;
    private HashMap<String, ArrayList<String>> indexMap;
    private HashMap<String, ArrayList<String>> linksMap;
    private HashMap<String, Integer> auxMap;

    public Barrel(int index) throws IOException, RemoteException {
        this.INDEXFILE = "src\\Barrels\\BarrelFiles\\Barrel" + index + ".txt";
        this.LINKSFILE = "src\\Barrels\\BarrelFiles\\Links" + index + ".txt";
        this.index = index;
        this.indexMap = new HashMap<>();
        this.linksMap = new HashMap<>();
        this.auxMap = new HashMap<>();

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

        UnicastRemoteObject.exportObject(this, 0);
        Naming.rebind("rmi://localhost/Barrel" + index, this);
    }

    public void run() {
        try {
            sendStatus("Waiting");
        } catch (Exception e) {
            e.printStackTrace();
        }
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

                sendStatus("Active");
                String received = new String(packet.getData(), 0, packet.getLength());

                ArrayList<String> data;
                data = textParser(received);
                writeToFile(data);
                writeToLinksFile(data);
                writeToHashMaps(data);
                sendStatus("Waiting");
            }

        } catch (IOException e) {
            e.printStackTrace();
            try {
                sendStatus("Offline");
                return;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
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

    private void writeToHashMaps(ArrayList<String> data) {
        synchronized (indexMap) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(INDEXFILE));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    String word = parts[0].toLowerCase();
                    ArrayList<String> urls = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++) {
                        urls.add(parts[i]);
                    }
                    indexMap.put(word, urls);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        synchronized (linksMap) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(LINKSFILE));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    String urlString = parts[0];

                    ArrayList<String> info = new ArrayList<>();
                    info.add(parts[1]); // title
                    info.add(parts[2]); // context

                    String[] urlParts = urlString.split("\\|");
                    for (int i = 1; i < urlParts.length; i++) {
                        info.add(urlParts[i]);
                    }

                    linksMap.put(urlString.split("\\|")[0], info);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            int titleSize = data.get(1).split(" ").length;
            String context = "";
            for (int i = 2 + titleSize; i < data.size() && i < Configuration.CONTEXT_SIZE + 2 + titleSize; i++) {
                context += data.get(i) + " ";
            }
            String linha;
            if (!otherUrls.equals("")) {
                linha = url + "|" + otherUrls + ";" + data.get(1) + ";" + context;
            } else {
                linha = url + ";" + data.get(1) + ";" + context;
            }

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
                String word = parts[0].toLowerCase();
                List<String> links = Arrays.asList(parts).subList(1, parts.length);
                if (word.equals(data.get(i).toLowerCase())) {
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

    // Find every link that points to a page
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

    private void sendStatus(String status) throws IOException {
        InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS_ADMIN);
        MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT_ADMIN);

        // if its active send the url and the ip and port
        // if its waiting send the ip and port

        String statusString = "BARREL;" + this.index + ";";

        if (status == "Active") {
            statusString += "Active;" + Configuration.MULTICAST_ADDRESS + ";" + Configuration.MULTICAST_PORT + ";";
        } else if (status == "Waiting") {
            statusString += "Waiting;";
        } else if (status == "Offline") {
            statusString += "Offline";
        } else {
            System.out.println("Invalid status barrel");
            socket.close();
            return;
        }

        byte[] buffer = statusString.getBytes();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, Configuration.MULTICAST_PORT_ADMIN);
        socket.send(packet);
        socket.close();
    }

    @Override
    public List<String> searchForWords(String word) throws FileNotFoundException, IOException {
        String words[] = word.split(" ");
        auxMap.clear();

        for (String palavra : words) {
            palavra = palavra.toLowerCase();
            if (indexMap.containsKey(palavra)) {
                ArrayList<String> urls = indexMap.get(palavra);
                for (String url : urls) {
                    if (auxMap.containsKey(url)) {
                        auxMap.put(url, auxMap.get(url) + 1);
                    } else {
                        auxMap.put(url, 1);
                    }
                }
            }
        }

        // Remove links from the map that don't have all the words
        Iterator<String> iterator = auxMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (auxMap.get(key) != words.length) {
                iterator.remove();
            }
        }

        // Each string has the format "url;title;context"
        ArrayList<String> results = new ArrayList<String>();
        for (String key : auxMap.keySet()) {
            ArrayList<String> info = linksMap.get(key);
            String result = key + ";" + info.get(0) + ";" + info.get(1);
            results.add(result);
        }


        // Count the number of times each url is referenced in the keys of linksMap
        Collections.sort(results, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] parts1 = o1.split(";");
                String[] parts2 = o2.split(";");
                String url1 = parts1[0];
                String url2 = parts2[0];
                int count1 = 0;
                int count2 = 0;
                for (String key : linksMap.keySet()) {
                    ArrayList<String> urls = linksMap.get(key);
                    if (urls.contains(url1)) {
                        count1++;
                    }
                    if (urls.contains(url2)) {
                        count2++;
                    }
                }
                return count2 - count1;
            }
        });

        return results;
    }

}

class BarrelHashMaps {
    private HashMap<String, ArrayList<String>> indexMap;
    private HashMap<String, ArrayList<String>> linksMap;

    public BarrelHashMaps(HashMap<String, ArrayList<String>> indexMap, HashMap<String, ArrayList<String>> linksMap) {
        this.indexMap = indexMap;
        this.linksMap = linksMap;
    }

    public HashMap<String, ArrayList<String>> getIndexMap() {
        return indexMap;
    }

    public HashMap<String, ArrayList<String>> getLinksMap() {
        return linksMap;
    }
}
