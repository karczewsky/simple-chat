package pl.jkarczewski.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatServerThread extends Thread {
    private ChatServer server;
    private Socket clientSocket;
    private DataInputStream streamIn;
    private DataOutputStream streamOut;
    private int Id;
    private String Nickname;
    private boolean working;

    public ChatServerThread(ChatServer server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.streamIn = new DataInputStream(clientSocket.getInputStream());
        this.streamOut = new DataOutputStream(clientSocket.getOutputStream());
        this.Id = clientSocket.getPort();
        this.Nickname = "anonymous";
        this.working = true;
    }

    public int getIdentifier() {
        return Id;
    }

    public void sendMessage(String msg) {
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
        } catch (IOException ex) {
            System.err.println("ERROR Thread " + Id + " sending message: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("Server Thread " + Id + " started");

        while (working) {
            try {
                String msg = streamIn.readUTF();
                server.handleMessage(this, msg);
            } catch (IOException ex) {
                System.err.println("ERROR Thread " + Id + " reading from client: " + ex.getMessage());
                server.removeClient(this);
                return;
            }
        }
    }

    public String getNickname() {
        return Nickname;
    }

    public void setNickname(String nickname) {
        Nickname = nickname;
    }

    public void shut() {
        working = false;

        try {
            clientSocket.close();
            streamIn.close();
            streamOut.close();
        } catch (IOException ex) {
            System.err.println("ERROR Thread " + Id + " closing connection: " + ex.getMessage());
        }
    }
}
