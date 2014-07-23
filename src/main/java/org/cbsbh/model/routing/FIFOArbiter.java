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
    //These IDs are used to locate a specific arbiter, so that it could receive a GRANT from an output channel.
    private int arbiterId;
    private int channelId;
    private int nodeId;

    //Input queue for the arbiter. Contains one WHOLE packet.
    private FIFOBuff<Long> fifoBuff;

    private HashMap<Integer, OutputChannel> outputChannels;

    private ArrayList<Integer> grantQueue;

    private ArrayList<Integer> possibleNextChannels;

    private int step;
    private Long popped;
    private Packet packet;

    /**
     * @param arbiterId      logically it's the arbiter id
     * @param channelId      the channel this here arbiter belongs to
     * @param nodeId         id of current smp
     * @param outputChannels output channels.
     */
    public FIFOArbiter(int arbiterId, int channelId, int nodeId, HashMap<Integer, OutputChannel> outputChannels) {
        this.arbiterId = arbiterId;
        this.nodeId = nodeId;
        this.channelId = channelId;
        this.outputChannels = outputChannels;

        step = 0;

        packet = new Packet();
        fifoBuff = new FIFOBuff<>();
        grantQueue = new ArrayList<>();
        possibleNextChannels = new ArrayList<>();
    }

    public void tick() {
       /* if (this.channelId == Integer.MAX_VALUE - 1) {
           // System.err.println("DMA! Fuck.");
        }
        if(channelId == 5 && nodeId == 4 && fifoBuff.getItemCount() > 0){
            System.err.println("stop1");
        }
        if(channelId == 4 && nodeId == 6 && fifoBuff.getItemCount() > 0){
            System.err.println("stop10");
        }
        if(channelId == 6 && nodeId == 14 && fifoBuff.getItemCount() > 0){
            System.err.println("stop3");
        }*/
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


                if (packet.getTR() == 0) {
                    // TODO: Пакетът е за текущия възел => няма какво да се прави тук.
                    System.err.printf("Received! Boom! I am arbiter %d, living deep in channel %d, and my Router is %d\n", +this.arbiterId, this.channelId, this.nodeId);

                    //System.exit(0xb);

                }

                boolean grantSent = false;
                for (int i = 0; i < 12; i++) { // 12. Like the 12 bits in the header. Duh...
                    if ((tr & (1 << i)) == 1 << i) {
                        int outPutChannelId = nodeId ^ (1 << i);
                        OutputChannel outputChannel = outputChannels.get(Integer.valueOf(outPutChannelId));

                        try {
                            grantSent = grantSent || outputChannel.requestToSend(arbiterId, channelId);
                        } catch (NullPointerException e) {
                            System.err.println("fuck you _|_");
                        }
                    }
                }
                if (grantSent) {
                    step++;
                }
                break;
            case 1:
                if (!grantQueue.isEmpty()) {
                    outputChannels.get(grantQueue.get(0)).grantAcknowledge();
                    long oldtr = packet.getTR();
                    packet.setTR(packet.getDNA() ^ outputChannels.get(grantQueue.get(0)).getNextNodeId());
                    System.err.printf("old tr: %d, new tr: %d\n", oldtr, packet.getTR());
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
                        grantQueue.clear();//TODO: make sure this is correctly written kyp
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

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
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
