package src.Downloader;

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

    private int ID;

    private int port = 4321;
    private String MULTICAST_ADDRESS = "224.3.2.1";

    /* TCP */
    private int port_tcp = 8080;
    String hostname_tcp = "localhost";
    ServerSocket serverSocket;
    Socket clientSocket;

    public Downloader(int ID) {
        this.ID = ID;
        this.links = new HashSet<String>();
        this.words = new HashSet<String>();
    }

    public void run() {

        while (true) {
            try {
                this.url = getNextUrl();
                if (this.url == null) {
                    System.out.println("No more urls to download");
                    continue;
                }

                this.doc = Jsoup.connect(this.url).get();
                extractLinks();
                extractWords();
                convertToString();
                sendWords();
                // sendLink();

                System.out.println("Downloader[" + this.ID + "] " + "downloaded: " + this.url);

            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
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

    private String getNextUrl() throws IOException {
        Socket socket = new Socket(hostname_tcp, port_tcp);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String url = in.readLine();

        System.out.println("Downloader[" + this.ID + "] " + "received url: " + url);
        socket.close();

        return url;
    }
}
