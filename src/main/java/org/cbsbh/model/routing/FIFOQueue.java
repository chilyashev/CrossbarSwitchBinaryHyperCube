package org.cbsbh.model.routing;

import org.cbsbh.Debug;
import org.cbsbh.model.StatusReporter;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.structures.Flit;
import org.cbsbh.model.structures.SignalArray;
import org.cbsbh.model.structures.StateStructure;

import java.util.*;

/**
 * Проверката на сигнали се случва в *SignalArray класовете. hasSignal(index), например.
 * Date: 3/16/15 10:48 AM
 *
 * @author Mihail Chilyashev
 */
public class FIFOQueue extends StateStructure implements Tickable, StatusReporter {
    /**
     * Начално състояние. По него се влиза след RESET, след INIT,
     * след освобождаване на опашката от вече приет и прочетен пакет пакет или след изтекъл timeout.
     */
    public static final int STATE0_INIT = 0; // S0

    public static final int STATE1_IDLE = 1; // S1

    public static final int STATE2_READY = 2; // S2

    public static final int STATE3_REQUEST_FOR_ROUTING = 3; // S3

    public static final int STATE4_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL = 4; // S4

    public static final int STATE5_READ_PACKET = 5; // S5

    public static final int STATE6_WAIT_FOR_END_OF_PACKET = 6; // S6


    long timer = 0;
    /**
     * баш флит
     */
    LinkedList<Flit> fifo;

    /**
     * Грижи се за комуникацията с OutputChannel-а.
     */
    Arbiter arby;


    /**
     * Входният канал, за който е това FIFO
     */
    InputChannel channel;

    ArrayList<Flit> receivedData; // Only to check what's in nyah
    boolean startedReceiving = false;

    int nextNodeId = -1;
    int id;
    int nodeId;//.getId();
    private String who;
    private boolean requestSent = false;

    public FIFOQueue() {

    }

    public FIFOQueue(InputChannel channel, int id) {
        this.channel = channel;
        this.id = id;
        // fifo = new PriorityQueue<>();
        // fifo = new ArrayDeque<>();
        fifo = new LinkedList<>();
    }

    public void init() {
        // TODO: големината на fifo трябва да е "размерът, указан в интерфейса - 2" заради Head/Tail флитовете
        requestSent = false;
        nodeId = channel.getNodeId();
        Debug.printf(getWho() + " init");

        ArrayList<OutputChannel> outputChannels = new ArrayList<>();
        assert (MPPNetwork.get(nodeId)) != null : "nopew.";
        outputChannels.addAll(MPPNetwork.get(nodeId).getOutputChannels().values());
        arby = new Arbiter(nodeId, outputChannels, id, channel.getId());
        setState(STATE0_INIT);
    }

