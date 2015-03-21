package org.cbsbh.model.routing;

import org.cbsbh.context.Context;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.structures.Channel;
import org.cbsbh.model.structures.InputSignalArray;
import org.cbsbh.model.structures.OutputSignalArray;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 6/1/14 7:34 PM
 *
 * @author Mihail Chilyashev
 */
public class InputChannel extends Channel implements Tickable {


    /**
     * Начално състояние. По него се влиза след RESET, след INIT,
     * след освобождаване на опашката от вече приет и прочетен пакет пакет или след изтекъл timeout.
     */
    public static final int STATE_INIT = 0; // S0

    public static final int STATE_IDLE = 1; // S1

    public static final int STATE_READY = 2; // S2

    public static final int STATE_REQUEST_FOR_ROUTING = 3; // S3

    public static final int STATE_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL = 4; // S4

    public static final int STATE_READ_PACKET = 5; // S5

    public static final int STATE_WAIT_FOR_END_OF_PACKET = 6; // S6


    ArrayList<FIFOQueue> fifoQueues;

    // Индексът на активната опашка
    private int activeFIFOIndex;

    private int fifoQueueCount; // TODO: това е сложено, щото може да се наложи да се разбере броя на опашките. Ако се остави без него, няма да е яко.

    private boolean[] B_FIFO_STATUS; // TODO: това що е тука? Не може ли да е в някой от signalArray-ите? Или не е такъв сигнал?


    public InputChannel() {
        this.fifoQueueCount = Context.getInstance().getInteger("fifoQueueCount");
        fifoQueues = new ArrayList<>(fifoQueueCount);

        setState(STATE_INIT);
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
        // TODO: освен RESET и INIT S0 трябва да се върне и при освобождаване на опашката и изтичане на таймаут.
        if (hasInputSignal(InputSignalArray.RESET) || inputSignalArray.hasSignal(InputSignalArray.INIT)) {
            return STATE_INIT;
        }

        //  Безусловно се минава от S0 към S1
        if (state == STATE_INIT) {
            return STATE_IDLE;
        }

        // Ако се намираме в STATE1 и са налични сигналите FIFO_SELECT и DEMUX_RDY, преминаваме в S2
        if (state == STATE_IDLE && hasInputSignalsAnd(InputSignalArray.FIFO_SELECT, InputSignalArray.DEMUX_RDY)) {
            return STATE_READY;
        }

        // Ако сме в STATE2 и са налични сигналите VALID_DATA и HEAD_FLIT, преминаваме в S3
        if (state == STATE_READY && hasInputSignal(InputSignalArray.VALID_DATA) && fifoQueues.get(activeFIFOIndex).head != null) {
            return STATE_REQUEST_FOR_ROUTING;
        }

        // Ако сме в STATE3, преминаваме в S4 безусловно
        if (state == STATE_REQUEST_FOR_ROUTING) {
            return STATE_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL;
        }

        // Ако сме в STATE4
        if (state == STATE_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL) {
            // NOT(VALID_DATA) AND NOT(TIME_ONE) AND TAIL_FLIT
            if (!hasInputSignal(InputSignalArray.VALID_DATA) && !hasInputSignal(InputSignalArray.TIME_ONE) && fifoQueues.get(activeFIFOIndex).tail != null) {
                return STATE_READ_PACKET;
            // Timeout
            } else if (hasInputSignal(InputSignalArray.TIME_ONE)) {
                return STATE_INIT;
            }
            return STATE_WRITE_PACKET_AND_WAIT_FOR_OUTPUT_CHANNEL;
        }

        // Ако сме в STATE5, може да започне да се чете пакет или вече се чете пакет, ако това е започнало в S4
        // TODO: да се разбере как се разбира дали е започнало предаването. Най-вероятно ще стане в tick()
        if(state == STATE_READ_PACKET){
            return STATE_WAIT_FOR_END_OF_PACKET;
        }
        return 0xb00b5;
    }


    @Override
    public void tick() {

        // Действия, които се извършват преди да се изчисли новото състояние.
        switch (state) {
            case STATE_READY:
                // Вземане на първата свободна опашка според B_FIFO_STATUS регистъра
                activeFIFOIndex = getFirstAvailableQueueIndex();
                assert activeFIFOIndex < 0 || activeFIFOIndex > fifoQueues.size() : "Invalid active queue index returned. Fuck you!";
                break;
        }

        ////////////////////////////////////////////////////////////////////////////////

        // Вземаме новото състояние
        state = calculateState();
        switch (state) {
            case STATE_INIT:
                state = STATE_IDLE;
                break;
            case STATE_READY:

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
}
