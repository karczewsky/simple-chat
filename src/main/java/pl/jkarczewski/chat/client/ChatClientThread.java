package pl.jkarczewski.chat.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClientThread extends Thread {
    private Socket socket;
    private ChatClient client;
    private DataInputStream streamIn;
    private boolean working;

    public ChatClientThread(ChatClient client, Socket socket) {
        this.client = client;
        this.socket = socket;
        this.working = true;
        open();
    }

    public void open() {
        try {
            streamIn = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.err.println("ERROR getting input stream: " + ex);
        }
    }

    public void run() {
        int errCount = 0;

        while (working) {
            try {
                String msg = streamIn.readUTF();
                client.handle(msg);
                errCount = 0;
            } catch (IOException ioe) {
                errCount++;
                if (errCount > 2) {
                    client.addMessageForUser("SYSTEM: Server is not responding, disconnecting...");
                    client.shut();
                }
            }
        }
    }

    public void shut() {
        working = false;

        try {
            streamIn.close();
            socket.close();
        } catch (IOException ex) {
            System.err.println("ERROR shutting down client chat thread");
        }
    }
}
