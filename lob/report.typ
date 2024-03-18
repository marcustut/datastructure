#import "template.typ": *

#show: project.with(
  title: "Assignment 2: Limit Order Book",
  authors: (
    (name: "Lee Kai Yang\n(23205838 - 100%)", email: "kai.y.lee@ucdconnect.ie"),
  ),
  date: "March 4, 2024",
  font: "CMU Serif",
  monofont: "CMU Typewriter Text",
)

#align(center)[
  #text(fill: blue, size: 9pt)[#underline(link("https://youtu.be/KmnG6tFzjKo")[🔗 Video Link])]
  #h(0.2em)
  #text(fill: blue, size: 9pt)[#underline(link("https://github.com/marcustut/datastructure/tree/main/lob")[🔗 GitHub Link])]
]

#pad(x: 12pt, [
  *Abstract* - The evolution of financial markets has been marked by the transition from traditional open outcry trading to electronic systems, emphasizing efficiency and speed. Central to modern electronic trading systems is the order book, a dynamic data structure managing buy and sell orders. In this paper, we present an in-depth exploration of the design, implementation, and performance analysis of an efficient limit order book for electronic trading systems. Our design decisions focus on using binary search trees and queues to maintain sorted order and facilitate fast order processing. Through comprehensive testing and benchmarking, we demonstrate the scalability and performance of our order book implementation. Real-world examples, including a live visualizer and a benchmarking tool, showcase the practical applications and performance characteristics of our system. Benchmark results indicate that our order book achieves remarkable throughput and latency, close to industry standards. Our work contributes a reliable and efficient solution for managing order flow in electronic trading systems, offering insights into the principles and practices of high-performance financial software engineering.
])

= Background

Since the first development of a stock exchange back in the 17th century, open outcry or also known as pit trading has always been the main medium of communication between different parties. It involves shouting and the use of hand signals to transfer information on buy or sell orders. At the time, clerks were hired to bookkeep these orders typically written down in a book, hence the name "order book". However, this method is inefficient and highly error prone thus it was gradually replaced by electronic trading systems since 1970s with the NASDAQ becoming world's first electronic stock market in 1971. The very "heartbeat" of these electronic trading systems is the order book, every single buy or sell order has to make its way through the order book before it can be executed. Therefore, to build an efficient electronic trading system the order book must be capable of processing an enourmous amount of order requests in a short amount of time. In other words, high throughput and low latency. 

= Introduction

As its name suggest, an order book is a data structure that holds a collection of buy and sell orders. @orderbook_sketch below shows an illustration of a limit order book.

#figure(
  image("images/orderbook.png", width: 60%),
  caption: [Limit Order Book Illustration]
) <orderbook_sketch>

As can be seen, both sell orders (asks) and buy orders (bids) are grouped into levels in the book and these levels are called "limit". Within a limit there can be one or more orders and the sequence is usually based on the timestamp at which these orders come in to the system as most matching engines match the oldest order first. Note that each limit has a `price` and a `volume`, all orders at this limit has the same price and the volume is the aggregated quantity of all the orders. Moreover, the first limit is often referred to as top of book or best bid / best ask.

One important property here is that the bids will never overlap the asks in the book because when they overlaps, the limits are "matched" and will be executed. The reason is that when two level overlaps, it meant that there is a seller at a certain price and a buyer willing to pay that price hence their orders will be executed.

= Design decisions

The main operations for an order book are listed below:

- *Limit* - Add a new order onto the book at a specfied limit price.
- *Cancel* - Cancel an existing order from the book.
- *Market* - Match orders from the book (remove existing orders).
- *Amend* - Update the quantity of a particular order.
- *Get Best Bid/Ask* - Get the current best bid or best ask.
- *Get Volume* - Get the current volume.
- *Top N* - Get the top N limits in the book.

To make the code more maintainable and extensible, I have created an interface `LimitOrderBook` which contains the methods mentioned above.

```java
public interface LimitOrderBook {
    public void limit(Order order);
    public void market(Order order);
    public void cancel(long orderId);
    public void amend(long orderId, long size);
    public long bestBuy();
    public long bestSell();
    public long volume();
    public Iterator<Limit> topN(int n, Side side);
}
```

Due to the sorted nature of the order book, storing these limits in a binary search tree is suitable since it maintains the order on each mutation (insertion / deletion). The idea is to use a binary search tree to store the limits and for each limit use a queue to store all the orders in the limit. The reason for using a queue is due to the fact that the order execution follows a first in first out scheme which can be achieved easily with queues. For simplicity, we call them a limit tree here and for an order book there will be two limit tree, one for bid and another for ask. @binary_search_tree_bids_asks below shows an illustration of the two limit trees.

