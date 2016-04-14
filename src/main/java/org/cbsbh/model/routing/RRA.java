package org.cbsbh.model.routing;

import org.cbsbh.Debug;

import java.util.Map;
import java.util.TreeMap;

/**
 * Round (Batman and) Robin логика.
 * Date: 3/16/15 11:40 AM
 *
 * @author Mihail Chilyashev
 */
public class RRA {
    int outputChannelId; // ID на канала, в който е този RRA

    /**
     * Идентификатори на арбитри (Arbiter), които са изпратили Request.
     */
    //ArrayList<Arbiter> requestQueue;
    TreeMap<Integer, TreeMap<Integer, Arbiter>> requestMap;


    int currentArbiterId;
    int currentChannelId;
    private boolean grantAckReceived;

    public RRA(int id) {
        outputChannelId = id;
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
        if (!valuesChanged) {
            // Тези редове правят завъртането на указателите. CurrentChannelId и CurrentArbiterID са указателя на RR логиката.
            TreeMap map = requestMap.firstEntry().getValue();
            Arbiter firstArbiter = (Arbiter) map.firstEntry().getValue();
            currentChannelId = firstArbiter.getChannelId();
            currentArbiterId = firstArbiter.getId();
        }
        requestMap.get(currentChannelId).get(currentArbiterId).takeGrant(outputChannelId);
    }

    public void init() {
        grantAckReceived = false;
        currentArbiterId = -1;
        currentChannelId = -1;
        requestMap = new TreeMap<>();
        requestMap.clear();
    }

    /**
     * Вика се не просто от някъде, а от арбитъра, за да се каже, че дадения арбитър иска да праща.
     *
     * @param arbiter дадения арбитър
     */
    public void requestToSend(Arbiter arbiter) {
        int channelId = arbiter.getChannelId();
        Debug.printf("requestToSend from Node(%d), arbiter(%d), channel(%d)", arbiter.getNodeId(), arbiter.getId(), arbiter.getChannelId());
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

    public void setGrantAckReceived(boolean grantAckReceived) {
        this.grantAckReceived = grantAckReceived;
    }

    public void removeRequest(Arbiter arbiter) {
        int channelId = arbiter.getChannelId();
        if(requestMap.get(channelId) != null) {
            requestMap.get(channelId).remove(arbiter.getId());
        }
    }
}
