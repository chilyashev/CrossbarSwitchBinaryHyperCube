package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.flit.Flit;
import org.cbsbh.model.structures.InputSignalArray;
import org.cbsbh.model.structures.OutputSignalArray;
import org.cbsbh.model.structures.StateStructure;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 6/1/14 7:44 PM
 *
 * @author Mihail Chilyashev
 */
public class OutputChannel extends StateStructure implements Tickable {

    public static final int STATE0_INIT = 0;
    public static final int STATE1_ROUTING_AND_ARBITRAGING = 1;
    public static final int STATE2_READY_FOR_TRANSFER = 2;
    public static final int STATE3_START_OF_TRANSFER = 3;
    public static final int STATE4_TRANSFER1 = 4;
    public static final int STATE5_TRANSFER2 = 5;
    public static final int STATE6_END_OF_TRANSFER = 6;


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


    private Flit buffer;
    private InputChannel nextInputChannel;


    /**
     * Определяне на състоянието на автомата според текущите активни входни сигнали и издаване на изходни сигнали.
     */
    public int calculateState() {
        if (hasInputSignal(InputSignalArray.RESET) || hasInputSignal(InputSignalArray.INIT)) {
            return STATE0_INIT;
        }

        if (state == STATE0_INIT) {
            return STATE1_ROUTING_AND_ARBITRAGING;
        }


        InputChannel nextInputChannel = MPPNetwork.get(nextNodeId).getInputChannel(this.id);
        if (state == STATE1_ROUTING_AND_ARBITRAGING) {
            if (!nextInputChannel.hasOutputSignal(OutputSignalArray.CHAN_BUSY) && rra.isGrantAckReceived()) {
                return STATE2_READY_FOR_TRANSFER;
            }
        }

        if (state == STATE2_READY_FOR_TRANSFER) {
            if (nextInputChannel.hasOutputSignal(OutputSignalArray.PACK_WAIT)) {
                return STATE3_START_OF_TRANSFER;
            }
        }

        if (state == STATE3_START_OF_TRANSFER) {
            return STATE4_TRANSFER1;
        }

        if (state == STATE4_TRANSFER1) {
            assert accepted != null : "Dang! sadface.wma";
            if (!accepted.hasOutputSignal(OutputSignalArray.CNT_EQU)) { // TODO: CNT_EQU
                return STATE5_TRANSFER2;
            } else {
                return STATE6_END_OF_TRANSFER;
            }
        }

        if (state == STATE5_TRANSFER2) {
            return STATE4_TRANSFER1;
        }

        if (state == STATE6_END_OF_TRANSFER) {
            if (!hasInputSignal(InputSignalArray.TIME_ONE) && nextInputChannel.hasOutputSignal(OutputSignalArray.DATA_ACK)) {
                return STATE1_ROUTING_AND_ARBITRAGING;
            } else if (hasInputSignal(InputSignalArray.TIME_ONE)) {
                return STATE0_INIT;
            }
        }

        return state;
    }


    public void tick() {


        int newState = calculateState();
        switch (newState) {
            case STATE0_INIT:
                getOutputSignalArray().setSignal(OutputSignalArray.RRA_BUSY, true);
                rra.init(); // Вика се, защото от S6 обикновено не се връща в S0, а в S1.
                break;
            case STATE1_ROUTING_AND_ARBITRAGING:
                getOutputSignalArray().setSignal(OutputSignalArray.RRA_WORK, true);
                getOutputSignalArray().setSignal(OutputSignalArray.STRB_SIG, true);
                if (rra.hasRequests()) {
                    rra.sendGrant();
                }
                break;
            case STATE2_READY_FOR_TRANSFER:
                getOutputSignalArray().setSignal(OutputSignalArray.RRA_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.WR_MUX_ADR, true);
                getOutputSignalArray().setSignal(OutputSignalArray.WR_RRA_PTR, true);

                nextInputChannel = MPPNetwork.get(nextNodeId).getInputChannel(id);
                assert nextInputChannel != null : "This can't be null";
                break;
            case STATE3_START_OF_TRANSFER:
                getOutputSignalArray().setSignal(OutputSignalArray.RRA_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.VALID_DATA, true);
                getOutputSignalArray().setSignal(OutputSignalArray.WR_RG_OUT, true);
                // Попълване на буфера. Става във FIFOQueue.sendDataToNextNode()
                assert nextInputChannel != null : "This can't be null";
                nextInputChannel.setInputBuffer(buffer);
                break;
            case STATE4_TRANSFER1:
                getOutputSignalArray().setSignal(OutputSignalArray.RRA_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.VALID_DATA, true);
                // и EXT_CLK, 'ма него не го ползваме
                break;
            case STATE5_TRANSFER2:
                getOutputSignalArray().setSignal(OutputSignalArray.RRA_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.VALID_DATA, true);
                getOutputSignalArray().setSignal(OutputSignalArray.WR_RG_OUT, true);
                getOutputSignalArray().setSignal(OutputSignalArray.FLT_RD, true);
                // Попълване на буфера. Става във FIFOQueue.sendDataToNextNode()
                break;
            case STATE6_END_OF_TRANSFER:
                getOutputSignalArray().setSignal(OutputSignalArray.RRA_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.CLR_MUX_ADDR, true);
                getOutputSignalArray().setSignal(OutputSignalArray.TIMER_EN, true);

                break;
        }

    }


    // Clutter
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

    public Flit getBuffer() {
        return buffer;
    }

    public void setBuffer(Flit buffer) {
        this.buffer = buffer;
    }
}
