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
import java.util.HashMap;
import java.util.List;

import src.Configuration;

public class Barrel extends Thread implements BarrelInterface, Serializable {
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

        data.add(fields[0]); // url
        data.add(fields[1]); // title

        for (int i = 2; i < fields.length; i++) {
            data.add(fields[i]);
        }

        return data;
    }

    private void writeToFile(ArrayList<String> data) throws IOException {
        FileWriter writer = new FileWriter(FILENAME, true);

        if (linkExists(data.get(0))) {
            writer.close();
            return;
        }

        for (int i = 0; i < data.size(); i++) {
            writer.write(data.get(i) + ";");
        }

        writer.write("\n");
        writer.close();
    }

    private boolean linkExists(String link) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(";");
            if (fields[0].equals(link)) {
                reader.close();
                return true;
            }
        }
        reader.close();
        return false;
    }

    @Override
    public List<String> searchForWords(String word) throws FileNotFoundException, IOException {
        HashMap<String, Integer> linksHash = new HashMap<String, Integer>();

        String[] words = word.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].toLowerCase();
            List<String> links = searchForWord(words[i]);
            for (String link : links) {
                if (linksHash.containsKey(link)) {
                    linksHash.put(link, linksHash.get(link) + 1);
                } else {
                    linksHash.put(link, 1);
                }
            }   
        }

        for (String link : linksHash.keySet()) {
            if (linksHash.get(link) != words.length) {
                linksHash.remove(link);
            }
        }

        // Return the link, the title, and some context
        List<String> result = new ArrayList<String>();
        for (String link : linksHash.keySet()) {
            BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";");
                if (fields[0].equals(link)) {
                    result.add(fields[0] + ";" + fields[1] + ";" + getContext(fields));
                }
            }
            reader.close();
        }
        
        return result;
    }

    private String getContext(String[] fields) {
        String context = "";
        for (int i = 2; i < 2 + Configuration.CONTEXT_SIZE; i++) {
            context += fields[i] + " ";
        }
        return context;
    }

    public List<String> searchForWord(String word) throws IOException {
        List<String> links = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(";");
            for (int i = 2; i < fields.length; i++) {
                if (fields[i].toLowerCase().equals(word)) {
                    if (!links.contains(fields[0]))
                        links.add(fields[0]);
                }
            }
        }
        reader.close();
        return links;
    }

}
