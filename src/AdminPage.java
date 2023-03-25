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
                System.out.println("Received: " + msg);
                update(msg);

                // Clear the terminal screen
                System.out.print("\033[H\033[2J");
                System.out.flush();

                printPanel();
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
            if (msg_split[1].equals("Active")) {
                this.downloaders.set(Integer.parseInt(msg_split[2]), "Active");
            } else if (msg_split[1].equals("Waiting")) {
                this.downloaders.set(Integer.parseInt(msg_split[2]), "Waiting");
            }
        } else if (msg_split[0].equals("BARREL")) {
            if (msg_split[1].equals("Active")) {
                this.barrels.set(Integer.parseInt(msg_split[2]), "Active");
            } else if (msg_split[1].equals("Waiting")) {
                this.barrels.set(Integer.parseInt(msg_split[2]), "Waiting");
            }
        } else {
            System.out.println("Error: " + msg);
        }
    }

    private void printPanel() {
        System.out.println("Downloaders: ");
        for (int i = 0; i < Configuration.NUM_DOWNLOADERS; i++) {
            System.out.println("Downloader[" + i + "] " + this.downloaders.get(i));
        }
        System.out.println("Barrels: ");
        for (int i = 0; i < Configuration.NUM_BARRELS; i++) {
            System.out.println("Barrel[" + i + "] " + this.barrels.get(i));
        }
        System.out.println("Most frequent searches: ");
        for (int i = 0; i < 10; i++) {
            System.out.println("Search[" + i + "] " + this.mostFrequentSearches.get(i));
        }
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
