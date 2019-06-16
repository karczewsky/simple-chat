package pl.jkarczewski.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Class responsible for holding remote connections with clients delegated by {@link ChatServer}
 */
public class ChatServerThread extends Thread {
    private ChatServer server;
    private Socket clientSocket;
    private DataInputStream streamIn;
    private DataOutputStream streamOut;
    private int Id;
    private String Nickname;
    private boolean working;

    /**
     * Constructor generating new {@link ChatServerThread}.
     *
     * @param server {@link ChatServer} which delegated client to this thread
     * @param clientSocket Socket with established connection to client
     * @throws IOException Error might occur during creation of socket related streams
     */
    public ChatServerThread(ChatServer server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.streamIn = new DataInputStream(clientSocket.getInputStream());
        this.streamOut = new DataOutputStream(clientSocket.getOutputStream());
        this.Id = clientSocket.getPort();
        this.Nickname = "anonymous";
        this.working = true;
    }

    /**
     * Retrieve custom thread identifier.
     *
     * @return Custom thread identifier
     */
    public int getIdentifier() {
        return Id;
    }

    /**
     * Method used to send bare message to client connected via this thread.
     *
     * @param msg bare message to send
     */
    public void sendMessage(String msg) {
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
        } catch (IOException ex) {
            System.err.println("ERROR Thread " + Id + " sending message: " + ex.getMessage());
        }
    }

    /**
     * Method responsible for receiving data incoming from client.
     */
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

    /**
     * Method used to get nickname of client associated with this thread.
     *
     * @return Nickname set by remote client
     */
    public String getNickname() {
        return Nickname;
    }

    /**
     * Method used to set nickname of remote client.
     *
     * @param nickname new nickname
     */
    public void setNickname(String nickname) {
        Nickname = nickname;
    }

    /**
     * Method used to safely close all streams and client socket
     */
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
