package src.UrlQueue;

import java.io.*;
import java.net.*;
import java.util.*;

import src.Configuration;

public class UrlQueue {

    private Queue<String> queue;
    private ArrayList <String> visited;

    public UrlQueue() {
        queue = new LinkedList<String>();
        visited = new ArrayList<String>();
        // queue.add("http://127.0.0.1:5500/Tests/Test_Site1.html");
        // visited.add("http://127.0.0.1:5500/Tests/Test_Site1.html");
    }

    public void addUrl(String url) {
        if (visited.contains(url))
            return;

        System.out.println("Added url: " + url);
        queue.add(url);
        visited.add(url);
    }

    public String getUrl() {
        if (queue.isEmpty())
            return null;
        return queue.poll();
    }

    public static void main(String[] args) throws UnknownHostException, IOException {

        UrlQueue urlQueue = new UrlQueue();

        QueueThread queueSend = new QueueThread(urlQueue, Configuration.PORT_A);
        QueueThread queueReceive = new QueueThread(urlQueue, Configuration.PORT_B);
        queueSend.start();
        queueReceive.start();
    }

    public void printQueue() {
        for (String url : queue) {
            System.out.println(url);
        }
    }
}
