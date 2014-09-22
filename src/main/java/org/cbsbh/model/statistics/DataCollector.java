package org.cbsbh.model.statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Description goes here
 * Date: 9/22/14 11:01 AM
 *
 * @author Mihail Chilyashev
 */
public class DataCollector {
    private final static DataCollector instance = new DataCollector();
    FileWriter fw = null;

    /**
     * Holds the data needed to start the model.
     */
    private LinkedHashMap<String, Object> data = new LinkedHashMap<>();

    public static DataCollector getInstance() {
        return instance;
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }


    public Integer getInteger(String key) {
        return (Integer) data.get(key);
    }


    public void addToList(String key, Object ob) {
        if (data.get(key) == null) {
            data.put(key, new ArrayList<Object>());
        }
        ((ArrayList<Object>)data.get(key)).add(ob);
    }

    public void addToSum(String key, int num) {
        if (data.get(key) == null) {
            data.put(key, 0);
        }
        data.put(key, (int) data.get(key) + num);
    }

    public void addToSum(String key, float num) {
        if (data.get(key) == null) {
            data.put(key, 0);
        }
        data.put(key, (float) data.get(key) + num);
    }

    public void log(){
        try {
            FileWriter fw = new FileWriter("/tmp/logg");
            //for(Object o : data.keySet())
            fw.write(data.toString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
