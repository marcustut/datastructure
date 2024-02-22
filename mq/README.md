# mq

*Message Queue over TCP/IP written in Java*

*Authors: @marcustut, @rita1773, @sauransh*

## Table of Contents

1. [Abstract](#abstract)
2. [Background](#background)
3. [Design decisions](#design-decisions)
    - [Array-based queue](#array-based-queue)
    - [Circular queue](#circular-queue)
    - [Linked list based queue](#linked-list-based-queue)
    - [Our choice](#our-choice)
    - [Code design](#code-design)
4. [Use cases](#use-cases)
5. [Performance benchmark](#performance-benchmark)

## Abstract <a id="abstract"></a>

...

## Background <a id="background"></a>

Message passing between different interconnected machines has always been something that was deeply embedded in computing originating back to the 1960s when computers were still in its early days [[1]](#1). The very core part that powers these message passing interface is the message queue, a data structure that governs the communication between multiple parties usually identify as producers or consumers [[2]](#2). In recent years, due to the rise of service oriented architecture (SOA) and microservices [[3]](#3), the need of a reliable communication layer across hundreds if not thousands of services presents itself which gave birth to a lot of the open source message queues found today such as Apache Kafka, RabbitMQ, ZeroMQ, etc. Each of which has their own set of strengths and weaknesses however the underlying idea is simple enough that we decided to challenge ourselves to implement one in Java that relies on Tranmission Control Protocol (TCP/IP) for its networking layer.

## Design decisions <a id="design-decisions"></a>

There are plenty different implementations for queue such as array-based queue, circular queue, etc. each of which has their own pros and cons, a brief walkthrough of each implementation is discussed below and lastly we present the reason for our choice. 

For context, the criterias for our selection are:

- **First-In-First-Out (FIFO)** - The first message that goes into the queue should be prioritised to be processed first.
- **Equivalent time complexity for polling and inserting** - We want the operations for both polling and inserting to be similar so that a high amount of writes will not slow down the reads or vice versa.
- **Stable latency across many different operations** - As a communication medium between different services, ideally the time for message exchange should be predictable and rather constant otherwise it could introduce jitter.

### Array-based queue <a id="array-based-queue"></a>

Using an array to build the queue is arguably the simplest implementation since it is the most common data stucture. However, there are certain downsides to it as we dig further into the details of it.

![array_queue](images/array_queue.png)

*<div align="center">Figure 1: Fixed-size array</div>*

#### Benefits 

Consider the figure above where we have a fixed size array of size 9 with the first 3 elements occupied, accessing the first element (polling) is fast since it is a constant time look up, inserting is also not an issue since it is also a constant time operation. In addition, having an array in memory is compact since all elements are stored in a contiguous block which helps with cache locality [[4]](#4) and can be further optimised to maximise cache hits in Translation Lookaside Buffer (TLB) by using huge pages in the operating system (OS) [[5]](#5).

#### Downsides

However there are several shortcomings to this approach one being that with every poll all subsequent elements has to be shifted down which is an `O(n)` operation, this does not scale well as the queue grows. Moreover, arrays are fixed size by default and when the array exceeds the current size it has to resize itself, this limitation still exist even if the implementation uses Java's `ArrayList` because the class itself also maintains a fixed size array behind the scenes and resizes whenever the number of element exceeds a certain capacity. In other words, insertion is constant time most of the time but it will be linear every once in a while when the array has to be resized and according to our selection criterias this is not acceptable since it would introduce an unpredictable spike in latency for the consumers.

### Circular queue <a id="cicular-queue"></a>

To overcome the shortcomings of the array-based queue, another common approach is to "wrap-around" the array resulting the queue to be circular, refer to the figure below for an illustration.

![circular_queue](images/circular_queue.png)

*<div align="center">Figure 2: Circular queue with an array</div>*

#### Benefits

The idea of "wrap-around" is when the queue becomes full instead of resizing to accomodate the new message, the new message replaces the head of the queue and keep going. By doing so, it took away the need of allocating more memory and this approach keep all the benefits of using an array such as compact memory layout and cache-friendly. 

#### Downsides

That said, the significant downside to this solution is that when the buffer is full, old messages will get overwritten and this might be tolerable depending on the use cases but in our case it is not acceptable as we do not want a message to be lost before it has been processed. That said, if the condition where rate of message consumption is far higher than the rate of message production is guaranteed then circular queue is an excellent choice with its performance benefits.

### Linked list based queue <a id="linked-list-based-queue"></a>

![linked_list_queue](images/linked_list_queue.png)

*<div align="center">Figure 3: Queue using singly linked list</div>*

#### Benefits

Linked list based queues are one of the most commonly used due to its ability to grow and shrink dynamically without needing to resize but at the same time preserving all elements in memory. On top of that, its implementation is also very simple usually does not involve too much code due to its strutural simplicity.

#### Downsides

Although it also supports all operations in `O(1)` time but it does not have the benefit of cache locality as compared to arrays because inherently the nodes does not necessarily live beside each other in a contiguous block, they are instead linked over using references or pointers since these nodes can be anywhere in memory. 

### Our choice <a id="our-choice"></a>

In the end we opted for a linked list based queue due to the following reasons:

1. Ability to grow and shrink in constant time, `O(1)`.
2. No resizing required.
3. Guarantees of no message lost since no elements will be overwritten.

On top of that, we made an optimisation to keep track of a `tail` node so that offering a new message would be also `O(1)` since the need to traverse the list has been lifted. So our implementation of a linked list queue resembles the following figure.

![our_queue](images/our_queue.png)

*<div align="center">Figure 4: Queue using singly linked list with tail</div>*

### Code design <a id="code-design"></a>

The class can be found at `src/mq/Queue.java` and its corresponding test is at `src/tests/QueueTest.java`. We implemented the `Queue` class to be generic although for the message queue the only type being using is `String` but there is no reason to not implement it as a generics since this code can be used in the future for other use cases. On top of that, instead of writing our own methods, we decided to implement `java.util.AbstractQueue` so it plays well with other part of the Java ecosystem code and also it gives us the ability to change the underlying implementation without updating the driver code since the queue is used through an interface. 

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

## Use cases <a id="Use cases"></a>

...

## Performance benchmark <a id="performance-benchmark"></a>

...

## References

<a id="1">[1]</a> *Leiner, B. M., Cerf, V. G., Clark, D. D., Kahn, R. E., Kleinrock, L., Lynch, D. C., ... & Wolff, S. S.* (1997). **The past and future history of the Internet**. Communications of the ACM, 40(2), 102-108. [PDF 🔗](https://dl.acm.org/doi/pdf/10.1145/253671.253741)

<a id="2">[2]</a> *Jeffay, K.* (1993, March). **The real-time producer/consumer paradigm: A paradigm for the construction of efficient, predictable real-time systems.** In Proceedings of the 1993 ACM/SIGAPP symposium on Applied computing: states of the art and practice (pp. 796-804). [PDF 🔗](https://dl.acm.org/doi/pdf/10.1145/162754.168703)

<a id="3">[3]</a> *Thönes, J*. (2015). **Microservices**. In IEEE Software (Vol. 32, Issue 1, pp. 116–116). Institute of Electrical and Electronics Engineers (IEEE). [![DOI:10.1109/ms.2015.11](https://zenodo.org/badge/DOI/10.1109/ms.2015.11.svg)](https://doi.org/10.1109/ms.2015.11)

<a id="4">[4]</a> *Grunwald, D., Zorn, B., & Henderson, R.* (1993). **Improving the cache locality of memory allocation**. In Proceedings of the ACM SIGPLAN 1993 conference on Programming language design and implementation. PLDI93: ACM SIGPLAN Conference on Programming Languages Design and Implementation. ACM. [![DOI:10.1145/155090.155107](https://zenodo.org/badge/DOI/10.1145/155090.155107.svg)](https://doi.org/10.1145/155090.155107)

<a id="5">[5]</a> *Panwar, A., Prasad, A., & Gopinath, K.* (2018). **Making Huge Pages Actually Useful**. In Proceedings of the Twenty-Third International Conference on Architectural Support for Programming Languages and Operating Systems. ASPLOS ’18: Architectural Support for Programming Languages and Operating Systems. ACM. [![DOI:10.1145/3173162.3173203](https://zenodo.org/badge/DOI/10.1145/3173162.3173203.svg)](https://doi.org/10.1145/3173162.3173203)