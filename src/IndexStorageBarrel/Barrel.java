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

        // Read from text file
        try {
            // Open the file
            FileInputStream fstream = new FileInputStream(
                    "src\\IndexStorageBarrel\\BarrelFiles\\Barrel" + index + ".txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            // TextFile Format:
            // Link;word1;word2;word3;word4;word5;word6;word7;word8;word9;word10(...)

            String strLine;

            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                // System.out.println(strLine);

                // Split the line
                String[] words = strLine.split(";");

                // the first word is the key of the hash map
                String key = words[0];

                // the rest of the words are the values of the hash map
                ArrayList<String> values = new ArrayList<String>();

                for (int i = 1; i < words.length; i++) {
                    values.add(words[i]);
                }

                // Add the key and the values to the hash map
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

            for (String key : dicionario.keySet()) {
                // write the key
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
                System.out.println(mensagem);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public void printHashMap() {
        // Print the content of the hash map line by line "Link: word1, word2,(...)"
        // If its the last line do not print the , at the end

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

    @Override
    public String toString() {
        return "Barrel []";
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public HashMap<String, ArrayList<String>> getDicionario() {
        return dicionario;
    }

    public void setDicionario(HashMap<String, ArrayList<String>> dicionario) {
        this.dicionario = dicionario;
    }

}
