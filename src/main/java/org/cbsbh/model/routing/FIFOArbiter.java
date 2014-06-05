package org.cbsbh.model.routing;

import org.cbsbh.context.Context;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.Packet;
import org.cbsbh.model.structures.FIFOBuff;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 6/1/14 7:35 PM
 *
 * @author Mihail Chilyashev
 */
public class FIFOArbiter implements Tickable {
    long channelId;
    int poppedCount;
    int channelCount;
    int step;
    private FIFOBuff<Long> fifoBuff;

    boolean req_sent, grant_received, grant_ack;

    private ArrayList<OutputChannel> channels;
    private long chosenChannelId;

    Long popped;
    long dna = 0;
    long xor = 0;
    Packet p = new Packet();


    public FIFOArbiter(long channelId) {
        poppedCount = 0;
        req_sent = false;
        grant_ack = false;
        grant_received = false;
        channelCount = Integer.parseInt((String) Context.getInstance().get("channelCount"));
        channels = new ArrayList<OutputChannel>();
        fifoBuff = new FIFOBuff<>();
        popped = 0l;
        step = 0;
    }


    public void tick() {

        switch (step) {
            case 0: // Pop + get suitable output channels
                popped = fifoBuff.pop();
                if (popped == null) {
                    return;
                }
                p.setHeader_1(popped);
                dna = p.getDNA(); // destination
                xor = dna ^ channelId;
                break;
            case 1: // Request
                for (int i = 0; i < channelCount; i++) {
                    if ((xor & 0x01) == 1) {
                        channels.add(OutputChannelCollection.get(channelId | i));
                    }
                    xor >>= 1;
                }
                break;
            case 2: // Grant
                for (OutputChannel channel : channels) {
                    if (!channel.isBusy()) {
                        chosenChannelId = channel.getId();
                        break;
                    }
                }
                break;
            case 3: // Accept
                OutputChannelCollection.get(chosenChannelId).setBusy(true);
                break;
            default:
                if (OutputChannelCollection.get(chosenChannelId).putData(popped)) {
                    popped = fifoBuff.pop();
                    if (popped == null) {
                        step = 0;
                    }
                }
        }
        step++;
    }


    public FIFOBuff<Long> getFifoBuff() {
        return fifoBuff;
    }

    public boolean isBusy() {
        return fifoBuff.getItemCount() > 0;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
}
