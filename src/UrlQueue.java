package src;

import java.io.*;
import java.net.*;
import java.util.*;

public class UrlQueue {

    private Queue<String> queue;

    public UrlQueue() {
        queue = new LinkedList<String>();
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        int port = 8080;

        Socket socket = new Socket("localhost", port);

        ServerThreadTcp serverThreadTcp = new ServerThreadTcp(socket);
        serverThreadTcp.start();
    }
}
