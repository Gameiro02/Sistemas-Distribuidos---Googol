package com.example.webappgoogol.UrlQueue;

import java.io.IOException;
import java.net.Socket;

import com.example.webappgoogol.Configuration;

import java.io.*;
import java.net.*;

public class QueueThread extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private UrlQueue urlQueue;

    public QueueThread(UrlQueue urlQueue, int port) throws IOException {
        if (port != Configuration.PORT_A && port != Configuration.PORT_B)
            throw new IllegalArgumentException("Invalid port number");

        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.urlQueue = urlQueue;
    }

    private void sendUrl() throws IOException {
        String url;

        synchronized (urlQueue) {
            url = urlQueue.getUrl();
        }

        if (url != null) {
            Socket socket = serverSocket.accept();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(url);
            socket.close();
            System.out.println("Sent url: " + url);
        }
    }

    private void receiveUrl() throws IOException {
        Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String url;
        boolean resend = false;
        while ((url = in.readLine()) != null) {
            if (url.startsWith("[RESEND]")) {
                url = url.substring(8);
                System.out.println("[RE-ADDED]: " + url);
                resend = true;
            }
            synchronized (urlQueue) {
                urlQueue.addUrl(url, resend);
            }
        }

        socket.close();
    }

    public void run() {
        while (true) {
            try {
                if (port == Configuration.PORT_A) {
                    sendUrl();
                } else if (port == Configuration.PORT_B) {
                    receiveUrl();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}