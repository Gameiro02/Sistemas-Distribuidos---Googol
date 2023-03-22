package src;

import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.io.*;
import java.net.*;

public class QueueThread extends Thread {
    private ServerSocket serverSocket;
    private int PORT = 8080;

    private Queue<String> urlQueue;

    public QueueThread() throws IOException {
        serverSocket = new ServerSocket(PORT);

        UrlQueue urlQueue = new UrlQueue();
        this.urlQueue = urlQueue.getQueue();
    }

    private void sendUrl() throws IOException {
        synchronized (urlQueue) {
            if (!urlQueue.isEmpty()) {
                Socket socket = serverSocket.accept();

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String url = urlQueue.poll();
                out.println(url);
                System.out.println("Sent url: " + url);

                socket.close();
            }
        }
    }

    public void run() {
        while (true) {
            try {
                sendUrl();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
