package pl.jkarczewski.chat.server;

public class Main {
    private static final int PORT = 6969;

    public static void main(String[] args) {

        ChatServer chatServer = new ChatServer(PORT);
    }
}
