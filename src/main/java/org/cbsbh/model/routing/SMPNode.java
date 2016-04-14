package org.cbsbh.model.routing;

import org.cbsbh.Debug;
import org.cbsbh.context.Context;
import org.cbsbh.model.structures.Flit;
import org.cbsbh.model.structures.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Description goes here
 * Date: 3/16/15 12:06 PM
 *
 * @author Mihail Chilyashev, Georgi Georgiev
 */
public class SMPNode {
    //Core:
    int id;
    HashMap<Integer, InputChannel> inputChannels;
    HashMap<Integer, OutputChannel> outputChannels;
    InputChannel DMA_IN;
    ArrayList<Packet> messageToSend;
    private Packet packetToSend = new Packet();

    //Debug:
    public ArrayList<Flit> sentFlits = new ArrayList<>();

    public SMPNode(int id) {
        this.id = id;
    }

    public void init() {
        Debug.printf("%s init", getWho());
        messageToSend = new ArrayList<>();
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::init);
        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::init);
        DMA_IN = new InputChannel(Context.getInstance().getInteger("nodeCount"), id);
        DMA_IN.init();
    }

    public void tick() {
        if (messageToSend.size() > 0) {
            if (packetToSend.isEmpty()) {
                packetToSend.addAll(messageToSend.remove(0));
            }
        }
        if (!packetToSend.isEmpty()) {
            Flit flit = packetToSend.get(0);
            if (DMA_IN.getState() == 2) {
                if (flit.getFlitType() == Flit.FLIT_TYPE_HEADER) {
                    DMA_IN.setInputBuffer(flit);
                    sentFlits.add(packetToSend.remove(0));
                }
            } else if (DMA_IN.getState() > 2 && flit.getFlitType() != Flit.FLIT_TYPE_HEADER) {
                DMA_IN.setInputBuffer(packetToSend.get(0));
                sentFlits.add(packetToSend.remove(0));
            }
        }

        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::tick);
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::tick);

        DMA_IN.tick();
    }

    public void generateMessage() {
        if (messageToSend.size() > 0) {
            return;
        }
        Debug.printf("Starting the generation for %d.", id);
        messageToSend.addAll(generateMessage(2, 2, Context.getInstance().getInteger("nodeCount")));
    }

    public ArrayList<Packet> generateMessage(int maxPacketCount, int maxPacketSize, int maxTargetId) {
        Random r = new Random();
        int msgSize = 1;//r.nextInt(maxPacketCount - 1) + 1;
        int target = r.nextInt(maxTargetId);

        Debug.printf("> Generating a message from %d to %d with %d packet%c", id, target, msgSize, msgSize == 1 ? ' ' : 's');

        ArrayList<Packet> message = new ArrayList<>();

        while (msgSize-- > 0) {
            int packetSize = 1;//r.nextInt(maxPacketSize - 1) + 1;
            Debug.printf(">> Generating a packet of %d flit%c", packetSize, packetSize == 1 ? ' ' : 's');
            message.add(new Packet(id, target, packetSize));
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

    public ArrayList<Packet> getMessageToSend() {
        return messageToSend;
    }
}