#figure(
  grid(
    columns: (1fr, 1fr),
    gutter: 4pt,
    grid.cell(image("images/bst_bids.png", width: 100%)),
    grid.cell(image("images/bst_asks.png", width: 100%)),
  ),
  caption: [Limit Tree Illustration]
) <binary_search_tree_bids_asks>

@binary_search_tree_bids_asks shows two limit trees, the tree highlighted in green is for bids and the tree highlighted in red is for asks. As can be seen, the tree resembles a binary search tree exactly where there are at most two child nodes for each node and all the nodes are sorted in-order. One notable property here is that since the nodes are ordered, the best bid / ask will always be the minimum or maximum node annotated in the figure above. Hence, the order book class have the following properties

```java
public class LOB implements LimitOrderBook {
    // The tree for storing buy limit levels.
    private LimitTree buy = new LimitTree(Side.BUY);

    // The tree for storing sell limit levels.
    private LimitTree sell = new LimitTree(Side.SELL);

    // Store the orders according to their id.
    private HashMap<Long, Order> orders = new HashMap<>();

    ...
}
```

Here the class also contains a `HashMap` mapping each order with their corresponding order id which is simply just a `long` integer, this is so that we can do a `O(1)` order query for the `cancel` and `amend` operations.

As for the `LimitTree`, we store the the limits in a `BST` (Binary Search Tree) and have some properties to track the metrics so they can be accessed in `O(1)` time such as `lastPrice`, `count`, `volume` and `best`.

```java
public class LimitTree {
    // The underlying binary search tree that stores the limits.
    BST<Limit> limits = new BST<>();

    // The price where the last order is executed.
    long lastPrice = 0;

    // The total number of active orders in this tree across all limits.
    int count = 0;

    // The total volume aggregated from all orders in this tree across all limits.
    long volume = 0;

    // The current top price limit.
    Limit best;

    // Indicate whether this is a buy tree or a sell tree.
    Side side;

    ...
}
```

Note that `Limit` here represents each individual node in the tree and all the orders are stored in the `Limit`. For the `Limit` class, it has the following properties:

```java
public class Limit implements Comparable<Limit> {
    // The price for this limit level.
    public long price;

    // The number of orders at this limit level
    public int count = 0;

    // The total volume at this limit level
    public long volume = 0;

    // A queue of orders at this limit level
    public LinkedList<Order> orders = new LinkedList<>();

    @Override
    public int compareTo(Limit o) {
        if (price == o.price)
            return 0;
        else if (price > o.price)
            return 1;
        else
            return -1;
    }

    ...
}
```

Note that it is required for the `Limit` class to implement `Comparable` as the `BST` class uses this implementation to compare different limits and keep them in a sorted manner. The `compareTo` method is simply overrided with a comparison using the `price` since limits are sorted by price in the order book. Moreover, the orders are kept in a queue using a linked list since this allows fast insertion and removal along with the ability to grow dynamically.

= Data structures and test cases

The data structures aforementioned such as `BST` are handwritten by myself and they can be found in the `lob.ds` package. Note that there also a `Stack` class which is used to implement iterators for the `BST` class. In addition, the `BST` supports three different traversal methods *PreOrderTraversal*, *InOrderTraversal* and *PostOrderTraversal* although only *InOrderTraversal* and *PostOrderTraversal* are used in the `LimitTree` implementation. 

The test cases can be found in `src/test/java/lob` and can be ran by executing `./gradlew test` in the terminal.

= Examples

To put the order book to test, I wrote three runnable examples located in the `lob.example` package: 

1. *Download*
2. *Benchmark*
3. *Visualiser*

== Download

This example connects to the #link("https://www.bitstamp.net/")[Bitstamp] cryptocurrency exchange through their public WebSocket API and download all the L3 orderbook (market-by-order) update messages to a local `.ndjson` file. This data is required for benchmarking the orderbook using the *Benchmark* example. To run the download example: 

```sh
# On unix systems
./gradlew run -Plaunch=lob.example.Download

# On windows
./gradlew.bat run -Plaunch=lob.example.Download
```

Note that to download 1 million messages it took 7.5 hours and the final file size is about _321MB_, if you would like to run the benchmark on your machine instead of downloading it, refer to the *README.md* file in this project as I have attached a Google Drive link to the data. Simply download the file and place it at `app/src/main/resources/l3_orderbook.ndjson`.

