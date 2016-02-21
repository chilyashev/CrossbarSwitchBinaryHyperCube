package org.cbsbh.model.routing;

import org.cbsbh.Debug;
import org.cbsbh.model.Tickable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

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
    //ArrayList<Arbiter> requestQueue;
    TreeMap<Integer, TreeMap<Integer, Arbiter>> requestMap;


    int currentRouterId;
    int nextNodeId;
    // ArbiterID => ChannelID
    // ChannelID => List of arbiter IDs
    //HashMap<Integer, ArrayList<Integer>> requestQueue;
    int currentArbiterId;
    int grantToArbiterId;
    int currentChannelId;
    int grantToChannelId;


    int arbiterCount;

    private boolean grantAckReceived;

    public RRA() {
        init();
    }

    /**
     * Изпращане на Grant.
     * Извършва се RR логиката. По изключително интелигентен начин.
     */
    public void sendGrant() {

        assert requestMap.size() > 0 : "Trying to send grants to an empty request map. You shouldn't do this. You bad boy, you.";

        boolean valuesChanged = false;
        for (Map.Entry<Integer, TreeMap<Integer, Arbiter>> integerTreeMapEntry : requestMap.entrySet()) {
            TreeMap map = integerTreeMapEntry.getValue();
            Arbiter firstArbiter = (Arbiter) map.firstEntry().getValue();
            if ((currentChannelId < firstArbiter.getChannelId())
                    || (currentChannelId == firstArbiter.getChannelId() && currentArbiterId < firstArbiter.getId())) {
                currentChannelId = firstArbiter.getChannelId();
                currentArbiterId = firstArbiter.getId();
                valuesChanged = true;
                break;
            }
        }

        /*
         * Ако след този цикъл указателите не са се променили, значи сочат към арбитър с комбинация idArbiter и idChannel по-големи от всички
         * арбитри, които са изпратили заявки.
         */
        if(!valuesChanged){
            TreeMap map = requestMap.firstEntry().getValue();
            Arbiter firstArbiter = (Arbiter) map.firstEntry().getValue();
            currentChannelId = firstArbiter.getChannelId();
            currentArbiterId = firstArbiter.getId();
        }

    }

    public void init() {
        grantAckReceived = false;
        currentArbiterId = -1;
        currentChannelId = -1;
        requestMap = new TreeMap<>();
        requestMap.clear();
    }

    public void etcetera() {
    }

    @Override
    public void tick() {
        // TODO: doStuff!
    }




    /**
     * Вика се не просто от някъде, а от арбитъра, за да се каже, че дадения арбитър иска да праща.
     *
     * @param arbiter дадения арбитър
     */
    public void requestToSend(Arbiter arbiter) {
        int channelId = arbiter.getChannelId();
        if (requestMap.get(channelId) == null) {
            requestMap.put(channelId, new TreeMap<Integer, Arbiter>());
        }
        requestMap.get(channelId).put(arbiter.getId(), arbiter);
    }

    public boolean hasRequests() {
        return requestMap.size() > 0;
    }

    public boolean isGrantAckReceived() {
        return grantAckReceived;
    }
}
