package org.cbsbh.ui.screens;

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
    NodeController controller;

    public GraphNode(int x, int y, SMPNode smpNode, NodeController controller) {
        this.x = x;
        this.y = y;
        this.smpNode = smpNode;
        this.controller = controller;
    }

    public ArrayList<Integer> getNeighbors() {
        ArrayList<Integer> ret = new ArrayList<>();

        for (OutputChannel ouc : smpNode.getOutputChannels().values()) {
            ret.add(ouc.getId());
        }

        return ret;
    }
}
