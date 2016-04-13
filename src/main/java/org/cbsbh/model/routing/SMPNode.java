package org.cbsbh.model.routing;

import org.cbsbh.Debug;
import org.cbsbh.context.Context;
import org.cbsbh.model.routing.packet.flit.Flit;
import org.cbsbh.model.structures.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Description goes here
 * Date: 3/16/15 12:06 PM
 *
 * @author Mihail Chilyashev
 */
public class SMPNode {
    //Core:
    int id;
    HashMap<Integer, InputChannel> inputChannels;
    HashMap<Integer, OutputChannel> outputChannels;
    InputChannel DMA_IN;
    ArrayList<Flit> packetToSend;
    ArrayList<ArrayList<Flit>> messageToSend;


    //Debug:
    public ArrayList<Flit> sentFlits = new ArrayList<>();

    //Arbitrary (most likely to be removed):
    ArrayList<Message> messages; //POSSIBLY OBSOLETE
    //ArrayList<Packet> messageData; //OBSOLETE

    public SMPNode(int id) {
        this.id = id;
        messages = new ArrayList<>();
        //messageData = new ArrayList<>();
    }

    public void init() {
        Debug.printf("%s init", getWho());
        messageToSend = new ArrayList<>();
        packetToSend = new ArrayList<>();
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::init);
        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::init);
        DMA_IN = new InputChannel(16, id);
        DMA_IN.init();
    }

    public void tick() {
        if (messageToSend.size() > 0) {
            if (packetToSend.isEmpty()) {
                packetToSend.addAll(messageToSend.remove(0));
            }
        }
        if (packetToSend.size() > 0 && DMA_IN.getState() == 2) {
            Flit flit = packetToSend.get(0);
            if (flit.getFlitType() == Flit.FLIT_TYPE_HEADER) {
                DMA_IN.setInputBuffer(flit);
                sentFlits.add(packetToSend.remove(0));
            }
        } else if (!packetToSend.isEmpty() && DMA_IN.getState() > 2 && packetToSend.get(0).getFlitType() != Flit.FLIT_TYPE_HEADER) {
            DMA_IN.setInputBuffer(packetToSend.get(0));
            sentFlits.add(packetToSend.remove(0));
        }

        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::tick);
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::tick);

        DMA_IN.tick();

        /*if (!messages.isEmpty()) {
            Message m = messages.remove(0);
            messageData.addAll(m.getAsPackets());
        }*/

    }

    private ArrayList<Flit> generatePacket(int target, int packetSize) {
        ArrayList<Flit> newPacket = new ArrayList<>();

        Flit flit = new Flit();
        flit.id = String.format("%d->%d_%d", id, target, packetToSend.size());
        flit.setFlitType(Flit.FLIT_TYPE_HEADER);
        flit.setDNA(target); // 10 is random. I can't even.
        flit.setTR(id ^ flit.getDNA());
        flit.setValidDataBit();
        Debug.printf("> [Just the tip] Generating a message. From %d to %d", id, flit.getDNA());
        newPacket.add(flit);
        while (packetSize-- >= 0) {
            flit = new Flit();
            flit.id = String.format("%d->%d_%d", id, target, packetToSend.size());
            if (packetSize >= 0) {
                flit.setFlitData(0x8000 + target);
                flit.setFlitType(Flit.FLIT_TYPE_BODY);
            } else {
                flit.setFlitType(Flit.FLIT_TYPE_TAIL);
            }
            flit.setValidDataBit();
            Debug.printf("> [Just the next piece] Generating a message. From %d, type %d", id, flit.getFlitType());
            newPacket.add(flit);
        }
        return newPacket;
    }

    public void generateMessage() {
        if (messageToSend.size() > 0) {
            return;
        }
        Debug.printf("Starting the generation for %d.", id);
        messageToSend.addAll(generateMessage(2, 2, Context.getInstance().getInteger("nodeCount")));
    }

    // TODO: Тая малоумщина (ArrayList<ArrayList<Flit>>) да се сложи в клас.
    public ArrayList<ArrayList<Flit>> generateMessage(int maxPacketCount, int maxPacketSize, int maxTargetId) {
        Random r = new Random();
        int msgSize = r.nextInt(maxPacketCount - 1) + 1;
        int target = r.nextInt(maxTargetId);

        Debug.printf("> Generating a message from %d to %d with %d packet%c", id, target, msgSize, msgSize == 1 ? ' ' : 's');

        ArrayList<ArrayList<Flit>> message = new ArrayList<>();

        while (msgSize-- > 0) {
            int packetSize = r.nextInt(maxPacketSize - 1) + 1;
            Debug.printf(">> Generating a packet of %d flit%c", packetSize, packetSize == 1 ? ' ' : 's');
            message.add(generatePacket(target, packetSize));
        }

        return message;
    }

    public InputChannel getInputChannel(int index) {
        return inputChannels.get(index);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashMap<Integer, InputChannel> getInputChannels() {
        return inputChannels;
    }

    public void setInputChannels(HashMap<Integer, InputChannel> inputChannels) {
        this.inputChannels = inputChannels;
    }

    public HashMap<Integer, OutputChannel> getOutputChannels() {
        return outputChannels;
    }

    public void setOutputChannels(HashMap<Integer, OutputChannel> outputChannels) {
        this.outputChannels = outputChannels;
    }

    public OutputChannel getOutputChannel(int outputChannelId) {
        return outputChannels.get(outputChannelId);
    }

    public String getWho() {
        return String.format("SMPNode {id: %d (%s)}", id, String.format("%s", Integer.toBinaryString(id)));
    }

    public void calculateNewStates() {
        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::calculateNewState);
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::calculateNewState);
        DMA_IN.calculateNewState();
    }

    public ArrayList<ArrayList<Flit>> getMessageToSend() {
        return messageToSend;
    }
}
