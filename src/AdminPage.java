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
                // System.out.println("Received: " + msg);
                update(msg);

                System.out.println(generatePanelString());
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

    private String generatePanelString() {
        // Define o cabeçalho centralizado
        String header = String.format("%50s", "Admin page\n");

        // Define o cabeçalho das colunas Downloaders, Barrels e Searches
        String columnHeader = String.format("%-30s %-30s %s\n", "Downloaders", "Barrels", "Most frequent searches:");

        // Itera sobre todos os Downloaders, Barrels e Searches e os exibe lado a lado
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        sb.append(columnHeader);
        for (int i = 0; i < Configuration.NUM_DOWNLOADERS || i < Configuration.NUM_BARRELS || i < 10; i++) {
            // Cria a string formatada para o Downloader atual
            String downloaderStr = "";
            if (i < Configuration.NUM_DOWNLOADERS) {
                String downloaderName = "Downloader[" + i + "]: ";
                downloaderStr = String.format("%-30s", downloaderName + this.downloaders.get(i));
            }

            // Cria a string formatada para o Barrel atual
            String barrelStr = "";
            if (i < Configuration.NUM_BARRELS) {
                String barrelName = "Barrel[" + i + "]: ";
                barrelStr = String.format("%-30s", barrelName + this.barrels.get(i));
            }

            // Cria a string formatada para a Search atual
            String searchStr = "";
            if (i < 10) {
                searchStr = "- Search[" + i + "]: " + this.mostFrequentSearches.get(i);
            }

            // Exibe os elementos lado a lado, separados por tabulações
            sb.append(String.format("%-30s %-30s\t%s\n", downloaderStr, barrelStr, searchStr));
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
