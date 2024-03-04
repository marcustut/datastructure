package lob.exchange;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lob.common.Side;

class BitstampOrderDeserializer extends StdDeserializer<BitstampOrder> {
    public BitstampOrderDeserializer() {
        this(null);
    }

    public BitstampOrderDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public BitstampOrder deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        BitstampOrder order = new BitstampOrder();
        String event = node.get("event").asText();
        switch (event) {
            case "order_created":
                order.event = BitstampOrder.OrderEvent.Created;
                break;
            case "order_deleted":
                order.event = BitstampOrder.OrderEvent.Deleted;
                break;
            case "order_changed":
                order.event = BitstampOrder.OrderEvent.Changed;
                break;
            default:
                throw new IOException(
                        "received unrecognised 'event', only 'order_created', 'order_deleted' or 'order_changed' is valid.");
        }

        order.id = node.get("data").get("id").asLong();

        int side = node.get("data").get("order_type").asInt();
        switch (side) {
            case 0:
                order.side = Side.BUY;
                break;
            case 1:
                order.side = Side.SELL;
                break;
            default:
                throw new IOException("received unrecognised 'order_type', only 0 or 1 is valid.");
        }

        order.amount = (long) (node.get("data").get("amount").asDouble() * 1e9);
        order.price = node.get("data").get("price").asLong();

        return order;
    }
}

@JsonDeserialize(using = BitstampOrderDeserializer.class)
public class BitstampOrder {
    public OrderEvent event;
    public Side side;
    public long id;
    public long amount;
    public long price; // the order is a taker if its price is 0.

    public enum OrderEvent {
        Created,
        Deleted,
        Changed,
    }

    @Override
    public String toString() {
        return event + " id: " + id + " side: " + side + " amount: " + amount + " price: " + price;
    }
}