package src.Barrels;

// Java class to store the index in barrels
// The class need to import from a file the current links of the barrel
// The page also need to store in a file the current links of the barrel
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.io.File;

import src.Configuration;

import java.io.FileWriter;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class OutdatedBarrel extends Thread implements BarrelInterface, Serializable {
    private String FILENAME;

    public Barrel(int index) throws IOException, RemoteException {
        this.FILENAME = "src\\Barrels\\BarrelFiles\\Barrel" + index + ".txt";

        File f = new File(FILENAME);

        if (f.exists()) {
            FileWriter writer = new FileWriter(FILENAME);
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

    public void receive_multicast() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(Configuration.MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
            socket.joinGroup(group);
            while (true) {
                byte[] buffer = new byte[16384];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String mensagem = new String(packet.getData(), 0, packet.getLength());

                ArrayList<String> data;
                data = textParser(mensagem);
                writeToFile(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private ArrayList<String> textParser(String text) throws IOException {
        String[] fields = text.split(";");
        ArrayList<String> data = new ArrayList<String>();

        data.add(fields[0]); // url
        data.add(fields[1]); // title

        for (int i = 2; i < fields.length; i++) {
            data.add(fields[i]);
        }

        return data;
    }

    private void writeToFile(ArrayList<String> data) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(FILENAME));
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        br.close();

        for (String fields : data) {
            String

            boolean found = false;
            for (String linha : lines) {
                String[] parts = linha.split(";");
                String word = parts[0];
                List<String> links = Arrays.asList(parts).subList(1, parts.length);
                if (word.equals(wordToAdd)) {
                    if (!links.contains(linkToAdd)) {
                        lines.set(lines.indexOf(linha), linha + ";" + linkToAdd);
                    }
                    found = true;
                }
            }
            if (!found) {
                lines.add(wordToAdd + ";" + linkToAdd);
            }
        }

        // Write to file
        FileWriter writer = new FileWriter(FILENAME);
        for (String linha : lines) {
            writer.write(linha);
            writer.write(System.getProperty("line.separator"));
        }
        writer.close();
    }

    // Searches for a single word in the barrel
    public String searchForWord(String word) throws IOException {
        // Read file
        List<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(FILENAME));
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        br.close();

        // Search for word
        for (String linha : lines) {
            String[] parts = linha.split(";");
            String wordInFile = parts[0];
            if (wordInFile.equals(word)) {
                List<String> links = Arrays.asList(parts).subList(1, parts.length);
                return String.join(";", links);
            }
        }
        return "Word not found";
    }

    // Searches for multiple words in the barrel
    @Override
    public String searchForWords(String words) throws IOException {
        String[] wordsArray = words.split(" ");
        HashMap<String, Integer> links = new HashMap<String, Integer>();
        for (String word : wordsArray) {
            String linksForWord = searchForWord(word);
            if (!linksForWord.equals("Word not found")) {
                String[] linksArray = linksForWord.split(";");
                for (String link : linksArray) {
                    if (links.containsKey(link)) {
                        links.put(link, links.get(link) + 1);
                    } else {
                        links.put(link, 1);
                    }
                }
            }
        }

        // Return only the links that appear in all words
        HashSet<String> linksSet = new HashSet<String>(Arrays.asList(links.keySet().toArray(new String[0])));
        for (String link : links.keySet()) {
            if (links.get(link) != wordsArray.length) {
                linksSet.remove(link);
            }
        }

        if (linksSet.size() == 0) {
            return "No links found";
        }

        return String.join("\n", linksSet);
    }

}