== Benchmark

This example simply reads all the downloaded messages and feed them into the limit order book and measure how long it takes. The benchmark result is discussed in later sections. To run the example: 

```sh
# On unix systems
./gradlew run -Plaunch=lob.example.Benchmark

# On windows
./gradlew.bat run -Plaunch=lob.example.Benchmark
```

Note that the file `app/src/main/resources/l3_orderbook.ndjson` must exists, otherwise the example will throw a runtime exception and exit with error.

== Visualiser 

This example comes with a simple GUI written with #link("https://openjfx.io/index.html")[JavaFX] that visualise the limit order book similar to what traders see on their trading terminal. The live orders data comes from Bitstamp's publicly available L3 Orderbook data feed through WebSocket. To run the example:

```sh
# On unix systems
./gradlew run -Plaunch=lob.example.Visualiser

# On windows
./gradlew.bat run -Plaunch=lob.example.Visualiser
```

Once the visualiser is launched, a window should appear as follows:

#figure(
  image("images/visualiser.png", width: 40%),
  caption: [Limit Order Book Visualiser]
) <orderbook_visualiser>

@orderbook_visualiser visualises the BTCUSD orderbook and it will be updated as actual orders are submitted to the Bitstamp exchange, the *Volume* column is the total order quantity at the limit level and *Value* column shows the total value (USD) that the limit level holds, it is calculated by $ "Value" = "Price" times "Volume" $

The spread here shows the bid ask spread in the orderbook where it is the difference between the best bid price and best ask price. In this case, $ 67034 "(best ask)" - 66990 "(best bid)" = 44 "(spread)" $

= Performance benchmark

To measure how well our orderbook performs, the benchmark example instantiate the order book, reads and parses the messages from the `.ndjson` file and perform the required operation for each message. The example then records the total time taken for each run and repeat the process 10 times and lastly write the result to a `.csv` file. We then plot the graphs below using a python script `plot.py`.

== Latency analysis

#figure(
  grid(
    columns: (1fr, 1fr),
    gutter: 4pt,
    grid.cell(image("images/benchmark_time_taken.png", width: 100%)),
    grid.cell(image("images/benchmark_latency.png", width: 100%)),
  ),
  caption: [Latency Benchmark]
) <latency_benchmark>

Figure on the left above shows the benchmark result in terms of the time taken to process one million messages and as can be seen that the first run takes a significantly longer time than the others, this is probably due to system cache are still cold meaning higher chances of cache misses. This can be evidented by the fact that after the first run all subsequent runs have negligible difference in the result which is a sign that the system cache has been warmed up and lesser chances of cache misses. 

Figure on the right above shows the average latency per operation which is calculated by $ "total time taken" div "number of operations" $ In this case the graph are very similar because there are one million operations hence the difference is only the time unit. The trend is similar to the previous graph due to cache warm up.

Based on the fastest run from the results above we can deduce that the order book is capable to process 1 million operations in *143.09ms* with an average latency of *143.09ns* per operation.

Note that the duration for reading from file and message parsing has been excluded since we only concern about the order book operations here. 

== Throughput analysis 

#figure(
  image("images/benchmark_throughput.png", width: 50%),
  caption: [Throughput Benchmark]
) <throughput_benchmark>

@throughput_benchmark above shows the throughput measured for the order book across all ten runs and the order book is performing exceptionally fast given that the benchmark was performed on commercial hardware, in this case a _2020 M1 Macbook Air (8GB RAM)_ which on average it is able to achieve *6,679,546 op/s* and *6,988,577 op/s* on the fastest run.

== Conclusion

Generally I am very satisfied with the outcome of the project, seeing that by using an efficient data structure it is possible to write highly performant software and for comparison I found the following resources:

- #text(fill: blue)[#underline(link("https://github.com/charles-cooper/itch-order-book")[charles-cooper/itch-order-book])] - A very fast order book implementation in C++ using only `std::vector` achieving 61ns per operation.
- #text(fill: blue)[#underline(link("https://www.elitetrader.com/et/threads/how-fast-is-your-limit-order-book-implementation.255567/")[How fast is your limit order book implementation?])] - An old forum post where some people claimed that their implementation are around $approx$ 210ns per operation.

Although these comparison are unfair because the results are taken from different machines but having an implementation where the performance is close to what people use in the industry is impressive enough.
