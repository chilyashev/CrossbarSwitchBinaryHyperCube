package org.cbsbh.model.routing;

import org.cbsbh.context.Context;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.Packet;

import java.util.HashMap;

/**
 * Description goes here
 * Date: 6/1/14 7:34 PM
 *
 * @author Mihail Chilyashev
 */
public class InputChannel implements Tickable {
    private int id;
    private HashMap<Integer, FIFOArbiter> arbiters;
//    private HashMap<Integer, OutputChannel> outputChannels;

    public InputChannel(int id, int nodeId, HashMap<Integer, OutputChannel> outputChannels) {
        this.id = id;
        arbiters = new HashMap<>(); // идва от интерфейса
        for (int i = 0; i < Context.getInstance().getInteger("bufferCountPerInputChannel"); i++) {
            FIFOArbiter tmp = new FIFOArbiter(i, id, nodeId, outputChannels);
            arbiters.put(tmp.getArbiterId(), tmp);
        }
//        this.outputChannels = outputChannels;
    }


    public boolean pushFlit(long data) {
        for (int id : arbiters.keySet()) {
            if (!arbiters.get(id).isBusy()) {
                arbiters.get(id).getFifoBuff().push(data);
                //System.err.println("[InputChannel] Found arbiter: " + id);
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        for (int id : arbiters.keySet()) {
            arbiters.get(id).tick();
        }
    }


    public FIFOArbiter getArbiter(int id) {
        return arbiters.get(id);
    }

    public void setPacket(Packet packet) {
        boolean sent = pushFlit(packet.getHeader_1()) &&
        pushFlit(packet.getMemoryAddress()) &&
        pushFlit(packet.getData_l()) &&
        pushFlit(packet.getData_h());
        assert sent;
    }
}