    /**
     * Определяне на състоянието на автомата според текущите активни входни сигнали и издаване на изходни сигнали.
     */
    public int calculateState() {

        // TODO: освен при RESET и INIT, S0 трябва да се върне и при освобождаване на опашката и изтичане на таймаут.
        if (hasSignal(SignalArray.RESET) || signalArray.hasSignal(SignalArray.INIT)) {
            return STATE0_INIT;
        }

        //  Безусловно се минава от S0 към S1
        if (state == STATE0_INIT) {
            return STATE1_IDLE;
        }

        // Ако се намираме в STATE1 и са налични сигналите FIFO_SELECT и DEMUX_RDY, преминаваме в S2
        //if (state == STATE1_IDLE && channel.hasSignalsAnd(SignalArray.FIFO_SELECT, SignalArray.DEMUX_RDY)) {
        if (state == STATE1_IDLE && hasSignalsAnd(SignalArray.FIFO_SELECT, SignalArray.DEMUX_RDY)) { // За да може само една опашка да е активна
            return STATE2_READY;
        }

        // Ако сме в STATE2 и са налични сигналите VALID_DATA и HEAD_FLIT, преминаваме в S3
        if (state == STATE2_READY && isCurrentFlitDataValid()
                && (getCurrentFlitType() == Flit.FLIT_TYPE_HEADER)) {
            return STATE3_REQUEST_FOR_ROUTING;
        }

        // Ако сме в STATE3, преминаваме в S4 безусловно
        if (state == STATE3_REQUEST_FOR_ROUTING) {
            return STATE4_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL;
        }

        // Ако сме в STATE4
        if (state == STATE4_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL) {
            // NOT(VALID_DATA) AND NOT(TIME_ONE) AND TAIL_FLIT
            if (getCurrentFlitType() == Flit.FLIT_TYPE_TAIL) {
                Debug.printf("GOTO STATE 5!");
                return STATE5_READ_PACKET;
                // Timeout
            } else if (hasSignal(SignalArray.TIME_ONE)) {
                return STATE0_INIT;
            }
            return STATE4_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL;
        }

        // Ако сме в STATE5, може да започне да се чете пакет или вече се чете пакет, ако това е започнало в S4
        // TODO: да се разбере как се разбира дали е започнало предаването. Най-вероятно ще стане в tick()
        if (state == STATE5_READ_PACKET) {
            Debug.printf("GOT IN STATE 5!");
            return STATE6_WAIT_FOR_END_OF_PACKET;
        }

        if (state == STATE6_WAIT_FOR_END_OF_PACKET) {
            if (hasSignal(SignalArray.CNT_EQU) || hasSignal(SignalArray.TIME_TWO)) {
                return STATE0_INIT;
            }
        }
        return state;
    }


    @Override
    public void calculateNewState() {
        // Вземаме новото състояние
        state = calculateState();
        lowerEmSignalsHny();
    }

    @Override
    public void tick() {
        Flit head;
        //Debug.println(getWho() + " current state = " + state);
        //Debug.printSignals(Debug.CLASS_FIFO_QUEUE, this);
        if (hasSignal(SignalArray.TIMER_EN)) {
            timer++;

            /*
            TODO: timerMax се задава от модела, когато се инициализира. Трябва да се види къде ще набием TIME_ONE и TIME_TWO
            if(timer >= timerMax){
                getSignalArray().setSignal(SignalArray.TIME_ONE, true);
            }*/
        }


        switch (state) {
            case STATE0_INIT:
                getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                getSignalArray().setSignal(SignalArray.CLR_FIFO, true);
                getSignalArray().setSignal(SignalArray.RESET, false);
                fifo.clear();
                nextNodeId = -1;
                requestSent = false;
                break;
            case STATE1_IDLE:
                // Нищо. Or is it?
                getSignalArray().setSignal(SignalArray.FIFO_BUSY, false);
                /*getSignalArray().setSignal(SignalArray.FIFO_BUSY, false);
                getSignalArray().setSignal(SignalArray.CLR_FIFO, false);*/
                break;
            case STATE2_READY:
                getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                getSignalArray().setSignal(SignalArray.WR_FIFO_EN, true);
                getSignalArray().setSignal(SignalArray.PACK_WAIT, true);
                getChannel().getSignalArray().setSignal(SignalArray.PACK_WAIT, true);
                //getSignalArray().setSignal(SignalArray.WR_IN_FIFO, true);

                // Вземане на първата свободна опашка според B_FIFO_STATUS регистъра
                //activeFIFOIndex = getFirstAvailableQueueIndex();
                //assert activeFIFOIndex < 0 || activeFIFOIndex > fifoQueues.size() : "Invalid active queue index returned. Fuck you!";
                break;
            case STATE3_REQUEST_FOR_ROUTING:
                getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                getSignalArray().setSignal(SignalArray.WR_FIFO_EN, true);
                getSignalArray().setSignal(SignalArray.WR_IN_FIFO, true);

                // Вече има head флит и поне един във fifo
                //assert fifo.size() == 1 : "Тук трябва да има само един flit";
                assert fifo.peekFirst().getFlitType() == Flit.FLIT_TYPE_HEADER : "Този флит трябва да е Head.";
                head = fifo.peekFirst();
                requestSent = arby.sendRequestByTR(head.getTR());
                if (!requestSent) {
                    Debug.printf("Ay!");
                }
                break;
            case STATE4_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL:
                getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                getSignalArray().setSignal(SignalArray.WR_FIFO_EN, true);
                getSignalArray().setSignal(SignalArray.WR_IN_FIFO, true);
                getSignalArray().setSignal(SignalArray.TIMER_EN, true); //  TODO: да реализираме таймера. Тук се пуска таймер 1

                // possible sadface
                if (nextNodeId == -1) {
                    nextNodeId = arby.getNextNodeId(this);

                    if (nextNodeId == -1 && fifo.peekFirst().getTR() != 0) {
                        requestSent = arby.sendRequestByTR(fifo.peekFirst().getTR());
                    }
                } else {
                    sendDataToNextNode();
                }


                break;
            case STATE5_READ_PACKET: // TODO: изпращането да го изкараме в метод
                // TODO: Тези сигнали трябва да се издадат към предишния възел. Единственият проблем е, FIFO_BUSY не се използва. Вторият единствен проблем е, че вместо InputChannel-а да пише в сигналите на OutputChannel-а, той пише в своите и OutputChannel-а ги проверява.
                // TODO: Уж работи правилно.
                getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                getSignalArray().setSignal(SignalArray.DATA_ACK, true);
                getChannel().getSignalArray().setSignal(SignalArray.DATA_ACK, true);

                sendDataToNextNode();
                break;
            case STATE6_WAIT_FOR_END_OF_PACKET:
                getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                getSignalArray().setSignal(SignalArray.TIMER_EN, true); //  TODO: да реализираме таймера. Тук се пуска таймер 2

                sendDataToNextNode();
                break;
        }

        //getSignalArray().resetAll();
        //getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
        //Debug.printSignals(Debug.CLASS_FIFO_QUEUE, this);
        //Debug.printf("End of tick");
    }

