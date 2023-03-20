package src.Downloader;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.*;
import java.io.*;

public class Downloader extends Thread {
    private String url;
    private Document doc;
    private HashSet<String> links;
    private HashSet<String> words;
    private String data;

    private int port = 4321;
    private String MULTICAST_ADDRESS = "224.3.2.1";

    /* TCP */
    private int port_tcp = 4322;
    String hostname_tcp = "localhost";

    public Downloader(String url) {
        this.url = url;
        this.links = new HashSet<String>();
        this.words = new HashSet<String>();
    }

    public void run() {
        try {
            this.doc = Jsoup.connect(url).get();
            extractLinks();
            extractWords();

            convertToString();

            sendWords();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send words to IndexStorageBarrel via TCP
        try {
            sendWordsTCP();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractLinks() {
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            this.links.add(link.attr("abs:href"));
        }
    }

    private void extractWords() {
        String[] words = doc.text().split(" ");
        for (String word : words) {

            if (word.contains("|") || word.contains(";") || word.contains("\n"))
                continue;

            this.words.add(word);
        }
    }

    private void convertToString() {
        String text = "";
        for (String word : words) {
            text += word + " | " + this.url + "; ";
        }

        this.data = text;
    }

    private void sendWords() throws Exception {
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        MulticastSocket socket = new MulticastSocket(port);

        byte[] buffer = data.getBytes();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
        socket.close();
    }

    private void sendWordsTCP() throws Exception {
        try (Socket socket = new Socket(hostname_tcp, port_tcp)) {

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            writer.println("Hello from " + socket.getLocalSocketAddress());

        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
