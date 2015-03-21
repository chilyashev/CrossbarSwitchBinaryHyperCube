package org.cbsbh.model.structures;

import java.util.Arrays;

/**
 * Description goes here
 * Date: 3/16/15 11:34 AM
 *
 * @author Mihail Chilyashev
 */
public class SignalArray {
    boolean signals[];

    public SignalArray() {
        signals = new boolean[40]; // TODO: това да се направи достатъчно голямо, когато се разбере колко сигнала има.
        Arrays.fill(signals, false);
    }

    public void setSignal(int index, boolean value) {
        assert index > 0 && index < signals.length : "this index doesn't exist. Sucker.";
        signals[index] = value;
    }

    public boolean hasSignal(int index){
        assert index > 0 && index < signals.length : "this index doesn't exist. Sucker.";
        return signals[index];
    }

}
