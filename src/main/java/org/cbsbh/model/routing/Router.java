package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;

import java.util.HashMap;

/**
 * An abstract class for the different routing algorithms
 * Date: 5/3/14 4:43 PM
 *
 * @author Mihail Chilyashev
 */
public class Router implements Tickable {

    HashMap<Integer, InputChannel> inputChannels;
    HashMap<Integer, OutputChannel> outputChannels;
//    InputChannel dmaIN;
//    OutputChannel dmaOUT;

    public Router(HashMap<Integer, InputChannel> inputChannels, HashMap<Integer, OutputChannel> outputChannels) {
        this.inputChannels = inputChannels;
        this.outputChannels = outputChannels;
    }


    public void tick() {

        for (Integer id : inputChannels.keySet()) {
            inputChannels.get(id).tick();
        }

        for (Integer id : outputChannels.keySet()) {
            outputChannels.get(id).tick();
        }

    }

    @Override
    public void calculateNewState() {

    }

    public HashMap<Integer, InputChannel> getInputChannels() {
        return inputChannels;
    }

    public HashMap<Integer, OutputChannel> getOutputChannels() {
        return outputChannels;
    }

    public InputChannel getInputChannel(int inputChannelId) {
        return inputChannels.get(inputChannelId);
    }

    public InputChannel getDmaOUT() {
        return inputChannels.get(Integer.MAX_VALUE-1);
    }

    public void setDmaOUT(InputChannel dmaOUT) {
        this.inputChannels.put(Integer.MAX_VALUE -1, dmaOUT);
    }
}
