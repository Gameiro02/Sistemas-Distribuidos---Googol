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
    private HashMap<String, ArrayList<String>> indexMap; // <<word>, <url1, url2, ...>>
    private HashMap<String, ArrayList<String>> linksMap; // <<url>, <title, description, url1, url2, ...>>
    private HashMap<String, Integer> auxMap;

    private String stats;

    public Barrel(int index) throws IOException, RemoteException {
        this.INDEXFILE = "webapp-googol\\webapp-googol\\src\\main\\java\\com\\example\\webappgoogol\\Barrels\\BarrelFiles\\Barrel"
                + index + ".txt";
        this.LINKSFILE = "webapp-googol\\webapp-googol\\src\\main\\java\\com\\example\\webappgoogol\\Barrels\\BarrelFiles\\Links"
                + index + ".txt";
        this.index = index;
        this.indexMap = new HashMap<>();
        this.linksMap = new HashMap<>();
        this.auxMap = new HashMap<>();
        this.stats = "Waiting";

        File f = new File(INDEXFILE);

        if (!f.exists()) {
            f.createNewFile();
        }

        if (Configuration.COLD_START) {
            f.delete();
            f.createNewFile();
        }

        f = new File(LINKSFILE);

        if (!f.exists()) {
            f.createNewFile();
        }

        if (Configuration.COLD_START) {
            f.delete();
            f.createNewFile();
        } else {
            writeToHashMaps();
        }

        UnicastRemoteObject.exportObject(this, 0);
        Naming.rebind("rmi://localhost/Barrel" + index, this);
    }

    public void run() {
        try {
            sendStatus("Waiting");
            receive_multicast();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receive_multicast() throws IOException {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(Configuration.MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
            socket.joinGroup(group);

            while (true) {
                byte[] buffer = new byte[65533];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                sendStatus("Active");
                String received = new String(packet.getData(), 0, packet.getLength());

                ArrayList<String> data;
                data = textParser(received);
                if (data == null)
                    continue;

                writeToFile(data);
                writeToLinksFile(data);
                writeToHashMaps();
                sendStatus("Waiting");
            }

        } catch (IOException e) {
            sendStatus("Offline");
            return;
        } finally {
            socket.close();
        }
    }

    private ArrayList<String> textParser(String received) {

        // Protocol :
        // type | url; item_count | number; url | www.example.com; referenced_urls |
        // url1 url2 url3; title | title; words | word1 word2 word3

        ArrayList<String> data = new ArrayList<String>();

        String[] fields = received.split("; ");

        String type = fields[0].split(" \\| ")[1];
        if (!type.equals("url"))
            return null;

        String[] url = fields[2].split(" \\| ");

        String referencedUrls = fields[3].split(" \\| ")[1];

        String[] list_referenceUrl = referencedUrls.split(" ");

        // Create a url: url|referencedUrl1|referencedUrl2|...
        String urlAndReferencedUrls = url[1];
        for (int i = 0; i < list_referenceUrl.length; i++) {
            if (list_referenceUrl[i].equals("None")) {
                // If the last character is a "|", remove it
                if (urlAndReferencedUrls.charAt(urlAndReferencedUrls.length() - 1) == '|') {
                    urlAndReferencedUrls = urlAndReferencedUrls.substring(0, urlAndReferencedUrls.length() - 1);
                }
                break;
            }

            urlAndReferencedUrls += "|" + list_referenceUrl[i];
        }

        data.add(urlAndReferencedUrls);

        // Get the title
        try {
            String[] title = fields[4].split(" \\| ");
            data.add(title[1]);
        } catch (Exception e) {
            for (String field : fields) {
                System.out.println("FIELD: " + field);
            }
            e.printStackTrace();
        }

        // Get the words
        String[] words = fields[5].split(" \\| ");

        // replace spaces with ";" except the first one
        String wordsSeparatedBySemicolon = words[1].replace(" ", ";");

        // Remove pontuation from wordsSeparatedBySemicolon except for ;
        wordsSeparatedBySemicolon = wordsSeparatedBySemicolon.replaceAll("[^a-zA-Z0-9\\p{L};]", "");

        data.add(wordsSeparatedBySemicolon);

        // Print data
        // System.out.println("Barrel[" + this.index + "] [url] " + data.get(0));
        // System.out.println("Barrel[" + this.index + "] [title] " + data.get(1));
        // System.out.println("Barrel[" + this.index + "] [words] " + data.get(2));

        return data;
    }

    private void writeToHashMaps() {

        synchronized (linksMap) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(LINKSFILE));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    String urlString = parts[0];

                    if (parts.length != 3) {
                        System.out.println("Barrel[" + this.index + "] [No description] failed to store in barrel");
                        reader.close();
                        return;
                    }

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
    }

    private void writeToLinksFile(ArrayList<String> data) {

        String[] firstElement = data.get(0).split("\\|"); // url|referencedUrl1|referencedUrl2|...
        String url = firstElement[0];

        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(LINKSFILE));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (line.split(";")[0].equals(url)) {
                    reader.close();
                    return;
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Erro ao ler o ficheiro dos links");
        }

        String otherUrls = "";
        for (int i = 2; i < firstElement.length; i++) {
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
            String[] words = data.get(2).split(";");
            String context = "";
            for (int i = 2 + titleSize; i < words.length && i < Configuration.CONTEXT_SIZE + 2 + titleSize; i++) {
                context += words[i] + " ";
            }

            String linha;
            if (!otherUrls.equals("")) {
                linha = url + "|" + otherUrls + ";" + data.get(1) + ";" + context;
            } else {
                linha = url + ";" + data.get(1) + ";" + context;
            }

            if (context == null || context.equals("")) {
                System.out.println("Barrel[" + this.index + "] [No description] failed to store in barrel");
                return;
            }

            try {
                FileWriter writer = new FileWriter(LINKSFILE, true);
                writer.write(linha);
                // System.out.println("Barrel[" + this.index + "] " + linha + " stored in
                // barrel");
                writer.write(System.getProperty("line.separator"));
                writer.close();
            } catch (IOException e) {
                System.err.println("Erro ao ler o ficheiro dos links [2]");
            }
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

        // System.out.println("==================================");
        // for (String info : data) {
        // System.out.println("Field: " + info);
        // }
        // System.out.println("==================================");

        String[] firstElement = data.get(0).split("\\|"); // url|referencedUrl1|referencedUrl2|...
        String url = firstElement[0];

        String[] words = data.get(2).split(";");

        for (String word : words) {
            boolean found = false;

            if (word == null || word.equals("")) {
                continue;
            }

            if (this.index % 2 == 0) {
                if (word.toLowerCase().charAt(0) >= 'm')
                    continue;
            } else {
                if (word.toLowerCase().charAt(0) < 'm')
                    continue;
            }

            for (String linha : lines) {
                String[] parts = linha.split(";");
                String wordInFile = parts[0].toLowerCase();
                List<String> links = Arrays.asList(parts).subList(1, parts.length);
                if (wordInFile.equals(word.toLowerCase())) {
                    if (!links.contains(url)) {
                        lines.set(lines.indexOf(linha), linha + ";" + url);
                    }
                    found = true;
                }
            }

            if (!found) {
                lines.add(word + ";" + url);
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
    @Override
    public List<String> linksToAPage(String word) throws FileNotFoundException, IOException {
        // this.linksMap format: url -> [title, context, referencedUrl1,
        // referencedUrl2,...]

        // Randomly throws RemoteException to simulate a crash
        if (Configuration.AUTO_FAIL_BARRELS) {
            int random = (int) (Math.random() * 2) + 1;
            if (random == 1) {
                System.out.println("Barrel[" + this.index + "] simulated a crash while searching for words");
                throw new RemoteException();
            }
        }

        List<String> result = new ArrayList<String>();

        for (String url : this.linksMap.keySet()) {
            ArrayList<String> info = this.linksMap.get(url);
            for (int i = 2; i < info.size(); i++) {
                if (info.get(i).equals(word)) {
                    result.add(url);
                }
            }
        }

        return result;
    }

    private void sendStatus(String status) throws IOException {

        // Compare this.stats with status
        if (this.stats.equals(status)) {
            return;
        }

        this.stats = status;

        InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
        MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT);

        String statusString = "type | Barrel; index | " + this.index + "; status | " + status + "; ip | "
                + Configuration.MULTICAST_ADDRESS + "; port | " + Configuration.MULTICAST_PORT + ";";

        byte[] buffer = statusString.getBytes();

        // System.out.println(statusString);

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, Configuration.MULTICAST_PORT);
        socket.send(packet);
        socket.close();
    }

    @Override
    public List<String> searchForWords(String word) throws FileNotFoundException, IOException {

        // Randomly throws RemoteException to simulate a crash
        if (Configuration.AUTO_FAIL_BARRELS) {
            int random = (int) (Math.random() * 2) + 1;
            if (random == 1) {
                System.out.println("Barrel[" + this.index + "] simulated a crash while searching for words");
                throw new RemoteException();
            }
        }

        String words[] = word.split(" ");
        auxMap.clear();

        // Gets the links that each words
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

        // Create a list with the results
        ArrayList<String> results = new ArrayList<String>();
        for (int i = 0; i < auxMap.size(); i++) {
            String key = (String) auxMap.keySet().toArray()[i];
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