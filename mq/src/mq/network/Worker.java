package mq.network;

import java.net.Socket;
import java.util.concurrent.ExecutorService;

import mq.Queue;

import java.io.*;

public class Worker implements Runnable {
    private Socket socket;
    private Queue<String> queue;
    private ExecutorService executor;

    public Worker(Socket socket, Queue<String> queue, ExecutorService executor) {
        this.socket = socket;
        this.queue = queue;
        this.executor = executor;
    }

    @Override
    public void run() {
        try {
            System.out.printf("A producer connected from %s\n", socket.getRemoteSocketAddress());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            // Print an acknowledgement message that the user has connected to SMQ
            out.println("[SMQ-SERVER] Welcome to SMQ");
            out.flush();

            String line = in.readLine();
            while (line != null && line.length() > 0) {
                boolean result = queue.offer(line);
                if (result)
                    out.printf("[SMQ-SERVER] Committed message of length %s\n", line.length());
                else
                    out.printf("[SMQ-SERVER] Failed comitting message of length %s\n", line.length());
                out.flush();

                if (executor.isShutdown()) {
                    out.printf("[SMQ-SERVER] The server is closing, terminating this connection\n");
                    out.flush();
                    break;
                }

                line = in.readLine();
            }

            in.close();
            out.close();
            socket.close();

            System.out.printf("A producer disconnected from %s\n", socket.getRemoteSocketAddress());
        } catch (IOException e) {
            if (!e.getMessage().equals("Connection reset"))
                e.printStackTrace();
        }
    }
}
