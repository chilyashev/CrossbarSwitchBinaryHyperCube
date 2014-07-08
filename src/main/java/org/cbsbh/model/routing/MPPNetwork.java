package org.cbsbh.model.routing;

import org.cbsbh.model.structures.SMP;

import java.util.Collection;
import java.util.HashMap;

/**
 * Description goes here
 * Date: 6/1/14 8:23 PM
 *
 * @author Mihail Chilyashev
 */
public class MPPNetwork {
    private final static MPPNetwork instance = new MPPNetwork();

    public static MPPNetwork getInstance() {
        return instance;
    }


    // ID => SMP
    private static HashMap<Integer, SMP> smps  = new HashMap<>();

    public static void push(SMP smp) {
        smps.put(smp.getId(), smp);
    }

    public static SMP get(int smpId) {
        return smps.get(smpId);
    }

    public static Collection<SMP> getAll() {
        return smps.values();
    }
}
