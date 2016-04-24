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
    private HashMap<String, PacketDescription> packets; // Key: packetId, Value: PacketController
    private Group graphGroup;
    private Group nodeGroup;

    private NodeDetailsController detailsController;
    private AnchorPane detailsControl;

    private Color packetColor;

    // Animation stuff
    private boolean animating = false;
    private boolean simulationFinished = false;
    private AnimatedStepByStep animationThread;

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

        //...
    }

    @Override
    public void init() {
        setTitle("Изпълнение стъпка по стъпка");

        vertices = new HashMap<>();
        packets = new HashMap<>();
        graphGroup = new Group();
        nodeGroup = new Group();
        graphNodes = new HashMap<>();
        runner = null;
        detailsController = null;
        packetColor = null;
        lastPacketButtonY = 0;

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
        setUpModelRunner();

        // Animation
        animationThread = new AnimatedStepByStep();
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

    private void setUpModelRunner() {
        runner = new ModelRunner(context, event -> {
            finishSimulation();
        });

        Context.getInstance().set("channelCount", 4);
        Context.getInstance().set("nodeCount", 16);
        Context.getInstance().set("maxMessageSize", 3);
        Context.getInstance().set("bufferCountPerInputChannel", 3); // TODO!
        Context.getInstance().set("messageGenerationFrequency", 1); // TODO
        Context.getInstance().set("minMessageSizel", 4); // TODO
        Context.getInstance().set("fifoQueueCount", 3); // TODO

        runner.setStepByStepExecution(true);
        runner.init(Context.getInstance().getInteger("channelCount"));
        runner.start();
    }

    private void finishSimulation() {
        Platform.runLater(() -> {
            animating = false;
            simulationFinished = true;
            statusLabel.setText("Симулацията завърши.");
            pauseUnpauseButton.setText("Нова симулация");
        });
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
            // System.err.printf("Inserting node %d\n", nodeId);
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
        updateNodes();
        //detailsControl.setVisible(false);
    }

    public void nextStep(ActionEvent actionEvent) {
        if (animating) {
            pauseAnimation();
        }
        updateGraph();
    }

    public Button getPacketButton(/*TODO*/) {
        Button b = new Button("Creap");
        b.setLayoutY(lastPacketButtonY);
        lastPacketButtonY += 50;
        return b;
    }

    private void updateGraph() {
        runner.wakeUp();

        statusLabel.setText("Такт: " + context.getString("currentModelTick"));

        updateNodeDetails(detailsController.getNodeId());

        updateNodes();
    }

    private synchronized void updateNodes() {
        for (GraphNode node : graphNodes.values()) {
            Tooltip tooltip = new Tooltip();

            Flit currentFlit = node.smpNode.getCurrentFlit();

            if (currentFlit != null) {
                if (currentFlit.isHeader()) {
                    node.currentTargetId = (int) currentFlit.getDNA();
                    addNewPacketButton(currentFlit);
                }else if (currentFlit.getFlitType() == Flit.FLIT_TYPE_BODY) {
                    updatePacketButton(currentFlit);
                }
                else if (currentFlit.getFlitType() == Flit.FLIT_TYPE_TAIL) {
                    updatePacketButton(currentFlit);
                    node.currentTargetId = -1;
                    PacketDescription packetDescription = packets.get(currentFlit.getPacketId());
                    if (currentFlit.getTargetId() == node.smpNode.getId()) {
                        packetDescription.setReceived(true);
                    }
                    for (Flit flit : packets.get(currentFlit.getPacketId()).flits.values()) {
                        System.err.println("Flit: " + flit);
                    }
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
            }

            node.controller.setTooltip(tooltip);
        }
    }

    private void updatePacketButton(Flit currentFlit) {
        if (packets.containsKey(currentFlit.getPacketId())) {
            packets.get(currentFlit.getPacketId()).flits.put(currentFlit.id, currentFlit); // TODO: smartify this shit
        }
    }

    private void addNewPacketButton(Flit currentFlit) {
        FXMLLoader packetButtonLoader;
        AnchorPane packetPane;
        PacketController packetController;
        Tooltip tooltip;

        if (packets.containsKey(currentFlit.getPacketId())) {
            packets.get(currentFlit.getPacketId()).flits.put(currentFlit.id, currentFlit); // TODO: smartify this shit
            return;
        }

        try {
            packetButtonLoader = new FXMLLoader(getClass().getResource("/screens/graph_vis/packet_vis.fxml"));

            packetPane = packetButtonLoader.load();

            assert packetPane != null;
            packetController = packetButtonLoader.getController();

            packetController.setOnEnterHandler(event -> drawPath(currentFlit.getPacketId()));
            packetController.setOnExitHandler(event -> updateVerticesOnExit());

            if (currentFlit.isHeader()) {
                tooltip = new Tooltip(String.format("Пакет, изпратен от %s до %s",
                        Util.binaryFormattedNodeID(currentFlit.getSourceId()),
                        Util.binaryFormattedNodeID((int) currentFlit.getDNA())));
                packetController.button.setTooltip(tooltip);
            }

            packetController.button.setStyle("-fx-background-color: #" + currentFlit.packetColor.toString().substring(2, 8));
            packets.put(currentFlit.getPacketId(),
                    new PacketDescription(packetController, currentFlit.getSourceId(), (int) currentFlit.getDNA()));

            packetVbox.getChildren().add(packetPane);
            packetController.button.setText("" + packets.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawPath(String packetId) {
        pauseAnimation(); // Ако не се спре анимацията, възлите си сменят цветовете. Не ме кефи само дъгите да са шарени, затова се спира.

        PacketDescription desc = packets.get(packetId);

        if (desc == null) {
            return;
        }

        for (GraphNode node : graphNodes.values()) {
            node.controller.resetColor();
        }

        for (HashMap<Integer, Line> lines : vertices.values()) {
            for (Line line : lines.values()) {
                line.setStroke(new Color(0, 0, 0, .2));
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

        for (PathEntry entry : pathEntries) {
            // Това не е възможно в момента. Няма и смисъл да е възможно. Поне засега.
            if (entry.sourceId == entry.targetId) {
                continue;
            }
            Line vertex = vertices.get(entry.sourceId).get(entry.targetId);
            vertex.setStroke(entry.color);
            vertex.setStrokeWidth(3);
        }

        // Details
        detailsController.setText(String.format(
                "Пакет: \n" +
                        "\tID: %s\n" +
                        "\tОт възел: %s\n" +
                        "\tКъм възел: %s\n" +
                        "\tБрой предадени флитове към момента: %d\n" +
                        "\tНапълно предаден: %s\n" +
                        "\tФлитове:\n" +
                        "\t\t%s",
                packetId,
                Util.binaryFormattedNodeID(desc.getSourceId()),
                Util.binaryFormattedNodeID(desc.getTargetId()),
                desc.flits.size(),
                desc.isReceived() ? "Да" : "Не",
                desc.flits.toString()
        ));
    }

    void pauseAnimation() {
        pauseUnpauseButton.setText("Продължи симулацията");
        animating = false;
    }

    void resumeAnimation() {
        pauseUnpauseButton.setText("Пауза");
        animating = true;
    }

    private void resetSimulation() {
        Platform.runLater(() -> {
            try {
                runner.join();
                animationThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace(); // Well, let me tell you a story about handling exceptions...
            }

            packets.clear();
            packetVbox.getChildren().clear();
            simulationFinished = false;
            pauseUnpauseButton.setText("Започни симулацията");
            statusLabel.setText("Симулацията не е стартирана.");

            init(); // Quick and dirty. Just the way I like it.
        });
    }

    public void toggleAnimation(ActionEvent actionEvent) {
        if (simulationFinished) {
            resetSimulation();
            return;
        }
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
                while (!simulationFinished) {
                    if (animating) {
                        Platform.runLater(StepByStepSimulationController.this::updateGraph);
                    }
                    sleep(Constants.ANIMATION_THREAD_SLEEP_TIME); // Shhh... no more loops, just dreams...
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

