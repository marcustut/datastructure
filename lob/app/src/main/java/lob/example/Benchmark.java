package lob.example;

import java.io.*;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lob.LimitOrderBook;
import lob.common.Order;
import lob.exchange.Bitstamp;
import lob.exchange.BitstampOrder;
import lob.v1.LOB;

public class Benchmark {
    private final static String filepath = System.getProperty("user.dir")
            + "/src/main/resources/l3_orderbook.ndjson";

    public static void main(String[] args) throws URISyntaxException, IOException {
        LimitOrderBook lob = new LOB();
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        ObjectMapper mapper = new ObjectMapper();

        String line = reader.readLine();
        long count = 0;
        long readDuration = 0;
        long parseDuration = 0;
        long start = System.nanoTime();
        while (line != null) {
            try {
                long parseStart = System.nanoTime();
                BitstampOrder order = Bitstamp.parseOrderMessage(mapper, line);
                long parseEnd = System.nanoTime();
                parseDuration += parseEnd - parseStart;

                switch (order.event) {
                    case Created:
                        if (order.price == 0) // if the price is 0 then it is a market order
                            lob.market(new Order(order.id, order.side, order.amount, order.price));
                        else // limit otherwise
                            lob.limit(new Order(order.id, order.side, order.amount, order.price));
                        break;
                    case Deleted:
                        lob.cancel(order.id);
                        break;
                    case Changed:
                        lob.amend(order.id, order.amount);
                        break;
                }
            } catch (Exception e) {
                if (!e.getMessage().contains("Received non-order message"))
                    System.err.println(e);
            }

            count++;
            long readStart = System.nanoTime();
            line = reader.readLine();
            long readEnd = System.nanoTime();
            readDuration += readEnd - readStart;
        }
        long end = System.nanoTime();
        long duration = end - start;
        System.out.printf("Time used for reading %d messages: %.2fms\n", count, readDuration * 1e-6);
        System.out.printf("Time used for parsing %d messages: %.2fms\n", count, parseDuration * 1e-6);
        System.out.printf("Time used for processing %d orders: %.2fms\n", count,
                (duration - readDuration - parseDuration) * 1e-6);
        System.out.printf("Average time used for processing 1 order: %dns",
                ((duration - readDuration - parseDuration)) / count);

        reader.close();
    }
}
