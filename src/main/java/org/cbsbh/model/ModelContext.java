package org.cbsbh.model;

import java.util.HashMap;

/**
 * @author Mihail Chilyashev
 */
public class ModelContext {
    private final static ModelContext instance = new ModelContext();

    /**
     * Holds the data needed to start the model.
     */
    private HashMap<Integer, HashMap<String, Object>> data = new HashMap<>();

    public static ModelContext getInstance() {
        return instance;
    }

    public void set(int routerId, String key, Object value) {
        HashMap<String, Object> routerData = data.get(routerId);
        if(routerData == null){
            data.put(routerId, new HashMap<String, Object>());
            routerData = data.get(routerId);
        }
        routerData.put(key, value);
    }

    public Object get(int routerId, String key) {
        return data.get(routerId).get(key);
    }


    public Integer getInteger(int routerId, String key) {
        return (Integer)data.get(routerId).get(key);
    }
}

