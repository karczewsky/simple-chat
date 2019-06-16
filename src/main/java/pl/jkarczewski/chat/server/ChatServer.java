package pl.jkarczewski.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
    private ServerSocket serverSocket;
    private Vector<ChatServerThread> clients;

    public ChatServer(int port) {
        clients = new Vector<>();

        try {
            System.out.println("Binding to port " + port + ", please wait ...");
            serverSocket = new ServerSocket(port);
            System.out.println("Port successfully bound");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        System.out.println("Server started: " + serverSocket);

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                ChatServerThread thread = new ChatServerThread(this, clientSocket);

                thread.start();

                clients.add(thread);

            } catch (IOException ex) {
                System.err.println("ERROR Server encountered error during client accept: " + ex.getMessage());
            }
        }
    }

    public void handleMessage(ChatServerThread originThread, String message) {
        String[] arr = message.split(" ");

        switch (arr[0]) {
            // format: !JOINED <NICKNAME>
            case "!JOINED":
                if (arr.length > 1)
                    originThread.setNickname(arr[1]);
                    sendSystemNotification(originThread.getNickname() + " joined, give them warm welcome!");
                break;
            default:
                sendToOtherClients(originThread, message);
                break;
        }

    }

    public void sendSystemNotification(String message) {
        for (ChatServerThread client: clients) {
            client.sendMessage("MSG SYSTEM " + message);
        }
    }

    public void sendToOtherClients(ChatServerThread originThread, String message) {
        for (ChatServerThread client: clients) {
            client.sendMessage("MSG " + originThread.getNickname() + " " + message);
        }
    }

    public void removeClient(ChatServerThread clientThread) {
        System.out.println("Removing client thread " + clientThread.getIdentifier() + " Nickname: " + clientThread.getNickname());
        clients.remove(clientThread);
        sendSystemNotification(clientThread.getNickname() + " left server, what a shame.");
    }
}
