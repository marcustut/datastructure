import smq.SMQ;

class Main {
    public static void main(String[] args) throws Exception {
        // Start a SMQ server at port 3333 with polling rate of 10 milliseconds
        SMQ mq = new SMQ(3333, 10);
        mq.start();

        // Register a shutdown hook (to catch CTRL+C or SIGTERM, etc.)
        // so we can properly stop the mq.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down...");
                mq.stop();
            }
        });

        // Keep polling for messages from queue (note that `recv()` polls at
        // 10 milliseconds since we specified that when creating the mq).
        while (true)
            System.out.printf("Received: %s\n", mq.recv());
    }
}
