/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lob.example;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

class BinanceWS extends WebSocketClient {
    public BinanceWS(URI uri) {
        super(uri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {

        System.out.println("Successfully connected to " + this.uri);

        // Subscribe to data feeds
        send("""
                {
                    "method": "SUBSCRIBE",
                    "params": [
                        "btcusdt@depth"
                    ],
                    "id": null
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

public class Binance {
    public static void main(String[] args) throws URISyntaxException {
        BinanceWS client = new BinanceWS(new URI("wss://stream.binance.com/ws"));
        client.connect();
    }
}
