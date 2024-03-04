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
        long processedCount = 0;

        String line = reader.readLine();
        while (line != null) {
            try {
                BitstampOrder order = Bitstamp.parseOrderMessage(mapper, line);

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
                System.err.println(e);
            }

            line = reader.readLine();
            processedCount++;
            if (processedCount % 100000 == 0)
                System.out.println("Procssed " + processedCount + " messages");
        }

        reader.close();
    }
}
