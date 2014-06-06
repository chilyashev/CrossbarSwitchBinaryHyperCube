package org.cbsbh.model.routing;

import org.cbsbh.Main;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 6/1/14 7:44 PM
 *
 * @author Mihail Chilyashev
 */
public class OutputChannel {

    int nextRouterId;
    int currentRouterId;
    ArrayList<String> requestQueue;

    int currentInputChannelId;
    Integer[] inputChannelIds;


    /**
     * Hardware-wise there is no way to transmit a whole flit for a single tick serially, so we just
     * ignore it for the model and <strong><i>assume</i></strong> the fixating buffer has no effect on the performance.
     */
    private long data;
    private boolean dataSent = false;

    int step;

    public OutputChannel(int nextRouterId, int currentRouterId, Integer[] inputChannelIds) {
        this.nextRouterId = nextRouterId;
        this.currentRouterId = currentRouterId;
        this.inputChannelIds = inputChannelIds;

        currentInputChannelId = 0;
        dataSent = false;
        step = 0;
    }

    public boolean sendData(){
        return MPPNetwork.get(nextRouterId).getInputChannel(currentRouterId).pushFlit(data);
    }


    public void tick() {

        switch (step){
            case 0:
                if(!requestQueue.isEmpty()){
                    MPPNetwork.get(currentRouterId).getInputChannel(currentInputChannelId).getArbiter(requestQueue.get(currentInputChannelId)).grant();
                    step++;
                }
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }

        return;
        if (!dataSent) {
            dataSent = sendData();
        } else {
            data = 0;
        }
    }

    public boolean putData(long data) {
        if (this.data != 0) {
            System.err.println("Data not sent! You can't put your dirty data in me!");
            System.exit(-0xB00B);
        }
        if (dataSent) {
            this.data = data;
            return true;
        }
        return false;
    }

    public void acceptGrant() {
        this.busy = true;
    }

    public void releaseChannel() {
        this.busy = false;
    }

    public boolean isBusy() {
        return busy;
    }

    public int getId() {
        return targetInputChannelId;
    } //TODO: make understandable
}
