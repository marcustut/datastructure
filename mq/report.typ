#import "template.typ": *

#show: project.with(
  title: "Assignment 1: Messaging Queue over TCP sockets",
  authors: (
    (name: "Rita Sharon\n(23200678 - 25%)", email: "rita.samuelselvaraj@ucdconnect.ie"),
    (name: "Lee Kai Yang\n(23205838 - 50%)", email: "kai.y.lee@ucdconnect.ie"),
    (name: "Sauransh Nayyar\n(23200301 - 25%)", email: "sauransh.nayyar@ucdconnect.ie"),
  ),
  date: "February 19, 2024",
  font: "CMU Serif",
  monofont: "CMU Typewriter Text",
)

#align(center)[
  #text(fill: blue, size: 9pt)[#underline(link("https://ucd-ie.zoom.us/rec/share/jzZMYYKbe6fNs_5qLpr737uCGAGch-KsMWyN-uPaxHvr5fo4ad3xKQwRoON5fr-U.4d90gw5wcW22KYnE")[🔗 Video Link (Password: y23\@PMy\$)])]
  #h(0.2em)
  #text(fill: blue, size: 9pt)[#underline(link("https://github.com/marcustut/datastructure")[🔗 GitHub Link])]
]

#pad(x: 12pt, [
  // *Abstract* - This project introduces a Java-based Message Queue (MQ) over TCP/IP, designed to facilitate efficient communication between interconnected machines. Motivated by the resurgence of message passing in modern computing, particularly within the context of microservices and service-oriented architectures, the authors embarked on the implementation challenge. The MQ relies on the Transmission Control Protocol (TCP/IP) for its networking layer.
  *Abstract* - This project explores the design and implementation of a message queue system in Java, focusing on the underlying data structure used for efficient communication. Addressing the need for a reliable communication layer across numerous services, we evaluate various queue implementations, including array-based, circular, and linked list-based queues, considering factors such as FIFO order, time complexity, and latency stability. Ultimately, we opt for a linked list-based queue, highlighting its advantages in dynamic resizing, avoidance of message loss, and consistent performance. The provided Java code implements this queue as a generic class and adheres to the Java Collections Framework, demonstrating its versatility for future use cases. Additionally, we present a real-world use case for our message queue system, illustrating its application as a task queue for an email notification service. Finally, we conduct a performance benchmark, comparing the latency and throughput of our implementation with Java standard library queues. The results showcase the competitive performance of our linked list-based queue for expected traffic, making it a suitable choice for applications with a moderate workload.
])

= Background

Message passing between different interconnected machines has always been deeply embedded in computing originating back to the 1960s when computers were still in their early days @internet_history. The very core part that powers these message-passing interfaces is the message queue, a data structure that governs the communication between multiple parties usually identified as producers or consumers @producer_consumer_paradigm. In recent years, due to the rise of service-oriented architecture (SOA) and microservices @microservices, the need for a reliable communication layer across hundreds if not thousands of services presents itself which gave birth to a lot of the open-source message queues found today such as Apache Kafka, RabbitMQ, ZeroMQ, etc. Each of these has its own set of strengths and weaknesses however the underlying idea is simple enough that we decided to challenge ourselves to implement one in Java that relies on Transmission Control Protocol (TCP/IP) for its networking layer.

= Design decisions

There are plenty of different implementations for queues such as array-based queues, circular queues, etc. each of which has its own pros and cons, a brief walkthrough of each implementation is discussed below and lastly, we present the reason for our choice. 

For context, the criteria for our selection are:

- *First-In-First-Out (FIFO)* - The first message that goes into the queue should be prioritized to be processed first.
- *Equivalent time complexity for polling and inserting* - We want the operations for both polling and inserting to be similar so that a high amount of writes will not slow down the reads or vice versa.
- *Stable latency across many different operations* - As a communication medium between different services, ideally the time for message exchange should be predictable and rather constant otherwise it could introduce jitter.

== Array-based queue

#figure(
  image("images/array_queue.png", width: 60%),
  caption: [Fixed-size array],
) <array_queue>

Using an array to build the queue is arguably the simplest implementation since it is the most common data stucture. However, there are certain downsides to it as we dig further into the details of it.

