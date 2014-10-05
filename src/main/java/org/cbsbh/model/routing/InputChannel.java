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


    // TODO: Това не работи правилно, защото се застъпват отделните пакети
    public boolean pushFlit(long data) {
        for (int id : arbiters.keySet()) {
            if(arbiters.get(id).getFifoBuff().getItemCount() == 0 && arbiters.get(id).getFifoBuff().isBusy()){
                arbiters.get(id).getFifoBuff().setBusy(false);
            }
            if (!arbiters.get(id).isBusy()) {
                arbiters.get(id).getFifoBuff().push(data);
                if(arbiters.get(id).getFifoBuff().getItemCount() == arbiters.get(id).getFifoBuff().maxSize){
                    arbiters.get(id).getFifoBuff().setBusy(true);
                }
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

    public HashMap<Integer, FIFOArbiter> getArbiters() {
        return arbiters;
    }

    public boolean setPacket(Packet packet) {
        for (int id : arbiters.keySet()) {
            if(arbiters.get(id).getFifoBuff().getItemCount() == 0 && arbiters.get(id).getFifoBuff().isBusy()){
                arbiters.get(id).getFifoBuff().setBusy(false);
            }

            if (!arbiters.get(id).isBusy() && arbiters.get(id).getFifoBuff().getItemCount() == 0) {
                arbiters.get(id).getFifoBuff().setBusy(true);
                arbiters.get(id).getFifoBuff().push(packet.getHeader_1());
                arbiters.get(id).getFifoBuff().push(packet.getMemoryAddress());
                arbiters.get(id).getFifoBuff().push(packet.getData_l());
                arbiters.get(id).getFifoBuff().push(packet.getData_h());
                return true;
            }
        }

        return false;
        /*boolean sent = pushFlit(packet.getHeader_1()) &&
        pushFlit(packet.getMemoryAddress()) &&
        pushFlit(packet.getData_l()) &&
        pushFlit(packet.getData_h());
        return sent;*/
    }
}
