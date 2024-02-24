import mq.MQ;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Main {
    static final int PORT = 3333;
    static final int POLLING_RATE = 1;

    static final int NUM_PRODUCERS = 10;
    static final int PRODUCE_RATE = 100;

    static MQ mq = new MQ(PORT, POLLING_RATE);
    static boolean[] producerStates = new boolean[NUM_PRODUCERS];
    static boolean shutdown = false;
    static long messageCount = 0;

    public static void main(String[] args) throws Exception {
        // Start a SMQ server at port 3333 with polling rate of 10 milliseconds
        mq.start();

        // Register a shutdown hook (to catch CTRL+C or SIGTERM, etc.)
        // so we can properly stop the mq.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down...");
                System.out.println("Signaling all producers to shutdown...");
                shutdown = true; // signal a shutdown to all producer threads

                // wait until all producers shutdown complete
                for (int i = 0; i < NUM_PRODUCERS; i++) {
                    while (producerStates[i]) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception err) {
                            continue;
                        }
                    }
                }

                System.out.println("All producers shutdown successfully");

                System.out.println("Stopping MQ...");
                mq.stop();
                System.out.println("MQ stopped successfully");
            }
        });

        // Spawn producer threads
        Main.spawnProducerThreads(NUM_PRODUCERS, PRODUCE_RATE);

        // Run a repeated task on an interval to measure messages received
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                System.out.println("Processed " + messageCount + " messages in 1 second");
                messageCount = 0;
            }
        };
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(repeatedTask, 1000L, 1000L, TimeUnit.MILLISECONDS);

        // Keep polling for messages from queue (note that `recv()` polls at
        // 1 millisecond since we specified that when creating the mq).
        while (true) {
            mq.recv();
            messageCount++;
        }
    }

    private static void spawnProducerThreads(int num, int rate) {
        for (int i = 0; i < num; i++) {
            Thread producer = new Thread() {
                private int idx;

                private Thread init(int idx) {
                    this.idx = idx;
                    return this;
                }

                @Override
                public void run() {
                    try (Socket socket = new Socket("localhost", PORT)) {
                        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                        while (!shutdown) {
                            writer.println(Main.randomMessage(100));
                            Thread.sleep(rate);
                        }

                        producerStates[idx] = false;
                        System.out.println("Producer " + idx + " shut down successfully");
                    } catch (Exception e) {
                        System.err.println("An exception occured in producer thread");
                    }
                }
            }.init(i);
            producer.start();
            producerStates[i] = true;
        }
    }

    private static String randomMessage(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
