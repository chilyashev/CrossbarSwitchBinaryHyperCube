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
    Message msg = null;
    boolean hasMessage = false;
    private int id;
    private Router router;
    private int ticksSinceLastMessageGeneration = 0;

    //TODO: packet doesn't need to be a field?
    //Packet packet;
    private int[] adjacentNodeIDs;

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
        while (target == id) { // TODO: disable sending packets to myself?
            System.err.println("I suck at loops.");
            target = (target+1) % max_id;
        }
        ret.setTarget(target);

        if (maxSize <= 4) {
            data.add(r.nextLong());
        } else {
            System.out.printf("%d, %d\n", minSize, maxSize);
            int n = maxSize != minSize? r.nextInt(maxSize - minSize): minSize; // If maxSize == minSize, the random class makes a boo-boo. So, if they are equal, just take one. TODO: maybe get rid of this shit.
            for (int i = 0; i <= n + minSize; i += 4) {
                data.add(r.nextLong());
            }
        }

        ret.setData(data);
        return ret;
    }


    @Override
    public void tick() {
        // Generate and send (a) message(s)

        //TODO:: ALL of the shit in the block bellow is WRONG and should be removed after testing is finished (in about 4-5 years).
        //##########################################################################################################################
        if (this.id == 0 && !hasMessage) {
            //TODO: Error numero uno: m is local, which is why only the first packet of it is send in this system. Any other packets go to Oblivion. Delicious.
            //Message m = generateMessage(4, 4);
//            m.setTarget(0b1110);

            msg = generateMessage(8, 8);
            msg.setTarget(0b1111);

            //TODO: Error numero dos: the packet getting is in the if, BUTT we enter the if only once, so again another reason we get only one packet from the whole msg.
//            packet = msg.getPacket();
//            assert packet != null;
//            router.getDmaOUT().setPacket(packet);
            hasMessage = true;
        }

        if (msg != null) {
            Packet packet = msg.getPacket();

            if (packet != null) {
                router.getDmaOUT().setPacket(packet);
            }
        }
        //##########################################################################################################################

        /*if (ticksSinceLastMessageGeneration == Context.getInstance().getInteger("messageGenerationFrequency")) {
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
*/
        if(id == 4){
            //System.err.println("stop");
        }

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
