package org.cbsbh.model;

import java.util.HashMap;

/**
 * Description goes here
 * Date: 4/20/14 1:52 AM
 *
 * @author Mihail Chilyashev
 */
public class ModelController {

    public HashMap<String, Object> simulate(){
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("running_time", 55);
        return ret;
    }
}
