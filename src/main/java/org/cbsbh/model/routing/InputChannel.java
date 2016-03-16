package org.cbsbh.model.routing;

import org.cbsbh.Debug;
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


    ArrayList<Flit> receivedData; // Only to check what's in nyah

    boolean transferStartedForMe = false; // If true, the virtual channel is set and every next flit is for the current node. Reset when a tail flit is encountered casually. Like a careless whisper
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
    private int activeFIFOIndex = -1;

    private int fifoQueueCount; // TODO: това е сложено, щото може да се наложи да се разбере броя на опашките. Ако се остави без него, няма да е яко.

    private boolean[] B_FIFO_STATUS; // TODO: това що е тука? Не може ли да е в някой от signalArray-ите? Или не е такъв сигнал?
    private Flit inputBuffer;

    private int nodeId;
    private String who;

    boolean gotoS2 = false;
    private boolean chanBusy;

    public InputChannel(int id, int currentNodeId) {
        this.id = id;

        this.nodeId = currentNodeId;
        this.fifoQueueCount = Context.getInstance().getInteger("fifoQueueCount");
        fifoQueues = new ArrayList<>(fifoQueueCount);
        B_FIFO_STATUS = new boolean[fifoQueueCount];
    }

    @Override
    public void init() {
        Debug.printf(getWho() + " init");

        this.node = MPPNetwork.get(nodeId);
        assert this.node != null : "The node can't be null. Fuck";

        for (int i = 0; i < fifoQueueCount; i++) {
            FIFOQueue e = new FIFOQueue(this, i);
            e.init();
            fifoQueues.add(e);
        }
        getSignalArray().setSignal(SignalArray.RESET, true);
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
            if (!isChanBusy()) {
                return STATE1_UPDATE_FIFO_STATUS;
            }
        }

        if (state == STATE1_UPDATE_FIFO_STATUS) {
            if (!isChanBusy()) {//hasSignal(SignalArray.CHAN_BUSY) /*&& gotoS2/* && inputBuffer != null*/) {
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

    @Override
    public void calculateNewState() {
        // Debug.printf("%s Old state: %d", getWho(), state);
        state = calculateState();
        // Debug.printf("%s New state: %d", getWho(), state);
        setState(state);
        lowerEmSignalsHny();
        // Фаза 1: Определяне на състоянието
        fifoQueues.forEach(FIFOQueue::calculateNewState);
    }

    public FIFOQueue getActiveFifo() {
        return getQueue(activeFIFOIndex);
    }

    public void tick() {

        // Фаза 2: ...
        fifoQueues.forEach(FIFOQueue::tick);


        //Debug.println(String.format("%s current state %d", getWho(), state));
        //Debug.printSignals(Debug.CLASS_INPUT_CHANNEL, this);

        /*for (FIFOQueue queue : fifoQueues) {
            Debug.printf("\n\nQueue %d status: %s\n\n", queue.id, queue.getStatus());
        }*/

        /*if (id == 0 && nodeId == 4) {
            Debug.println(String.format("[%s, id=%d] stop %d", getWho(), id, state));
        }*/
        /*for (FIFOQueue queue : fifoQueues) {
            if (queue.hasSignal(SignalArray.WR_IN_FIFO)) {
                getSignalArray().setSignal(SignalArray.WR_IN_FIFO, true);
                break;
            }
        }*/
        // Вземаме новото състояние

        boolean allBusy;
        switch (state) {
            case STATE0_INIT:
                getSignalArray().setSignal(SignalArray.BUFF_BUSY, true);
                getSignalArray().setSignal(SignalArray.RESET, false);

                // Обикаляме всички опашки и проверяваме дали са свободни. Ако всички са заети, вдигаме CHAN_BUSY.
                getSignalArray().setSignal(SignalArray.CHAN_BUSY, isChanBusy());

                break;
            case STATE1_UPDATE_FIFO_STATUS:
                getSignalArray().setSignal(SignalArray.WR_B_RG, true);
                getSignalArray().setSignal(SignalArray.BUFF_BUSY, true);

                getSignalArray().setSignal(SignalArray.CHAN_BUSY, isChanBusy());

                if (activeFIFOIndex == -1) {
                    activeFIFOIndex = getFirstAvailableQueueIndex();
                    Debug.printf("Got a new FIFO, activeFIFOIndex: %d", activeFIFOIndex);
                }
                break;

            // TODO: I'm watching you.
            case STATE2_WRITE_IN_FIFO:
                //getSignalArray().setSignal(SignalArray.DEMUX_RDY, true);
                produceWR_IN_FIFO();
                if (activeFIFOIndex == -1) {
                    activeFIFOIndex = getFirstAvailableQueueIndex();
                    Debug.printf("Got a new FIFO, activeFIFOIndex: %d", activeFIFOIndex);
                }
                assert activeFIFOIndex != -1 : "activeFIFOIndex не би трябвало да е -1. Чекираут, мейн!";
                fifoQueues.get(activeFIFOIndex).getSignalArray().setSignal(SignalArray.FIFO_SELECT, true);
                fifoQueues.get(activeFIFOIndex).getSignalArray().setSignal(SignalArray.DEMUX_RDY, true);
                getSignalArray().setSignal(SignalArray.DEMUX_RDY, true);
                if (activeFIFOIndex != -1 && getActiveFifo().hasSignal(SignalArray.FIFO_BUSY) && hasSignal(SignalArray.WR_IN_FIFO)) {

                    //FIFOQueue qq = ;
                    //assert inputBuffer != null : "Trying to push a null flit in the FIFO";

                    //fifoQueues.get(activeFIFOIndex).push(inputBuffer);


                    //getSignalArray().setSignal(SignalArray.FIFO_SELECT, true);
                    /*fifoQueues.get(activeFIFOIndex).getSignalArray().setSignal(SignalArray.FIFO_SELECT, true);
                    fifoQueues.get(activeFIFOIndex).getSignalArray().setSignal(SignalArray.DEMUX_RDY, true);*/
                    inputBuffer = null;
                }
                break;
            case STATE3_END_WRITE:
                getSignalArray().setSignal(SignalArray.DEMUX_RDY, true);
                produceWR_IN_FIFO();
                for (FIFOQueue queue : fifoQueues) {
                    if (queue.hasSignal(SignalArray.FIFO_BUSY)) {
                        getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                        break;
                    }
                }
                break;
        }

        //Debug.printSignals(Debug.CLASS_INPUT_CHANNEL, this);
        //Debug.printf("End of tick");
        //getSignalArray().resetAll();
    }

    private void lowerEmSignalsHny() {
        switch (state) {
            case STATE0_INIT:
                getSignalArray().resetAll();
                break;
            case STATE1_UPDATE_FIFO_STATUS:
                getSignalArray().setSignal(SignalArray.RESET, false);
                break;
            case STATE2_WRITE_IN_FIFO:
                getSignalArray().setSignal(SignalArray.WR_B_RG, false);
                break;
        }
    }

    /**
     * Produces WR_IN_FIFO
     */
    private void produceWR_IN_FIFO() {
        boolean hasWR_IN_FIFO = false;
        for (FIFOQueue queue : fifoQueues) {
            if (queue.hasSignal(SignalArray.WR_IN_FIFO)) {
                hasWR_IN_FIFO = true;
                break;
            }
        }
        getSignalArray().setSignal(SignalArray.WR_IN_FIFO, hasWR_IN_FIFO);
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

    public void setInputBuffer(Flit flit) {
        Debug.printf("%s Got a flitter: %s", getWho(), flit);

        Debug.printSignals(Debug.CLASS_INPUT_CHANNEL, this);


        if (flit.getFlitType() == Flit.FLIT_TYPE_HEADER && flit.getTR() == 0) {
            transferStartedForMe = true;
            receivedData = new ArrayList<>();
            Debug.println("FINALLY! A header flit!");
        }

        if (transferStartedForMe) {
            receivedData.add(flit);
            if(flit.getFlitType() == Flit.FLIT_TYPE_TAIL) {
                transferStartedForMe = false;
                Debug.printf("Received for %s", getWho());
                for (Flit flit1 : receivedData) {
                    Debug.println("Flit: " + flit1.toString());
                }

            }
        }


        if (activeFIFOIndex == -1) {
            activeFIFOIndex = getFirstAvailableQueueIndex();
        }

        this.inputBuffer = flit;
        getActiveFifo().getSignalArray().setSignal(SignalArray.FIFO_BUSY, false);
        getActiveFifo().push(flit);
        Debug.printf("After push: size: %d", getActiveFifo().fifo.size());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActiveFIFOIndex() {
        return activeFIFOIndex;
    }

    public FIFOQueue getActiveFIFOQueue() {
        if (activeFIFOIndex != -1) {
            return fifoQueues.get(activeFIFOIndex);
        }
        return null;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getWho() {
        return String.format("\tInputChannel {id: %d (%s), nodeID: %d (%s), state: %d}", id, Integer.toBinaryString(id), nodeId, Integer.toBinaryString(nodeId), state);
    }

    public boolean isChanBusy() {
        for (int i = 0; i < fifoQueues.size(); i++) {
            FIFOQueue queue = fifoQueues.get(i);
            B_FIFO_STATUS[i] = queue.hasSignal(SignalArray.FIFO_BUSY);
            if (!queue.hasSignal(SignalArray.FIFO_BUSY)) {
                return false;
            }
        }
        return true;
    }
}
