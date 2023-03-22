package src;

import java.io.*;
import java.net.*;
import java.util.*;

public class UrlQueue {

    private Queue<String> queue;

    public UrlQueue() {
        queue = new LinkedList<String>();

        queue.add("https://www.google.com");
        queue.add("https://www.uc.pt");
        queue.add("https://www.facebook.com");
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        QueueThread queueThread = new QueueThread();
        queueThread.start();
    }

    public Queue<String> getQueue() {
        return queue;
    }
}
