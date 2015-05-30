package org.cbsbh.model.routing;

import org.cbsbh.context.Context;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.flit.Flit;
import org.cbsbh.model.structures.SignalArray;
import org.cbsbh.model.structures.StateStructure;
import org.cbsbh.model.structures.SignalArray;

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
        |    Signals|<------|OutputSignals   |
        |                |       |                |
        |                | DATA  |                |
        |              o0|-------|i0              |
        +----------------+       +----------------+

    InputChannel.SMPNode
    OutputChannel.SMPNode
    SMPNode.HashMap<Integer, InputChannel>
    SMPNode.HashMap<Integer, OutputChannel>


 */

    int id;

    ArrayList<FIFOQueue> fifoQueues;

    // Индексът на активната опашка
    private int activeFIFOIndex;

    private int fifoQueueCount; // TODO: това е сложено, щото може да се наложи да се разбере броя на опашките. Ако се остави без него, няма да е яко.

    private boolean[] B_FIFO_STATUS; // TODO: това що е тука? Не може ли да е в някой от signalArray-ите? Или не е такъв сигнал?
    private Flit inputBuffer;


    public InputChannel(int id, int currentNodeId) {
        this.id = id;
        this.node = MPPNetwork.get(currentNodeId);
        this.fifoQueueCount = Context.getInstance().getInteger("fifoQueueCount");
        fifoQueues = new ArrayList<>(fifoQueueCount);
        for(int i = 0; i < fifoQueueCount; i++){
            fifoQueues.add(new FIFOQueue(this, i));
        }
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
        signalArray.setSignal(SignalArray.CHAN_BUSY, true);
        return null;
    }

    /**
     * Определяне на състоянието на автомата според текущите активни входни сигнали и издаване на изходни сигнали.
     */
    public int calculateState() {
        if (hasSignal(SignalArray.RESET) || hasSignal(SignalArray.INIT)) {
            return STATE0_INIT;
        }

        if (state == STATE0_INIT) {
            if (!hasSignal(SignalArray.CHAN_BUSY)) {
                return STATE1_UPDATE_FIFO_STATUS;
            }
        }

        if (state == STATE1_UPDATE_FIFO_STATUS) {
            if (!hasSignal(SignalArray.CHAN_BUSY)) {
                return STATE2_WRITE_IN_FIFO;
            } else {
                return STATE0_INIT;
            }
        }

        if (state == STATE2_WRITE_IN_FIFO) {
            // Ако текущата активна опашка е заета...
            if (getActiveFifo().hasSignal(SignalArray.FIFO_BUSY)
                    && hasSignal(SignalArray.WR_IN_FIFO)
                    ) {
                return STATE3_END_WRITE;
            }
        }

        if (state == STATE3_END_WRITE) {
            if (!hasSignal(SignalArray.WR_IN_FIFO)
                    && getActiveFifo().hasSignal(SignalArray.FIFO_BUSY)) {
                return STATE0_INIT;
            }
        }

        return state;
    }

    private FIFOQueue getActiveFifo() {
        return getQueue(activeFIFOIndex);
    }


    public void tick() {
        for (FIFOQueue queue : fifoQueues) {
            if (queue.hasSignal(SignalArray.WR_IN_FIFO)) {
                getSignalArray().setSignal(SignalArray.WR_IN_FIFO, true);
                break;
            }
        }
        // Вземаме новото състояние
        state = calculateState();

        switch (state) {
            case STATE0_INIT:
                getSignalArray().setSignal(SignalArray.BUFF_BUSY, true);

                // Обикаляме всички опашки и проверяваме дали са свободни. Ако всички са заети, вдигаме CHAN_BUSY.
                boolean allBusy = true;
                for (FIFOQueue queue : fifoQueues) {
                    if (!queue.hasSignal(SignalArray.FIFO_BUSY)) {
                        allBusy = false;
                        break;
                    }
                }

                getSignalArray().setSignal(SignalArray.CHAN_BUSY, allBusy);
                getSignalArray().setSignal(SignalArray.CHAN_BUSY, allBusy);

                break;
            case STATE1_UPDATE_FIFO_STATUS:
                getSignalArray().setSignal(SignalArray.WR_B_RG, true);
                getSignalArray().setSignal(SignalArray.BUFF_BUSY, true);
                for (int i = 0; i < fifoQueues.size(); i++) {
                    FIFOQueue queue = fifoQueues.get(i);
                    B_FIFO_STATUS[i] = queue.hasSignal(SignalArray.FIFO_BUSY);
                }
                break;

            case STATE2_WRITE_IN_FIFO:
                getSignalArray().setSignal(SignalArray.DEMUX_RDY, true);
                activeFIFOIndex = getFirstAvailableQueueIndex();
                FIFOQueue qq = fifoQueues.get(activeFIFOIndex);
                assert inputBuffer != null : "Trying to push a null flit in the FIFO";
                qq.push(inputBuffer);
                break;
            case STATE3_END_WRITE:
                getSignalArray().setSignal(SignalArray.DEMUX_RDY, true);
                for (FIFOQueue queue : fifoQueues) {
                    if (queue.hasSignal(SignalArray.FIFO_BUSY)) {
                        getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                        break;
                    }
                }
                break;
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

    public Flit getInputBuffer() {
        return inputBuffer;
    }

    public void setInputBuffer(Flit inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
