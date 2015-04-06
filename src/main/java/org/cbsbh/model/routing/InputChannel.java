package org.cbsbh.model.routing;

import jdk.internal.util.xml.impl.Input;
import org.cbsbh.context.Context;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.flit.Flit;
import org.cbsbh.model.structures.StateStructure;
import org.cbsbh.model.structures.InputSignalArray;
import org.cbsbh.model.structures.OutputSignalArray;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 6/1/14 7:34 PM
 *
 * @author Mihail Chilyashev
 */
public class InputChannel extends StateStructure implements Tickable {


    public static final int STATE0_INIT = 0;
    public static final int STATE1_UPDATE_FIFO_STATUS = 1;
    public static final int STATE2_WRITE_IN_FIFO = 2;
    public static final int STATE3_END_WRITE = 3;

    SMPNode node;

/*
       Node0                Node1
        +----------------+  CB   +----------------+
i0------|              O0|------>|I               |
        |    InputSignals|<------|OutputSignals   |
        |                |       |                |
        |                | DATA  |                |
        |              o0|-------|i0              |
        +----------------+       +----------------+

    InputChannel.SMPNode
    OutputChannel.SMPNode
    SMPNode.HashMap<Integer, InputChannel>
    SMPNode.HashMap<Integer, OutputChannel>


 */

    ArrayList<FIFOQueue> fifoQueues;

    // Индексът на активната опашка
    private int activeFIFOIndex;

    private int fifoQueueCount; // TODO: това е сложено, щото може да се наложи да се разбере броя на опашките. Ако се остави без него, няма да е яко.

    private boolean[] B_FIFO_STATUS; // TODO: това що е тука? Не може ли да е в някой от signalArray-ите? Или не е такъв сигнал?


    public InputChannel() {
        this.fifoQueueCount = Context.getInstance().getInteger("fifoQueueCount");
        fifoQueues = new ArrayList<>(fifoQueueCount);

        setState(STATE0_INIT);
    }

    /**
     * Проверка и избор на свободна опашка (издване на сигнал CHAN_BUSY при нужда).
     */
    public FIFOQueue pickFreeQueue() {
        // обикаля се масива с опашки и се намира свободна. Издава се CHAN_BUSY при нужда.
        for (FIFOQueue fifoQueue : fifoQueues) {
            if (fifoQueue.getFifo().size() < Context.getInstance().getInteger("fifoQueueSize")) {
                return fifoQueue;
            }
        }
        // Издаване на CHAN_BUSY
        outputSignalArray.setSignal(OutputSignalArray.CHAN_BUSY, true);
        return null;
    }

    /**
     * Определяне на състоянието на автомата според текущите активни входни сигнали и издаване на изходни сигнали.
     */
    public int calculateState() {
        if (hasInputSignal(InputSignalArray.RESET) || hasInputSignal(InputSignalArray.INIT)) {
            return STATE0_INIT;
        }

        if (state == STATE0_INIT) {
            if (!hasInputSignal(InputSignalArray.CHANNEL_BUSY)) {
                return STATE1_UPDATE_FIFO_STATUS;
            }
        }

        if (state == STATE1_UPDATE_FIFO_STATUS) {
            if (!hasInputSignal(InputSignalArray.CHANNEL_BUSY)) {
                return STATE2_WRITE_IN_FIFO;
            } else {
                return STATE0_INIT;
            }
        }

        if (state == STATE2_WRITE_IN_FIFO) {
            // Ако текущата активна опашка е заета...
            if (getActiveFifo().hasOutputSignal(InputSignalArray.FIFO_BUSY)
                    && hasInputSignal(InputSignalArray.WR_IN_FIFO)
                    ) {
                return STATE3_END_WRITE;
            }
        }

        if (state == STATE3_END_WRITE) {
            if (!hasInputSignal(InputSignalArray.WR_IN_FIFO)
                    && getActiveFifo().hasOutputSignal(InputSignalArray.FIFO_BUSY)) {
                return STATE0_INIT;
            }
        }

        return state;
    }

    private FIFOQueue getActiveFifo() {
        return getQueue(activeFIFOIndex);
    }


    @Override
    public void tick() {
        if (state == STATE0_INIT) {
            // TODO: обикаляме всички опашки и проверяваме дали са свободни. Ако всички са заети, вдигаме CHAN_BUSY.
        }

    }

    /**
     * Взема първата свободна опашка според B_FIFO_STATUS регистъра и я маркира като заета
     *
     * @return индексът на първата свободна опашка
     */
    private int getFirstAvailableQueueIndex() {
        for (int i = 0; i < B_FIFO_STATUS.length; i++) {
            if (!B_FIFO_STATUS[i]) {
                B_FIFO_STATUS[i] = true;
                return i;
            }
        }
        return -1;
    }

    public FIFOQueue getFirstAvailableQueue() {
        return fifoQueues.get(getFirstAvailableQueueIndex());
    }

    public FIFOQueue getQueue(int index) {
        return fifoQueues.get(index);
    }


    public SMPNode getNode() {
        return node;
    }

    public void setNode(SMPNode node) {
        this.node = node;
    }

    public ArrayList<FIFOQueue> getFifoQueues() {
        return fifoQueues;
    }

    public void setFifoQueues(ArrayList<FIFOQueue> fifoQueues) {
        this.fifoQueues = fifoQueues;
    }
}
