package org.cbsbh.ui.screens;

import org.cbsbh.model.structures.Flit;
import org.cbsbh.ui.screens.graph_visualisation.PacketController;

import java.util.HashMap;

/**
 * Created by Mihail Chilyashev on 4/21/16.
 * All rights reserved, unless otherwise noted.
 */
public class PacketDescription {
    PacketController controller;
    HashMap<String, Flit> flits; // FlitID => Flit

    public PacketDescription(PacketController controller) {
        this.controller = controller;
        flits = new HashMap<>();
    }
}
