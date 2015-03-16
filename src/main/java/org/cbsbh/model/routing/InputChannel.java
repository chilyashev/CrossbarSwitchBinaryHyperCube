package org.cbsbh.model.routing;

import org.cbsbh.context.Context;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.structures.InputSignalArray;
import org.cbsbh.model.structures.OutputSignalArray;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 6/1/14 7:34 PM
 *
 * @author Mihail Chilyashev
 */
public class InputChannel implements Tickable {

    ArrayList<FIFOQueue> fifoQueues;
    private int fifoQueueCount; // TODO: това е сложено, щото може да се наложи да се разбере броя на опашките. Ако се остави без него, няма да е яко.

    public InputChannel() {
        this.fifoQueueCount = Context.getInstance().getInteger("fifoQueueCount");
        fifoQueues = new ArrayList<>(fifoQueueCount);
    }

    /**
     * Масив от флагове. Всеки флаг отговаря на изходен сигнал.
     */
    OutputSignalArray outputSignalArray;

    /**
     * Масив от флагове. Всеки флаг отговаря на входен сигнал.
     */
    InputSignalArray inputSignalArray;

    private int B_FIFO_STATUS; // TODO: това що е тука? Не може ли да е в някой от signalArray-ите? Или не е такъв сигнал?

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
     * Издаване на сигнал(и) – към Signal Array на друг хардуерен елемент.
     *
     * @param index име на сигнала
     * @param val   стойност на сигнала (0/1, true/false)
     * @param ic    хардуерния елемент
     */
    public void sendSignal(int index, int val, InputChannel ic) {
        //ic.setInputSignal(index, val);
    }

    /**
     * Издаване на сигнал(и) – към Signal Array на друг хардуерен елемент.
     *
     * @param index име на сигнала
     * @param val   стойност на сигнала (0/1, true/false)
     * @param oc    хардуерния елемент
     */
    public void sendSignal(int index, int val, OutputChannel oc) {
        //ic.setInputSignal(index, val);
    }


    /**
     * Определяне на състоянието на автомата според текущите активни входни сигнали и издаване на изходни сигнали.
     */
    public int calculateState(){
        return 0xb00b5;
    }


    @Override
    public void tick() {
        int newState = calculateState();
        switch (newState){
            // doStuff
        }
    }
}
