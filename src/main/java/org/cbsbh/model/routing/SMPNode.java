package org.cbsbh.model.routing;

import org.cbsbh.model.routing.packet.Packet;
import org.cbsbh.model.structures.Message;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 3/16/15 12:06 PM
 *
 * @author Mihail Chilyashev
 */
public class SMPNode {

    int id;

    ArrayList<InputChannel> inputChannels;

    ArrayList<OutputStateStructure> outputChannels;

    InputChannel DMA_OUT;

    OutputStateStructure DMA_IN;


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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<InputChannel> getInputChannels() {
        return inputChannels;
    }

    public void setInputChannels(ArrayList<InputChannel> inputChannels) {
        this.inputChannels = inputChannels;
    }

    public ArrayList<OutputStateStructure> getOutputChannels() {
        return outputChannels;
    }

    public void setOutputChannels(ArrayList<OutputStateStructure> outputChannels) {
        this.outputChannels = outputChannels;
    }

    public InputChannel getDMA_OUT() {
        return DMA_OUT;
    }

    public void setDMA_OUT(InputChannel DMA_OUT) {
        this.DMA_OUT = DMA_OUT;
    }

    public OutputStateStructure getDMA_IN() {
        return DMA_IN;
    }

    public void setDMA_IN(OutputStateStructure DMA_IN) {
        this.DMA_IN = DMA_IN;
    }
}
