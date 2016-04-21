package org.cbsbh.util;

/**
 * Created by Mihail Chilyashev on 4/21/16.
 * All rights reserved, unless otherwise noted.
 */
public class Util {

    /**
     * There is no such thing as padding.
     * @param nodeID the ID of the node. Pretty self-explanatory if you ask me.
     * @return The node ID padded with zeroes.
     */
    public static String binaryFormattedNodeID(int nodeID) {
        String binId = Integer.toBinaryString(nodeID);
        return String.format("%s%s", "0000".substring(0, 4 - binId.length()), binId);
    }
}
