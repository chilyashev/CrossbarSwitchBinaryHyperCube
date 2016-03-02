package org.cbsbh.model.routing;

import jdk.internal.util.xml.impl.Input;
import org.cbsbh.Debug;
import org.cbsbh.context.Context;
import org.cbsbh.model.routing.packet.Packet;
import org.cbsbh.model.routing.packet.flit.Flit;
import org.cbsbh.model.structures.Message;
import org.cbsbh.model.structures.SignalArray;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description goes here
 * Date: 3/16/15 12:06 PM
 *
 * @author Mihail Chilyashev
 */
public class SMPNode {

    int id;

    HashMap<Integer, InputChannel> inputChannels;

    HashMap<Integer, OutputChannel> outputChannels;

    InputChannel DMA_OUT;

    OutputChannel DMA_IN;

    ArrayList<Message> messages;
    ArrayList<Packet> messageData;


    public SMPNode(int id) {
        this.id = id;
        messages = new ArrayList<>();
        messageData = new ArrayList<>();
    }

    public void init() {
        Debug.printf("%s init", getWho());
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::init);
        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::init);
    }

    boolean cockLove = false; // Who doesn't love roosters?
    boolean doLove = true;

    public void tick() {
        //if (!messageData.isEmpty())
        Debug.printf("%s tick", getWho());
        {
            //for(Integer icId : getInputChannels().keySet())
            Integer icId = 2;


            if (doLove) {
                if (id == 0) {
                    if (!cockLove) {

                        if (inputChannels.get(icId).getState() == 1) {
                            Flit flit = new Flit();
                            flit.setFlitType(Flit.FLIT_TYPE_HEADER);
                            flit.setDNA(10); // 10 is random. I can't even.
                            flit.setTR(id ^ flit.getDNA());
                            flit.setValidDataBit();
                            Debug.printf("> [Just the tip] Generating a message. From %d to %d", id, flit.getDNA());

                            inputChannels.get(icId).setInputBuffer(flit);
                            inputChannels.get(icId).getActiveFifo().getSignalArray().setSignal(SignalArray.WR_FIFO_EN, true);
                            inputChannels.get(icId).getActiveFifo().getSignalArray().setSignal(SignalArray.WR_IN_FIFO, true);
                            inputChannels.get(icId).getActiveFifo().getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                            inputChannels.get(icId).getActiveFifo().setState(3);
                            cockLove = true;
                        }
                    } else if(inputChannels.get(icId).getActiveFifo().getFifo().size() <1) {
                        doLove = false;
                        Flit flit = new Flit();
                        flit.setFlitType(Flit.FLIT_TYPE_TAIL);
                        flit.setValidDataBit();
                        flit.setFlitData(0x999);
                        Debug.printf("> [Just the dick] Generating a message. From %d to %d", id, flit.getDNA());

                        inputChannels.get(icId).setInputBuffer(flit);
                        inputChannels.get(icId).getActiveFifo().getSignalArray().setSignal(SignalArray.WR_FIFO_EN, true);
                        inputChannels.get(icId).getActiveFifo().getSignalArray().setSignal(SignalArray.WR_IN_FIFO, true);
                        inputChannels.get(icId).getActiveFifo().getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                        inputChannels.get(icId).getActiveFifo().setState(4);

                    }
                }
            }
        }

        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::tick);
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::tick);

        //getDMA_IN().tick();
        //getDMA_OUT().tick();
        /*if (!messages.isEmpty()) {
            Message m = messages.remove(0);
            messageData.addAll(m.getAsPackets());
        }*/

    }

    // TODO: do.
    public Message generateMessage() {
        Message m = new Message();
        m.setSource(this.id);
        m.setTarget(10); // As random as it gets
        messages.add(m);
        return m;
    }


    /**
     * Изпращане на съобщение пакет по пакет
     */
    public void sendMessage() {

    }

    /**
     * Разделяне на съобщение на пакети
     *
     * @param m съобщението
     * @return колекция от пакети
     */
    public Packet[] messageAsPackets(Message m) {
        return null;
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

    public InputChannel getDMA_OUT() {
        return DMA_OUT;
    }

    public void setDMA_OUT(InputChannel DMA_OUT) {
        this.DMA_OUT = DMA_OUT;
    }

    public OutputChannel getDMA_IN() {
        return DMA_IN;
    }

    public void setDMA_IN(OutputChannel DMA_IN) {
        this.DMA_IN = DMA_IN;
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
    }
}
