package examples;

import mq.MQ;

class Email {
    static final int PORT = 3333;
    static final int POLLING_RATE = 1;

    static MQ mq = new MQ(PORT, POLLING_RATE);
    static boolean shutdown = false;

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

                System.out.println("Stopping MQ...");
                mq.stop();
                System.out.println("MQ stopped successfully");
            }
        });

        // Keep polling for messages from queue (note that `recv()` polls at
        // 1 millisecond since we specified that when creating the mq).
        while (true) {
            String msg = mq.recv();
            System.out.println("Received message: " + msg);
        }
    }
}
