package pl.jkarczewski.chat.client;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ChatClient {
    private Controller controller;

    private Socket socket;
    private DataOutputStream streamOut;
    private ChatClientThread client;

    public ChatClient(Controller controller) {
        this.controller = controller;
    }

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

    public void send(String msg) {
        try {
            streamOut.writeUTF(msg.trim());
            streamOut.flush();
        } catch (IOException ex) {
            addMessageForUser("SYSTEM: Error sending message to server.");
        }
    }

    public void open() {
        try {
            streamOut = new DataOutputStream(socket.getOutputStream());
            client = new ChatClientThread(this, socket);
            client.start();
        } catch (IOException ex) {
            addMessageForUser("SYSTEM: Unknown error occurred: " + ex.getMessage());
        }
    }

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

    public void addMessageForUser(String msg) {
        controller.addMessage(msg);
    }


}
