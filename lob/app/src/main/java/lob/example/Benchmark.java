package lob.example;

import java.io.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import lob.LimitOrderBook;
import lob.common.Order;
import lob.exchange.Bitstamp;
import lob.exchange.BitstampOrder;

class BenchmarkResult {
    long readDuration, parseDuration, orderDuration, count;

    public BenchmarkResult(long readDuration, long parseDuration, long orderDuration, long count) {
        this.readDuration = readDuration;
        this.parseDuration = parseDuration;
        this.orderDuration = orderDuration;
        this.count = count;
    }
}

public class Benchmark {
    private final static String filepath = System.getProperty("user.dir")
            + "/src/main/resources/l3_orderbook.ndjson";
    private final static String benchmarkFilepath = System.getProperty("user.dir")
            + "/src/main/resources/benchmark.csv";

    public static void main(String[] args) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(benchmarkFilepath));

        writer.println("readDuration,parseDuration,orderDuration,count");
        for (int i = 0; i < 10; i++) {
            BenchmarkResult result = benchmark();
            writer.printf("%d,%d,%d,%d\n", result.readDuration, result.parseDuration, result.orderDuration,
                    result.count);
        }

        writer.close();
    }

    private static BenchmarkResult benchmark() throws IOException {
        LimitOrderBook lob = new lob.v1.LOB();
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
        System.out.printf("Average time used for processing 1 order: %dns\n",
                ((duration - readDuration - parseDuration)) / count);

        reader.close();

        return new BenchmarkResult(readDuration, parseDuration, duration - readDuration - parseDuration, count);
    }
}
