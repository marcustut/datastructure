#import "template.typ": *

#show: project.with(
  title: "Assignment 5: Social Network",
  authors: (
    (name: "Lee Kai Yang\n(23205838 - 100%)", email: "kai.y.lee@ucdconnect.ie"),
  ),
  date: "May 2, 2024",
  font: "CMU Serif",
  monofont: "CMU Typewriter Text",
)

#align(center)[
  #text(fill: blue, size: 9pt)[#underline(link("https://youtu.be/GwDDnRrRWLY")[🔗 Video Link])]
  #h(0.2em)
  #text(fill: blue, size: 9pt)[#underline(link("https://github.com/marcustut/datastructure/tree/main/graph")[🔗 GitHub Link])]
]

#pad(x: 12pt, [
  *Abstract* - This project focuses on developing a database access layer for a social network application, enabling users to execute queries efficiently, including finding mutual friends and determining the shortest path between users. Utilizing graph theory and algorithms, the system represents social connections as an undirected, unweighted graph stored in memory. Through meticulous design decisions, such as employing adjacency lists and selecting optimal algorithms like Dijkstra and BFS for pathfinding, the project achieves responsiveness within the desired latency range of 100ms to 1000ms. Performance evaluations, conducted on a subset of the Facebook WOSN Links dataset, demonstrate the effectiveness of the implemented methods. This work not only provides insights into the underlying mechanisms of social networks but also underscores the importance of benchmarking and algorithm selection in optimizing system performance.
])

= Background

Social networks has and only been something abstract for a long time up until the Internet boom along with the rise of online social medias such as MySpace, Friendster and most notably Facebook @internet_boom @social_media_history where software engineers had to find a way to describe relationship between one user and another to form a social circle whereby users interact with each other on the platform through this "network" where one can be friends with another and one can meet new friends through mutual friends. One intuitive way to represent these social networks in computer memory is through the use of graphs where every user is a vertex in a huge network and their relationship is described by the edges between them @social_network_analysis. By doing so, it is possible to store these relationship as a state in computer memory and identifying mutual friends or looking for friends of friends can be done so by finding the intersection of two graphs and graph traversal.

= Introduction

The goal of this project is to create a database access layer in which users can make two type of queries in a responsive manner:

1. Find mutual friends between two users
2. Determine how far away one user from another user

As for the measure of "responsive", it should be between 100ms to 1000ms according to an experiment conducted by the performance team at Sentry @api_response_time. 

The idea for these queries is to accomodate features as shown in @social_network_features below for a social network application.

#figure(
  grid(
    columns: (1fr, 1fr),
    gutter: 8pt,
    grid.cell(image("images/fb_friend_requests.png", width: 100%)),
    grid.cell(image("images/linkedin_profile.png", width: 100%)),
  ),
  caption: [Social network features],
) <social_network_features>

On the left in the figure above, it shows that when a user sends a friend request to another user, it is common for the application to show how many mutual friends they both share and who they are. On the right, the word "2nd" represents that this user has a second-order distance from the current user or in simpler terms the user is a "friend of friend".

However the challenge here is to find an algorithm to accomplish this with a good scaling factor since most social media platforms nowadays have massive amount of users and traversing through the entire graph is simply impossible.

== Social graphs

#figure(
  image("images/unconnected_social_graph.png", width: 80%),
  caption: [Unconnected Social Graph],
) <unconnected_social_graph>

@unconnected_social_graph shows a graph where the nodes are all unconnected, this is the initial state for a social network where everyone has an account registered but they have not make friends with anyone else.

#figure(
  image("images/one_connected_social_graph.png", width: 80%),
  caption: [Social Graph (one connected)],
) <one_connected_social_graph>

@one_connected_social_graph shows that "marcus" and "liana" are now friends since there is an edge connected them two and note that this edge is not directed because the relationship exist both ways, "marcus" is a friend of "liana" and at the same time "liana" is a friend of "marcus".

#figure(
  image("images/fully_connected_social_graph.png", width: 80%),
  caption: [Social Graph (fully connected)],
) <fully_connected_social_graph>

@fully_connected_social_graph shows a fully connected graph where everyone has at least one friend or more. In this case, "alex" and "anson" are the mutual friends between "marcus" and "liana". Furthermore, "liana" has a 2nd-order relationship with "roger" or also known as "friend of friend". Also, it is good to point out that the weights for all the edges are equal.

= Design decisions

As can be seen from the explanation above, social networks can be represented as an undirected unweighted graph. Hence, we can store them efficiently in memory as adjacency lists using a HashMap. Details of how the code is implemented is detailed below.

== Classes overview

#figure(
  image("images/uml.png", width: 90%),
  caption: [UML Diagram],
) <uml>

As can be seen from @uml, there are a few classes in the application:

- *Graph* - A data structure to represent interconnected, undirected graphs.
- *Dataset* - A helper class that is purposed for loading external datasets.
- *App* - The entrypoint for the GUI visualiser.
- *GraphContainer* - A wrapper container UI component for the graph in GUI.
- *GraphUtil* - A utility class providing helper methods.

== Graph

This class is where all the main logic resides, it maintains the graph in a HashMap as an adjacency list. Hence, the important operations such as `common()` and `shortestPath()` are found in this class.

