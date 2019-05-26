package pl.jkarczewski.chat.server;


import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private ServerSocket serverSocket;
    private DataInputStream inputStream;

    public ChatServer(int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait ...");
            serverSocket = new ServerSocket(port);
            System.out.println("Server started: " + serverSocket);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();

                    ChatServerConsumer consumer = new ChatServerConsumer(socket);

                    Thread thread = new Thread(consumer);

                    thread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
