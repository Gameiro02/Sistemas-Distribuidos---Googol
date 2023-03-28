package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.*;
import java.io.IOException;

public class AdminPage {
    private ArrayList<String> downloaders;
    private ArrayList<String> barrels;
    private HashMap<String, Integer> searchDictionary;

    private String stringMenu;

    public AdminPage(HashMap<String, Integer> searchDictionary) {
        this.downloaders = new ArrayList<String>();
        this.barrels = new ArrayList<String>();
        this.searchDictionary = searchDictionary;
    }

    public void showMenu() {
        inicialize_arrays();
        stringMenu = generatePanelString();
        get_active_downloaders_and_barrels();

    }

    private void get_active_downloaders_and_barrels() {
        MulticastSocket socket = null;

        try {
            socket = new MulticastSocket(Configuration.MULTICAST_PORT_ADMIN);
            InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS_ADMIN);
            socket.joinGroup(group);

            while (true) {
                byte[] buffer = new byte[16384];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String msg = new String(packet.getData(), 0, packet.getLength());
                update(msg);

                stringMenu = generatePanelString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private void update(String msg) {
        String[] msg_split = msg.split(";");

        if (msg_split[0].equals("DOWNLOADER")) {
            if (msg_split[2].equals("Active")) {
                this.downloaders.set(Integer.parseInt(msg_split[1]) - 1, msg_split[3] + ":" + msg_split[4]);
            } else if (msg_split[2].equals("Waiting")) {
                this.downloaders.set(Integer.parseInt(msg_split[1]) - 1, "Waiting");
            } else if (msg_split[2].equals("Offline")) {
                this.downloaders.set(Integer.parseInt(msg_split[1]) - 1, "Offline");
            }
        } else if (msg_split[0].equals("BARREL")) {
            if (msg_split[2].equals("Active")) {
                this.barrels.set(Integer.parseInt(msg_split[1]) - 1,
                        "Active" + ":" + msg_split[3] + ":" + msg_split[4]);
            } else if (msg_split[2].equals("Waiting")) {
                this.barrels.set(Integer.parseInt(msg_split[1]) - 1, "Waiting");
            } else if (msg_split[2].equals("Offline")) {
                this.barrels.set(Integer.parseInt(msg_split[1]) - 1, "Offline");
            }
        }
    }

    private String generatePanelString() {
        StringBuilder sb = new StringBuilder();
        sb.append("------- Downloaders -------\n");
        for (int i = 0; i < Configuration.NUM_DOWNLOADERS; i++) {
            sb.append("Downloader[" + i + "] " + this.downloaders.get(i) + "\n");
        }

        sb.append("\n------- Barrels -------\n");
        for (int i = 0; i < Configuration.NUM_BARRELS; i++) {
            sb.append("Barrel[" + i + "] " + this.barrels.get(i) + "\n");
        }

        sb.append("\n------- Most Frequent Searches -------\n");
        if (this.searchDictionary.isEmpty()) {
            for (int i = 0; i < 10; i++)
                sb.append("Search[" + i + "] None\n");
        } else {

            for (int i = 0; i < 10 && i < this.searchDictionary.size(); i++) {
                int aux = i+1;
                if (this.searchDictionary.containsKey(this.searchDictionary.keySet().toArray()[i])) {
                    sb.append("Search[" + aux + "]: " + this.searchDictionary.keySet().toArray()[i] + " - "
                            + this.searchDictionary.get(this.searchDictionary.keySet().toArray()[i])
                            + "\n");
                }
            }
        }

        return sb.toString();
    }

    private void inicialize_arrays() {
        // Inicialize the downloaders with "Waiting";
        for (int i = 0; i < Configuration.NUM_DOWNLOADERS; i++) {
            this.downloaders.add("Waiting");
        }
        for (int i = 0; i < Configuration.NUM_BARRELS; i++) {
            this.barrels.add("Waiting");
        }
    }

    public void updateHashMap(HashMap<String, Integer> dic) {

        this.searchDictionary = dic;
        stringMenu = generatePanelString();
    }

    public String getStringMenu() {
        return this.stringMenu;
    }

}
