#import "template.typ": *

#show: project.with(
  title: "Assignment 3: Huffman Code",
  authors: (
    (name: "Lee Kai Yang\n(23205838 - 100%)", email: "kai.y.lee@ucdconnect.ie"),
  ),
  date: "March 23, 2024",
  font: "CMU Serif",
  monofont: "CMU Typewriter Text",
)

#align(center)[
  #text(fill: blue, size: 9pt)[#underline(link("https://youtu.be/KmnG6tFzjKo")[🔗 Video Link])]
  #h(0.2em)
  #text(fill: blue, size: 9pt)[#underline(link("https://github.com/marcustut/datastructure/tree/main/huffman")[🔗 GitHub Link])]
]

#pad(x: 12pt, [
  *Abstract* - The evolution of financial markets has been marked by the transition from traditional open outcry trading to electronic systems, emphasizing efficiency and speed. Central to modern electronic trading systems is the order book, a dynamic data structure managing buy and sell orders. In this paper, we present an in-depth exploration of the design, implementation, and performance analysis of an efficient limit order book for electronic trading systems. Our design decisions focus on using binary search trees and queues to maintain sorted order and facilitate fast order processing. Through comprehensive testing and benchmarking, we demonstrate the scalability and performance of our order book implementation. Real-world examples, including a live visualizer and a benchmarking tool, showcase the practical applications and performance characteristics of our system. Benchmark results indicate that our order book achieves remarkable throughput and latency, close to industry standards. Our work contributes a reliable and efficient solution for managing order flow in electronic trading systems, offering insights into the principles and practices of high-performance financial software engineering.
])

= Background

Back then in the early ages of computing resources are limited, for example the 305 RAMAC (Random Access Method of Accounting and Control) launched by IBM in September 1956 is capable of only storing 5MB (megabytes) of data albeit weighting over 2000 pounds and cost 35,000 USD a year to operate. In contrast, a commerical memory card that weights no more than 5 gram can easily stores 1TB (terabytes) of data, in fact storage is so affordable that most people do not think twice on what they are storing e.g. images, music, and the all-important funny cat videos. However, the techniques to optimise storage space back then had since lived on until now, one of the most well known technique is data compression where given a larger file, it will be compressed by taking advantage of repetitive patterns in the contents, allowing it to use lesser space while having the ability to reconstruct the original data later. Such technique is also useful for speeding up communication across networks since it reduces the payload size. Huffman encoding is one of the most commonly known algorithm in this regard where it was being used in ZIP, GZIP as well as image compression such as JPEG and PNG, created by David Huffman in 1952 at MIT.

= Introduction

== Message Encoding

Assuming that we are only dealing with ASCII characters, each character maps to 1 byte so for the message *"yippy ya"*, it would need $8 times 8" bits" = 64 "bits"$ in total. The table below shows the mapping of each character to their corresponding ASCII values.

#figure(
  table(
    columns: (auto, auto, auto),
    inset: 6pt,
    align: horizon,
    fill: (col, row) => if row == 0 { silver } else { white },
    [*Character*], [*ASCII (Decimal)*], [*ASCII (Binary)*],
    [y], [121], [01111001],
    [i], [105], [01101001],
    [p], [112], [01110000],
    [a], [97], [01100001],
    [space], [32], [00100000],
  ),
  caption: [ASCII Table for the message "yippy ya"]
) <ascii_yippy_ya>

Using @ascii_yippy_ya above, the message encoded using ASCII would be 

#rect(fill: silver, stroke: 1pt, width: 100%, height: 2em, inset: 6.5pt)[
  #text(font: "CMU Typewriter Text", size: 10pt, weight: "bold")[01111001 01111001 01110000 01110000 01111001 01111001 01111001 01111001]
]

However do we really need that many bits? With huffman encoding, it leverages the frequency information of individual character in the message to devise a more compact encoding scheme. For example, the same message would use the following scheme:

#figure(
  table(
    columns: (auto, auto),
    inset: 6pt,
    align: horizon,
    fill: (col, row) => if row == 0 { silver } else { white },
    [*Character*], [*ASCII (Binary)*],
    [y], [0],
    [i], [100],
    [p], [110],
    [a], [111],
    [space], [101],
  ),
  caption: [Huffman Encoding Table for the message "yippy ya ya"]
) <huffman_yippy_ya_ya>

Using @huffman_yippy_ya_ya above, the encoded message would be 

#rect(fill: silver, stroke: 1pt, width: 100%, height: 2em, inset: 6.5pt)[
  #text(font: "CMU Typewriter Text", size: 10pt, weight: "bold")[0 100 110 110 0 101 0 111]
]

which only uses 18 bits, comparing this to the ASCII version, it achieved a 28% compression. However, most operating systems's file system work with byte as the smallest unit hence we need to pad extra zeroes at the end for the message making it to use 24 bits. That said, it is still a big improvement from only using ASCII.

