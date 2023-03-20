package src;

import java.io.*;
import java.net.*;
import java.util.*;

public class UrlQueue {

    private Queue<String> queue;

    public UrlQueue() {
        queue = new LinkedList<String>();
    }

    public static void main(String[] args) {
        int port = 4322;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                new ServerThreadTcp(socket).start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
