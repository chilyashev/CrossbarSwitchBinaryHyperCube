package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.structures.FIFOBuff;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Description goes here
 * Date: 6/1/14 7:35 PM
 *
 * @author Mihail Chilyashev
 */
public class FIFOArbiter implements Tickable {
    private String arbiterId;
    private HashMap<Integer, OutputChannel> outputChannels;

    private int currentOutputChannel;
    private Integer[] outputChannelIds;

    // Old crap:
    int routerId;

    private FIFOBuff<Long> fifoBuff;
    private int chosenChannelId;

    int step;
    private boolean grantReceived;

    public FIFOArbiter(String arbiterId, HashMap<Integer, OutputChannel> outputChannels) {
        this.arbiterId = arbiterId;
        this.outputChannels = outputChannels;

        currentOutputChannel = 0;
        outputChannelIds = (Integer[])outputChannels.keySet().toArray();
        Arrays.sort(outputChannelIds);

        step = 0;
        grantReceived = false;

        fifoBuff = new FIFOBuff<>();
    }

    public void tick(){

        switch (step){
            case 0:

                break;
            case 1:
                if(grantReceived){
                    step++;
                }
                break;
            case 2:
                break;
            case 3:
                break;
            default:
                break;
        }

    }

    /**public void tick() {

        switch (step) {
            case 0: // Pop + get suitable output channels
                popped = fifoBuff.pop();
                if (popped == null) {
                    return;
                }
                p.setHeader_1(popped);
                dna = p.getDNA(); // destination
                xor = dna ^ routerId;
                break;
            case 1: // Request
                for (int i = 0; i < channelCount; i++) { //TODO: Arbiter SHOULD already know what his output channels are!
                    if ((xor & (1 << i)) == 1) {
                        int nextRouterId = routerId ^ 1 << i;
                        channels.add(nextRouterId);
                    }
                    xor >>= 1;
                }
                break;
            case 2: // Grant
                for (Integer channel : channels) {
                    if (!OutputChannelCollection.get(channel, routerId).isBusy()) {
                        chosenChannelId = OutputChannelCollection.get(channel, routerId).getId();
                        break;
                    }
                }
                break;
            case 3: // Accept
                OutputChannelCollection.get(chosenChannelId, routerId).acceptGrant();
                break;
            default:
                if (OutputChannelCollection.get(chosenChannelId, routerId).putData(popped)) {
                    popped = fifoBuff.pop();
                    if (popped == null) {
                        step = 0;
                        OutputChannelCollection.get(chosenChannelId, routerId).releaseChannel();
                    }
                }
        }
        step++;
    }*/


    public FIFOBuff<Long> getFifoBuff() {
        return fifoBuff;
    }

    public boolean isBusy() {
        return fifoBuff.getItemCount() > 0;
    }

    public long getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }


    public String getArbiterId() {
        return arbiterId;
    }

    public void setArbiterId(String arbiterId) {
        this.arbiterId = arbiterId;
    }

    public void grant() {
        grantReceived = true;
    }
}
