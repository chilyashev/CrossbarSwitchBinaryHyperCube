package org.cbsbh.model.routing;

/**
 * Description goes here
 * Date: 6/1/14 7:44 PM
 *
 * @author Mihail Chilyashev
 */
public class OutputChannel {

    /**
     * Hardware-wise there is no way to transmit a whole flit for a single tick serially, so we just
     * ignore it for the model and <strong><i>assume</i></strong> the fixating buffer has no effect on the performance.
     */
    private long data;
    private boolean busy;
    private boolean dataSent = false;

    private int targetRouterId;
    private int targetInputChannelId;

    public OutputChannel(int targetRouterId, int targetInputChannelId) {
        this.targetRouterId = targetRouterId;
        this.targetInputChannelId = targetInputChannelId;
    }

    public void tick() {
        if (!dataSent) {
            dataSent = InputChannelCollection.get(targetRouterId, targetInputChannelId).pushFlit(data);
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
