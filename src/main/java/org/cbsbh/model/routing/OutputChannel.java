package org.cbsbh.model.routing;

import org.cbsbh.Debug;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.flit.Flit;
import org.cbsbh.model.structures.SignalArray;
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
    // ID-то на възела, към който води този изходен канал е същото като ID-то на текущия канал
    //int nextNodeId;

    /**
     * Попълва се от sendGrantAck в Arbiter
     */
    private FIFOQueue accepted; // Опашка, върнала Accept
    private ArrayList<FIFOQueue> requestList; // Опашки, пусннали Request


    private Flit buffer;
    private InputChannel nextInputChannel;
    private int currentNodeId;

    public OutputChannel(int id, int currentNodeId) {
        this.id = id;
        this.currentNodeId = currentNodeId;
    }


    @Override
    public void init() {
        Debug.println(getClass() + " init");
        rra = new RRA();
    }


    /**
     * Определяне на състоянието на автомата според текущите активни входни сигнали и издаване на изходни сигнали.
     */
    public int calculateState() {
        if (hasSignal(SignalArray.RESET) || hasSignal(SignalArray.INIT)) {
            return STATE0_INIT;
        }

        if (state == STATE0_INIT) {
            return STATE1_ROUTING_AND_ARBITRAGING;
        }


        if (state == STATE1_ROUTING_AND_ARBITRAGING) {
            // Входният канал за всички изходни канали на рутер XXXX е XXXX на рутер YYYY, където YYYY ID-то на изходния канал.
            InputChannel nextInputChannel = MPPNetwork.get(this.id).getInputChannel(this.currentNodeId);
            if (!nextInputChannel.hasSignal(SignalArray.CHAN_BUSY) && rra.isGrantAckReceived()) {
                return STATE2_READY_FOR_TRANSFER;
            }
        }

        if (state == STATE2_READY_FOR_TRANSFER) {
            if (nextInputChannel.getActiveFIFOIndex() != -1
                    && nextInputChannel.getActiveFIFOQueue().hasSignal(SignalArray.PACK_WAIT)) {
                return STATE3_START_OF_TRANSFER;
            }
        }

        if (state == STATE3_START_OF_TRANSFER) {
            return STATE4_TRANSFER1;
        }

        if (state == STATE4_TRANSFER1) {
            assert accepted != null : "Dang! sadface.wma";
            if (!accepted.hasSignal(SignalArray.CNT_EQU)) { // TODO: CNT_EQU
                return STATE5_TRANSFER2;
            } else {
                return STATE6_END_OF_TRANSFER;
            }
        }

        if (state == STATE5_TRANSFER2) {
            return STATE4_TRANSFER1;
        }

        if (state == STATE6_END_OF_TRANSFER) {
            if (!hasSignal(SignalArray.TIME_ONE) && nextInputChannel.hasSignal(SignalArray.DATA_ACK)) {
                return STATE1_ROUTING_AND_ARBITRAGING;
            } else if (hasSignal(SignalArray.TIME_ONE)) {
                return STATE0_INIT;
            }
        }

        return state;
    }


    public void tick() {


        int newState = calculateState();
        setState(newState);
        switch (newState) {
            case STATE0_INIT:
                getSignalArray().setSignal(SignalArray.RRA_BUSY, true);
                getSignalArray().setSignal(SignalArray.RESET, false);
                rra.init(); // Вика се, защото от S6 обикновено не се връща в S0, а в S1.
                break;
            case STATE1_ROUTING_AND_ARBITRAGING:
                getSignalArray().setSignal(SignalArray.RRA_WORK, true);
                getSignalArray().setSignal(SignalArray.STRB_SIG, true);
                if (rra.hasRequests()) {
                    rra.sendGrant();
                }
                break;
            case STATE2_READY_FOR_TRANSFER:
                getSignalArray().setSignal(SignalArray.RRA_BUSY, true);
                getSignalArray().setSignal(SignalArray.WR_MUX_ADR, true);
                getSignalArray().setSignal(SignalArray.WR_RRA_PTR, true);

                // Входният канал за всички изходни канали на рутер XXXX е XXXX на рутер YYYY, където YYYY ID-то на изходния канал.
                nextInputChannel = MPPNetwork.get(id).getInputChannel(currentNodeId);
                assert nextInputChannel != null : "This can't be null";
                break;
            case STATE3_START_OF_TRANSFER:
                getSignalArray().setSignal(SignalArray.RRA_BUSY, true);
//                getSignalArray().setSignal(SignalArray.VALID_DATA, true);
                getSignalArray().setSignal(SignalArray.WR_RG_OUT, true);
                // Попълване на буфера. Става във FIFOQueue.sendDataToNextNode()
                assert nextInputChannel != null : "This can't be null";
                assert buffer.getFlitType() == Flit.FLIT_TYPE_HEADER : "Този Flit трябва да е HeaderFlit!";
                buffer.setValidDataBit();
                //nextInputChannel.setInputBuffer(buffer);
                break;
            case STATE4_TRANSFER1:
                getSignalArray().setSignal(SignalArray.RRA_BUSY, true);
//                getSignalArray().setSignal(SignalArray.VALID_DATA, true);
                buffer.setValidDataBit();
                // и EXT_CLK, 'ма него не го ползваме
                assert buffer != null : "Този buffer трябва да е не-null!";
                nextInputChannel.setInputBuffer(buffer);
                break;
            case STATE5_TRANSFER2:
                getSignalArray().setSignal(SignalArray.RRA_BUSY, true);
//                getSignalArray().setSignal(SignalArray.VALID_DATA, true);
                buffer.setValidDataBit();
                getSignalArray().setSignal(SignalArray.WR_RG_OUT, true);
                getSignalArray().setSignal(SignalArray.FLT_RD, true);
                // Попълване на буфера. Става във FIFOQueue.sendDataToNextNode()
                assert buffer != null : "Този buffer трябва да е не-null!";
                //nextInputChannel.setInputBuffer(buffer);
                break;
            case STATE6_END_OF_TRANSFER:
                getSignalArray().setSignal(SignalArray.RRA_BUSY, true);
                getSignalArray().setSignal(SignalArray.CLR_MUX_ADDR, true);
                getSignalArray().setSignal(SignalArray.TIMER_EN, true);

                break;
        }

    }

    /**
     * Входният канал за всички изходни канали на рутер XXXX е XXXX на рутер YYYY, където YYYY ID-то на изходния канал.
     * @return
     */
    public int getNextNodeId(){
        return id;
    }


    // Clutter
    public RRA getRra() {
        return rra;
    }

    public void setRra(RRA rra) {
        this.rra = rra;
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

    public int getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(int currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public FIFOQueue getAccepted() {
        return accepted;
    }

    public void setAccepted(FIFOQueue accepted) {
        this.accepted = accepted;
    }
}
