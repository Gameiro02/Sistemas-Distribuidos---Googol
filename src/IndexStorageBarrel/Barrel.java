package src.IndexStorageBarrel;

// Java class to store the index in barrels
// The class need to import from a file the current links of the barrel
// The page also need to store in a file the current links of the barrel
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.io.File;
import java.util.HashMap;
import java.io.FileWriter;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Barrel {
    private int index; // Numero do barrel para ter um ficehiro so para si
    private HashMap<String, ArrayList<String>> dicionario = new HashMap<String, ArrayList<String>>();

    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;

    public Barrel(int index) {
        this.index = index;
    }

    public void run() {
        readFromtextFile();
        receive_multicast();
        writeToTextFile();
    }

    public void readFromtextFile() {

        File f = new File("src\\IndexStorageBarrel\\BarrelFiles\\Barrel" + index + ".txt");
        if (!f.exists()) {
            return;
        }

        try {
            FileInputStream fstream = new FileInputStream(
                    "src\\IndexStorageBarrel\\BarrelFiles\\Barrel" + index + ".txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            while ((strLine = br.readLine()) != null) {
                String[] words = strLine.split(";");
                String key = words[0];

                ArrayList<String> values = new ArrayList<String>();
                for (int i = 1; i < words.length; i++) {
                    values.add(words[i]);
                }

                dicionario.put(key, values);
            }

            br.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

    }

    public void writeToTextFile() {

        String directoryPath = "src\\IndexStorageBarrel\\BarrelFiles";

        try {
            FileWriter writer = new FileWriter(directoryPath + "\\Barrel" + index + ".txt");

            System.out.println("Writing to file: " + directoryPath + "\\Barrel" + index + ".txt");

            for (String key : dicionario.keySet()) {
                // Print the key and the values

                System.out.println(key + " " + dicionario.get(key));

                writer.write(key + ";");

                for (int i = 0; i < dicionario.get(key).size(); i++) {
                    if (i == dicionario.get(key).size() - 1) {
                        writer.write(dicionario.get(key).get(i));
                    } else {
                        writer.write(dicionario.get(key).get(i) + ";");
                    }
                }
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao criar o arquivo: " + e.getMessage());
            return;
        }
    }

    public void receive_multicast() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(PORT); // create socket and bind it
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            while (true) {
                byte[] buffer = new byte[16384];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // System.out.println("Received packet from " +
                // packet.getAddress().getHostAddress() + ":"
                // + packet.getPort() + " with message:");

                String mensagem = new String(packet.getData(), 0, packet.getLength());
                // System.out.println(mensagem);
                textParser(mensagem);
                writeToTextFile();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private void textParser(String text) {
        String[] words = text.split("; ");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            // Format: word | link
            String[] wordAndLink = word.split(" | ");

            // Check if word is already in the hash map
            if (dicionario.containsKey(wordAndLink[0])) {
                // Check if the link is already in the list of links
                if (!dicionario.get(wordAndLink[0]).contains(wordAndLink[2])) {
                    dicionario.get(wordAndLink[0]).add(wordAndLink[2]);
                }
            } else {
                ArrayList<String> links = new ArrayList<String>();
                links.add(wordAndLink[2]);
                dicionario.put(wordAndLink[0], links);
            }
        }

    }

    // Print the content of the hash map line by line "Link: word1, word2,(...)"
    // If its the last line do not print the , at the end
    public void printHashMap() {

        for (String key : dicionario.keySet()) {
            System.out.print(key + ": ");

            for (int i = 0; i < dicionario.get(key).size(); i++) {
                if (i == dicionario.get(key).size() - 1) {
                    System.out.print(dicionario.get(key).get(i));
                } else {
                    System.out.print(dicionario.get(key).get(i) + ", ");
                }
            }

            System.out.println();
        }
    }

}
