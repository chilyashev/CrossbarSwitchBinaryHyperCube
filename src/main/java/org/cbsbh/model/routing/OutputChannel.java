package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.structures.Channel;

/**
 * Description goes here
 * Date: 6/1/14 7:44 PM
 *
 * @author Mihail Chilyashev
 */
public class OutputChannel extends Channel implements Tickable {

    RRA rra;

    /**
     * ID на възела, към който води този изходен канал
     */
    int nextNodeId;


    /**
     * Определяне на състоянието на автомата според текущите активни входни сигнали и издаване на изходни сигнали.
     */
    public int calculateState() {
        return 0xb00b5;
    }


    @Override
    public void tick() {
        int newState = calculateState();
        switch (newState) {
            // doStuff
        }
    }


    public RRA getRra() {
        return rra;
    }

    public void setRra(RRA rra) {
        this.rra = rra;
    }

    public int getNextNodeId() {
        return nextNodeId;
    }

    public void setNextNodeId(int nextNodeId) {
        this.nextNodeId = nextNodeId;
    }
}
