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

            clear();

            try {
                this.url = getUrl();
            } catch (InterruptedException e) {
                System.err.println("Downloader[" + this.ID + "] [ON WHILE TRUE] failed to get url from queue");
            }

            if (this.url == null) {
                System.out.println("No more urls to download");
                continue;
            }

            sendStatus("Active");

            try {
                this.doc = Jsoup.connect(this.url).get();

                // This url doesn't need to be sent to the queue
                // It failed to download, because it is not a valid url
            } catch (ConnectException e) {
                System.out.println(
                        "Downloader[" + this.ID + "] [Connection failed] failed to connect to url: " + this.url);
                continue;
            } catch (Exception e) {
                System.err.println("Downloader[" + this.ID + "] [Not valid] failed to download url: " + this.url);
                continue;
            }

            try {

                if (Configuration.AUTO_FAIL_DOWNLOADERS) {
                    int random = (int) (Math.random() * 5) + 1;
                    if (this.ID == random) {
                        System.out.println("Downloader[" + this.ID + "] Simulated a crash");
                        throw new Exception();
                    }
                }

                download();

                if (this.title == null || this.title.equals("")) {
                    // This url doesn't need to be sent to the queue
                    // It doesn't have a title, so it's not a valid url
                    System.err.println("Downloader[" + this.ID + "] [No title] failed to download url: " + this.url);
                    continue;
                }

                sendWords();
                sendLinkToQueue();

            } catch (Exception e) {
                System.err.println("Downloader[" + this.ID + "] stopped working!");

                // Send current link to queue
                try {
                    this.links.clear();
                    this.links.add(this.url);
                    sendLinkToQueue();
                } catch (Exception e1) {
                    System.err.println("Downloader[" + this.ID + "] failed to send url to queue");
                }

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

    private String getUrl() throws InterruptedException {
        String url = null;
        while (url == null) {
            try {
                Socket socket = new Socket("localhost", Configuration.PORT_A);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                url = in.readLine();
                socket.close();
            } catch (Exception e) {
                System.err.println("Downloader[" + this.ID + "] " + "failed to get url from queue, trying again in 3 seconds");
                Thread.sleep(3000); // Wait 3 second before trying again
            }
        }

        return url;
    }

    private void sendLinkToQueue() throws IOException, InterruptedException {

        int numberTries = 0;
        boolean success = false;
        while (!success) {
            try {
                Socket socket = new Socket("localhost", Configuration.PORT_B);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                for (String link : links) {
                    out.println(link);
                }

                socket.close();
                success = true;
            } catch (Exception e) {
                // If this fails, the url needs to be sent to the queue again
                numberTries++;
                System.err.println(
                        "Downloader[" + this.ID + "] [Attempts: " + numberTries + "] " + "failed to send url to queue, trying again in 3 seconds");
                Thread.sleep(3000);
            }
        }
    }

    private void sendStatus(String status) {
        try {
            InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
            MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT);

            // Protocol : "type | Downloader; status | Active; url | www.example.com;
            String statusString = "type | Downloader; index | " + this.ID + "; status | " + status + "; url | "
                    + this.url;

            byte[] buffer = statusString.getBytes();

            // System.out.println(statusString);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group,
                    Configuration.MULTICAST_PORT);
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
