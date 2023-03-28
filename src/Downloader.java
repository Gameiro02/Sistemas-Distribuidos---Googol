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

    public void run() throws RuntimeException {
        try {
            sendStatus("Waiting");
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                this.url = getUrl();
                if (this.url == null) {
                    System.out.println("No more urls to download");
                    continue;
                }

                sendStatus("Active");

                // System.out.println("Downloader[" + this.ID + "] " + "downloading: " +
                // this.url);
                this.doc = Jsoup.connect(this.url).get();
                download();
                sendWords();

                // System.out.println("Downloader[" + this.ID + "] " + "downloaded: " +
                // this.url);
                sendLinkToQueue();

                clear();

                if (this.ID == 1) {
                    throw new Exception();
                }
            } catch (Exception e) {
                System.err.println("Downloader[" + this.ID + "] stopped working!");
                try {
                    sendStatus("Offline");
                    return;
                } catch (Exception e1) {
                    System.err.println("Faild to send Downloader[" + this.ID + "] status");
                }
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

    private void sendLinkToQueue() throws IOException {
        Socket socket = new Socket("localhost", Configuration.PORT_B);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        for (String link : links) {
            out.println(link);
            System.out.println("Downloader[" + this.ID + "] " + "sent url: " + link);
        }

        socket.close();
    }

    private void sendStatus(String status) throws IOException {
        InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS_ADMIN);
        MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT_ADMIN);

        // Protocol : "type | Downloader; index | 1; ip | 192.168.1.1; port | 1234"
        String statusString = "type | Downloader; index | " + this.ID + "; status | " + status + "; ip | "
                + InetAddress.getLocalHost().getHostAddress() + "; port | " + Configuration.PORT_A;

        byte[] buffer = statusString.getBytes();

        // System.out.println(statusString);

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, Configuration.MULTICAST_PORT_ADMIN);
        socket.send(packet);
        socket.close();
    }

    private void clear() {
        this.links.clear();
        this.words = "";
        this.data = "";
    }
}
