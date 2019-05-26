package pl.jkarczewski.chat.client;

import org.apache.groovy.json.internal.IO;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ChatClient {
    private String username;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean working;

    private final Runnable hearthbeatTask = () -> {
        while (working) {
            if (!socket.isClosed()) {
                out.println("PING");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };

    private BlockingQueue<String> messageQueue;

    ChatClient(String host, int port, String username) throws IOException {
        this.working = true;
        this.socket = new Socket(host, port);

        if (username.contains(" "))
            throw new RuntimeException("Usernames with spaces are invalid");

        this.username = username;

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void work() {
        Thread thread = new Thread(hearthbeatTask);
        thread.start();
    }
}