    private void lowerEmSignalsHny() {
        switch (state) {
            case STATE0_INIT:
                getSignalArray().resetAll();
                break;
            case STATE1_IDLE:
                getSignalArray().setSignal(SignalArray.FIFO_BUSY, false);
                getSignalArray().setSignal(SignalArray.CLR_FIFO, false);
                break;
            case STATE3_REQUEST_FOR_ROUTING:
                getSignalArray().setSignal(SignalArray.PACK_WAIT, false);
                break;
            case STATE5_READ_PACKET:
                getSignalArray().setSignal(SignalArray.WR_IN_FIFO, false);
                getSignalArray().setSignal(SignalArray.WR_FIFO_EN, false);
                getSignalArray().setSignal(SignalArray.TIMER_EN, false);
                break;
            case STATE6_WAIT_FOR_END_OF_PACKET:
                getSignalArray().setSignal(SignalArray.DATA_ACK, false);
                getChannel().getSignalArray().setSignal(SignalArray.DATA_ACK, false);
                getSignalArray().setSignal(SignalArray.CNT_EQU, false);
                break;
        }
    }

    public void sendDataToNextNode() {
        if (fifo.isEmpty()) {
            return;
        }
        Flit nextFlit = fifo.peekFirst();
        //Debug
        if (nextFlit.getFlitType() == Flit.FLIT_TYPE_HEADER) {
            nextFlit.setTR(nextFlit.getDNA() ^ nextNodeId); // верен ред.
            if ((nextFlit.getTR()) == 0) {
                /*Debug.println("FINALLY! A header flit! FIFOQueue size: " + fifo.size());
                receivedData = new ArrayList<>();
                receivedData.add(nextFlit);*/
            }
        }

        if (nextFlit.getFlitType() == Flit.FLIT_TYPE_BODY) {
            //Debug.println("FINALLY! A body flit! FIFOQueue size: " + fifo.size());

        }

        // sadfase.dwg
        // Ако не е изпратен буферът на изходния канал, няма да пишем, защото се омазва
        if (nextNodeId != -1) {
            OutputChannel out = channel.getNode().getOutputChannel(nextNodeId);
            if (out.getBuffer() != null) {
                return;
            }
            Debug.println(getWho() + " Sending all my ropes to: " + channel.getNode().getOutputChannel(nextNodeId).getWho() + " c: " + nextFlit.toString());
            nextFlit.history.add(getWho() + " oc: " + channel.getNode().getOutputChannel(nextNodeId).getWho());
            channel.getNode().getOutputChannel(nextNodeId).setBuffer(nextFlit); // верен метод за изпращане.
        } else {
            nextFlit.history.add(getWho() + " I've reached my final destination. Time for masturbation");
        }

        if (nextFlit.getFlitType() == Flit.FLIT_TYPE_TAIL) {
            Debug.println(getWho() + "FINALLY! A tail flit! FIFOQueue size: " + fifo.size());
            getSignalArray().setSignal(SignalArray.CNT_EQU, true);
            Debug.printSignals(Debug.CLASS_FIFO_QUEUE, this);
        }
        fifo.removeFirst();
    }

