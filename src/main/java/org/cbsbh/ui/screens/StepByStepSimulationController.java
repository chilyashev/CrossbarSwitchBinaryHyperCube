package org.cbsbh.ui.screens;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.cbsbh.Constants;
import org.cbsbh.context.Context;
import org.cbsbh.model.ModelRunner;
import org.cbsbh.model.routing.MPPNetwork;
import org.cbsbh.model.routing.SMPNode;
import org.cbsbh.model.structures.Flit;
import org.cbsbh.model.structures.FlitHistoryEntry;
import org.cbsbh.ui.AbstractScreen;
import org.cbsbh.ui.screens.graph_visualisation.NodeController;
import org.cbsbh.ui.screens.graph_visualisation.NodeDetailsController;
import org.cbsbh.ui.screens.graph_visualisation.PacketController;
import org.cbsbh.util.Util;

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
    private HashMap<Integer, PacketDescription> packets; // Key: packetId, Value: PacketController
    private Group graphGroup;
    private Group nodeGroup;

    private NodeDetailsController detailsController;
    private AnchorPane detailsControl;

    private Color packetColor;

    private boolean animating = false;

    private int lastPacketButtonY;

    // FXML controls
    @FXML
    public AnchorPane mainPane;
    @FXML
    public Label statusLabel;
    @FXML
    public AnchorPane rightPane;
    @FXML
    public Button pauseUnpauseButton;
    @FXML
    public VBox packetVbox;
    // eo FXML controls


    public StepByStepSimulationController() {
        context = Context.getInstance();
        vertices = new HashMap<>();
        packets = new HashMap<>();
        graphGroup = new Group();
        nodeGroup = new Group();
        graphNodes = new HashMap<>();
        currentPathInGraph = new ArrayList<>();
        runner = null;
        detailsController = null;
        packetColor = null;
        lastPacketButtonY = 0;
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

        // Animation
        AnimatedStepByStep animationThread = new AnimatedStepByStep();
        animationThread.start();


        int[] leftCube = {6, 7, 2, 3, 4, 5, 0, 1};
        int[] rightCube = {14, 15, 10, 11, 12, 13, 8, 9};

        drawNodeCube(leftCube, 0, 180);
        drawNodeCube(rightCube, 360, 0);

        // Дъгите се добавят тук, защото иначе не работи. Дъгите на първия куб не реагират на hover-а върху някой възел
        for (HashMap<Integer, Line> lines : vertices.values()) {
            graphGroup.getChildren().addAll(lines.values());
        }

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
            String buttonText = Util.binaryFormattedNodeID(nodeId);
            try {
                loader = new FXMLLoader(getClass().getResource("/screens/graph_vis/node.fxml"));
                control = loader.load();
                assert control != null;
                controller = loader.getController();
                controller.setText(buttonText);
                controller.setLocation(x, y);
                controller.setTooltip(new Tooltip("Все още няма информация за този възел."));

                controller.setOnEnterHandler(event -> updateVerticesOnEnter(nodeId));
                controller.setOnExitHandler(event -> updateVerticesOnExit());

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
        updateNodeDetails(nodeId);


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

    private void updateNodeDetails(int nodeId) {
        detailsControl.setVisible(true);
        detailsController.setText(graphNodes.get(nodeId).smpNode.toString());
        detailsController.setNodeId(nodeId);
    }

    private void updateVerticesOnExit() {
        for (HashMap<Integer, Line> lines : vertices.values()) {
            for (Line line : lines.values()) {
                line.setStroke(new Color(0, 0, 0, 1));
                line.setStrokeWidth(1);
            }
        }

        //detailsControl.setVisible(false);
    }

    public void nextStep(ActionEvent actionEvent) {
        pauseAnimation();
        updateGraph();
    }

    public Button getPacketButton(/*TODO*/){
        Button b = new Button("Creap");
        b.setLayoutY(lastPacketButtonY);
        lastPacketButtonY += 50;
        return b;
    }

    private void updateGraph() {
        runner.wakeUp();

        statusLabel.setText("Такт: " + context.getString("currentModelTick"));

        updateNodeDetails(detailsController.getNodeId());


        for (GraphNode node : graphNodes.values()) {
            Tooltip tooltip = new Tooltip();

            Flit currentFlit = node.smpNode.getCurrentFlit();
            if (currentFlit != null) {
                if (currentFlit.getFlitType() == Flit.FLIT_TYPE_HEADER) {
                    node.currentTargetId = (int) currentFlit.getDNA();
                    addNewPacketButton(currentFlit);
                } else if (currentFlit.getFlitType() == Flit.FLIT_TYPE_TAIL) {
                    node.currentTargetId = -1;
                }
                packetColor = currentFlit.packetColor;
            } else {
                packetColor = null;
            }
            tooltip.setText(String.format("Изпратени флитове: %d, Получени флитове: %d%s",
                            node.smpNode.sentFlits.size(),
                            node.smpNode.receivedFlits.size(),
                            node.currentTargetId != -1 ?
                                    String.format(", Съдържа флит с дестинация: %s", Util.binaryFormattedNodeID(node.currentTargetId)) : ""
                    )
            );

            if (packetColor != null) {
                node.controller.nodeButton.setStyle("-fx-background-color: #" + packetColor.toString().substring(2, 8));
            } else {
                //tooltip.setText("Not now, delicious friend");
                node.controller.nodeButton.setStyle(null);
                node.controller.nodeButton.setBlendMode(null);
            }

            node.controller.setTooltip(tooltip);
        }
    }

    private void addNewPacketButton(Flit currentFlit) {
        FXMLLoader packetButtonLoader;
        AnchorPane packetPane;
        PacketController packetController;

        if (packets.containsKey(currentFlit.getPacketId())){
            packets.get(currentFlit.getPacketId()).flits.put(currentFlit.id, currentFlit); // TODO: smartify this shit
            return;
        }

        packetButtonLoader = new FXMLLoader(getClass().getResource("/screens/graph_vis/packet_vis.fxml"));
        try {
            packetPane = packetButtonLoader.load();
            assert packetPane != null;
            packetController = packetButtonLoader.getController();

            packetController.setOnEnterHandler(event -> drawPath(currentFlit.getPacketId()));
            packetController.setOnExitHandler(event -> updateVerticesOnExit());

            packetController.button.setText("" + currentFlit.getPacketId());
            packetController.button.setStyle("-fx-background-color: #" + currentFlit.packetColor.toString().substring(2, 8));
            packets.put(currentFlit.getPacketId(), new PacketDescription(packetController));
            packets.get(currentFlit.getPacketId()).flits.put(currentFlit.id, currentFlit);
            packetVbox.getChildren().add(packetPane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawPath(int packetId) {

        PacketDescription desc = packets.get(packetId);

        if (desc == null) {
            return;
        }

        for (HashMap<Integer, Line> lines : vertices.values()) {
            for (Line line : lines.values()) {
                line.setStroke(new Color(0, 0, 0, 1));
                line.setStrokeWidth(1);
            }
        }

        ArrayList<PathEntry> pathEntries = new ArrayList<>();
        for (Flit flit : desc.flits.values()) {
            for (FlitHistoryEntry historyEntry : flit.pathHistory) {
                graphNodes.get(historyEntry.getSourceNodeId()).controller.nodeButton.setStyle("-fx-background-color: #" + flit.packetColor.toString().substring(2, 8));
                pathEntries.add(
                        new PathEntry(historyEntry.getSourceNodeId(), historyEntry.getTargetNodeId(), flit.packetColor)
                );
            }

        }


        /*PathEntry pathEntries[] = {
                new PathEntry(0, 1),
                new PathEntry(1, 3),
                new PathEntry(3, 7),
                new PathEntry(7, 15),
                new PathEntry(15, 11),
        };*/

        for (PathEntry entry : pathEntries) {
            Line vertex = vertices.get(entry.sourceId).get(entry.targetId);
            vertex.setStroke(entry.color);
            vertex.setStrokeWidth(3);
        }
    }


    void pauseAnimation() {
        pauseUnpauseButton.setText("Продължи симулацията");
        animating = false;
    }

    void resumeAnimation() {
        pauseUnpauseButton.setText("Пауза");
        animating = true;
    }

    public void toggleAnimation(ActionEvent actionEvent) {
        if (!animating) {
            resumeAnimation();
        } else {
            pauseAnimation();
        }
    }

    class AnimatedStepByStep extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    System.err.println("animu: " + animating);
                    if (animating) {
                        Platform.runLater(StepByStepSimulationController.this::updateGraph);
                    }
                    sleep(Constants.ANIMATION_THREAD_SLEEP_TIME);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

