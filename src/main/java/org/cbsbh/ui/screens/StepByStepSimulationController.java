package org.cbsbh.ui.screens;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.cbsbh.context.Context;
import org.cbsbh.model.ModelRunner;
import org.cbsbh.model.routing.MPPNetwork;
import org.cbsbh.model.routing.SMPNode;
import org.cbsbh.ui.AbstractScreen;
import org.cbsbh.ui.screens.graph_visualisation.NodeController;
import org.cbsbh.ui.screens.graph_visualisation.NodeDetailsController;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Description goes here
 * Date: 4/20/14 3:14 AM
 *
 * @author Mihail Chilyashev
 */
public class StepByStepSimulationController extends AbstractScreen {

    private Context context;

    private ModelRunner runner;

    private HashMap<Integer, GraphNode> graphNodes;
    private HashMap<Integer, HashMap<Integer, Line>> vertices;
    private ArrayList<PathEntry> currentPathInGraph;
    private Group graphGroup;
    private Group nodeGroup;

    private NodeDetailsController detailsController;
    private AnchorPane detailsControl;


    // FXML controls
    @FXML
    public AnchorPane mainPane;
    @FXML
    public Label statusLabel;
    @FXML
    public AnchorPane rightPane;
    // eo FXML controls


    public StepByStepSimulationController() {
        context = Context.getInstance();
        vertices = new HashMap<>();
        graphGroup = new Group();
        nodeGroup = new Group();
        graphNodes = new HashMap<>();
        currentPathInGraph = new ArrayList<>();
        runner = null;
        detailsController = null;

        //runnerThread = null;
        //...
    }

