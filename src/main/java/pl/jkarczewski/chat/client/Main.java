package pl.jkarczewski.chat.client;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        ChatClient chatClient;

        try {
            chatClient = new ChatClient("localhost", 6969, "jakub");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        chatClient.work();
    }
}
