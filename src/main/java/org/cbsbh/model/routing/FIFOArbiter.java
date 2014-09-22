package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.Packet;
import org.cbsbh.model.statistics.DataCollector;
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

    private FIFOBuff<Long> receivedData; // Временно решение за тестване на приетите данни.
    private boolean receivingData = false;

    public int receivedPacketCount = 0;

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
        receivedData = new FIFOBuff<>();
    }

    public void tick() {
        /*if (this.channelId == Integer.MAX_VALUE - 1) {
            // System.err.println("DMA! Fuck.");
        }*/
        // Временен код за тестване на приемането
        if (receivingData) {
            // Yeah, baby! austinpowers.jpg
            popped = fifoBuff.pop();
            if (popped == null) {
                receivingData = false;
                System.err.println("Receiving completed. Received data: ");
                if (receivedData.toList().size() < 4) {
                    System.err.println("sadface");
                    System.exit(-9);
                }
                for (Long l : receivedData.toList()) {
                    System.err.println("" + l);
                }
                receivedPacketCount++;
                DataCollector.getInstance().addToSum(String.format("packet_%d_%d_%d_for_%d_rcvd", nodeId, channelId, arbiterId, packet.getDNA()), 1);
                DataCollector.getInstance().addToSum(String.format("router_%d", nodeId), 1);
                System.err.printf("eo received crap, Router %d gpt %d packets.\n", nodeId, receivedPacketCount);
                return;
            }
            receivedData.push(popped);
            return;
        }

        if(channelId == 8 && nodeId == 0 && fifoBuff.getItemCount() > 0){
            System.err.println("stop10");
        }/*//* /
        if(channelId == 6 && nodeId == 14 && fifoBuff.getItemCount() > 0){
            System.err.println("stop3");
        }//*/
        switch (step) {
            case 0:

                /*
                peek(), защото може да се случи така, че в текущия такт няма grant и в следващия да се pop-не нещо неправилно.
                 */
                popped = fifoBuff.peek();
                if (popped == null || !fifoBuff.isFull()) {
                    return;
                }

                packet.setHeader_1(popped);

                long tr = packet.getTR();
                long dna = packet.getDNA();


                if (packet.getDNA() == nodeId) {
                    // TODO: Пакетът е за текущия възел => няма какво да се прави тук.
                    receivingData = true;
                    break;
                }

                boolean requestSent = false;
                for (int i = 0; i < 12; i++) { // 12. Like the 12 bits in the header. Duh...
                    if ((tr & (1 << i)) == 1 << i) {
                        int outPutChannelId = nodeId ^ (1 << i);
                        OutputChannel outputChannel = outputChannels.get(Integer.valueOf(outPutChannelId));

                        requestSent = requestSent || outputChannel.requestToSend(channelId, arbiterId);
                    }
                }

                if (requestSent) {
                    step++;
                    fifoBuff.pop();
                }else{
                    DataCollector.getInstance().addToSum(String.format("router_%d_step_%d_ticks", nodeId, step), 1);
                }

                break;
            case 1:
                if(nodeId == 8 && packet.getDNA() == 0){
                    System.err.println("");
                }
                if(nodeId == 4 && packet.getDNA() == 0){
                    System.err.println("");
                }
                if(nodeId == 2 && packet.getDNA() == 0){
                    System.err.println("");
                }
                if(nodeId == 1 && packet.getDNA() == 0){
                    System.err.println("");
                }

                if (!grantQueue.isEmpty()) {
                    outputChannels.get(grantQueue.get(0)).grantAcknowledge();
                    long oldtr = packet.getTR();
                    packet.setTR(packet.getDNA() ^ outputChannels.get(grantQueue.get(0)).getNextNodeId()); // верен ред.
                    DataCollector.getInstance().addToList(String.format("packet_%d_%d_%d_for_%d_trs", nodeId, channelId, arbiterId, packet.getDNA()), packet.getTR());
                    //System.err.printf("current input channel: %d, node: %d, arbiter: %d, old tr: %d, new tr: %d\n", this.channelId, this.nodeId, this.arbiterId, oldtr, packet.getTR());
                    popped = packet.getHeader_1();

                    step++;
                }
                break;
            case 2:
               /* if(channelId == 0 && nodeId == 1 && fifoBuff.getItemCount() > 0){
                    System.err.println("stop1");
                }*/
                if (outputChannels.get(grantQueue.get(0)).putData(popped)) {
                    popped = fifoBuff.pop();
                    if (popped == null) {
                        /*if (packet.getDNA() == nodeId) {
                            System.err.printf("Received! Boom! I am arbiter %d, living deep in channel %d, and my Router is %d\n", +this.arbiterId, this.channelId, this.nodeId);
                        }*/
                        step = 0;
                        outputChannels.get(grantQueue.get(0)).releaseChannel();
                        fifoBuff.setBusy(false);
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
        return fifoBuff.isBusy();
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
