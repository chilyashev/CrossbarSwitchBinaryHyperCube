package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;

import java.util.ArrayList;

/**
 * An abstract class for the different routing algorithms
 * Date: 5/3/14 4:43 PM
 *
 * @author Mihail Chilyashev
 */
abstract public class Router implements Tickable{

    private ArrayList<InputChannel> inputChannels;
    private ArrayList<OutputChannel> outputChannels;
    InputChannel dmaIN;
    OutputChannel dmaOUT;

    protected Router(ArrayList<InputChannel> inputChannels, ArrayList<OutputChannel> outputChannels, InputChannel dmaIN, OutputChannel dmaOUT) {
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

}
