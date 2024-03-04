package lob.exchange;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Bitstamp extends WebSocketClient {
    private final static String uri = "wss://ws.bitstamp.net";
    private ObjectMapper mapper = new ObjectMapper();
    private OrderCallback orderCallback;
    private String[] symbols;

    public interface OrderCallback {
        public void onOrderMessage(BitstampOrder order, String message);
    }

    public Bitstamp(String[] symbols, OrderCallback orderCallback) throws URISyntaxException {
        super(new URI(Bitstamp.uri));
        this.symbols = symbols;
        this.orderCallback = orderCallback;
    }

    public static BitstampOrder parseOrderMessage(ObjectMapper mapper, String message) throws Exception {
        try {
            BitstampOrder order = mapper.readValue(message, BitstampOrder.class);
            return order;
        } catch (Exception _e1) {
            try {
                JsonNode node = mapper.readTree(message);
                throw new Exception("Received non-order message: " + node);
            } catch (Exception e2) {
                throw new Exception("Failed to parse message as JSON: " + e2);
            }
        }
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Successfully connected to " + Bitstamp.uri);
        subscribeLiveOrders();
    }

    @Override
    public void onMessage(String message) {
        try {
            BitstampOrder order = Bitstamp.parseOrderMessage(mapper, message);
            orderCallback.onOrderMessage(order, message);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                        + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    private void subscribeLiveOrders() {
        for (String symbol : symbols)
            send(String.format("""
                    {
                        "event": "bts:subscribe",
                        "data": {
                            "channel": "live_orders_%s"
                        }
                    }""", symbol.toLowerCase()));
    }
}