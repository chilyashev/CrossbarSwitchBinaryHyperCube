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
     * Масив от флагове. Всеки флаг отговаря на изходен, входен или вътрешен сигнал.
     */
    protected SignalArray signalArray;

    public StateStructure() {
        signalArray = new SignalArray();
        getSignalArray().setSignal(SignalArray.RESET, true);
    }

    /**
     * Издаване на сигнал(и) – към Signal Array на друг хардуерен елемент.
     *
     * @param index име на сигнала
     * @param val   стойност на сигнала (0/1, true/false)
     * @param c     хардуерния елемент
     */
    public void sendSignal(int index, boolean val, StateStructure c) {
        c.getSignalArray().setSignal(index, val);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    public boolean hasSignal(int index) {
        return signalArray.hasSignal(index);
    }

    /**
     * Обикаля всички сигнали и им прави AND
     *
     * @param signals всички сигнали
     * @return резултат
     */
    public boolean hasSignalsAnd(int... signals) {
        assert signals.length > 0 : "You mockcongler, don't give me empty stuff!";
        boolean ret = signalArray.hasSignal(signals[0]);
        if (signals.length > 1) {
            for (int signal : signals) {
                ret = ret && signalArray.hasSignal(signal);
            }
        }
        return ret;
    }

    /**
     * Обикаля всички сигнали и им прави OR.
     *
     * @param signals всички всички сигнали
     * @return резултат
     */
    public boolean hasSignalsOr(int... signals) {
        assert signals.length > 0 : "You mockcongler, don't give me empty stuff!";
        boolean ret = signalArray.hasSignal(signals[0]);
        if (signals.length > 1) {
            for (int signal : signals) {
                ret = ret || signalArray.hasSignal(signal);
            }
        }
        return ret;
    }

    public SignalArray getSignalArray() {
        return signalArray;
    }

    public void setSignalArray(SignalArray signalArray) {
        this.signalArray = signalArray;
    }

    public abstract void init();
}
