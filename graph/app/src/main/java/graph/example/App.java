package graph.example;

import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartRandomPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;

import javafx.application.*;
import javafx.scene.*;
import javafx.stage.Stage;

import graph.Dataset;
import graph.Graph;
import graph.Dataset.Data;
import graph.ui.GraphContainer;
import graph.ui.GraphUtil;

public class App extends Application {
    Graph graph;
    SmartGraphPanel<Integer, String> graphView;
    com.brunomnsilva.smartgraph.graph.Graph<Integer, String> uiGraph;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Dataset dataset = new Dataset();

        // Load the Facebook WOSN Links dataset
        try {
            dataset.loadFacebookWOSNLinks(60);
            System.out.printf("Loaded %d data points\n", dataset.size());
        } catch (Exception e) {
            System.err.println("Failed to load data: " + e);
        }

        // Construct a graph from the dataset
        graph = dataset.toGraph();

        // Construct a graph for the UI
        uiGraph = new GraphEdgeList<>();
        for (int vertex : graph.vertices())
            uiGraph.insertVertex(vertex);
        for (Data data : dataset.datas)
            uiGraph.insertEdge(data.from, data.to, GraphUtil.makeKey(data.from, data.to));

        graphView = new SmartGraphPanel<>(uiGraph, new SmartRandomPlacementStrategy());
        Scene scene = new Scene(new GraphContainer(graphView, graph, uiGraph, dataset), 1024, 768);
        stage.setTitle("Friends Explorer");
        stage.setScene(scene);
        stage.show();

        graphView.init(); // initialise the UI Graph

        // Apply styling for the main vertices
        for (Data data : dataset.datas)
            graphView.getStylableVertex(data.from).setStyleClass("main-vertex");
    }
}