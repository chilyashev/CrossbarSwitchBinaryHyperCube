package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.flit.Flit;
import org.cbsbh.model.structures.InputSignalArray;
import org.cbsbh.model.structures.OutputSignalArray;
import org.cbsbh.model.structures.StateStructure;

import java.util.ArrayList;
import java.util.Queue;

/**
 * Проверката на сигнали се случва в *SignalArray класовете. hasSignal(index), например.
 * Date: 3/16/15 10:48 AM
 *
 * @author Mihail Chilyashev
 */
public class FIFOQueue extends StateStructure implements Tickable {
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
    Queue<Flit> fifo;

    /**
     * Грижи се за комуникацията с Output StateStructure.
     */
    Arbiter arby;


    /**
     * Входният канал, за който е това FIFO
     */
    InputChannel channel;


    int nextNodeId = -1;

    public FIFOQueue() {
        // TODO: големината на fifo трябва да е "размерът, указан в интерфейса - 2" заради Head/Tail флитовете
        int nodeId = channel.getNode().getId();
        ArrayList<OutputChannel> outputChannels = new ArrayList<>();
        outputChannels.addAll(MPPNetwork.get(nodeId).getOutputChannels().values());
        arby = new Arbiter(nodeId, outputChannels);
        setState(STATE0_INIT);
    }

    /**
     * Определяне на състоянието на автомата според текущите активни входни сигнали и издаване на изходни сигнали.
     */
    public int calculateState() {

        // TODO: освен RESET и INIT S0 трябва да се върне и при освобождаване на опашката и изтичане на таймаут.
        if (hasInputSignal(InputSignalArray.RESET) || inputSignalArray.hasSignal(InputSignalArray.INIT)) {
            return STATE0_INIT;
        }

        //  Безусловно се минава от S0 към S1
        if (state == STATE0_INIT) {
            return STATE1_IDLE;
        }

        // Ако се намираме в STATE1 и са налични сигналите FIFO_SELECT и DEMUX_RDY, преминаваме в S2
        if (state == STATE1_IDLE && hasInputSignalsAnd(InputSignalArray.FIFO_SELECT, InputSignalArray.DEMUX_RDY)) {
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
            if (!isCurrentFlitDataValid() && !hasInputSignal(InputSignalArray.TIME_ONE)
                    && getCurrentFlitType() == Flit.FLIT_TYPE_TAIL) {
                return STATE5_READ_PACKET;
                // Timeout
            } else if (hasInputSignal(InputSignalArray.TIME_ONE)) {
                return STATE0_INIT;
            }
            return STATE4_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL;
        }

        // Ако сме в STATE5, може да започне да се чете пакет или вече се чете пакет, ако това е започнало в S4
        // TODO: да се разбере как се разбира дали е започнало предаването. Най-вероятно ще стане в tick()
        if (state == STATE5_READ_PACKET) {
            return STATE6_WAIT_FOR_END_OF_PACKET;
        }

        if (state == STATE6_WAIT_FOR_END_OF_PACKET) {
            if (hasInputSignal(InputSignalArray.CNT_EQU) || hasInputSignal(InputSignalArray.TIME_TWO)) {
                return STATE0_INIT;
            }
        }
        return state;
    }


    @Override
    public void tick() {
        Flit head;
        if (hasOutputSignal(OutputSignalArray.TIMER_EN)) {
            timer++;

            /*
            TODO: timerMax се задава от модела, когато се инициализира. Трябва да се види къде ще набием TIME_ONE и TIME_TWO
            if(timer >= timerMax){
                getOutputSignalArray().setSignal(OutputSignalArray.TIME_ONE, true);
            }*/
        }


        // Вземаме новото състояние
        state = calculateState();

        switch (state) {
            case STATE0_INIT:
                getOutputSignalArray().setSignal(OutputSignalArray.FIFO_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.CLR_FIFO, true);
                fifo.clear();
                break;
            case STATE1_IDLE:
                // Нищо.
                break;
            case STATE2_READY:
                getOutputSignalArray().setSignal(OutputSignalArray.FIFO_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.WR_FIFO_EN, true);
                getOutputSignalArray().setSignal(OutputSignalArray.PACK_WAIT, true);

                // Вземане на първата свободна опашка според B_FIFO_STATUS регистъра
                //activeFIFOIndex = getFirstAvailableQueueIndex();
                //assert activeFIFOIndex < 0 || activeFIFOIndex > fifoQueues.size() : "Invalid active queue index returned. Fuck you!";
                break;
            case STATE3_REQUEST_FOR_ROUTING:
                getOutputSignalArray().setSignal(OutputSignalArray.FIFO_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.WR_FIFO_EN, true);
                getOutputSignalArray().setSignal(OutputSignalArray.WR_IN_FIFO, true);

                // TODO:
                // Вече има head флит и поне един във fifo
                assert fifo.size() == 1 : "Тук трябва да има само един flit";
                assert fifo.peek().getFlitType() == Flit.FLIT_TYPE_HEADER : "Този флит трябва да е Head.";
                head = fifo.peek(); // TODO: това дали трябва да се pop-ва
                arby.sendRequestByTR(head.getTR());
                break;
            case STATE4_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL:
                getOutputSignalArray().setSignal(OutputSignalArray.FIFO_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.WR_FIFO_EN, true);
                getOutputSignalArray().setSignal(OutputSignalArray.WR_IN_FIFO, true);
                getOutputSignalArray().setSignal(OutputSignalArray.TIMER_EN, true); //  TODO: да реализираме таймера. Тук се пуска таймер 1

                // TODO: тука да видим кога ще се "занулява" това nextNodeID
                if (nextNodeId == -1) {
                    nextNodeId = arby.getNextNodeId();
                } else {
                    sendDataToNextNode();
                }
                break;
            case STATE5_READ_PACKET: // TODO: изпращането да го изкараме в метод
                getOutputSignalArray().setSignal(OutputSignalArray.FIFO_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.DATA_ACK, true);

                sendDataToNextNode();
                break;
            case STATE6_WAIT_FOR_END_OF_PACKET:
                getOutputSignalArray().setSignal(OutputSignalArray.FIFO_BUSY, true);
                getOutputSignalArray().setSignal(OutputSignalArray.TIMER_EN, true); //  TODO: да реализираме таймера. Тук се пуска таймер 2

                sendDataToNextNode();
                break;
        }

    }

    public void sendDataToNextNode(){
        Flit nextFlit = fifo.remove();
        if(nextFlit.getFlitType() == Flit.FLIT_TYPE_HEADER){
            nextFlit.setTR(nextFlit.getDNA() ^ nextNodeId); // верен ред.
        }
        channel.getNode().getOutputChannel(nextNodeId).setBuffer(nextFlit.getFlitData());
    }

    /**
     * Добавяне в опашката
     *
     * @param flit елемент.
     */
    public void push(Flit flit) {
        fifo.add(flit);
    }

    /**
     * Махане от опашката
     */
    public void pop() {
        fifo.remove();
    }

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

    public void setFifo(Queue<Flit> fifo) {
        this.fifo = fifo;
    }

    public Arbiter getArby() {
        return arby;
    }

    public void setArby(Arbiter arby) {
        this.arby = arby;
    }

    public int getCurrentFlitType() {
        assert fifo.peek() != null : "This is not the flit you are looking for.";
        if (fifo.peek() != null) {
            return fifo.peek().getFlitType();
        }
        return -1;
    }

    public boolean isCurrentFlitDataValid() {
        assert fifo.peek() != null : "This is not the flit you are looking for.";
        return fifo.peek().isDataValid();
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
}
