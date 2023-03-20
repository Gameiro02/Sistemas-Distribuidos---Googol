package src.Downloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import src.dados;

public class Downloader extends Thread {
    private String url;
    private Document doc;
    private HashSet<String> links;
    private HashSet<String> words;
    private dados data;

    private int port = 1337;

    public Downloader(String url) {
        this.url = url;
        this.links = new HashSet<String>();
        this.words = new HashSet<String>();
        this.data = new dados();
        this.data.url = this.url;
    }

    public HashSet<String> getLinks() {
        return links;
    }

    public HashSet<String> getWords() {
        return words;
    }

    public void run() {
        try {
            this.doc = Jsoup.connect(url).get();
            extractLinks();
            extractWords();

            sendWords();

        } catch (IOException e) {
            e.printStackTrace();
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
            this.words.add(word);
        }

        data.words = this.words;
    }


    private void sendWords() throws Exception {
        InetAddress group = InetAddress.getByName("224.3.2.1");
        MulticastSocket socket = new MulticastSocket(port);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this.data);
        byte[] buffer = baos.toByteArray();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
        socket.close();
    }
}
