package pl.jkarczewski.chat.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Class responsible for socket connection with remote server.
 */
public class ChatClientThread extends Thread {
    private Socket socket;
    private ChatClient client;
    private DataInputStream streamIn;
    private boolean working;

    /**
     * Constructor creating new ChatClientThread and setting all required streams.
     *
     * @param client ChatClient, which uses this thread
     * @param socket socket used to connect to server
     */
    public ChatClientThread(ChatClient client, Socket socket) {
        this.client = client;
        this.socket = socket;
        this.working = true;
        open();
    }

    /**
     * Method used to open new DataInputStream from socket.
     */
    public void open() {
        try {
            streamIn = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.err.println("ERROR getting input stream: " + ex);
        }
    }

    /**
     * Loop executed by thread, reads incoming data from socket.
     */
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

    /**
     * Method used to finish connection with remote server.
     */
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
