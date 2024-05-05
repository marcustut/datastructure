package graph;

import java.util.*;
import java.nio.file.*;
import java.io.IOException;

/**
 * A helper class that is purposed for loading external datasets. At the moment,
 * it only supports the Facebook WOSN Links dataset.
 */
public class Dataset {
    final static String DATADIR = System.getProperty("user.dir") + "/src/main/resources";
    public List<Data> datas;

    public class Data {
        public int from, to;

        Data(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    public Dataset() {
        this.datas = new ArrayList<>();
    }

    /**
     * Get a data point by the index.
     * 
     * @param i
     * @return data point
     */
    public Data get(int i) {
        return datas.get(i);
    }

    /**
     * Size of the current loaded data.
     * 
     * @return size
     */
    public int size() {
        return datas.size();
    }

    /**
     * Load Facebook WOSN Links dataset without a maximum limit.
     * 
     * @throws IOException
     */
    public void loadFacebookWOSNLinks() throws IOException {
        loadFacebookWOSNLinks(-1);
    }

    /**
     * Load Facebook WOSN Links dataset for the specified limit only. Pass -1 as the
     * limit if you want to load the entire dataset.
     * 
     * @throws IOException
     */
    public void loadFacebookWOSNLinks(int limit) throws IOException {
        var lines = Files.readAllLines(Paths.get(DATADIR + "/facebook-wosn-links/out.facebook-wosn-links"));

        for (int i = 0; i < lines.size(); i++) {
            String[] tokens = lines.get(i).split(" ");
            if (tokens[0].compareTo("%") == 0)
                continue;
            int from = Integer.parseInt(tokens[0]);
            int to = Integer.parseInt(tokens[1]);
            datas.add(new Data(from, to));
            if (limit > 0 && i > limit)
                break;
        }
    }

    /**
     * Construct a graph from the currently loaded data.
     * 
     * @return graph of the currently loaded data.
     */
    public Graph toGraph() {
        Graph graph = new Graph();
        for (var data : datas)
            graph.addEdge(data.from, data.to, 1);
        return graph;
    }
}