=== Benefits

Consider @array_queue where we have a fixed size array of size 9 with the first 3 elements occupied, accessing the first element (polling) is fast since it is a constant time look-up, inserting is also not an issue since it is also a constant time operation. In addition, having an array in memory is compact since all elements are stored in a contiguous block which helps with cache locality @cache_locality and can be further optimized to maximize cache hits in Translation Lookaside Buffer (TLB) by using huge pages in the operating system (OS) @huge_pages.

=== Downsides

However, there are several shortcomings to this approach one being that with every poll all subsequent elements have to be shifted down which is an `O(n)` operation, this does not scale well as the queue grows. Moreover, arrays are fixed size by default and when the array exceeds the current size it has to resize itself, this limitation still exists even if the implementation uses Java's `ArrayList` because the class itself also maintains a fixed size array behind the scenes and resizes whenever the number of element exceeds a certain capacity. In other words, insertion is constant time most of the time but it will be linear every once in a while when the array has to be resized and according to our selection criteria this is not acceptable since it would introduce an unpredictable spike in latency for the consumers.

== Circular queue

To overcome the shortcomings of the array-based queue, another common approach is to "wrap around" the array resulting the queue to be circular, refer to @circular_queue for an illustration.

#figure(
  image("images/circular_queue.png", width: 55%),
  caption: [Circular queue with an array],
) <circular_queue>

=== Benefits

The idea of "wrap-around" is when the queue becomes full instead of resizing to accommodate the new message, the new message replaces the head of the queue and keeps going. By doing so, it took away the need to allocate more memory and this approach keeps all the benefits of using an array such as compact memory layout and cache-friendly. 

=== Downsides

That said, the significant downside to this solution is that when the buffer is full, old messages will get overwritten and this might be tolerable depending on the use cases but in our case, it is not acceptable as we do not want a message to be lost before it has been processed. That said, if the condition where the rate of message consumption is far higher than the rate of message production is guaranteed then circular queue is an excellent choice with its performance benefits.

== Linked list based queue

#figure(
  image("images/linked_list_queue.png", width: 60%),
  caption: [Queue using singly linked list],
) <linked_list_queue>

=== Benefits

Linked list-based queues are one of the most commonly used due to their ability to grow and shrink dynamically without needing to resize but at the same time preserving all elements in memory. On top of that, its implementation is also very simple and usually does not involve too much code due to its structural simplicity.

=== Downsides

Although it also supports all operations in `O(1)` time it does not have the benefit of cache locality as compared to arrays because inherently the nodes do not necessarily live beside each other in a contiguous block, they are instead linked over using references or pointers since these nodes can be anywhere in memory. 

== Our choice

In the end we opted for a linked list-based queue due to the following reasons:

1. Ability to grow and shrink in constant time, `O(1)`.
2. No resizing required.
3. Guarantees of no message lost since no elements will be overwritten.

On top of that, we made an optimisation to keep track of a `tail` node so that offering a new message would be also `O(1)` since the need to traverse the list has been lifted. So our implementation of a linked list queue resembles @our_queue.

#figure(
  image("images/our_queue.png", width: 60%),
  caption: [Queue using singly linked list with tail],
) <our_queue>

= Code design

The class can be found at `src/mq/Queue.java` and its corresponding test is at `src/tests/QueueTest.java`. We implemented the `Queue` class to be generic although for the message queue, the only type being used is `String` but there is no reason to not implement it as a generics since this code can be used in the future for other use cases. On top of that, instead of writing our methods, we decided to implement `java.util.AbstractQueue` so it plays well with other parts of the Java ecosystem code and also it gives us the ability to change the underlying implementation without updating the driver code since the queue is used through an interface. 

The following snippet shows the class members and the operations it supports through the `AbstractQueue` interface redacting the implementation.

```java
public class Queue<T> extends AbstractQueue<T> {
    private class Node {
        private T data;
        private Node next;

        Node(T data) {
            this.data = data;
        }
    }

    private Node head, tail;
    private int size = 0;

    @Override
    public boolean offer(T e) {}

    @Override
    public T poll() {}

    @Override
    public T peek() {}

    @Override
    public Iterator<T> iterator() {}

    @Override
    public int size() {}

    @Override
    public int isEmpty() {}
}
```

