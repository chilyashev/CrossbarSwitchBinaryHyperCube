package org.cbsbh.model.routing;

import java.util.Collection;
import java.util.HashMap;

/**
 * Description goes here
 * Date: 6/1/14 8:23 PM
 *
 * @author Mihail Chilyashev
 */
public class MPPNetwork {
    private final static MPPNetwork instance = new MPPNetwork();
    // ID => SMPNode
    private static HashMap<Integer, SMPNode> SMPNodes = new HashMap<>();

    public static MPPNetwork getInstance() {
        return instance;
    }

    /**
     * Be gentle
     * @param smpNode the thing to be pushed.
     */
    public static void push(SMPNode smpNode) {
        SMPNodes.put(smpNode.getId(), smpNode);
    }

    public static SMPNode get(int SMPNodeId) {
        return SMPNodes.get(SMPNodeId);
    }

    public static Collection<SMPNode> getAll() {
        return SMPNodes.values();
    }

    public void tick() {
        SMPNodes.values().forEach(org.cbsbh.model.routing.SMPNode::tick);
    }
}