== Constructing the Huffman Table

The major difference between Huffman encoding and ASCII encoding is that ASCII has a fixed table mapping each character to a bit pattern but Huffman encoding does not, it requires some processing to generate this table. The approach that Huffman took is to construct a binary tree that we can traverse to find the character and the path it took is the code for that particular character. Such trees are called _Huffman Trees_.

For example the message *"yippy ya"* would result in a huffman tree as follows:

```
                     0                       {=11}             1                                     
                    ┌───────────────────┴───────────┐                   
                  {y=4}                             0         {=7}          1
                                                    ┌─────────┴───────┐         
                                             0    {=3}    1           0    {=4}   1      
                                             ┌────┴────┐         ┌────┴────┐    
                                           {i=1}         { =2}     {p=2}         {a=2}
```

Note that in the message, there are five distinct characters and these characters correspond to the five leaf nodes in the tree, *y*, *i*, *space*, *p* and *a*. When traversing the tree, every turn to the left adds a '0' to the path and adds a '1' to the path when turning to the right. Therefore, for the character *y*, its path is just *0* since its only takes one turn to the left and arrived at the leaf node, for the character *i*, the path is *100*, the same idea applies for the other characters.

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

Due to the sorted nature of the order book, storing these limits in a binary search tree is suitable since it maintains the order on each mutation (insertion / deletion). The idea is to use a binary search tree to store the limits and for each limit use a queue to store all the orders in the limit. The reason for using a queue is due to the fact that the order execution follows a first in first out scheme which can be achieved easily with queues. For simplicity, we call them a limit tree here and for an order book there will be two limit tree, one for bid and another for ask. below shows an illustration of the two limit trees.

shows two limit trees, the tree highlighted in green is for bids and the tree highlighted in red is for asks. As can be seen, the tree resembles a binary search tree exactly where there are at most two child nodes for each node and all the nodes are sorted in-order. One notable property here is that since the nodes are ordered, the best bid / ask will always be the minimum or maximum node annotated in the figure above. Hence, the order book class have the following properties

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

visualises the BTCUSD orderbook and it will be updated as actual orders are submitted to the Bitstamp exchange, the *Volume* column is the total order quantity at the limit level and *Value* column shows the total value (USD) that the limit level holds, it is calculated by $ "Value" = "Price" times "Volume" $

The spread here shows the bid ask spread in the orderbook where it is the difference between the best bid price and best ask price. In this case, $ 67034 "(best ask)" - 66990 "(best bid)" = 44 "(spread)" $

= Performance benchmark

To measure how well our orderbook performs, the benchmark example instantiate the order book, reads and parses the messages from the `.ndjson` file and perform the required operation for each message. The example then records the total time taken for each run and repeat the process 10 times and lastly write the result to a `.csv` file. We then plot the graphs below using a python script `plot.py`.

== Latency analysis

Figure on the left above shows the benchmark result in terms of the time taken to process one million messages and as can be seen that the first run takes a significantly longer time than the others, this is probably due to system cache are still cold meaning higher chances of cache misses. This can be evidented by the fact that after the first run all subsequent runs have negligible difference in the result which is a sign that the system cache has been warmed up and lesser chances of cache misses. 

Figure on the right above shows the average latency per operation which is calculated by $ "total time taken" div "number of operations" $ In this case the graph are very similar because there are one million operations hence the difference is only the time unit. The trend is similar to the previous graph due to cache warm up.

Based on the fastest run from the results above we can deduce that the order book is capable to process 1 million operations in *143.09ms* with an average latency of *143.09ns* per operation.

Note that the duration for reading from file and message parsing has been excluded since we only concern about the order book operations here. 

== Throughput analysis 

above shows the throughput measured for the order book across all ten runs and the order book is performing exceptionally fast given that the benchmark was performed on commercial hardware, in this case a _2020 M1 Macbook Air (8GB RAM)_ which on average it is able to achieve *6,679,546 op/s* and *6,988,577 op/s* on the fastest run.

== Conclusion

Generally I am very satisfied with the outcome of the project, seeing that by using an efficient data structure it is possible to write highly performant software and for comparison I found the following resources:

- #text(fill: blue)[#underline(link("https://github.com/charles-cooper/itch-order-book")[charles-cooper/itch-order-book])] - A very fast order book implementation in C++ using only `std::vector` achieving 61ns per operation.
- #text(fill: blue)[#underline(link("https://www.elitetrader.com/et/threads/how-fast-is-your-limit-order-book-implementation.255567/")[How fast is your limit order book implementation?])] - An old forum post where some people claimed that their implementation are around $approx$ 210ns per operation.

Although these comparison are unfair because the results are taken from different machines but having an implementation where the performance is close to what people use in the industry is impressive enough.
