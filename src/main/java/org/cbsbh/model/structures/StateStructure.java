package org.cbsbh.model.structures;

/**
 * Description goes here
 * Date: 3/21/15 11:43 AM
 *
 * @author Mihail Chilyashev
 */
abstract public class StateStructure {

    protected int state;

    /**
     * Масив от флагове. Всеки флаг отговаря на изходен сигнал.
     */
    protected OutputSignalArray outputSignalArray;

    /**
     * Масив от флагове. Всеки флаг отговаря на входен сигнал.
     */
    protected InputSignalArray inputSignalArray;


    /**
     * Издаване на сигнал(и) – към Signal Array на друг хардуерен елемент.
     *
     * @param index име на сигнала
     * @param val   стойност на сигнала (0/1, true/false)
     * @param c     хардуерния елемент
     */
    public void sendSignal(int index, boolean val, StateStructure c) {
        c.getInputSignalArray().setSignal(index, val);
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean hasInputSignal(int index) {
        return inputSignalArray.hasSignal(index);
    }

    public boolean hasOutputSignal(int index) {
        return outputSignalArray.hasSignal(index);
    }

    /**
     * Обикаля всички сигнали и им прави AND
     * @param signals всички сигнали
     * @return резултат
     */
    public boolean hasInputSignalsAnd(int... signals) {
        assert signals.length > 0 : "You mockcongler, don't give me empty stuff!";
        boolean ret = inputSignalArray.hasSignal(signals[0]);
        if (signals.length > 1) {
            for (int signal : signals) {
                ret = ret && inputSignalArray.hasSignal(signal);
            }
        }
        return ret;
    }

    /**
     * Обикаля всички сигнали и им прави OR.
     * @param signals всички всички сигнали
     * @return резултат
     */
    public boolean hasInputSignalsOr(int... signals) {
        assert signals.length > 0 : "You mockcongler, don't give me empty stuff!";
        boolean ret = inputSignalArray.hasSignal(signals[0]);
        if (signals.length > 1) {
            for (int signal : signals) {
                ret = ret || inputSignalArray.hasSignal(signal);
            }
        }
        return ret;
    }

    /**
     * Обикаля всички сигнали и им прави AND
     * @param signals всички сигнали
     * @return резултат
     */
    public boolean hasOutputSignalsAnd(int... signals) {
        assert signals.length > 0 : "You mockcongler, don't give me empty stuff!";
        boolean ret = outputSignalArray.hasSignal(signals[0]);
        if (signals.length > 1) {
            for (int signal : signals) {
                ret = ret && outputSignalArray.hasSignal(signal);
            }
        }
        return ret;
    }

    /**
     * Обикаля всички сигнали и им прави OR.
     * @param signals всички всички сигнали
     * @return резултат
     */
    public boolean hasOutputSignalsOr(int... signals) {
        assert signals.length > 0 : "You mockcongler, don't give me empty stuff!";
        boolean ret = outputSignalArray.hasSignal(signals[0]);
        if (signals.length > 1) {
            for (int signal : signals) {
                ret = ret || outputSignalArray.hasSignal(signal);
            }
        }
        return ret;
    }
}