    @Override
    public void init() {
        setTitle("Изпълнение стъпка по стъпка");

        // UI stuff
        FXMLLoader detailsLoader;
        detailsLoader = new FXMLLoader(getClass().getResource("/screens/graph_vis/node_details.fxml"));
        try {
            detailsControl = detailsLoader.load();
            assert detailsControl != null;
            detailsController = detailsLoader.getController();
            rightPane.getChildren().setAll(detailsControl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Model stuff
        runner = new ModelRunner(context, event -> System.err.println("Well..."));
        runner.setStepByStepExecution(true);

        Context.getInstance().set("channelCount", 4);
        Context.getInstance().set("nodeCount", 16);
        Context.getInstance().set("maxMessageSize", 3);
        Context.getInstance().set("bufferCountPerInputChannel", 3); // TODO!
        Context.getInstance().set("messageGenerationFrequency", 1); // TODO
        Context.getInstance().set("minMessageSizel", 4); // TODO
        Context.getInstance().set("fifoQueueCount", 3); // TODO

        runner.init(Context.getInstance().getInteger("channelCount"));
        runner.start();

        //new AnimatedStepByStep().start();


        int[] leftCube = {6, 7, 2, 3, 4, 5, 0, 1};
        int[] rightCube = {14, 15, 10, 11, 12, 13, 8, 9};

        drawNodeCube(leftCube, 0, 180);
        drawNodeCube(rightCube, 360, 0);

        //
        // Дъгите се добавят тук, защото иначе не работи. Първият куб остава винаги дебел.
        for (HashMap<Integer, Line> lines : vertices.values()) {
            graphGroup.getChildren().addAll(lines.values());
        }
        //

        graphGroup.toBack();
        nodeGroup.toFront();
        graphGroup.getChildren().add(nodeGroup);
        //mainPane.getChildren().add(nodeGroup);
        mainPane.getChildren().add(graphGroup);
    }

    private void drawNodeCube(int nodeIds[], int offsetX, int offsetY) {
        int x, y, xInc, yInc, maxX, xMargin;
        int nodesInRow, maxNodesPerRow, currentRow;
        xMargin = offsetX;
        x = xMargin;
        y = offsetY;
        xInc = 200;
        yInc = 100;
        maxX = 700;
        nodesInRow = 0;
        maxNodesPerRow = 2;
        currentRow = 1;

        // Load the custom control
        FXMLLoader loader;
        AnchorPane control;
        Line vertex;
        NodeController controller;

        // Add all the nodes to the graph
        //for (SMPNode node : MPPNetwork.getAll()) {
        for (int nodeId : nodeIds) {
            SMPNode node = MPPNetwork.get(nodeId);
            System.err.printf("Inserting node %d\n", nodeId);
            String binId = Integer.toBinaryString(nodeId);
            String buttonText = String.format("%s%s", "0000".substring(0, 4 - binId.length()), binId);
            try {
                loader = new FXMLLoader(getClass().getResource("/screens/graph_vis/node.fxml"));
                control = loader.load();
                assert control != null;
                controller = loader.getController();
                controller.setText(buttonText);
                controller.setLocation(x, y);
                controller.setTooltip(new Tooltip("Все още няма информация за този възел."));

                controller.setOnEnterHandler(event -> updateVerticesOnEnter(nodeId));
                controller.setOnExitHandler(event -> updateVerticesOnExit(nodeId));

                graphNodes.put(nodeId,
                        new GraphNode(
                                (int) (x + control.getPrefWidth() / 2),
                                (int) (y + control.getPrefHeight() / 2),
                                node,
                                controller
                        )
                );

                nodeGroup.getChildren().addAll(control.getChildrenUnmodifiable());

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (currentRow % 2 == 0) {
                xMargin = offsetX;
            } else {
                xMargin = offsetX + 80;
            }

            x += xInc;
            nodesInRow++;
            if (x >= maxX || nodesInRow >= maxNodesPerRow) {
                x = xMargin;
                y += yInc;
                nodesInRow = 0;
                currentRow++;
            }
        }

        for (GraphNode graphNode : graphNodes.values()) {
            for (Integer neighbor : graphNode.getNeighbors()) {
                GraphNode neighborNode = graphNodes.get(neighbor);
                if (neighborNode == null) {
                    continue;
                }
                vertex = new Line(graphNode.x, graphNode.y, neighborNode.x, neighborNode.y);

                if (vertices.get(graphNode.smpNode.getId()) == null) {
                    vertices.put(graphNode.smpNode.getId(), new HashMap<>());
                }

                vertices.get(graphNode.smpNode.getId()).put(neighbor, vertex);
                //graphGroup.getChildren().add(vertex);
            }
        }
    }

    private void updateVerticesOnEnter(int nodeId) {
        detailsControl.setVisible(true);
        detailsController.setText(graphNodes.get(nodeId).smpNode.toString());


        for (HashMap<Integer, Line> lines : vertices.values()) {
            for (Line line : lines.values()) {
                line.setStroke(new Color(0, 0, 0, .2));
            }
        }

        for (Line line : vertices.get(nodeId).values()) {
            line.setStroke(new Color(1, 0, 0, 1));
            line.setStrokeWidth(3.14159);
        }
    }

    private void updateVerticesOnExit(int nodeId) {
        for (HashMap<Integer, Line> lines : vertices.values()) {
            for (Line line : lines.values()) {
                line.setStroke(new Color(0, 0, 0, 1));
                line.setStrokeWidth(1);
            }
        }
        //rightPane.getChildren().removeAll(detailsControl);
    }

    String lastPacketColor = "43ff00";
    String lastFlitId = "";
    Color packetColor = null;

    public void nextStep(ActionEvent actionEvent) {

        statusLabel.setText("Такт: " + context.getString("currentModelTick"));

        for (GraphNode node : graphNodes.values()) {
            Tooltip tooltip = new Tooltip("Not now, delicious friend.");

            packetColor = node.smpNode.getPacketColor();
            if (packetColor != null) {
                tooltip.setText("INCOMING! EXTRA! EXTRA!");
                node.controller.nodeButton.setStyle("-fx-background-color: #" + packetColor.toString().substring(2, 8));
            } else {
                tooltip.setText("Not now, delicious friend");
                node.controller.nodeButton.setStyle(null);
                node.controller.nodeButton.setBlendMode(null);
            }

            node.controller.setTooltip(tooltip);
        }
        runner.wakeUp();




        /*for (HashMap<Integer, Line> lines : vertices.values()) {
            for (Line line : lines.values()) {
                line.setStroke(new Color(0, 0, 0, 1));
                line.setStrokeWidth(1);
            }
        }

        PathEntry pathEntries[] = {
                new PathEntry(0, 1),
                new PathEntry(1, 3),
                new PathEntry(3, 7),
                new PathEntry(7, 15),
                new PathEntry(15, 11),
        };

        for (PathEntry entry : pathEntries) {
            Line vertex = vertices.get(entry.sourceId).get(entry.targetId);
            vertex.setStroke(new Color(0, 0, 1, 1));
            vertex.setStrokeWidth(3);
        }*/
    }


    class AnimatedStepByStep extends Thread {
        @Override
        public void run() {

            try {
                while(true) {
                    Platform.runLater(() -> {
                        nextStep(new ActionEvent());
                    });
                    sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

