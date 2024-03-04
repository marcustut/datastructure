package lob.example;

import java.net.*;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;

import javafx.application.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.util.Callback;

import lob.LimitOrderBook;
import lob.common.Order;
import lob.common.Side;
import lob.exchange.Bitstamp;
import lob.v1.LOB;
import lob.v1.Limit;

public class Visualiser extends Application {
    private LimitOrderBook lob = new LOB();
    private Bitstamp client;

    private final String symbol = "BTCUSD";
    private final static int WIDTH = 242;
    private final static int HEIGHT = 503;

    private void startOrderBook() throws URISyntaxException {
        client = new Bitstamp(new String[] { symbol }, (order, _message) -> {
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

    public static void main(String[] args) throws URISyntaxException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        startOrderBook();
        stage.setOnCloseRequest(event -> {
            client.close(1000, "User closed the application");
            executor.close();
        });
        stage.setTitle("Bitstamp - " + symbol);
        stage.setMinWidth(WIDTH);
        stage.setMaxWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
        stage.setMaxHeight(HEIGHT);

        TableView<Limit> bidsTable = makeOfferTable(Side.BUY);
        TableView<Limit> asksTable = makeOfferTable(Side.SELL);
        Text price = new Text("");
        Text spread = new Text("");
        price.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 14));
        spread.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 14));
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(8));
        hbox.getChildren().addAll(price, region, spread);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(asksTable, hbox, bidsTable);

        stage.setScene(new Scene(vbox, WIDTH, HEIGHT));

        // Run a repeated task on an interval to measure messages received
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    Iterator<Limit> bids = lob.topN(8, Side.BUY);
                    Iterator<Limit> asks = lob.topN(8, Side.SELL);

                    price.setText("" + (lob.bestBuy() + lob.bestSell()) / 2);
                    spread.setText("Spread: " + (lob.bestSell() - lob.bestBuy()));

                    bidsTable.getItems().clear();
                    while (bids.hasNext())
                        bidsTable.getItems().add(bids.next());

                    asksTable.getItems().clear();
                    StreamSupport
                            .stream(Spliterators.spliteratorUnknownSize(asks, Spliterator.ORDERED), false)
                            .sorted(Collections.reverseOrder())
                            .forEach(limit -> asksTable.getItems().add(limit));
                });
            }
        };
        executor.scheduleAtFixedRate(repeatedTask, 0, 100L, TimeUnit.MILLISECONDS);

        stage.show();
    }

    @SuppressWarnings("unchecked")
    private TableView<Limit> makeOfferTable(Side side) {
        TableView<Limit> table = new TableView<>();
        TableColumn<Limit, Long> colPrice = new TableColumn<>("Price");
        colPrice.setSortable(false);
        colPrice.setCellFactory(new Callback<TableColumn<Limit, Long>, TableCell<Limit, Long>>() {
            @Override
            public TableCell<Limit, Long> call(TableColumn<Limit, Long> param) {
                return new TableCell<Limit, Long>() {
                    @Override
                    public void updateItem(Long item, boolean empty) {
                        super.updateItem(item, empty);

                        if (isEmpty()) {
                            setText("");
                        } else {
                            setTextFill(side == Side.BUY ? Color.GREEN : Color.RED);
                            setFont(Font.font(Font.getDefault().getName(), FontWeight.SEMI_BOLD,
                                    Font.getDefault().getSize()));
                            setText(item.toString());
                        }
                    }
                };
            }
        });
        TableColumn<Limit, String> colVolume = new TableColumn<>("Volume");
        colVolume.setSortable(false);
        TableColumn<Limit, String> colValue = new TableColumn<>("Value");
        colValue.setSortable(false);

        table.getColumns().addAll(colPrice, colVolume, colValue);
        colPrice.setCellValueFactory(cell -> new SimpleLongProperty(cell.getValue().price).asObject());
        colVolume.setCellValueFactory(
                cell -> new SimpleStringProperty(String.format("%.8f", (double) cell.getValue().volume / 1e9)));
        colValue.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        String.format("%.2f", cell.getValue().price * ((double) cell.getValue().volume / 1e9))));
        return table;
    }
}