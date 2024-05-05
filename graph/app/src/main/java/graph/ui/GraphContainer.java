package graph.ui;

import com.brunomnsilva.smartgraph.containers.ContentZoomScrollPane;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import graph.Graph.SearchAlgorithm;

/**
 * A wrapper UI container for GraphView that includes a side bar and a bottom
 * bar. The sidebar shows the current zoom level with a slider control where the
 * bottom bar includes UI elements that user can interact with.
 */
public class GraphContainer extends BorderPane {

    private final ContentZoomScrollPane contentZoomPane;
    private com.brunomnsilva.smartgraph.graph.Graph<Integer, String> uiGraph;
    private graph.Graph graph;
    private graph.Dataset dataset;

    /**
     * Creates a new instance of GraphContainer pane.
     */
    public GraphContainer(SmartGraphPanel<Integer, String> graphView, graph.Graph graph,
            com.brunomnsilva.smartgraph.graph.Graph<Integer, String> uiGraph, graph.Dataset dataset) {
        if (graphView == null)
            throw new IllegalArgumentException("View cannot be null.");

        this.graph = graph;
        this.uiGraph = uiGraph;
        this.dataset = dataset;

        setCenter(this.contentZoomPane = new ContentZoomScrollPane(graphView));
        Background background = new Background(new BackgroundFill(Color.WHITE, null, null));

        setRight(createSidebar(this.contentZoomPane, background));
        setBottom(createBottomBar(graphView, background));
    }

    private void reset(SmartGraphPanel<Integer, String> view) {
        for (var vertex : uiGraph.vertices())
            view.getStylableVertex(vertex).setStyleClass("vertex");

        for (var data : dataset.datas)
            view.getStylableVertex(data.from).setStyleClass("main-vertex");

        for (var edge : uiGraph.edges())
            view.getStylableEdge(edge).setStyleClass("edge");
    }

    /**
     * Create bottom pane with UI Elements.
     */
    private Node createBottomBar(SmartGraphPanel<Integer, String> view, Background bg) {
        HBox bar = new HBox(20);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(10));
        bar.setBackground(bg);

        // Create toggle to control automatic layout
        CheckBox automatic = new CheckBox("Automatic layout");
        automatic.selectedProperty().bindBidirectional(view.automaticLayoutProperty());

        ComboBox<Integer> sourceComboBox = new ComboBox<>();
        for (int vertex : graph.vertices())
            sourceComboBox.getItems().add(vertex);
        sourceComboBox.setValue(1);

        ComboBox<Integer> destinationComboBox = new ComboBox<>();
        for (int vertex : graph.vertices())
            destinationComboBox.getItems().add(vertex);
        destinationComboBox.setValue(2);

        ComboBox<SearchAlgorithm> algorithmComboBox = new ComboBox<>();
        algorithmComboBox.getItems().add(SearchAlgorithm.BreadthFirstSearch);
        algorithmComboBox.getItems().add(SearchAlgorithm.Dijkstra);
        algorithmComboBox.setValue(SearchAlgorithm.BreadthFirstSearch);

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                reset(view);
            }
        });

        Button cfButton = new Button("Show Common Friends");
        cfButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                reset(view);
                for (int vertex : graph.common(sourceComboBox.getValue(), destinationComboBox.getValue()))
                    view.getStylableVertex(vertex).setStyleClass("common-vertex");
            }
        });

        Button pathButton = new Button("Show Path");
        pathButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                reset(view);
                var path = graph.shortestPath(sourceComboBox.getValue(), destinationComboBox.getValue(),
                        algorithmComboBox.getValue());
                for (int i = 0; i < path.size() - 1; i++)
                    view.getStylableEdge(GraphUtil.makeKey(path.get(i), path.get(i + 1)))
                            .setStyleClass("highlighted-edge");
            }
        });

        // Add components
        bar.getChildren().addAll(
                automatic,
                resetButton,
                new Separator(Orientation.VERTICAL),
                sourceComboBox,
                destinationComboBox,
                algorithmComboBox,
                new Separator(Orientation.VERTICAL),
                cfButton,
                pathButton);

        return bar;
    }

    /**
     * Creates a sidebar with slider control pane to control the zoom level
     */
    private Node createSidebar(ContentZoomScrollPane zoomPane, Background bg) {
        VBox paneSlider = new VBox(10);
        paneSlider.setAlignment(Pos.CENTER);
        paneSlider.setPadding(new Insets(10));
        paneSlider.setSpacing(10);
        paneSlider.setBackground(bg);

        // Create slider to control zoom level
        Slider slider = new Slider(zoomPane.getMinScaleFactor(),
                zoomPane.getMaxScaleFactor(), zoomPane.getMinScaleFactor());

        slider.setOrientation(Orientation.VERTICAL);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(zoomPane.getDeltaScaleFactor());
        slider.setMinorTickCount(1);
        slider.setBlockIncrement(0.125f);
        slider.setSnapToTicks(true);

        slider.valueProperty().bind(zoomPane.scaleFactorProperty());

        // Add components
        paneSlider.getChildren().addAll(slider, new Text("Zoom"));

        return paneSlider;
    }

}