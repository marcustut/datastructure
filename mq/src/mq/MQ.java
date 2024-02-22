package mq;

import java.io.IOException;
import java.util.AbstractQueue;
import java.util.logging.Logger;

import mq.network.Server;

public class MQ {
    private int pollingRateMs;
    private Server server;
    private AbstractQueue<String> queue;
    private Logger logger = Logger.getLogger(MQ.class.getName());

    public MQ(int port, int pollingRateMs) {
        this.pollingRateMs = pollingRateMs;
        this.queue = new Queue<String>();
        this.server = new Server(port, this.queue);
    }

    public void start() {
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        logger.warning("Stopping server");
        server.stop();
    }

    public int pending() {
        return queue.size();
    }

    public String recv() throws InterruptedException {
        String result = queue.poll();
        while (result == null) {
            Thread.sleep(pollingRateMs);
            result = queue.poll();
        }
        return result;
    }
}
