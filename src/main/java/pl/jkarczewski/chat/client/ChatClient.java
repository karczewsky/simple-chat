package pl.jkarczewski.chat.client;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Class which holds all business logic of chat app.
 */
public class ChatClient {
    private Controller controller;

    private Socket socket;
    private DataOutputStream streamOut;
    private ChatClientThread client;

    public ChatClient(Controller controller) {
        this.controller = controller;
    }

    /**
     * Method preparing new connection to remote server.
     *
     * @param serverName hostname of remote server
     * @param serverPort port on which remote server is running
     */
    public void connect(String serverName, int serverPort) {
        try {
            socket = new Socket(serverName, serverPort);
            addMessageForUser("Connected to server: " + socket);
            open();

            controller.setConnected(true);
        } catch (IOException ex) {
            addMessageForUser("SYSTEM: Unknown error occurred: " + ex.getMessage());
        }
    }

    /**
     * Method used to send bare UTF message to remote server.
     *
     * @param msg String message sent to server
     */
    public void send(String msg) {
        try {
            streamOut.writeUTF(msg.trim());
            streamOut.flush();
        } catch (IOException ex) {
            addMessageForUser("SYSTEM: Error sending message to server.");
        }
    }

    /**
     * Method used to open required streams and set up {@link ChatClientThread}.
     */
    public void open() {
        try {
            streamOut = new DataOutputStream(socket.getOutputStream());
            client = new ChatClientThread(this, socket);
            client.start();
        } catch (IOException ex) {
            addMessageForUser("SYSTEM: Unknown error occurred: " + ex.getMessage());
        }
    }


    /**
     * Method responsible for streams shutdown and safe exit of {@link ChatClientThread}.
     */
    public void shut() {
        client.shut();
        try {
            streamOut.close();
        } catch (IOException ex) {
            System.err.println("ERROR closing connection");
        }

        addMessageForUser("SYSTEM: Disconnected from server.");
        controller.setConnected(false);
    }


    /**
     * Method performing necessary data parse of data, retrieved from server.
     *
     * @param msg Message received from remote server
     */
    public void handle(String msg) {
        String[] arr = msg.split(" ");

        switch (arr[0]) {
            case "MSG":
                if (arr.length >= 3) {
                    String[] cp = Arrays.copyOfRange(arr, 2, arr.length);
                    addMessageForUser(arr[1] + ": " + String.join(" ", cp));
                }
                break;
        }

    }

    /**
     * Method used to to add new message/notification to chat log.
     *
     * @param msg
     */
    public void addMessageForUser(String msg) {
        controller.addMessage(msg);
    }


}
