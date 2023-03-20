package src;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.io.*;
import java.net.*;

import src.Downloader.Downloader;

public class ServerThreadTcp extends Thread {

    private Queue<String> queue = new LinkedList<String>();

    private Socket socket;

    public ServerThreadTcp(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        System.out.println("Connected to server");

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String message = in.readLine();
            System.out.println("Message received: " + message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
