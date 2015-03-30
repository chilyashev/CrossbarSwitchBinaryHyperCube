package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.flit.Flit;
import org.cbsbh.model.routing.packet.flit.HeadFlit;
import org.cbsbh.model.routing.packet.flit.TailFlit;
import org.cbsbh.model.structures.InputSignalArray;
import org.cbsbh.model.structures.OutputSignalArray;
import org.cbsbh.model.structures.StateStructure;

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


    /**
     * баш флит
     */
    Queue<Flit> fifo;

    /**
     * Грижи се за комуникацията с Output StateStructure.
     */
    Arbiter arby;

    public FIFOQueue() {
        // TODO: големината на fifo трябва да е "размерът, указан в интерфейса - 2" заради Head/Tail флитовете
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
        return 0xb00b5;
    }


    @Override
    public void tick() {
        // Действия, които се извършват преди да се изчисли новото състояние.
        switch (state) {
            case STATE2_READY:
                // Вземане на първата свободна опашка според B_FIFO_STATUS регистъра
                //activeFIFOIndex = getFirstAvailableQueueIndex();
                //assert activeFIFOIndex < 0 || activeFIFOIndex > fifoQueues.size() : "Invalid active queue index returned. Fuck you!";
                break;
        }

        ////////////////////////////////////////////////////////////////////////////////

        // Вземаме новото състояние
        state = calculateState();
        switch (state) {
            case STATE0_INIT:
                state = STATE1_IDLE;
                break;
            case STATE2_READY:

                break;
        }
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
}
