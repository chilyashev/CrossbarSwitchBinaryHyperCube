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
    InputChannel dmaIN;
    OutputChannel dmaOUT;
    private int[] channelsIDs;

    public Router(int id, int[] channelsIDs) {
        this.id = id;
        this.channelsIDs = channelsIDs;
    }

//    protected Router(int id, ArrayList<InputChannel> inputChannels, ArrayList<OutputChannel> outputChannels, InputChannel dmaIN, OutputChannel dmaOUT) {
//        this.id = id;
//        this.inputChannelsIDs = inputChannels;
//        this.outputChannelsIDs = outputChannels;
//        this.dmaIN = dmaIN;
//        this.dmaOUT = dmaOUT;
//    }

    /**
     * All the logic goes in here.
     */
    public void tick(){
//        dmaIN.tick();
//        dmaOUT.tick();

        for (int channelID : channelsIDs) {
            InputChannelCollection.get(id, channelID).tick();
            OutputChannelCollection.get(id, channelID).tick();
        }
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
