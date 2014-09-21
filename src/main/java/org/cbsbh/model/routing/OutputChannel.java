package org.cbsbh.model.routing;

import org.cbsbh.model.structures.SMP;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description goes here
 * Date: 6/1/14 7:44 PM
 *
 * @author Mihail Chilyashev
 */
public class OutputChannel {

    int currentRouterId;
    int nextNodeId;
    // ArbiterID => ChannelID
    // ChannelID => List of arbiter IDs
    HashMap<Integer, ArrayList<Integer>> requestQueue;

    int currentArbiterId;
    int grantToArbiterId;
    int currentChannelId;
    int grantToChannelId;

    int arbiterCount;

    boolean busy = false;
    int step;
    private boolean grantAckReceived;
    /**
     * Hardware-wise there is no way to transmit a whole flit for a single tick serially, so we just
     * ignore it for the model and <strong><i>assume</i></strong> the fixating buffer has no effect on the performance.
     */
    private long data;
    private boolean dataSent = false;
    Router router;
    InputChannel inputChannel;
    private int channelCount;

    public OutputChannel(int nextNodeId, int currentRouterId, int arbiterCount, int channelCount) {
        this.nextNodeId = nextNodeId;
        this.currentRouterId = currentRouterId;
        this.arbiterCount = arbiterCount;
        this.channelCount = channelCount;

        requestQueue = new HashMap<>();
        grantAckReceived = false;
        currentArbiterId = 0;
        currentChannelId = 0;
        dataSent = false;
        step = 0;
    }

    public boolean sendData() {
        SMP smp = MPPNetwork.get(nextNodeId);
        Router router = smp.getRouter();
        InputChannel in = router.getInputChannel(currentRouterId);
  /*      if(currentRouterId == 5 && nextNodeId == 4){
            System.err.println("brayk");
        }
        if(currentRouterId == 4 && nextNodeId == 6){
            System.err.println("brayk");
        }
        if(currentRouterId == 6 && nextNodeId == 14){
            System.err.println("brayk");
        }
*/
        return MPPNetwork.get(nextNodeId).getRouter().getInputChannel(currentRouterId).pushFlit(data);
        /*boolean pushFlit = in.pushFlit(data);
        return pushFlit;*/

    }


    public void tick() {

        switch (step) {
            case 0:
                if (!requestQueue.isEmpty()) { // There are requests.
                    grantToArbiterId = currentArbiterId;
                    grantToChannelId = currentChannelId;
//TODO: GrantToChannelID

                    for (; ; currentArbiterId++) {
                        if (currentArbiterId <= arbiterCount && requestQueue.containsKey(currentChannelId)) {
                            ArrayList<Integer> arbiters = requestQueue.get(currentChannelId);
                            if (arbiters.contains(currentArbiterId)) {
                                grantToArbiterId = currentArbiterId;

                                requestQueue.get(currentChannelId).remove(requestQueue.get(currentChannelId).indexOf(currentArbiterId));

                                if (requestQueue.get(currentChannelId).size() < 1) {
                                    requestQueue.remove(currentChannelId);
                                }
                                try {
                                    router = MPPNetwork.get(currentRouterId).getRouter();
                                    inputChannel = router.getInputChannel(currentChannelId);
                                    FIFOArbiter arbiter = inputChannel.getArbiter(currentArbiterId);
                                    arbiter.grant(nextNodeId);
                                    step++;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.err.println("goeba");
                                }
                                break;
                            }
                        } else {
                            currentArbiterId = -1;
                            if (currentChannelId == channelCount) {
                                currentChannelId = Integer.MAX_VALUE - 1;
                            } else if (currentChannelId == Integer.MAX_VALUE - 1) {
                                currentChannelId = /*currentChannelId < channelCount ? currentChannelId + 1 : */0;
                            } else {
                                currentChannelId++;
                            }
                        }
                    }


                    /*outer:
                    for (int i = currentChannelId; ; i = currentChannelId < channelCount ? currentChannelId + 1 : 0) {
                        if (requestQueue.containsKey(i)) {
                            ArrayList<Integer> arbiters = requestQueue.get(i);
                            for (int j = currentArbiterId; ; j = currentArbiterId < arbiterCount ? currentArbiterId + 1 : 0) {
                                if (arbiters.contains(j)) {
                                    int arbId = arbiters.get(arbiters.indexOf(j));
                                    if (arbId == currentArbiterId) {
                                        grantToArbiterId = arbId;

                                        grantToChannelId = currentChannelId;
                                        requestQueue.get(i).remove(j);
                                        if (requestQueue.get(i).size() < 1) {
                                            requestQueue.remove(i);
                                        }
                                        break outer;
                                    }
                                }
                            }
                        }
                        currentArbiterId = 0;
                    }

*/

                }
                break;
            case 1:
                if (grantAckReceived) {
                    currentArbiterId = grantToArbiterId;
                    step++;
                }
                break;
            case 2:
                if (sendData()) {
                    dataSent = true;
                    data = 0;
                }
                if (!isBusy()) {
                    step = 0;
                }

                break;
        }
    }

    public void grantAcknowledge() {
        busy = true;
        grantAckReceived = true;
    }

    public boolean putData(long data) {
        //System.err.println("put data");
        if (this.data != 0) {
            //System.out.println("Data not sent! You can't put your dirty data in me!");
            return false;
            //System.exit(-0xB00B);
        }
        //if (dataSent) {
        dataSent = false;
        this.data = data;
        return true;
        //}
        //return false;
    }

    public void releaseChannel() {
        busy = false;
        grantAckReceived = false;
    }

    public boolean requestToSend(int channelId, int arbiterId) {
        if (!busy) {
            if (requestQueue.get(channelId) != null) {
                requestQueue.get(channelId).add(arbiterId);
            } else {
                ArrayList<Integer> arbiters = new ArrayList<>();
                arbiters.add(arbiterId);
                requestQueue.put(channelId, arbiters);
            }
            return true;
        }
        return false;
    }

    public boolean isBusy() {
        return busy;
    }

    public int getNextNodeId() {
        return nextNodeId;
    }
}
