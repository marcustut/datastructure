package lob.example;

import java.net.URISyntaxException;

import lob.LimitOrderBook;
import lob.common.Order;
import lob.exchange.Bitstamp;
import lob.v1.LOB;

public class Visualiser {
    public static void main(String[] args) throws URISyntaxException {
        LimitOrderBook lob = new LOB();
        Bitstamp client = new Bitstamp(new String[] { "BTCUSD" }, (order, _message) -> {
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
        });
        client.connect();
    }
}
