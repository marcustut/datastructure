package graph.ui;

/**
 * A utility class that provide helper methods for Graph UI.
 */
public class GraphUtil {
    /**
     * Make a unique key for each pair of edge.
     * 
     * @param from edge source
     * @param to   edge destination
     * @return unique key for the given edge.
     */
    public static String makeKey(int from, int to) {
        if (from < to)
            return Integer.toString(from) + "->" + Integer.toString(to);
        else
            return Integer.toString(to) + "->" + Integer.toString(from);
    }
}
