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

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }
}
