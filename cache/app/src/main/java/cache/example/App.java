package cache.example;

import java.util.TimerTask;
import java.util.concurrent.*;

import cache.Cache;
import javafx.application.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) throws InterruptedException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Cache<String, String> cache = new Cache<>();
        stage.setOnCloseRequest(event -> {
            cache.close();
        });

        stage.setTitle("Cache Viewer");

        TextField keyField = new TextField();
        keyField.setPromptText("Enter key");
        keyField.setMaxWidth(140);
        TextField valueField = new TextField();
        valueField.setPromptText("Enter value");
        valueField.setMaxWidth(140);
        TextField ttlField = new TextField();
        ttlField.setPromptText("Enter ttl");
        ttlField.setMaxWidth(80);
        Button button = new Button("Add");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String key = keyField.getText();
                String value = valueField.getText();
                long ttl = Long.parseLong(ttlField.getText());
                cache.put(key, value, ttl);
            }
        });

        HBox hbox = new HBox();
        hbox.getChildren().addAll(keyField, valueField, ttlField, button);
        hbox.setSpacing(4);

        TableView<TableEntry> table = makeEntryTable();

        // Setup callback functions to keep the UI in-sync with the cache
        cache.setOnAdd((entry, ttl) -> {
            table.getItems().add(new TableEntry(entry.getKey(), entry.getValue(), ttl));
        });
        cache.setOnEvict((entry, ttl) -> {
            table.getItems().removeIf((_entry) -> _entry.key == entry.getKey());
        });

        // Set a repeated task to decrease the TTL
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    table.getItems().replaceAll(entry -> {
                        entry.ttl--;
                        return entry;
                    });
                });
            }
        };
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(repeatedTask, 0, 1000L, TimeUnit.MILLISECONDS);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(hbox, table);
        vbox.setSpacing(4);
        vbox.setPadding(new Insets(10, 10, 10, 10));

        Scene scene = new Scene(vbox);
        stage.setScene(scene);

        stage.show();
    }

    /**
     * Create the table for displaying data in the cache.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private TableView<TableEntry> makeEntryTable() {
        TableView<TableEntry> table = new TableView<>();
        TableColumn<TableEntry, String> colKey = new TableColumn<>("Key");
        TableColumn<TableEntry, String> colValue = new TableColumn<>("Value");
        TableColumn<TableEntry, String> colTTL = new TableColumn<>("Time-to-live (TTL)");
        colKey.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        colValue.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        colTTL.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        colKey.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().key));
        colValue.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().value));
        colTTL.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%d secs", cell.getValue().ttl)));
        table.getColumns().addAll(colKey, colValue, colTTL);
        return table;
    }

    /**
     * The entry for each cache item in the table.
     */
    private class TableEntry {
        String key;
        String value;
        long ttl;

        TableEntry(String key, String value, long ttl) {
            this.key = key;
            this.value = value;
            this.ttl = ttl;
        }
    }
}
