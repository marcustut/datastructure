package graph.example;

import java.util.*;

import graph.Dataset;
import graph.Graph;
import graph.Graph.SearchAlgorithm;

public class Benchmark {
    public static void main(String[] args) throws Exception {
        Dataset dataset = new Dataset();

        try {
            dataset.loadFacebookWOSNLinks();
            System.out.printf("Loaded %d data points\n", dataset.size());
        } catch (Exception e) {
            System.err.println("Failed to load data: " + e);
        }

        Graph graph = dataset.toGraph();

        // Random rand = new Random();
        int count = 100;
        long dijkstraTotal = 0;
        long bfsTotal = 0;
        long commonTotal = 0;
        for (int i = 0; i < count; i++) {
            if (i % 10 == 0)
                System.out.printf("%d/%d\n", i, count);
            // int from = dataset.get(rand.nextInt(dataset.size())).from;
            // int to = dataset.get(rand.nextInt(dataset.size())).to;
            int from = 4;
            int to = 1747;

            long start = System.nanoTime();
            List<Integer> dijkstra = graph.shortestPath(from, to, SearchAlgorithm.Dijkstra);
            dijkstraTotal += System.nanoTime() - start;

            start = System.nanoTime();
            List<Integer> bfs = graph.shortestPath(from, to, SearchAlgorithm.BreadthFirstSearch);
            bfsTotal += System.nanoTime() - start;

            start = System.nanoTime();
            graph.common(from, to);
            commonTotal += System.nanoTime() - start;

            if (dijkstra.size() != bfs.size())
                throw new Exception("dijkstra.size() != bfs.size()");
            if (dijkstra.size() == 0)
                continue;

            if (dijkstra.getFirst().compareTo(bfs.getFirst()) != 0)
                throw new Exception("dijkstra.getFirst() != bfs.getFirst()");

            if (dijkstra.getLast().compareTo(bfs.getLast()) != 0)
                throw new Exception("dijkstra.getLast() != bfs.getLast()");
        }

        System.out.printf("%d runs of graph.shortestPathDijkstra() completed in %.2fms, averaging %.2fms/op.\n", count,
                dijkstraTotal * 1e-6,
                ((double) dijkstraTotal / count) * 1e-6);
        System.out.printf("%d runs of graph.shortestPathBFS() completed in %.2fms, averaging %.2fms/op.\n", count,
                bfsTotal * 1e-6,
                ((double) bfsTotal / count) * 1e-6);
        System.out.printf("%d runs of graph.common() completed in %.2fms, averaging %.2fms/op.\n", count,
                commonTotal * 1e-6,
                ((double) commonTotal / count) * 1e-6);
    }
}