Since the graph is stored as an adjacency list, the `common()` method can be implemented by simply finding the intersection between the edges of the two vertices. However for the `shortestPath()` method, there are plenty of path traversal algorithm that we can use such as Bellman-Ford, Floyd-Warshall, Dijkstra, etc. Considering the performance requirements, I decided to implement Dijkstra algorithm and a simple breadth first search (BFS) since these algorithms does not need to pre-compute the graph and is much more dynamic to change as in we don't have to recompute distances for the entire graph every time a new vertice or edge are added. Hence when calling the `shortestPath()` method, it is required to specify which search algorithm should be used by passing in the `SearchAlgorithm` enum which is defined as:

```java
public enum SearchAlgorithm {
    Dijkstra,
    BreadthFirstSearch
}
```

To find the shortest path between two vertices, one can use either specify `SearchAlgorithm.Dijkstra` or `SearchAlgorithm.BreadthFirstSearch`: 

```java
graph.shortestPath(from, to, SearchAlgorithm.Dijkstra);
graph.shortestPath(from, to, SearchAlgorithm.BreadthFirstSearch);
```

== Dataset

This class is used to help simplify the process of loading external datasets and transforming them into the `Graph` class. In this project we used the public dataset Facebook WOSN Links @facebook_wosn_links which can be loaded using the method `loadFacbookWOSNLinks()`. An example of using the class is as follows:

```java
Dataset dataset = new Dataset();

try {
    dataset.loadFacebookWOSNLinks();
    System.out.printf("Loaded %d data points\n", dataset.size());
} catch (Exception e) {
    System.err.println("Failed to load data: " + e);
}

Graph graph = dataset.toGraph();
```

== App

This class along with *GraphContainer* and *GraphUtil* are all related to the GUI made to showcase and visualise the operations for the *Graph* class using a subset of the Facebook WOSN Links dataset.

= Test cases

The test cases for the aforementioned classes can be found in `src/test/java/graph` and can be ran by executing `./gradlew test` in the terminal.

= Examples

== Friends Explorer

To visualise the graph I build a visualizer using JavaFX. What it does is simply loading a subset of the Facebook WOSN Links dataset, build a graph and visualise them, at the same time allow users to perform two operations:

1. Find the common vertices (mutual friends) between two nodes.
2. Find the shortest path between two nodes.

Note that we only load 60 data points otherwise the graph is too big to even see visually. To run the visualizer, run `./gradlew run -Plaunch=graph.example.App` in the terminal.

#figure(
  image("images/visualiser_initial.png", width: 100%),
  caption: [Friends Explorer (Start screen)],
) <visualiser_initial>

@visualiser_initial shows the initial screen when the visualizer is opened. As can be seen, the side bar on the right shows the current zoom level, a user can scroll their mouse wheel to control the zoom. Moreover, there is a bottom bar where users can select the *from* and *to* vertices and perform the aforementioned operations.

#figure(
  image("images/visualiser_common_friends.png", width: 100%),
  caption: [Friends Explorer (Common friends)],
) <visualiser_common_friends>

@visualiser_common_friends shows the visualiser when a user asks for the common friends between "1" and "2", the common friends are those vertices highlighted in orange.

#figure(
  image("images/visualiser_shortest_path.png", width: 100%),
  caption: [Friends Explorer (Shortest path)],
) <visualiser_shortest_path>

@visualiser_shortest_path shows the visualiser when a user asks for the shortest path between "1" and "2054", the path is highlighted in blue.

For a more detailed demonstration of the visualiser refer to the video linked at the beginning of the paper.

== Benchmark

The goal of this project is to find a way to provide the two operations aforementioned, finding mutual friends and shortest path between two vertices in a responsive manner where the latency is ideally between 100ms - 1000ms.

To find out the performance of the graph database I built, I wrote a benchmark program which load the entire Facebook WOSN Links dataset which has 817035 data points and measure the time taken for `graph.common()`, `graph.shortestPath() (Dijkstra)` and `graph.shortestPath() (BFS)` across 1000 runs. The benchmark can be ran by the command `./gradlew run -Plaunch=graph.example.Benchmark`.

#figure(
  image("images/benchmark.png", width: 100%),
  caption: [Benchmark results],
) <benchmark>

@benchmark shows the output of the benchmark program and as can be seen the `graph.common()` is indeed very fast which is expected since it is merely finding intersections between two vertices which has a time complexity of `O(m+n)` where `m` is the number of edges for node one and `n` is the number of edges for node two. 

Comparing the latencies for the two shortest path algorithms, we see that in our case BFS is much faster than Dijkstra by almost 3 times, this is due to the fact that the BFS algorithm has a time complexity of `O(V + E)` but the Dijkstra is `O(V + E log V)`. The reason that Dijkstra has an extra log term in its time complexity is because it uses a priority queue to keep track of the current shortest path but BFS does not. However, Dijkstra is still a solid algorithm for cases where the graph is weighted and with added heuristics it can be optimised to go even faster which essentially is an A\* algorithm but in our case because our graph is unweighted, there is no benefit in using Dijkstra, BFS is superior in our case.

That said, all three operations are well under the 1000ms limit which comforms to our initial performance goals. Note that in our benchmark we do not consider network latencies, it is only measuring the time elapsed for each function call and the results in a real production environment will likely differ a lot with the added HTTP overhead, etc.

= Conclusion

In conclusion, I am satisfied with what this project had achieved and it helped me "demystified" the core ideas behind modern days social media platforms. Also, by completing this project I learned that it is important to test and benchmark the algorithm before making a conclusion which one is better. In this case, Dijkstra's algorithm is the most well-know and dominant path finding algorithm that is being used in Google Maps or any other GPS system but it does not necessarily mean that it will be the best algorithm for our application, after performing benchmark we found that a simple BFS outperforms Dijkstra's algorithm in this application.

#bibliography("references.yaml", style: "ieee")