One interesting mention here is that the `Queue` supports `java.util.Iterator` and this opens up the door for users to use it with the new Java Stream API since converting an iterator to a stream is simple. Then, the users will be able to use the queue just as any stream such as using the `.forEach()`, `.map()` and `.filter()`.

For example, the following snippet is taken from the test code and it shows the usage of Java Stream with the class.

```java
    // Create the queue
    Queue<String> queue = new Queue<>();
    queue.add("Alice");
    queue.add("Bob");
    queue.add("Candy");

    // Turn the iterator into a stream
    Stream<String> stream = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(queue.iterator(), Spliterator.ORDERED),
        false);

    // Map over the stream to make a list of wordcount
    List<Integer> list = stream.map(x -> x.length()).collect(Collectors.toList());
```

= Use cases

== Use case 1: Producer-consumer across different threads

In `src/Main.java` we provided a sample use case for the message queue where a single consumer starts in the main thread while there are 10 other producer threads producing random messages in the background. The purpose of this example is to provide a proof of concept and test the message queue for TCP connectivity. 

Following snippet shows the output of the example program where we can see that message queue is listening on port 3333 for TCP connections. The consumer on the main thread merely count how many messages it received in 1 second and print it out to the console.

```
❯ make run
javac -g -Xlint:deprecation -d bin src/benchmark/Main.java src/Main.java src/mq/MQ.java src/mq/network/Worker.java src/mq/network/Server.java src/mq/Queue.java src/tests/Context.java src/tests/QueueTest.java src/tests/Main.java src/benchmark/Main.java
java -cp bin Main
Listening for TCP connections on port 3333
A producer connected from /127.0.0.1:54675
A producer connected from /127.0.0.1:54677
A producer connected from /127.0.0.1:54679
A producer connected from /127.0.0.1:54676
A producer connected from /127.0.0.1:54680
A producer connected from /127.0.0.1:54678
A producer connected from /127.0.0.1:54682
A producer connected from /127.0.0.1:54681
A producer connected from /127.0.0.1:54683
A producer connected from /127.0.0.1:54684
Processed 47 messages in 1 second
Processed 61 messages in 1 second
Processed 72 messages in 1 second
Processed 70 messages in 1 second
...
```

To stop the program, we can press `CTRL + C` to send a `SIGINT` signal to the program and the program will gracefully shut down the threads and the message queue. The snippet below shows the console output when `CTRL + C` is invoked.

```
^CShutting down...
Signaling all producers to shutdown...
Producer 4 shut down successfully
A producer disconnected from /127.0.0.1:54678
Producer 2 shut down successfully
Producer 8 shut down successfully
Producer 3 shut down successfully
Producer 6 shut down successfully
A producer disconnected from /127.0.0.1:54676
Producer 5 shut down successfully
A producer disconnected from /127.0.0.1:54677
Producer 9 shut down successfully
Producer 7 shut down successfully
A producer disconnected from /127.0.0.1:54682
Producer 0 shut down successfully
A producer disconnected from /127.0.0.1:54683
Producer 1 shut down successfully
A producer disconnected from /127.0.0.1:54684
All producers shutdown successfully
Stopping MQ...
MQ stopped successfully
```

== Use case 2: Task Queue for Email Notification Service

Email notification service for a small online marketplace. Users of the marketplace can subscribe to receive email notifications for various events, such as new product listings, order updates, or promotions. As the user base grows, the system needs to handle sending a large volume of emails efficiently and reliably.

Implemented Message Queue System:

*1. Task Queuing:*
  
Use a message queue system to queue email notification tasks. Each task represents an email to be sent, including recipient, subject, and content.
  
  In the provided code, the Queue<T> class serves as the underlying data structure for the task queue. It provides methods for adding tasks (enqueueing) and retrieving tasks (dequeuing), ensuring thread-safe access to the queue.
  
*2. Producer-Consumer Architecture:*
 
Producer: In the provided code, the producer is represented by the application code responsible for triggering email notifications. It enqueues email tasks into the message queue.

Consumer: The worker processes in the Worker class act as consumers. They dequeue tasks from the message queue and perform the corresponding email-sending operations.

