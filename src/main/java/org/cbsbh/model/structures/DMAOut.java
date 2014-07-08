package org.cbsbh.model.structures;

import org.cbsbh.model.routing.packet.Packet;

/**
 * Description goes here
 * Date: 7/8/14 9:44 PM
 *
 * @author Mihail Chilyashev
 */
public class DMAOut {
    int[] adjacentNodes;

    public DMAOut(int[] adjacentNodes) {
        this.adjacentNodes = adjacentNodes;
    }

    public void sendPacket(Packet packet){
        for (int adjacentNode : adjacentNodes) {
            if(adjacentNode == packet.getTR()){

            }
        }

    }
}
