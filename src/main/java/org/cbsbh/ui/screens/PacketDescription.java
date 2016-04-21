package org.cbsbh.ui.screens;

import org.cbsbh.model.structures.Flit;
import org.cbsbh.ui.screens.graph_visualisation.PacketController;

import java.util.HashMap;

/**
 * Created by Mihail Chilyashev on 4/21/16.
 * All rights reserved, unless otherwise noted.
 */
public class PacketDescription {
    private int sourceId;
    private int targetId;
    private int messageId; // TODO!
    private boolean received;
    PacketController controller;
    HashMap<String, Flit> flits; // FlitID => Flit

    public PacketDescription(PacketController controller, int sourceId, int targetId) {
        this.controller = controller;
        this.sourceId = sourceId;
        this.targetId = targetId;
        flits = new HashMap<>();
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public boolean isReceived() {
        return received;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getTargetId() {
        return targetId;
    }
}
