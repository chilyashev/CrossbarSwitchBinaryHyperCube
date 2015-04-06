package org.cbsbh.model.routing;

import jdk.internal.util.xml.impl.Input;
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


    // TODO: do.
    public Message generateMessage() {
        return null;
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
}
