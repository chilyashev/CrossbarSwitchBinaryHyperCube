package org.cbsbh.model;

import java.util.HashMap;

/**
 * Description goes here
 * Date: 4/20/14 1:52 AM
 *
 * @author Mihail Chilyashev
 */
public class ModelController {

    /**
     *
     * @return The results from the simulation. Including, but not limited to:
     *  <ul>
     *      <li>running time</li>
     *      <li>Other data</li>
     *  </ul>
     */
    public HashMap<String, Object> simulate(){
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("running_time", 0xB00B1E5);

        /*ModelRunner runner = new ModelRunner(Context.getInstance());
        Thread modelThread = new Thread(runner);
        modelThread.start();
*/
        return ret;
    }

}
