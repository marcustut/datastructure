package mq.network;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import mq.Queue;

public class Server {
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Thread thread;
    private ServerSocket serverSocket;
    private int port;
    private Queue<String> queue;
    private volatile boolean running = false;

    public Server(int port, Queue<String> queue) {
        this.port = port;
        this.queue = queue;
    }

    public synchronized void start() throws IOException {
        if (thread != null)
            throw new IllegalStateException("The server has already started");

        serverSocket = new ServerSocket(port);

        // Start the server on a separate thread (so it does not block the main thread)
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    running = true;
                    System.out.printf("Listening for TCP connections on port %d\n", port);

                    while (running) {
                        Socket socket = serverSocket.accept();
                        // For each client we spawn a new worker thread to handle them
                        executorService.execute(new Worker(socket, queue, executorService));
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, Server.class.getName());
        thread.start();
    }

    public synchronized void stop() {
        if (thread == null)
            throw new IllegalStateException("The server has not been started yet");

        running = false;
        try {
            // Shutdown the current threadpool (not allow new tasks to be accepted)
            executorService.shutdown();
            try {
                // Wait for the current tasks in executor to finish
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    // If current tasks not finish within timeout, we cancel all currently executing
                    // tasks
                    executorService.shutdownNow();
                    // Wait a while for tasks to respond to being cancelled
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                        System.err.println("Pool did not terminate");
                }
            } catch (InterruptedException e) {
                // Cancel all current tasks if current thread was interrupted
                executorService.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }

            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread = null;
    }
}