#figure(
  image("images/usecase_arch.png", width: 60%),
  caption: [Message queue architecture],
) <Architecture>

  
*3. Asynchronous Processing:*

Decouple email sending from the user interaction flow by using asynchronous processing with the message queue. Users can trigger email notifications without waiting for emails to be sent, improving responsiveness. The `ExecutorService` in the `Server` class manages the execution of worker threads, allowing for asynchronous task processing without blocking the main thread.

```java
// Inside Server class
// Start worker thread to handle incoming connections
executorService.execute(new Worker(socket, queue, executorService));
```
   
*4. Scalability:*
  
Scale the email notification service horizontally by adding more worker processes to handle increased email load during peak times. The message queue system distributes tasks evenly among available workers, ensuring efficient resource utilization. Cloud-based message queue services offer dynamic provisioning capabilities, enabling the email notification service to scale up or down based on demand, ensuring cost efficiency.

  *5. Error Handling:*

Implement retry logic and error handling within the worker processes to handle transient failures, such as temporary network issues or email service outages. Failed email tasks can be retried or moved to a dead-letter queue for manual inspection and resolution.
  
1. Retry Logic:
When an error occurs during email sending (e.g., network timeout, SMTP server error), the worker process can implement retry logic to attempt sending the email again.
The retry logic can include configurable parameters such as maximum retry attempts, backoff intervals between retries, and exponential backoff strategies to mitigate congestion.

2. Error Logging:
In case of failures during email sending, it's essential to log relevant error information for debugging and troubleshooting purposes.
Errors can be logged to standard output/error streams, log files, or a centralized logging system for easier monitoring and analysis.

3. Dead-Letter Queue:
If an email task repeatedly fails after exhausting retry attempts, it may be moved to a dead-letter queue for manual inspection and resolution.
A dead-letter queue allows administrators to review and address failed tasks, investigate underlying issues, and take appropriate corrective actions.

*6. Monitoring and Metrics:*
  Monitor the message queue system and worker processes to track email-sending performance, queue length, and error rates.
  Use metrics to identify bottlenecks, optimize resource allocation, and ensure timely delivery of email notifications.
  
Key Metrics to Monitor:

- Queue Length:

The length of the message queue indicates the number of pending email tasks waiting to be processed.
Monitoring queue length helps assess system workload and identify potential bottlenecks or congestion points.
Example: Implement a method to periodically log the current queue length to track changes over time.
```java

// Inside Server class
public void logQueueLength() {
    System.out.println("Queue Length: " + queue.size());
}```

- Throughput:

Throughput measures the rate at which email tasks are processed by the system.
Monitoring throughput helps evaluate system performance and capacity, ensuring that the system can handle the expected workload efficiently.

Example: We can calculate and log the throughput by measuring the rate of email tasks processed per unit of time.

```java
// Inside Worker class
private long startTime = System.currentTimeMillis();
private long processedTasks = 0;

public void logThroughput() {
    long currentTime = System.currentTimeMillis();
    long elapsedTime = currentTime - startTime;
    double throughput = (double) processedTasks / (elapsedTime / 1000.0); // Tasks per second
    System.out.println("Throughput: " + throughput + " tasks/sec");
}```

- Latency:

Latency measures the time taken to process email tasks from enqueueing to completion.
Monitoring latency helps identify performance issues and optimize system responsiveness.

Example: Record the start and end times of email-sending operations to calculate the latency.
```java
// Inside Worker class
public void sendEmailWithLatencyMeasurement(String emailTask) {
    long startTime = System.currentTimeMillis();
    // Perform email sending operation
    long endTime = System.currentTimeMillis();
    long latency = endTime - startTime;
    System.out.println("Latency: " + latency + " milliseconds");
}```


- Error Rates:

Error rates track the frequency of failed email-sending operations or other system errors.
Monitoring error rates helps detect issues early, allowing for timely intervention and resolution.

Example: Count and log the number of failed email-sending operations to track the error rate.

```java
// Inside Worker class
private int failedTasks = 0;

public void logErrorRate() {
    System.out.println("Error Rate: " + ((double) failedTasks / processedTasks) ** 100 + "%");
}```

Visualization and Alerting:

In addition to logging metrics, we used visualizing them using monitoring tools or dashboards for easy analysis and interpretation.
Implement alerting mechanisms to notify administrators or operators of critical issues or abnormal system behavior, allowing for prompt intervention and resolution.

= Performance benchmark

To measure how well our implementation of queue performs, we designed a benchmark to compare the latency and throughput of `mq.Queue` against queue implementations from the Java standard library, namely `LinkedList`, `PriorityQueue`, and `ArrayDeque`. The benchmark code can be found in `src/benchmark/Main.java` and these plots are generated by running `plot.py` which uses `matplotlib` to visualize the outputs from the benchmark program.

== Latency analysis

#figure(
  grid(
    columns: (1fr, 1fr),
    rows: (auto),
    gutter: 1em,
    image("images/benchmark_offer.png"),
    image("images/benchmark_poll.png"),
  ),
  caption: [Latency for offer and poll across different queues],
) <latency>

From @latency above we can see two charts, one for the *offer* operation which enqueues an element at the tail, and the other for the *poll* operation which dequeues an element from the head. We can see that for the *offer* operation, both `ArrayDeque` and `PriorityQueue` performed significantly faster than `LinkedList` and `mq.Queue` which are both based on the linked list under the hood. We speculate that as many operations increase, the effect of cache locality starts to show its significance since both `ArrayDeque` and `PriorityQueue` use an array under the hood to store the elements. In addition, one interesting observation is that the latency for `ArrayDeque` spiked at around 0 - 10k operations, this is expected since the underlying array has to resize when the insertion exceeds the current capacity.

As for the *poll* operation, `PriorityQueue` performed the worst and from the graph, we can clearly see that `mq.Queue`, `ArrayDeque` and `LinkedList` scaled well as the number of operations increases which is close to constant time _O(1)_. This is because `PriorityQueue` uses a binary heap under the hood which implies ordering hence to poll (remove) an element it has to find the element in a heap which results in a _O(log n)_ time complexity.

== Throughput analysis 

#figure(
  grid(
    columns: (1fr, 1fr, 1fr),
    rows: (auto),
    gutter: 0.2em,
    image("images/benchmark_offer_100.png"),
    image("images/benchmark_offer_1000.png"),
    image("images/benchmark_offer_10000.png"),
    image("images/benchmark_offer_100000.png"),
    image("images/benchmark_offer_1000000.png"),
    image("images/benchmark_offer_10000000.png"),
  ),
  caption: [Throughput comparison for offer across different queues],
) <throughput_offer>

@throughput_offer above shows the throughput for the `offer` operation across different numbers of operations which measure the operations that the queue can process per second. Here we can observe that `ArrayDeque` has the highest throughput in most scenarios except at 10k operations where it performed marginally slower than the others, this is due to the resizing issue as can also be seen in @latency above. As for `mq.Queue`, it performed generally well between 1k - 100k operations beating `LinkedList` and `PriorityQueue` and is on par with `ArrayDeque` at 1k operations.

#figure(
  grid(
    columns: (1fr, 1fr, 1fr),
    rows: (auto),
    gutter: 0.2em,
    image("images/benchmark_poll_100.png"),
    image("images/benchmark_poll_1000.png"),
    image("images/benchmark_poll_10000.png"),
    image("images/benchmark_poll_100000.png"),
    image("images/benchmark_poll_1000000.png"),
    image("images/benchmark_poll_10000000.png"),
  ),
  caption: [Throughput comparison for poll across different queues],
) <throughput_poll>

@throughput_poll above shows the throughput for the poll operation and as can be seen, both `mq.Queue` and `ArrayDeque` outperformed the others significantly with `mq.Queue` having higher throughput at 100 and 1000 operations and `ArrayDeque` performed the best in other number of operations.

== Conclusion

From the analysis that we have performed, we can deduce that generally `ArrayDeque` has the highest performance except for the fact that it has a resizing issue which can introduce latency spikes (jitter). Although this can be solved by reserving a large chunk of memory it incurs an overhead in memory cost when these reserved spaces are not being used. Considering that we do not expect high traffic for our message queue, `mq.Queue` is still the best choice for expected traffic of 0 - 100k operations while only using the memory that it needs.

#bibliography("references.yaml", style: "ieee")