package org.cbsbh.context;

import java.util.HashMap;

/**
 * Date: 4/1/14 10:15 AM
 *
 * @author Mihail Chilyashev
 */
public class SignalContext {
    private final static SignalContext instance = new SignalContext();

    private HashMap<Integer, HashMap<Integer, Boolean>> signals = new HashMap<>();

    public static SignalContext getInstance() {
        return instance;
    }

    public void set(Integer nodeId, HashMap<Integer, Boolean> value) {
        signals.put(nodeId, value);
    }

    public HashMap<Integer, Boolean> get(Integer nodeId) {
        return signals.get(nodeId);
    }


    public boolean hasSignal(Integer nodeId, int signal) {
        if(signals.get(nodeId) == null){
            return false;
        }
        return signals.get(nodeId).get(signal);
    }
}
