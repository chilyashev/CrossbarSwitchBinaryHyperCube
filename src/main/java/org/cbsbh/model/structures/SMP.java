package org.cbsbh.model.structures;

import org.cbsbh.context.Context;
import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.InputChannel;
import org.cbsbh.model.routing.Router;
import org.cbsbh.model.routing.packet.Packet;

import java.util.LinkedList;
import java.util.Random;

/**
 * An SMP computer.
 * Date: 6/5/14 6:15 PM
 *
 * @author Mihail Chilyashev
 */
public class SMP implements Tickable {
    private int id;
    private Router router;
    private int ticksSinceLastMessageGeneration = 0;
    private int[] adjacentNodeIDs;

    Message msg = null;

    int step = 0;
    Packet packet;

    boolean hasMessage = false;

    public SMP(int id, Router router, int[] adjacentNodeIDs) {
        this.id = id;
        this.router = router;
        this.adjacentNodeIDs = adjacentNodeIDs;
        this.ticksSinceLastMessageGeneration = (int) (Math.random() % Context.getInstance().getInteger("messageGenerationFrequency"));

        InputChannel dmaOUT = new InputChannel(Integer.MAX_VALUE - 1, id, router.getOutputChannels());


        router.setDmaOUT(dmaOUT);
    }


    /**
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
        while(target == id){ // TODO: disable sending packets to myself?
            System.err.println("I suck at loops.");
            target = (int) (Math.random() % max_id);
        }
        ret.setTarget(target);

        if (maxSize < 4) {
            data.add(r.nextLong());
        } else {
            System.out.printf("%d, %d\n", minSize, maxSize);
            for (int i = 0; i <= r.nextInt(maxSize - minSize) + minSize; i += 4) {
                data.add(r.nextLong());
            }
        }

        ret.setData(data);
        return ret;
    }


    @Override
    public void tick() {
        // Generate and send (a) message(s)
        if (ticksSinceLastMessageGeneration == Context.getInstance().getInteger("messageGenerationFrequency")) {
            msg = this.generateMessage(Context.getInstance().getInteger("minMessageSize"), Context.getInstance().getInteger("maxMessageSize"));
            hasMessage = true;
            ticksSinceLastMessageGeneration = 0;
        } else if(hasMessage){
            packet = msg.getPacket();
            assert packet != null;
            router.getDmaOUT().setPacket(packet); // TODO: дали е валидно за един такт да се пращат 4 флита?
            hasMessage = false;
        }
        ticksSinceLastMessageGeneration++;

        router.tick();
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
