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

    public OutputChannel(int nextNodeId, int currentRouterId, int arbiterCount) {
        this.nextNodeId = nextNodeId;
        this.currentRouterId = currentRouterId;
        this.arbiterCount = arbiterCount;

        requestQueue = new HashMap<>();
        grantAckReceived = false;
        currentArbiterId = 0;
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
        //return MPPNetwork.get(nextNodeId).getRouter().getInputChannel(currentRouterId).pushFlit(data);
        boolean pushFlit = in.pushFlit(data);
        return pushFlit;

    }


    public void tick() {

        switch (step) {
            case 0:
                int grantToChannel = -1;
                if (!requestQueue.isEmpty()) { // There are requests.
                    grantToArbiterId = currentArbiterId;
//TODO: GrantToChannelID

                    outer:for(int i = currentArbiterId;;i = grantToArbiterId < arbiterCount ? i + 1 : 0){
                        for(Integer channelId: requestQueue.keySet()){
                            ArrayList<Integer> arbiters = requestQueue.get(channelId);
                            for(Integer arbiterId : arbiters){
                                if (i == arbiterId) {
                                    grantToArbiterId = arbiterId;
                                    grantToChannel = channelId;
                                    break outer;
                                }
                            }
//                        grantToArbiterId = grantToArbiterId < arbiterCount ? grantToArbiterId + 1 : 0;
                        }
                    }

                    //////////////////////
                    outer:for(int i = 0; i < arbiterCount; i++){
                        for (int arbiterId : requestQueue.keySet()) {
                            if (grantToArbiterId == arbiterId) {
                                break outer;
                            }
                        }
                        grantToArbiterId = grantToArbiterId < arbiterCount ? grantToArbiterId + 1 : 0;
                    }

                    try{
                        router = MPPNetwork.get(currentRouterId).getRouter();
                        inputChannel = router.getInputChannel(requestQueue.get(grantToArbiterId));
                        FIFOArbiter arbiter = inputChannel.getArbiter(grantToArbiterId);
                        arbiter.grant(nextNodeId);
                        step++;
                    }catch (Exception e){
                        e.printStackTrace();
                        System.err.println("goeba");
                    }
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

    public void grantAcknowledge(){
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
        if(!busy){
            if(requestQueue.get(channelId) != null){
                requestQueue.get(channelId).add(arbiterId);
            }else{
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
