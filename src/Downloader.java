package src;

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

    public Downloader(int ID) {
        this.ID = ID;
        this.links = new HashSet<String>();
        this.words = new HashSet<String>();
    }

    public void run() {

        while (true) {
            try {
                this.url = getUrl();
                if (this.url == null) {
                    System.out.println("No more urls to download");
                    continue;
                }

                this.doc = Jsoup.connect(this.url).get();
                download();
                convertToString();
                sendWords();
                // sendLink();

                System.out.println("Downloader[" + this.ID + "] " + "downloaded: " + this.url);
                sendLinkToQueue();

                clear();

            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private void download() {
        String[] words = doc.text().split(" ");
        for (String word : words) {

            if (word.contains("|") || word.contains(";") || word.contains("\n"))
                continue;

            this.words.add(word);
        }

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            this.links.add(link.attr("abs:href"));
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
        InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
        MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT);

        byte[] buffer = data.getBytes();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, Configuration.MULTICAST_PORT);
        socket.send(packet);
        socket.close();
    }

    private String getUrl() throws IOException {
        Socket socket = new Socket("localhost", Configuration.PORT_A);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String url = in.readLine();
        socket.close();

        System.out.println("Downloader[" + this.ID + "] " + "received url: " + url);
        return url;
    }

    private void sendLinkToQueue() throws IOException{
        Socket socket = new Socket("localhost", Configuration.PORT_B);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        for (String link : links) {
            out.println(link);
            System.out.println("Downloader[" + this.ID + "] " + "sent url: " + link);
        }

        socket.close();
    }

    private void clear() {
        this.links.clear();
        this.words.clear();
        this.data = "";
    }
}