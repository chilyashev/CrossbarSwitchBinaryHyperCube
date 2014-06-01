package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 6/1/14 7:34 PM
 *
 * @author Mihail Chilyashev
 */
public class InputChannel implements Tickable {
    private long id;
    private int bufferCount;
    private ArrayList<FIFOArbiter> arbiters;

    public InputChannel(long id, int bufferCount) {
        this.id = id;
        this.bufferCount = bufferCount;
        arbiters = new ArrayList<>(bufferCount);
        for (FIFOArbiter arbiter : arbiters) {
            arbiter.setChannelId(id);
        }

    }

    @Override
    public void tick() {
        for (FIFOArbiter arbiter : arbiters) {
            arbiter.tick();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
