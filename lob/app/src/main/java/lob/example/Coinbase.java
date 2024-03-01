/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lob.example;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

class CoinbaseWS extends WebSocketClient {
    public CoinbaseWS(URI uri) {
        super(uri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Successfully connected to " + this.uri);

        // Subscribe to data feeds
        send("""
                {
                    "type": "subscribe",
                    "channels": [
                        {
                            "name": "heartbeat",
                            "product_ids": [
                                "BTC-USD"
                            ]
                        },
                        {
                            "name": "full",
                            "product_ids": [
                                "BTC-USD"
                            ]
                        }
                    ]
                }
                """);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
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
}

public class Coinbase {

    public static void main(String[] args) throws URISyntaxException {
        CoinbaseWS client = new CoinbaseWS(new URI("wss://ws-feed.exchange.coinbase.com"));
        client.connect();
        // WebSocketClient ws = new WebSocketClient(new
        // URI("wss://ws-feed.exchange.coinbase.com"));
    }
}