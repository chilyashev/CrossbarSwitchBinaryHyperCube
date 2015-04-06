package org.cbsbh.model.routing;

import com.sun.javaws.jnl.MatcherReturnCode;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.structures.InputSignalArray;
import org.cbsbh.model.structures.OutputSignalArray;
import org.cbsbh.model.structures.StateStructure;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description goes here
 * Date: 6/1/14 7:44 PM
 *
 * @author Mihail Chilyashev
 */
public class OutputChannel extends StateStructure implements Tickable {

    public static final int STATE0_INIT = 0;
    public static final int STATE1_ROUTING_AND_ARBITRAGING1 = 1;
    public static final int STATE2_ROUTING_AND_ARBITRAGING2 = 2;
    public static final int STATE3_READY_FOR_TRANSFER = 3;
    public static final int STATE4_START_OF_TRANSFER = 4;
    public static final int STATE5_TRANSFER1 = 5;
    public static final int STATE6_TRANSFER2 = 6;
    public static final int STATE7_END_OF_TRANSFER = 7;


    int id;


    /**
     * БРрррра!
     */
    RRA rra;

    /**
     * ID на възела, към който води този изходен канал
     */
    int nextNodeId;

    private FIFOQueue accepted; // Опашка, върнала Accept
    private ArrayList<FIFOQueue> requestList; // Опашки, пусннали Request


    /**
     * Определяне на състоянието на автомата според текущите активни входни сигнали и издаване на изходни сигнали.
     */
    public int calculateState() {
        if (hasInputSignal(InputSignalArray.RESET) || hasInputSignal(InputSignalArray.INIT)) {
            return STATE0_INIT;
        }

        if (state == STATE0_INIT) {
            return STATE1_ROUTING_AND_ARBITRAGING1;
        }

        if (state == STATE1_ROUTING_AND_ARBITRAGING1) {
            return STATE2_ROUTING_AND_ARBITRAGING2;
        }

        InputChannel nextInputChannel = MPPNetwork.get(nextNodeId).getInputChannel(this.id);
        if (state == STATE2_ROUTING_AND_ARBITRAGING2) {
            if (nextInputChannel.hasOutputSignal(OutputSignalArray.CHAN_BUSY)
                    || !hasInputSignal(InputSignalArray.TRANSFER)) {
                return STATE1_ROUTING_AND_ARBITRAGING1;
            } else if (!nextInputChannel.hasOutputSignal(OutputSignalArray.CHAN_BUSY) && hasInputSignal(InputSignalArray.TRANSFER)) {
                return STATE3_READY_FOR_TRANSFER;
            }
        }

        if (state == STATE3_READY_FOR_TRANSFER) {
            if (nextInputChannel.hasOutputSignal(OutputSignalArray.PACK_WAIT)) {
                return STATE4_START_OF_TRANSFER;
            }
        }

        if (state == STATE4_START_OF_TRANSFER) {
            return STATE5_TRANSFER1;
        }

        if (state == STATE5_TRANSFER1) {
            assert accepted != null : "Dang! sadface.wma";
            if (!accepted.hasOutputSignal(OutputSignalArray.CNT_EQU)) { // TODO: CNT_EQU
                return STATE6_TRANSFER2;
            } else {
                return STATE7_END_OF_TRANSFER;
            }
        }

        if (state == STATE6_TRANSFER2) {
            return STATE5_TRANSFER1;
        }

        if (state == STATE7_END_OF_TRANSFER) {
            if (!hasInputSignal(InputSignalArray.TIME_ONE) && nextInputChannel.hasOutputSignal(OutputSignalArray.DATA_ACK)) {
                return STATE1_ROUTING_AND_ARBITRAGING1;
            } else if (hasInputSignal(InputSignalArray.TIME_ONE)) {
                return STATE0_INIT;
            }
        }

        return state;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
