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
    private ArrayList<FIFOArbiter> arbiters;

    @Override
    public void tick() {

    }
}
