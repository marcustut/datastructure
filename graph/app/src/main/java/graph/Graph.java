package graph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A data structure to represent interconnected, undirected graphs.
 */
public class Graph {
    private Map<Integer, Set<Edge>> adjList;

    /**
     * Represents an edge in the graph
     */
    public class Edge {
        public int key, weight;

        public Edge(int key, int weight) {
            this.key = key;
            this.weight = weight;
        }
    }

    /**
     * Variants of algorithm to be used for `graph.shortestPath`
     */
    public enum SearchAlgorithm {
        Dijkstra,
        BreadthFirstSearch
    }

    public Graph() {
        adjList = new HashMap<>();
    }

    /**
     * Add an edge onto the graph
     * 
     * @param source      start vertex
     * @param destination end vertex
     * @param weight      edge weight
     */
    public void addEdge(int source, int destination, int weight) {
        adjList.computeIfAbsent(source, k -> new HashSet<>()).add(new Edge(destination, weight));
        adjList.computeIfAbsent(destination, k -> new HashSet<>()).add(new Edge(source, weight));
    }

    /**
     * Returns a set of vertices
     * 
     * @return set of vertices in the graph
     */
    public Set<Integer> vertices() {
        Set<Integer> set = new HashSet<>();
        for (var entry : adjList.entrySet()) {
            set.add(entry.getKey());
            set.addAll(entry.getValue().stream().map(edge -> edge.key).collect(Collectors.toSet()));
        }
        return set;
    }

    /**
     * Find the intersection of two vertices.
     * 
     * @param nodeOne first vertex
     * @param nodeTwo second vertex
     * @return list of intersected vertices.
     */
    public List<Integer> common(int nodeOne, int nodeTwo) {
        Set<Integer> nodesOne = new HashSet<>(
                adjList.getOrDefault(nodeOne, Collections.emptySet())
                        .stream()
                        .map(edge -> edge.key)
                        .collect(Collectors.toSet()));
        Set<Integer> nodesTwo = adjList.getOrDefault(nodeTwo, Collections.emptySet())
                .stream()
                .map(edge -> edge.key)
                .collect(Collectors.toSet());

        // Find intersection of two sets of nodes
        nodesOne.retainAll(nodesTwo);

        return new ArrayList<>(nodesOne);
    }

    /**
     * Distance between two vertices.
     * 
     * @param source      start vertex
     * @param destination end vertex
     * @return number indicating the distance between two vertices.
     */
    public int distance(int source, int destination) {
        return shortestPath(source, destination, SearchAlgorithm.BreadthFirstSearch).size() - 1;
    }

    /**
     * Find the shortest path between two vertices.
     * 
     * @param source      start vertex
     * @param destination end vertex
     * @param algorithm   search method to use
     * @return list of vertices indicating the shortest path between two vertices.
     */
    public List<Integer> shortestPath(int source, int destination, SearchAlgorithm algorithm) {
        switch (algorithm) {
            case Dijkstra:
                return shortestPathDijkstra(source, destination);
            case BreadthFirstSearch:
                return shortestPathBFS(source, destination);
            default:
                return Collections.emptyList();
        }
    }

    class Path implements Comparable<Path> {
        int key;
        List<Integer> paths;
        int distance;

        Path(int key, List<Integer> paths, int distance) {
            this.key = key;
            this.paths = paths;
            this.distance = distance;
        }

        @Override
        public int compareTo(Path o) {
            return Integer.compare(distance, o.distance);
        }

        @Override
        public String toString() {
            return key + "(" + distance + " via " + paths + ")";
        }
    }

    /**
     * Find shortest path using Dijkstra's Algorithm.
     */
    private List<Integer> shortestPathDijkstra(int source, int destination) {
        Map<Integer, Integer> distance = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Path> queue = new PriorityQueue<>();

        // Initialise the distance map
        for (int vertex : adjList.keySet())
            distance.put(vertex, Integer.MAX_VALUE);
        distance.put(source, 0);

        // Start from the source
        queue.offer(new Path(source, Arrays.asList(), 0));

        while (!queue.isEmpty()) {
            Path current = queue.poll();
            visited.add(current.key);

            List<Integer> newPath = new ArrayList<>(current.paths);
            newPath.add(current.key);

            if (current.key == destination)
                return newPath;

            for (Edge edge : adjList.getOrDefault(current.key, Collections.emptySet()))
                if (!visited.contains(edge.key) && (current.distance + edge.weight < distance.get(edge.key))) {
                    distance.put(edge.key, current.distance + edge.weight);
                    queue.offer(new Path(
                            edge.key,
                            newPath,
                            current.distance + edge.weight));
                }
        }

        // If target node is not reachable from source node, return an empty path
        return Collections.emptyList();
    }

    /**
     * Find shortest path using Breadth First Search.
     */
    private List<Integer> shortestPathBFS(int source, int destination) {
        Queue<List<Integer>> queue = new LinkedList<>(); // Queue of paths
        Set<Integer> visited = new HashSet<>();

        // Initialize with the start node
        queue.offer(Arrays.asList(source));

        while (!queue.isEmpty()) {
            List<Integer> path = queue.poll();
            int lastNode = path.get(path.size() - 1);

            // If we reach the target node, return the path
            if (lastNode == destination)
                return path;

            // Explore neighbors
            for (Edge neighbor : adjList.getOrDefault(lastNode, Collections.emptySet())) {
                if (!visited.contains(neighbor.key)) {
                    visited.add(neighbor.key);
                    List<Integer> newPath = new ArrayList<>(path);
                    newPath.add(neighbor.key);
                    queue.offer(newPath);
                }
            }
        }

        // If target node is not reachable from start node, return an empty path
        return Collections.emptyList();
    }
}
