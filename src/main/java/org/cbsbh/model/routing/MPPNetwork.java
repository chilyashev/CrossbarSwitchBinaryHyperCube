package org.cbsbh.model.routing;

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


    // ID => Router
    private static HashMap<Integer, Router> routers;

    public static void push(Router router) {
        routers.put(router.getNodeId(), router);
    }

    public static Router get(int routerId) {
        return routers.get(routerId);
    }
}
