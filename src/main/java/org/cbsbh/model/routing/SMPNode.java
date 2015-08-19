package org.cbsbh.model.routing;

import jdk.internal.util.xml.impl.Input;
import org.cbsbh.Debug;
import org.cbsbh.model.routing.packet.Packet;
import org.cbsbh.model.structures.Message;

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
        Debug.println(getClass() + " init");
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::init);
        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::init);
    }

    public void tick() {
        inputChannels.values().forEach(org.cbsbh.model.routing.InputChannel::tick);
        outputChannels.values().forEach(org.cbsbh.model.routing.OutputChannel::tick);
        if(!messages.isEmpty()){
            Message m = messages.remove(0);
            messageData.addAll(m.getAsPackets());
        }
        if(!messageData.isEmpty()){
            Packet p = messageData.remove(0);
            // TODO: Send this shit!
        }
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
    public void sendMessage(){

    }

    /**
     * Разделяне на съобщение на пакети
     * @param m съобщението
     * @return колекция от пакети
     */
    public Packet[] messageAsPackets(Message m){
        return null;
    }

    public InputChannel getInputChannel(int index){
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
}
