package org.cbsbh.model.structures;

import org.cbsbh.context.Context;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.InputChannel;
import org.cbsbh.model.routing.Router;
import org.cbsbh.model.routing.packet.Packet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * An SMP computer.
 * Date: 6/5/14 6:15 PM
 *
 * @author Mihail Chilyashev
 */
public class SMP implements Tickable {
    Message msg = null;
    boolean hasMessage = false;
    private int id;
    private Router router;
    private int ticksSinceLastMessageGeneration = 0;
    int generatedMessageCount = 0;

    //TODO: packet doesn't need to be a field?
    //Packet packet;
    private int[] adjacentNodeIDs;
    private int generatedPacketCount;

    ArrayList<Packet> packetsToSend;

    public SMP(int id, Router router, int[] adjacentNodeIDs) {
        this.id = id;
        this.router = router;
        this.adjacentNodeIDs = adjacentNodeIDs;
        this.ticksSinceLastMessageGeneration = (int) (Math.random() % Context.getInstance().getInteger("messageGenerationFrequency"));
        packetsToSend = new ArrayList<>();


        InputChannel dmaOUT = new InputChannel(Integer.MAX_VALUE - 1, id, router.getOutputChannels());


        router.setDmaOUT(dmaOUT);
    }


    /**
     * Generates a random message to a random target.
     *
     * @param minSize size in Bytes
     * @param maxSize size in Bytes
     * @return the slab
     */
    private Message generateMessage(int minSize, int maxSize) {
        Message ret = new Message();
        Random r = new Random();
        LinkedList<Long> data = new LinkedList<>();

        ret.setSource(id);
        int max_id = 1 << Context.getInstance().getInteger("channelCount");
        int target = r.nextInt(max_id);
        while (target == id) { // TODO: disable sending packets to myself?
            System.err.println("I suck at loops.");
            target = (target + 1) % max_id;
        }
        if(id != 15){
            target = 15;
        }else{
            target = 0;
        }
        ret.setTarget(target);

        data.add(id + 100l);
        data.add(id + 1000L);/*
        data.add(id + 200l);
        data.add(id + 2000L);*/

        ret.setData(data);

        return ret;
    }


    @Override
    public void tick() {
        // Generate and send (a) message(s)
/*
        //TODO:: ALL of the shit in the block bellow is WRONG and should be removed after testing is finished (in about 4-5 years).
        //##########################################################################################################################
        if (((this.id == 0) || (id == 4) || (id == 8)) && !hasMessage) {
            //TODO: Error numero uno: m is local, which is why only the first packet of it is sent in this system. Any other packets go to Oblivion. Delicious.
            //Message m = generateMessage(4, 4);
//            m.setTarget(0b1110);


            //msg = generateMessage(8, 8);
            msg = new Message();
            msg.setSource(id);
            LinkedList<Long> d = new LinkedList<>();
            d.add(0x0L+id);
            d.add(0x0L+id);
            msg.setData(d);
//            msg.setTarget(0b1111);
            if(id == 0){
                if(!generated){
                    msg = new Message();
                    msg.setSource(id);
                    d = new LinkedList<>();
                    d.add(0x0L+id);
                    d.add(0x0L+id);
                    msg.setData(d);
                    msg.setTarget((int) (Math.random()%15)+1);
                    generated = true;
                }
            }else if(id == 4){
                msg.setTarget(8);
            }else{
                msg.setTarget(15);
            }

            //TODO: Error numero due: the packet getting is in the if, BUTT we enter the if only once, so again another reason we get only one packet from the whole msg.
//            packet = msg.getPacket();
//            assert packet != null;
//            router.getDmaOUT().setPacket(packet);
            hasMessage = true;
        }

        if (msg != null) {
            Packet packet = msg.getPacket();

            if (packet != null) {
                router.getDmaOUT().setPacket(packet);
                System.err.printf("Sending from %d to %d\n", id, 4-id);
            }
        }else {
            if(id == 0){
                generated = false;
                hasMessage = false;
            }
        }*/
        //##########################################################################################################################

        if (!hasMessage && ticksSinceLastMessageGeneration >= Context.getInstance().getInteger("messageGenerationFrequency") && ((generatedMessageCount) < 10)) {
            msg = this.generateMessage(Context.getInstance().getInteger("minMessageSize"), Context.getInstance().getInteger("maxMessageSize"));
            System.err.printf("Sending message №%d from %d to %d\n", generatedMessageCount, id, msg.getTarget());
            hasMessage = true;
            ticksSinceLastMessageGeneration = 0;
            generatedMessageCount++;
            packetsToSend = msg.getAsPackets();
        } else if (!packetsToSend.isEmpty()) {
            //assert packet != null;
//            if (packetsToSend.size() > 0) {
                Packet packet = packetsToSend.remove(0);
                generatedPacketCount++;
                router.getDmaOUT().setPacket(packet); // TODO: дали е валидно за един такт да се пращат 4 флита?
            /*} else {
                hasMessage = false;
            }*/
            if(packetsToSend.isEmpty()){
                hasMessage = false;
            }
        }
        ticksSinceLastMessageGeneration++;
        router.tick();
    }

    public int getGeneratedMessageCount() {
        return generatedMessageCount;
    }

    public int getGeneratedPacketCount() {
        return generatedPacketCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }
}
