# huffman

*Huffman Coding implemented in Java*

## Project structure

The source code are mainly in `app/src/main/java/huffman` with several data structures being used written in `app/src/main/java/huffman/ds`. The test code can be found at `app/src/test/java/huffman` and the examples can be found in `app/src/main/java/huffman/example/App.java`.

## Running the tests 

This project uses gradle as its build tool so to run the tests you can do 

```sh
# On unix systems
./gradlew test

# On windows
./gradlew.bat test
```

## Running the examples

### Visualiser

This example comes with a simple GUI written with [JavaFX](https://openjfx.io/index.html) that visualise the limit order book similar to what traders see on their trading terminal. The live orders data comes from Bitstamp's publicly available L3 Orderbook data feed through WebSocket.

```sh
# On unix systems
./gradlew run -Plaunch=huffman.example.Visualiser

# On windows
./gradlew.bat run -Plaunch=huffman.example.Visualiser
```

Once the visualiser is opened, you should see a window as follows:

### Benchmark

This example reads 1 million messages which was previously downloaded and feed them into the limit order book and measure how long it takes.

```sh
# On unix systems
./gradlew run -Plaunch=huffman.example.Benchmark

# On windows
./gradlew.bat run -Plaunch=huffman.example.Benchmark
```
