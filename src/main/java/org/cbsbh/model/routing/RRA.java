package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 3/16/15 11:40 AM
 *
 * @author Mihail Chilyashev
 */
public class RRA implements Tickable {
    /**
     * Идентификатори на арбитри (Arbiter), които са изпратили Request.
     */
    ArrayList<Arbiter> requestQueue;


    public RRA() {
        init();
    }

    /**
     * Изпращане на Grant
     */
    public void sendGrant() {
        int arbiterId = this.doRRLogic();
    }

    /**
     * Изпълнява RR алгоритъма и връща id на арбитъра, на който ще се прати Grant.
     * Also, това избира арбитър.
     *
     * @return id на арбитъра, на който ще се прати Grant
     */
    private int doRRLogic() {
        // TODO: RRA algorithm
        return 0xbabe;
    }

    public void init() {
        throw new NotImplementedException();
    }

    public void etc() {
        // etc
    }

    @Override
    public void tick() {
        // TODO: doStuff!
    }


    /**
     * Вика се не просто от някъде, а от арбитъра, за да се каже, че дадения арбитър иска да праща.
     * @param arbiter дадения арбитър
     */
    public void requestToSend(Arbiter arbiter) {
        requestQueue.add(arbiter);
    }
}
