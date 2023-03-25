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
    private String words;
    private String title;
    private String data;

    private int ID;

    public Downloader(int ID) {
        this.ID = ID;
        this.links = new HashSet<String>();
        this.words = "";
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
                sendWords();

                System.out.println("Downloader[" + this.ID + "] " + "downloaded: " + this.url);
                sendLinkToQueue();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private void download() {
        String title = doc.title();
        this.title = title;

        String[] words = doc.text().split(" ");
        for (String word : words) {
            if (word.contains("|") || word.contains(";") || word.contains("\n"))
                continue;

            this.words += word + ";";
        }

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            this.links.add(link.attr("abs:href"));
        }
    }

    private void sendWords() throws Exception {
        String referencedUrls = "";
        for (String link : this.links) {
            referencedUrls += link + "|";
        }
        this.data = this.url + "|" + referencedUrls + ";" + this.title + ";" + this.words;

        InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
        MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT);

        byte[] buffer = this.data.getBytes();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, Configuration.MULTICAST_PORT);
        socket.send(packet);
        socket.close();
    }

    private String getUrl() throws IOException {
        Socket socket = new Socket("localhost", Configuration.PORT_A);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String url = in.readLine();
        socket.close();

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
}
