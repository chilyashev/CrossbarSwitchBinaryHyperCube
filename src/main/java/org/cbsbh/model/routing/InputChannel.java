package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.structures.FIFOBuff;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 6/1/14 7:34 PM
 *
 * @author Mihail Chilyashev
 */
public class InputChannel implements Tickable {
    private int routerId;
    private int bufferCount;
    private ArrayList<FIFOArbiter> arbiters;

    public InputChannel(int routerId, int bufferCount) {
        this.routerId = routerId;
        this.bufferCount = bufferCount;
        arbiters = new ArrayList<>(bufferCount);
        for (FIFOArbiter arbiter : arbiters) {
            arbiter.setRouterId(routerId);
        }

    }

    public boolean pushFlit(long data) {
        for (FIFOArbiter arbiter : arbiters) {
            FIFOBuff<Long> fifoBuff = arbiter.getFifoBuff();
            if(fifoBuff.getItemCount() < 1){
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

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }
}
