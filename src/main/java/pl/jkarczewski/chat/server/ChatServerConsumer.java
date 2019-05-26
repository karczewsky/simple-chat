package pl.jkarczewski.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServerConsumer implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final BlockingQueue<String> inBuffer;

    private Thread threadReceive;
    private Thread threadProcessing;
    private Thread threadHearthbeat;
    private AtomicInteger missedHearthbeats;


    ChatServerConsumer(Socket socket) throws IOException {
        this.socket = socket;

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        inBuffer = new ArrayBlockingQueue<String>(20);
    }

    @Override
    public void run() {
        initThreads();
    }

    private void respond(String s) {
        System.out.println(s);
        switch (s) {
            case "PING":
                out.println("PONG");
                break;
            default:
                break;
        }
    }

    private void log(String msg) {
        System.out.println("Thread " + Thread.currentThread().getName() + " : " + msg);
    }

    private void initThreads() {

        threadReceive = new Thread(() -> {
            while (!socket.isClosed()) {
                try {

                    String input = in.readLine();
                    if (input == null) continue;
                    inBuffer.add(input);

                } catch (IOException e) {

                    System.err.println(e);
                }
            }
        });
        threadReceive.setDaemon(true);

        threadProcessing = new Thread(() -> {
           while (true) {
               try {

                   String s = inBuffer.take();
                   respond(s);

               } catch (InterruptedException e) {

                   e.printStackTrace();
               }
           }
        });
        threadProcessing.setDaemon(true);

        threadHearthbeat = new Thread(() -> {
           while (true) {
               missedHearthbeats.addAndGet(1);
           }
        });

    }
}
