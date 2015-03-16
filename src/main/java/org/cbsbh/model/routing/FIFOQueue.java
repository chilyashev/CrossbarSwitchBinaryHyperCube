package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.Flit;
import org.cbsbh.model.structures.InputSignalArray;
import org.cbsbh.model.structures.OutputSignalArray;

import java.util.Queue;

/**
 * Проверката на сигнали се случва в *SignalArray класовете. getSignal(index), например.
 * Date: 3/16/15 10:48 AM
 *
 * @author Mihail Chilyashev
 */
public class FIFOQueue implements Tickable {
    /**
     *
     */
    Queue<Flit> fifo;

    /**
     * Грижи се за комуникацията с Output Channel.
     */
    Arbiter arby;

    /**
     * Масив от флагове. Всеки флаг отговаря на изходен сигнал.
     */
    OutputSignalArray outputSignalArray;

    /**
     * Масив от флагове. Всеки флаг отговаря на входен сигнал.
     */
    InputSignalArray inputSignalArray;


    /**
     * Издаване на сигнал(и) – към Signal Array на друг хардуерен елемент.
     * @param index име на сигнала
     * @param val стойност на сигнала (0/1, true/false)
     * @param ic хардуерния елемент
     */
    public void sendSignal(int index, int val, InputChannel ic){
            //ic.setInputSignal(index, val);
    }

    /**
     * Издаване на сигнал(и) – към Signal Array на друг хардуерен елемент.
     * @param index име на сигнала
     * @param val стойност на сигнала (0/1, true/false)
     * @param oc хардуерния елемент
     */
    public void sendSignal(int index, int val, OutputChannel oc){
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

    /**
     * Добавяне в опашката
     * @param flit елемент.
     */
    public void push(Flit flit){
        fifo.add(flit);
    }

    /**
     * Махане от опашката
     */
    public void pop(){
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

    public OutputSignalArray getOutputSignalArray() {
        return outputSignalArray;
    }

    public void setOutputSignalArray(OutputSignalArray outputSignalArray) {
        this.outputSignalArray = outputSignalArray;
    }

    public InputSignalArray getInputSignalArray() {
        return inputSignalArray;
    }

    public void setInputSignalArray(InputSignalArray inputSignalArray) {
        this.inputSignalArray = inputSignalArray;
    }
}
