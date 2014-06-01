package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.structures.FIFOBuff;

/**
 * Description goes here
 * Date: 6/1/14 7:35 PM
 *
 * @author Mihail Chilyashev
 */
public class FIFOArbiter implements Tickable {
    private FIFOBuff<Long> fifoBuff;

    public FIFOArbiter() {

    }

    public void tick(){
        // TODO: figure out how to figure out if you've just popped a head
        // Arbitrary logic
    }
}
