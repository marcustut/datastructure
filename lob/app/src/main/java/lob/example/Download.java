package lob.example;

import java.io.*;
import java.net.URISyntaxException;

import lob.exchange.Bitstamp;

public class Download {
    private final static String filepath = System.getProperty("user.dir")
            + "/src/main/resources/l3_orderbook.ndjson";
    private static long writeCount = 0;
    private static Bitstamp client;

    public static void main(String[] args) throws URISyntaxException, IOException {
        long writeLimit = 1000000;

        PrintWriter writer = new PrintWriter(new FileWriter(filepath));
        client = new Bitstamp(new String[] { "BTCUSD" }, (order, message) -> {
            if (writeCount % 1000 == 0)
                System.out.println("Written " + writeCount + " messages");

            if (writeCount <= writeLimit) {
                writer.println(message);
                writeCount++;
                return;
            }

            client.close();
            writer.close();
            System.out.println("Completed writing " + writeLimit + " messages, ending the program");
            System.exit(0);
        });

        client.connect();
    }
}
