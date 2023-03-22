package src;

import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.net.*;

public class QueueThread extends Thread {
    private ServerSocket serverSocket;
    private static int PORT_A = 8080;
    private static int PORT_B = 8081;
    private int port;
    private UrlQueue urlQueue;

    public QueueThread(UrlQueue urlQueue, int port) throws IOException {
        if (port != PORT_A && port != PORT_B)
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

        while (in.ready()) {
            String url = in.readLine();
            synchronized (urlQueue) {
                urlQueue.addUrl(url);
            }
            System.out.println("Received url: " + url);
        }

        socket.close();
    }

    public void run() {
        while (true) {
            try {
                if (port == PORT_A) {
                    sendUrl();
                } else if (port == PORT_B)
                    receiveUrl();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
