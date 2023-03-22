package src;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.io.*;
import java.net.*;

import src.Downloader.Downloader;
import src.UrlQueue;

public class ServerThreadTcp extends Thread {
    private Socket socket;

    public ServerThreadTcp(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        System.out.println("Connected to server");

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            UrlQueue urlQueue = new UrlQueue();
            Queue<String> urls = urlQueue.getQueue();

            while (true) {
                try {
                    String url = urls.poll();

                    if (url == null) {
                        Thread.sleep(1000);
                        continue;
                    }

                    System.out.println("Sending url: " + url);
                    out.println(url);
                } catch (Exception e) {
                    continue;
                }
    
                // String message = in.readLine();
                // System.out.println("Message received: " + message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