    /**
     * Добавяне в опашката
     *
     * @param flit елемент.
     */
    public void push(Flit flit) {
        Debug.printSignals(Debug.CLASS_FIFO_QUEUE, this);
        if (flit.getFlitType() == Flit.FLIT_TYPE_HEADER && flit.getTR() == 0) {
            receivedData = new ArrayList<>();
            startedReceiving = true;
        }
        if (startedReceiving) {
            if (receivedData != null) {
                receivedData.add(flit);
            }
            if (flit.getFlitType() == Flit.FLIT_TYPE_TAIL) {
                Debug.printf("I am the final gay ninja. I have taken all the spunk packets. Here's the good stuff: %s", receivedData.toString());
                startedReceiving = false;
            }
        }
        fifo.addLast(flit);
    }

    /**
     * Махане от опашката
     */
    /*public void pop() {
        fifo.removeFirst();
    }*/

    /**
     * Извиква се в началото на вски такт
     * Определя състоянията на сигналите. Примерно. Де да знам.
     */
    public void updateSignals() {
        // TODO: CHAN_BUSY, FIFO_BUSY, WR_IN_FIFO
    }


    /////////////////////// Clutter

    public Queue<Flit> getFifo() {
        return fifo;
    }

    public Arbiter getArby() {
        return arby;
    }

    public void setArby(Arbiter arby) {
        this.arby = arby;
    }

    public int getCurrentFlitType() {
        //assert fifo.peek() != null : "This is not the flit you are looking for.";
        if (fifo.peekLast() != null) {
            String c = "";
            for (Flit flit : fifo) {
                c += flit.toString() + "\n";
            }

            Debug.printf("%s Flit type: %d, contents: %s", getWho(), fifo.peekLast().getFlitType(), c);
            return fifo.peekLast().getFlitType();
        }
        return -1;
    }

    public boolean isCurrentFlitDataValid() {
        //assert fifo.peek() != null : "This is not the flit you are looking for.";
        //.isDataValid();
        // TODO: да проверим дали наистина няма смисъл да се проверява бита за валидни данни.
        // Защото все пак не предаваме апаратно и няма как да има грешки при предаването на ниво бит.
        return fifo.size() > 0 && fifo.peekFirst() != null && fifo.peekFirst().isDataValid();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public InputChannel getChannel() {
        return channel;
    }

    public void setChannel(InputChannel channel) {
        this.channel = channel;
    }

    @Override
    public Map<String, String> getStatus() {
        HashMap<String, String> status = new HashMap<>();
        status.put("state", String.valueOf(state));
        status.put("arby_grants", arby.grantOutputChannelIdsToString());
        return status;
    }

    public String getWho() {
        return String.format("\t\tFIFOQueue {id: %d, nodeID: %d, channelID: %d, queue size: %d, state: %d", id, nodeId, channel.id, fifo.size(), state);
    }
}
