package pl.jkarczewski.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Class responsible for controlling chat server
 */
public class ChatServer {
    private ServerSocket serverSocket;
    private Vector<ChatServerThread> clients;

    /**
     * Constructor responsible for creation of new chat server.
     *
     * @param port number of port on which server should operate
     */
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

    /**
     * Method responsible for accepting new client connections and delegating them to separate threads {@link ChatServerThread}.
     */
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

    /**
     * Method responsible for handling messages received by client threads {@link ChatServerThread}.
     *
     * @param originThread Thread from which message came
     * @param message Bare message received from client
     */
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

    /**
     * Method, which is used to send SYSTEM notifications to all connected clients.
     *
     * @param message Message, which is going to be propagated to all clients.
     */
    public void sendSystemNotification(String message) {
        for (ChatServerThread client: clients) {
            client.sendMessage("MSG SYSTEM " + message);
        }
    }

    /**
     * Method used to propagate standard user messages over system.
     *
     * @param originThread Thread, which received message from client
     * @param message Bare message received from client
     */
    public void sendToOtherClients(ChatServerThread originThread, String message) {
        for (ChatServerThread client: clients) {
            client.sendMessage("MSG " + originThread.getNickname() + " " + message);
        }
    }

    /**
     * Method used to remove client from active clients registry.
     *
     * @param clientThread Thread responsible for connection with client, which should be removed.
     */
    public void removeClient(ChatServerThread clientThread) {
        System.out.println("Removing client thread " + clientThread.getIdentifier() + " Nickname: " + clientThread.getNickname());
        clients.remove(clientThread);
        sendSystemNotification(clientThread.getNickname() + " left server, what a shame.");
    }
}
