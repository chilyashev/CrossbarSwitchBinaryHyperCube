package org.cbsbh.ui.screens;

import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import org.cbsbh.model.routing.OutputChannel;
import org.cbsbh.model.routing.SMPNode;
import org.cbsbh.ui.screens.graph_visualisation.NodeController;

import java.util.ArrayList;

/**
 * Created by Mihail Chilyashev on 4/19/16.
 * All rights reserved, unless otherwise noted.
 */
public class GraphNode {
    int x;
    int y;
    int currentTargetId;
    SMPNode smpNode;
    //NodeController controller;
    Ellipse nodeButton;

    public GraphNode(SMPNode smpNode, Ellipse nodeButton) {
        this.smpNode = smpNode;
        this.nodeButton = nodeButton;
    }

    /*public GraphNode(int x, int y, SMPNode smpNode, NodeController controller) {
        this.x = x;
        this.y = y;
        this.smpNode = smpNode;
        this.controller = controller;
    }*/

    public ArrayList<Integer> getNeighbors() {
        ArrayList<Integer> ret = new ArrayList<>();

        for (OutputChannel ouc : smpNode.getOutputChannels().values()) {
            ret.add(ouc.getId());
        }

        return ret;
    }

    /**
     * Resets the ellipse's color to the default. This should be the same as the one from the SVG
     */
    public void resetColor() {
        nodeButton.setFill(new Color(0.59215686274, 0, 0, 1));
    }
}
