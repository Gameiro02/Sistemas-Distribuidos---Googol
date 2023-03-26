package src;

import java.util.ArrayList;

import src.Barrels.Barrel;

import java.util.*;
import java.net.*;

import java.io.IOException;

public class AdminPage {
    // Lista de Downloaders ativos -> cada downloadter tem de mostrar o ip e o porto
    // Lista de Barrels ativos
    // 10 pesquisas mais frequentes

    private ArrayList<String> downloaders;
    private ArrayList<String> barrels;
    private ArrayList<String> mostFrequentSearches;

    private String stringMenu;

    public AdminPage() {
        this.downloaders = new ArrayList<String>();
        this.barrels = new ArrayList<String>();
        this.mostFrequentSearches = new ArrayList<String>();
    }

    public void showMenu() {
        // Show a list of active downloaders on the left side
        // Show a list of active barrels on the right side
        // Show a list of the 10 most frequent searches
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
                // System.out.println("Received: " + msg);
                update(msg);

                // If the new string is different from the old one, update the panel
                if (!stringMenu.equals(generatePanelString())) {
                    stringMenu = generatePanelString();
                    System.out.println(stringMenu);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private void update(String msg) {
        // Update the list of active downloaders
        // Update the list of active barrels
        // Update the list of the 10 most frequent searches

        String[] msg_split = msg.split(";");

        if (msg_split[0].equals("DOWNLOADER")) {
            if (msg_split[2].equals("Active")) {
                this.downloaders.set(Integer.parseInt(msg_split[1]) - 1, msg_split[3] + ":" + msg_split[4]);
            } else if (msg_split[2].equals("Waiting")) {
                this.downloaders.set(Integer.parseInt(msg_split[1]) - 1, "Waiting");
            }
        } else if (msg_split[0].equals("BARREL")) {
            if (msg_split[2].equals("Active")) {
                this.barrels.set(Integer.parseInt(msg_split[1]) - 1, "Active");
            } else if (msg_split[2].equals("Waiting")) {
                this.barrels.set(Integer.parseInt(msg_split[1]) - 1, "Waiting");
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
        for (int i = 0; i < 10; i++) {
            sb.append("Search[" + i + "] " + this.mostFrequentSearches.get(i) + "\n");
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
        for (int i = 0; i < 10; i++) {
            this.mostFrequentSearches.add("None");
        }
    }

}