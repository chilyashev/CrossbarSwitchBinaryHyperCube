package org.cbsbh.model.structures;

/**
 * Created by Mihail Chilyashev on 4/21/16.
 * All rights reserved, unless otherwise noted.
 */
public class FlitHistoryEntry {
    int sourceNodeId;
    int targetNodeId;
    String description;

    public FlitHistoryEntry(int sourceNodeId, int targetNodeId, String description) {
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.description = description;
    }

    public int getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(int sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public int getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(int targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
