package org.cbsbh.ui.screens;

import javafx.scene.paint.Color;

/**
 * Created by Mihail Chilyashev on 4/19/16.
 * All rights reserved, unless otherwise noted.
 */

public class PathEntry {
    int sourceId;
    int targetId;
    Color color;

    public PathEntry(int sourceId, int targetId, Color color) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.color = color;
    }
}
