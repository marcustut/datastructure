package graph;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import graph.Graph.SearchAlgorithm;

class ShortestPathTestCase {
    int source, destination;

    ShortestPathTestCase(int source, int destination) {
        this.source = source;
        this.destination = destination;
    }
}

public class GraphTest {
    Graph graph;

    @BeforeEach
    void setup() throws IOException {
        Dataset dataset = new Dataset();
        dataset.loadFacebookWOSNLinks();
        graph = dataset.toGraph();
    }

    @Test
    void testCommon() {
        assertIterableEquals(graph.common(1, 2), Arrays.asList(3, 7, 9, 11, 12, 16, 17, 18, 23));
    }

    @Test
    void testShortestPath() {
        var tcs = Arrays.asList(new ShortestPathTestCase(1, 2), new ShortestPathTestCase(1, 2061),
                new ShortestPathTestCase(1, 1645), new ShortestPathTestCase(4, 1747));

        for (var tc : tcs) {
            var bfs = graph.shortestPath(tc.source, tc.destination, SearchAlgorithm.BreadthFirstSearch);
            var dijkstra = graph.shortestPath(tc.source, tc.destination, SearchAlgorithm.Dijkstra);

            assertEquals(bfs.size(), dijkstra.size());
            if (bfs.size() == 0)
                continue;

            assertEquals(bfs.getFirst(), dijkstra.getFirst());
            assertEquals(bfs.getLast(), dijkstra.getLast());
        }
    }
}
