package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.structures.InputSignalArray;
import org.cbsbh.model.structures.OutputSignalArray;

/**
 * Description goes here
 * Date: 6/1/14 7:44 PM
 *
 * @author Mihail Chilyashev
 */
public class OutputChannel implements Tickable {

    RRA rra;

    /**
     * ID на възела, към който води този изходен канал
     */
    int nextNodeId;

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
    public int calculateState() {
        return 0xb00b5;
    }


    @Override
    public void tick() {
        int newState = calculateState();
        switch (newState) {
            // doStuff
        }
    }


    public RRA getRra() {
        return rra;
    }

    public void setRra(RRA rra) {
        this.rra = rra;
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

    public int getNextNodeId() {
        return nextNodeId;
    }

    public void setNextNodeId(int nextNodeId) {
        this.nextNodeId = nextNodeId;
    }
}
