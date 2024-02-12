package smq;

import java.io.IOException;
import java.util.logging.Logger;

import smq.network.Server;

public class SMQ {
    private int pollingRateMs = 1000;
    private Server server;
    private Queue<String> queue;
    private Logger logger = Logger.getLogger(SMQ.class.getName());

    public SMQ(int port, int pollingRateMs) {
        this.pollingRateMs = pollingRateMs;
        this.queue = new Queue<String>();
        this.server = new Server(port, this.queue);
    }

    public void start() {
        try {
            this.server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        logger.warning("Stopping server");
        this.server.stop();
    }

    public String recv() throws InterruptedException {
        String result = queue.poll();
        while (result == null) {
            Thread.sleep(this.pollingRateMs);
            result = queue.poll();
        }
        return result;
    }
}
