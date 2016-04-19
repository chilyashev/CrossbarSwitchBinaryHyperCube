package org.cbsbh.ui.screens;

import org.cbsbh.model.routing.OutputChannel;
import org.cbsbh.model.routing.SMPNode;

import java.util.ArrayList;

/**
 * Created by Mihail Chilyashev on 4/19/16.
 * All rights reserved, unless otherwise noted.
 */
public class GraphNode {
    int x;
    int y;
    SMPNode smpNode;

    public GraphNode(int x, int y, SMPNode smpNode) {
        this.x = x;
        this.y = y;
        this.smpNode = smpNode;
    }

    public ArrayList<Integer> getNeighbors() {
        ArrayList<Integer> ret = new ArrayList<>();

        for (OutputChannel ouc : smpNode.getOutputChannels().values()) {
            ret.add(ouc.getId());
        }

        return ret;
    }
}
