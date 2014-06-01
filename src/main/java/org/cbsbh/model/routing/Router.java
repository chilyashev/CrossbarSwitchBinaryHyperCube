package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;

import java.util.ArrayList;

/**
 * An abstract class for the different routing algorithms
 * Date: 5/3/14 4:43 PM
 *
 * @author Mihail Chilyashev
 */
abstract public class Router implements Tickable{

    private ArrayList<InputChannel> demuxes;

    protected Router(int demuxCount) {
        demuxes = new ArrayList<>(demuxCount);
    }

    /**
     * All the logic goes in here.
     */
    abstract public void tick();

}
