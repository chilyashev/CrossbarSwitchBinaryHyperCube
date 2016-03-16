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

    InputChannel DMA_IN;

    ArrayList<Message> messages;
    ArrayList<Packet> messageData;
    public ArrayList<Flit> sentFlits = new ArrayList<>();


    public SMPNode(int id) {
        this.id = id;
        messages = new ArrayList<>();
        messageData = new ArrayList<>();
    }

    public void init() {
        Debug.printf("%s init", getWho());
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::init);
        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::init);
        DMA_IN = new InputChannel(16, id);
        DMA_IN.init();
    }

    boolean cockLove = false; // Who doesn't love roosters?
    boolean doLove = true;
    int tock = 1;
    int flitters = 1;
    int queueId;

    public void tick() {
        //if (!messageData.isEmpty())
        //Debug.printf("%s tick", getWho());
        {
            //for(Integer icId : getInputChannels().keySet())
            Integer icId = 2;


            if (doLove) {
                if (id == 0) {
                    icId = 2;
                    if (DMA_IN.getState() == 2) {
                        sendFlits(icId, 15); // Send from 0 to 10
                    }
                }/*else if(id == 3) {
                    icId = 1;
                    sendFlits(icId, 0b1100); // Send from 9 to 1
                }*/
            }
        }

        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::tick);
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::tick);
        DMA_IN.tick();
        //getDMA_IN().tick();
        //getDMA_OUT().tick();
        /*if (!messages.isEmpty()) {
            Message m = messages.remove(0);
            messageData.addAll(m.getAsPackets());
        }*/

    }

    private void sendFlits(Integer icId, int target) {
        if (!cockLove) {
            Flit flit = new Flit();
            flit.setFlitType(Flit.FLIT_TYPE_HEADER);
            flit.setDNA(target); // 10 is random. I can't even.
            flit.setTR(id ^ flit.getDNA());
            flit.setValidDataBit();
            Debug.printf("> [Just the tip] Generating a message. From %d to %d", id, flit.getDNA());
            sentFlits.add(flit);

            DMA_IN.setInputBuffer(flit);
            //queueId = DMA_IN.getActiveFIFOIndex();
            /*DMA_IN.getActiveFifo().getSignalArray().setSignal(SignalArray.WR_FIFO_EN, true);
            DMA_IN.getActiveFifo().getSignalArray().setSignal(SignalArray.WR_IN_FIFO, true);
            DMA_IN.getActiveFifo().getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
            DMA_IN.getActiveFifo().setState(3);*/
            cockLove = true;
            tock = 1;
            while (flitters-- >= 0) {
                //doLove = false;
                flit = new Flit();
                if (flitters >= 0) {
                    flit.setFlitData(0x8000 + target);
                    flit.setFlitType(Flit.FLIT_TYPE_BODY);
                } else {
                    flit.setFlitType(Flit.FLIT_TYPE_TAIL);
                    doLove = false;
                }
                flit.setValidDataBit();
                Debug.printf("> [Just the next piece] Generating a message. From %d, type %d", id, flit.getFlitType());
                sentFlits.add(flit);
                DMA_IN.setInputBuffer(flit);
            }
        }

    }

    private void sendFlits_o(Integer icId, int target) {
        if (!cockLove) {

            if (inputChannels.get(icId).getState() == 1) {
                Flit flit = new Flit();
                flit.setFlitType(Flit.FLIT_TYPE_HEADER);
                flit.setDNA(target); // 10 is random. I can't even.
                flit.setTR(id ^ flit.getDNA());
                flit.setValidDataBit();
                Debug.printf("> [Just the tip] Generating a message. From %d to %d", id, flit.getDNA());
                sentFlits.add(flit);

                DMA_IN.setInputBuffer(flit);
                queueId = DMA_IN.getActiveFIFOIndex();
                DMA_IN.getActiveFifo().getSignalArray().setSignal(SignalArray.WR_FIFO_EN, true);
                DMA_IN.getActiveFifo().getSignalArray().setSignal(SignalArray.WR_IN_FIFO, true);
                DMA_IN.getActiveFifo().getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
                DMA_IN.getActiveFifo().setState(3);
                cockLove = true;
            }
        } else if (tock-- == 0 && inputChannels.get(icId).getFifoQueues().get(queueId).getState() == 4) {// && inputChannels.get(icId).getActiveFifo().getFifo().size() <1) {
            tock = 1;
            flitters--;

            //doLove = false;
            Flit flit = new Flit();
            if (flitters >= 0) {
                flit.setFlitData(0x8000 + target);
                flit.setFlitType(Flit.FLIT_TYPE_BODY);
            } else {
                flit.setFlitType(Flit.FLIT_TYPE_TAIL);
                doLove = false;
            }
            flit.setValidDataBit();
            //flit.setFlitData(0x999);
            Debug.printf("> [Just the dick] Generating a message. From %d, type %d", id, flit.getFlitType());

            sentFlits.add(flit);
            inputChannels.get(icId).setInputBuffer(flit);
            inputChannels.get(icId).getFifoQueues().get(queueId).getSignalArray().setSignal(SignalArray.WR_FIFO_EN, true);
            inputChannels.get(icId).getFifoQueues().get(queueId).getSignalArray().setSignal(SignalArray.WR_IN_FIFO, true);
            inputChannels.get(icId).getFifoQueues().get(queueId).getSignalArray().setSignal(SignalArray.FIFO_BUSY, true);
            inputChannels.get(icId).getFifoQueues().get(queueId).setState(4);

        }
    }

    // TODO: do.
    public Message generateMessage() {
        Message m = new Message();
        m.setSource(this.id);
        m.setTarget(3);
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
}
