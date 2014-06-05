package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;

import java.util.ArrayList;

/**
 * An abstract class for the different routing algorithms
 * Date: 5/3/14 4:43 PM
 *
 * @author Mihail Chilyashev
 */
public class Router implements Tickable{

    int id;
    private ArrayList<InputChannel> inputChannels;
    private ArrayList<OutputChannel> outputChannels;
    InputChannel dmaIN;
    OutputChannel dmaOUT;

    public Router() {
    }

    protected Router(int id, ArrayList<InputChannel> inputChannels, ArrayList<OutputChannel> outputChannels, InputChannel dmaIN, OutputChannel dmaOUT) {
        this.id = id;
        this.inputChannels = inputChannels;
        this.outputChannels = outputChannels;
        this.dmaIN = dmaIN;
        this.dmaOUT = dmaOUT;
    }

    /**
     * All the logic goes in here.
     */
    public void tick(){
        dmaIN.tick();
        dmaOUT.tick();
        for(int i = 0; i < inputChannels.size(); i++){
            InputChannel inputChannel = inputChannels.get(i);
            OutputChannel outputChannel = outputChannels.get(i);
            inputChannel.tick();
            outputChannel.tick();
        }

    }

    public ArrayList<InputChannel> getInputChannels() {
        return inputChannels;
    }

    public void setInputChannels(ArrayList<InputChannel> inputChannels) {
        this.inputChannels = inputChannels;
    }

    public ArrayList<OutputChannel> getOutputChannels() {
        return outputChannels;
    }

    public void setOutputChannels(ArrayList<OutputChannel> outputChannels) {
        this.outputChannels = outputChannels;
    }

    public InputChannel getDmaIN() {
        return dmaIN;
    }

    public void setDmaIN(InputChannel dmaIN) {
        this.dmaIN = dmaIN;
    }

    public OutputChannel getDmaOUT() {
        return dmaOUT;
    }

    public void setDmaOUT(OutputChannel dmaOUT) {
        this.dmaOUT = dmaOUT;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
