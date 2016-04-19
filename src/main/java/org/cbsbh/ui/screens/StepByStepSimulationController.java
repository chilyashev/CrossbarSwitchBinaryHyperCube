package org.cbsbh.ui.screens;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

import java.util.HashMap;
import java.util.Random;


/**
 * Description goes here
 * Date: 4/20/14 3:14 AM
 *
 * @author Mihail Chilyashev
 */
public class StepByStepSimulationController extends AbstractScreen {

    private Context context;

    private ModelRunner runner;

    private HashMap<Integer, GraphNode> graphNodes = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, Line>> vertices;
    private Group graphGroup;
    private Group nodeGroup;

    //private Thread runnerThread;


    // FXML controls
    @FXML
    public AnchorPane mainPane;
    @FXML
    public Label statusLabel;
    // eo FXML controls


    public StepByStepSimulationController() {
        context = Context.getInstance();
        vertices = new HashMap<>();
        graphGroup = new Group();
        nodeGroup = new Group();
        runner = null;
        //runnerThread = null;
        //...
    }

    @Override
    public void init() {
        setTitle("Изпълнение стъпка по стъпка");

        Random r = new Random(); // Real science done RIGHT HERE!

        runner = new ModelRunner(context, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.err.println("Well...");
            }
        });


        Context.getInstance().set("channelCount", 4);
        Context.getInstance().set("nodeCount", 16);
        Context.getInstance().set("maxMessageSize", 3);
        Context.getInstance().set("bufferCountPerInputChannel", 3); // TODO!
        Context.getInstance().set("messageGenerationFrequency", 1); // TODO
        Context.getInstance().set("minMessageSizel", 4); // TODO
        Context.getInstance().set("fifoQueueCount", 3); // TODO

        runner.init(Context.getInstance().getInteger("channelCount"));
        runner.start();


        int[] leftCube = {6, 7, 2, 3, 4, 5, 0, 1};
        int[] rightCube = {14, 15, 10, 11, 12, 13, 8, 9};

        drawNodeCube(leftCube, 0, 180);
        drawNodeCube(rightCube, 360, 0);
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

            try {
                loader = new FXMLLoader(getClass().getResource("/screens/graph_vis/node.fxml"));
                control = loader.load();
                assert control != null;
                controller = loader.getController();
                controller.setText("Node " + Integer.toBinaryString(nodeId));
                controller.setLocation(x, y);
                controller.setTooltip(new Tooltip("Възел еди-кой си, в състояние 90001"));

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
                graphGroup.getChildren().add(vertex);
            }
        }
    }

    private void updateVerticesOnEnter(int nodeId) {
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
    }

    public void nextStep(ActionEvent actionEvent) {

        //runner.notify();
        runner.wakeUp();
        statusLabel.setText("Такт: " + context.getString("currentModelTick"));

        for (GraphNode node : graphNodes.values()) {
            node.controller.setTooltip(new Tooltip(node.smpNode.toString()));
        }



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

}
