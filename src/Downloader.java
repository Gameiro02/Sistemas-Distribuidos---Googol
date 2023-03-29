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
            this.url = getUrl();
            if (this.url == null) {
                System.out.println("No more urls to download");
                continue;
            }

            sendStatus("Active");

            try {
                this.doc = Jsoup.connect(this.url).get();
            } catch (Exception e) {
                // This url doesn't need to be sent to the queue
                // It failed to download, because it is not a valid url
                System.err.println("Downloader[" + this.ID + "] [Not valid] failed to download url: " + this.url);
            }

            try {
                download();

                if (this.title == null || this.title.equals("")) {
                    // This url doesn't need to be sent to the queue
                    // It doesn't have a title, so it's not a valid url
                    System.err.println("Downloader[" + this.ID + "] [No title] failed to download url: " + this.url);
                    continue;
                }

                sendWords();
                sendLinkToQueue();

                // if (this.ID == 1) {
                // throw new Exception();
                // }

                clear();

            } catch (Exception e) {
                System.err.println("Downloader[" + this.ID + "] stopped working!");
                e.printStackTrace();
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
        try {
            title = doc.title();
        } catch (NullPointerException e) {
            return;
        }
        this.title = title;
        this.title = this.title.replace("|", "");
        this.title = this.title.replace(";", "");
        this.title = this.title.replace("\n", "");

        String[] words = doc.text().split(" ");
        for (String word : words) {
            if (word.contains("|") || word.contains(";") || word.contains("\n"))
                continue;

            this.words += word + ";";
        }

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String url = link.attr("abs:href");
            url = url.replace("|", "");
            url = url.replace(";", "");
            url = url.replace("\n", "");
            this.links.add(url);
        }
    }

    private void sendWords() throws Exception {

        // Protocol :
        // type | url; item_count | number; url | www.example.com; referenced_urls |
        // url1 url2 url3; title | title; words | word1 word2 word3

        // TODO: Only allow to send 10 links
        
        String referencedUrls = "type | url; item_count | " + this.links.size() + "; url | " + this.url
                + "; referenced_urls | ";

        if (this.links.size() == 0)
            referencedUrls += "None; ";

        int linkCount = 0;
        for (String link : this.links) {
            if (linkCount++ == Configuration.MAXIMUM_REFERENCE_LINKS) {
                referencedUrls += "; ";
                break;
            }
            // if its the last link, dont add a space
            if (link == this.links.toArray()[this.links.size() - 1])
                referencedUrls += link + "; ";
            else
                referencedUrls += link + " ";
        }

        if (this.title == null)
            this.title = "None; ";

        if (this.words == null)
            this.words = "None";

        this.words = this.words.replace(";", " ");

        referencedUrls += "title | " + this.title + "; " + "words | " + this.words;
        this.data = referencedUrls;


        InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
        MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT);

        byte[] buffer = this.data.getBytes();

        if (buffer.length > 65534) {
            System.err.println("Downloader[" + this.ID + "] [Page too long] " + "failed to send url to queue");
            socket.close();
            return;
        }

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, Configuration.MULTICAST_PORT);
        socket.send(packet);
        socket.close();
    }

    private String getUrl() {
        String url;
        try {
            Socket socket = new Socket("localhost", Configuration.PORT_A);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            url = in.readLine();
            socket.close();
        } catch (Exception e) {
            System.err.println("Downloader[" + this.ID + "] " + "failed to get url from queue");
            e.printStackTrace();
            return null;
        }

        return url;
    }

    private void sendLinkToQueue() throws IOException {
        try {
            Socket socket = new Socket("localhost", Configuration.PORT_B);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            for (String link : links) {
                out.println(link);
            }

            socket.close();
        } catch (Exception e) {
            // If this fails, the url needs to be sent to the queue again
            System.err.println("[NEED TO HANDLE] Downloader[" + this.ID + "] " + "failed to send url to queue");
        }
    }

    private void sendStatus(String status) {
        try {
            InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS_ADMIN);
            MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT_ADMIN);

            // Protocol : "type | Downloader; index | 1; ip | 192.168.1.1; port | 1234"
            String statusString = "type | Downloader; index | " + this.ID + "; status | " + status + "; ip | "
                    + InetAddress.getLocalHost().getHostAddress() + "; port | " + Configuration.PORT_A;

            byte[] buffer = statusString.getBytes();

            // System.out.println(statusString);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group,
                    Configuration.MULTICAST_PORT_ADMIN);
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            System.err.println("Downloader[" + this.ID + "] " + "failed to send status to admin");
        }
    }

    private void clear() {
        this.links.clear();
        this.words = "";
        this.data = "";
    }
}
