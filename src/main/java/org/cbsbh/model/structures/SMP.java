package org.cbsbh.model.structures;

import org.cbsbh.model.routing.Router;

/**
 * An SMP computer.
 * Date: 6/5/14 6:15 PM
 *
 * @author Mihail Chilyashev
 */
public class SMP {
    int id;
    Router router;

    public SMP(int id, Router router) {
        this.id = id;
        this.router = router;
    }
}
