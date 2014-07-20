package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.Packet;
import org.cbsbh.model.structures.FIFOBuff;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description goes here
 * Date: 6/1/14 7:35 PM
 *
 * @author Mihail Chilyashev
 */
public class FIFOArbiter implements Tickable {
    private int arbiterId;
    private HashMap<Integer, OutputChannel> outputChannels;
    private ArrayList<Integer> grantQueue;

    private ArrayList<Integer> possibleNextChannels;

    // Old crap:
    int routerId;

    private FIFOBuff<Long> fifoBuff;

    int step;

    private Long popped;
    private Packet packet;
    private int channelId;
    private String iama = "nope.";

    /**
     * @param arbiterId      logically it's the arbiter id
     * @param channelId      the channel this here arbiter belongs to
     * @param outputChannels output channels.
     */
    public FIFOArbiter(int arbiterId, int channelId, HashMap<Integer, OutputChannel> outputChannels) {
        this.arbiterId = arbiterId;
        this.outputChannels = outputChannels;
        this.channelId = channelId;

        step = 0;


        packet = new Packet();
        fifoBuff = new FIFOBuff<>();
        grantQueue = new ArrayList<>();
        possibleNextChannels = new ArrayList<>();
    }

    public void tick() {
        if (this.channelId == Integer.MAX_VALUE - 1) {
           // System.err.println("DMA! Fuck.");
        }
        if(channelId == 5 && routerId == 4 && fifoBuff.getItemCount() > 0){
            System.err.println("stop1");
        }
        if(channelId == 4 && routerId == 6 && fifoBuff.getItemCount() > 0){
            System.err.println("stop10");
        }
        if(channelId == 6 && routerId == 14 && fifoBuff.getItemCount() > 0){
            System.err.println("stop3");
        }
        switch (step) {
            case 0:
                popped = fifoBuff.pop();
                if (popped == null || popped == 0) {
                    return;
                }
             //   if(packet.getHeader_1() == 0){
                    packet.setHeader_1(popped);
            //    }
                long tr = packet.getTR();
                long dna = packet.getDNA();

                boolean grantSent = false;
                for (int i = 0; i < 12; i++) { // 12. Like the 12 bits in the header. Duh...
                    if ((tr & 1 << i) == 1) {
                        int outPutChannelId = routerId ^ (1 << i);
                        OutputChannel outputChannel = outputChannels.get(Integer.valueOf(outPutChannelId));

                        grantSent = grantSent || outputChannel.requestToSend(arbiterId, channelId);
                    }
                }
                if (grantSent) {
                    step++;
                }
                break;
            case 1:
                if (!grantQueue.isEmpty()) {
                    outputChannels.get(grantQueue.get(0)).grantAcknowledge();
                    packet.setTR(packet.getDNA() ^ outputChannels.get(grantQueue.get(0)).getNextNodeId());
                    if (packet.getTR() == 0) {
                        // TODO: Пакетът е за текущия възел => няма какво да се прави тук.
                        System.err.printf("Received! Boom! I am arbiter %d, living deep in channel %d, and my Router is %d\n", + this.arbiterId, this.channelId, this.routerId);
                    }
                    popped = packet.getHeader_1();
                    step++;
                }
                break;
            case 2:
                if (outputChannels.get(grantQueue.get(0)).putData(popped)) {
                    popped = fifoBuff.pop();
                    if (popped == null) {
                        step = 0;
                        outputChannels.get(grantQueue.get(0)).releaseChannel();
                        break;
                    }

                }
                break;
        }
    }


    public FIFOBuff<Long> getFifoBuff() {
        return fifoBuff;
    }

    public boolean isBusy() {
        return fifoBuff.isFull();
    }

    public long getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }


    public int getArbiterId() {
        return arbiterId;
    }

    public void setArbiterId(int arbiterId) {
        this.arbiterId = arbiterId;
    }

    public void grant(int outputChannelId) {
        grantQueue.add(outputChannelId);
    }
}
