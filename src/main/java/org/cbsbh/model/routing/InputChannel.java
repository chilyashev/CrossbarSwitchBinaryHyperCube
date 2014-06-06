package org.cbsbh.model.routing;

import org.cbsbh.context.Context;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.structures.FIFOBuff;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description goes here
 * Date: 6/1/14 7:34 PM
 *
 * @author Mihail Chilyashev
 */
public class InputChannel implements Tickable {
    private String id;
    private HashMap<String, FIFOArbiter> arbiters;
//    private HashMap<Integer, OutputChannel> outputChannels;

    public InputChannel(int id, int routerId, HashMap<Integer, OutputChannel> outputChannels) {
        this.id = id + "_" + routerId;
        arbiters = new HashMap<>(); // идва от интерфейса
        for (int i = 0; i < Context.getInstance().getInteger("bufferCountPerInputChannel"); i++) {
            FIFOArbiter tmp = new FIFOArbiter(this.id + "_" + i, outputChannels);
            arbiters.put(tmp.getArbiterId(), tmp);
        }
//        this.outputChannels = outputChannels;
    }

    public InputChannel(int routerId, int bufferCount) {
        this.routerId = routerId;
        arbiters = new ArrayList<>(bufferCount);
        for (FIFOArbiter arbiter : arbiters) {
            arbiter.setRouterId(routerId);
        }

    }

    public boolean pushFlit(long data) {
        for (FIFOArbiter arbiter : arbiters) {
            FIFOBuff<Long> fifoBuff = arbiter.getFifoBuff();
            if (fifoBuff.getItemCount() < 1) {
                fifoBuff.push(data);
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        for (FIFOArbiter arbiter : arbiters) {
            arbiter.tick();
        }
    }


    public FIFOArbiter getArbiter(String id) {
        return arbiters.get(id);
    }
}
