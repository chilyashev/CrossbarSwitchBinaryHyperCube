package org.cbsbh.ui.screens;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cbsbh.Constants;
import org.cbsbh.context.Context;
import org.cbsbh.model.ModelRunner;
import org.cbsbh.model.routing.MPPNetwork;
import org.cbsbh.model.routing.OutputChannel;
import org.cbsbh.model.routing.SMPNode;
import org.cbsbh.model.structures.Flit;
import org.cbsbh.model.structures.FlitHistoryEntry;
import org.cbsbh.ui.AbstractScreen;
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

    private static final double NODE_ELLIPSE_RADIUS = 20;
    private static final Font NODE_TEXT_FONT = new Font(11);

    private Context context;

    private ModelRunner runner;

    private HashMap<Integer, GraphNode> graphNodes;
    private HashMap<Integer, HashMap<Integer, Group>> outputChannelVertices;
    //private HashMap<Integer, HashMap<Integer, Group>> inputChannelVertices;
    private HashMap<String, PacketDescription> packets; // Key: packetId, Value: PacketController

    private NodeDetailsController detailsController;
    private AnchorPane detailsControl;
    private Group nodesControl;

    private Color packetColor;

    // Animation stuff
    private boolean animating = false;
    private boolean simulationFinished = false;
    private AnimatedStepByStep animationThread;

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
    @FXML
    public AnchorPane nodesPane;
    // eo FXML controls


    public StepByStepSimulationController() {
        context = Context.getInstance();

        //...
    }

    @Override
    public void init() {
        setTitle("Изпълнение стъпка по стъпка");
        AnchorPane.setTopAnchor(mainPane, 0.0);
        AnchorPane.setRightAnchor(mainPane, 0.0);
        AnchorPane.setBottomAnchor(mainPane, 0.0);
        AnchorPane.setLeftAnchor(mainPane, 0.0);

        outputChannelVertices = new HashMap<>();
        // inputChannelVertices = new HashMap<>();
        packets = new HashMap<>();
        graphNodes = new HashMap<>();
        runner = null;
        detailsController = null;
        packetColor = null;

        // Window handling stuff
        // When the window is closed, the remaining threads should be killed, so the application can exit gracefully.
        parent.setOnCloseHandler((event) -> {
            // Set the simulation as finished and wait for the animation thread to die.
            animating = true;
            simulationFinished = true;
            try {
                animationThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Force the model runner to stop.
            runner.wakeUp();
            runner.stopModel();
        });

        // Model stuff
        // Do this here, because some stuff below uses the MPPNetwork class which gets populated while initializing the model
        setUpModelRunner();

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

        FXMLLoader nodesLoader;
        nodesLoader = new FXMLLoader(getClass().getResource("/screens/nodes.fxml"));

        try {
            nodesControl = nodesLoader.load();
            assert nodesControl != null;
            nodesControl.setLayoutX(0);
            nodesControl.setLayoutY(0);

            // Възлите в SVG-то имат ID-та, които са числовата стойност на ID-тата на възлите от мрежата.
            // Пример: #node6 за SMP възел с id 0110.
            // Каналите имат ID-та от вида #channel_{from}_{to},
            // където {from} е числовата стойност на ID-то на възела, за който каналът е изходящ, а {to} - накъде сочи.
            for (SMPNode node : MPPNetwork.getAll()) {
                final Ellipse nodeNode = (Ellipse) nodesControl.lookup("#node" + node.getId());
                Node lookup;
                Text text;

                for (OutputChannel oc : node.getOutputChannels().values()) {
                    lookup = nodesControl.lookup(String.format("#channel_%d_%d", node.getId(), oc.getId()));

                    if (lookup == null) {
                        continue;
                    }

                    outputChannelVertices.putIfAbsent(node.getId(), new HashMap<>());
                    //inputChannelVertices.putIfAbsent(node.getId(), new HashMap<>());

                    outputChannelVertices.get(node.getId()).put(oc.getId(), (Group) lookup);
                    //inputChannelVertices.get(oc.getId()).put(node.getId(), (Group) lookup);
                }

                if (nodeNode != null) {
                    text = new Text(Util.binaryFormattedNodeID(node.getId()));
                    text.setMouseTransparent(true); // Without this hovering the text doesn't call the Node Ellipse's onHover method.
                    text.setFill(Color.BLACK);
                    text.setStroke(Color.WHITE);
                    text.setFont(NODE_TEXT_FONT);

                    text.setX(nodeNode.getCenterX() - NODE_ELLIPSE_RADIUS / 2 - 1.5);
                    text.setY(nodeNode.getCenterY() + NODE_ELLIPSE_RADIUS / 3 - 2.5);


                    ObservableList<Node> children = ((Group) nodeNode.getParent()).getChildren();
                    children.add(text);


                    graphNodes.put(node.getId(), new GraphNode(node, nodeNode));
                    nodeNode.hoverProperty().addListener((observable, wasHovered, isHovered) -> {
                        // Ходим през изходните канали на възела, за да видим накъде сочат.
                        SVGPath channelPath;
                        Group channelArrow;
                        Node channelLookup;

                        // DetailsPanel stuff
                        updateNodeDetails(node.getId());

                        // Channel stuff
                        for (OutputChannel oc : node.getOutputChannels().values()) {
                            channelLookup = nodesControl.lookup(String.format("#channel_%d_%d", node.getId(), oc.getId()));
                            if (channelLookup == null) {
                                continue;
                            }
                            if (channelLookup instanceof SVGPath) {
                                channelPath = (SVGPath)
                                        channelLookup;
                                if (isHovered) {
                                    channelPath.strokeProperty().setValue(Color.RED);
                                    channelPath.fillProperty().setValue(Color.RED);
                                } else {
                                    channelPath.strokeProperty().setValue(Color.BLACK);
                                    channelPath.fillProperty().setValue(Color.BLACK);
                                }
                            } else {
                                channelArrow = (Group) channelLookup;
                                if (isHovered) {
                                    // It's a shame...
                                    ((SVGPath) channelArrow.getChildren().get(0)).strokeProperty().setValue(Color.RED);
                                    ((SVGPath) channelArrow.getChildren().get(1)).strokeProperty().setValue(Color.RED);

                                    ((SVGPath) channelArrow.getChildren().get(1)).fillProperty().setValue(Color.RED);
                                } else {
                                    ((SVGPath) channelArrow.getChildren().get(0)).strokeProperty().setValue(Color.BLACK);
                                    ((SVGPath) channelArrow.getChildren().get(1)).strokeProperty().setValue(Color.BLACK);

                                    ((SVGPath) channelArrow.getChildren().get(1)).fillProperty().setValue(Color.BLACK);
                                }
                            }
                        }
                    });
                } else {
                    System.err.printf("#node%d not found :(\n", node.getId());
                }
            }
            nodesPane.getChildren().setAll(nodesControl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Animation
        animationThread = new AnimatedStepByStep();
        animationThread.start();
    }

    private void setUpModelRunner() {
        runner = new ModelRunner(context, event -> finishSimulation());

        Context.getInstance().set("channelCount", 4);
        Context.getInstance().set("nodeCount", 16);
        Context.getInstance().set("maxMessageSize", 3);
        Context.getInstance().set("bufferCountPerInputChannel", 3); // TODO!
        Context.getInstance().set("messageGenerationFrequency", 1); // TODO
        Context.getInstance().set("minMessageSizel", 4); // TODO
        Context.getInstance().set("fifoQueueCount", 3); // TODO
        Context.getInstance().set("messageCount", 13);
        Context.getInstance().set("maxTickForGeneratingMessages", 30);
        Context.getInstance().set("workingTime", 200);
        Context.getInstance().set("maxMessageSize", 2);
        Context.getInstance().set("maxPacketSize", 3);
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

    private void updateNodeDetails(int nodeId) {
        detailsControl.setVisible(true);
        detailsController.setText(graphNodes.get(nodeId).smpNode.toString());
        detailsController.setNodeId(nodeId);
    }

    public void nextStep() {
        if (animating) {
            pauseAnimation();
        }
        updateGraph();
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
                } else if (currentFlit.getFlitType() == Flit.FLIT_TYPE_BODY) {
                    updatePacketButton(currentFlit);
                } else if (currentFlit.getFlitType() == Flit.FLIT_TYPE_TAIL) {
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
                node.nodeButton.setStyle("-fx-background-color: #" + packetColor.toString().substring(2, 8));
            } else {
                tooltip.setText("Not now, delicious friend");
                node.nodeButton.setStyle(null);
            }

            //node.nodeButton.setTooltip(tooltip);
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

    private void updateVerticesOnExit() {
        resetColors(Color.BLACK);
    }

    private void resetColors(Color arrowColor) {
        graphNodes.values().forEach(GraphNode::resetColor);
        for (HashMap<Integer, Group> lines : outputChannelVertices.values()) {
            for (Group line : lines.values()) {
                SVGPath arrow = (SVGPath) line.getChildren().get(0);
                arrow.setStroke(arrowColor);
                arrow.setStrokeWidth(2);
                SVGPath arrowHead = (SVGPath) line.getChildren().get(1);
                arrowHead.setStroke(arrowColor);
                arrowHead.setFill(arrowColor);
                arrowHead.setStrokeWidth(2);
            }
        }
    }

    /**
     * Рисува пътя за даден пакет.
     *
     * @param packetId ID на пакета. In the grand scheme of things.
     */
    private void drawPath(String packetId) {
        pauseAnimation(); // Ако не се спре анимацията, възлите си сменят цветовете. Не ме кефи само дъгите да са шарени, затова се спира.

        PacketDescription desc = packets.get(packetId);

        if (desc == null) {
            return;
        }

        // Reset node and channel colors
        // Make channel arrows grey
        // resetColors(Color.GREY);
        resetColors(Color.BLACK); // Temporarily using black because of the dumb flicker when moving between packets. TODO: Fix it.

        ArrayList<PathEntry> pathEntries = new ArrayList<>();
        for (Flit flit : desc.flits.values()) {
            for (FlitHistoryEntry historyEntry : flit.pathHistory) {
                graphNodes.get(historyEntry.getSourceNodeId()).nodeButton.setFill(Color.RED);//.setStyle("-fx-background-color: #" + flit.packetColor.toString().substring(2, 8));
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
            // TODO: !!!!!!
            Group arrowGroup = outputChannelVertices.get(entry.sourceId).get(entry.targetId);
            SVGPath vertex = (SVGPath) arrowGroup.getChildren().get(0);
            SVGPath arrowHead = (SVGPath) arrowGroup.getChildren().get(1);
            vertex.setStroke(entry.color);
            vertex.setStrokeWidth(3);

            arrowHead.setStroke(entry.color);
            arrowHead.setFill(entry.color);
            arrowHead.setStrokeWidth(3);
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

    private void pauseAnimation() {
        pauseUnpauseButton.setText("Продължи симулацията");
        animating = false;
    }

    private void resumeAnimation() {
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

    public void toggleAnimation() {
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

    private class AnimatedStepByStep extends Thread {
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

