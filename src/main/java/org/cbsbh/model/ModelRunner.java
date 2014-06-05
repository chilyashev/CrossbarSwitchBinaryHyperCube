package org.cbsbh.model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.cbsbh.context.Context;
import org.cbsbh.model.routing.*;

import java.util.ArrayList;
import java.util.Date;


/**
 * Takes care of bootstrapping the model, running it, collecting the data and telling the controller that
 * the simulation has finished.
 * Date: 4/25/14 1:46 PM
 *
 * @author Mihail Chilyashev
 */
public class ModelRunner implements Runnable {
    Context context;
    EventHandler<ActionEvent> handler;
    private boolean running = true;
    private long ticks = 0;
    private ArrayList<Router> MPPNetwork;

    public ModelRunner(Context context, EventHandler<ActionEvent> handler) {
        this.context = context;
        this.handler = handler;
        this.MPPNetwork = new ArrayList<>();
    }

    private int[] getGrayCodes(int n) {
        int[] list = new int[n];
        for (int i = 0; i < n; i++) {
            list[i] = ((i >> 1) ^ i);
        }
        return list;
    }

    private void init(int channelCount, int bufferCount) {
        //compute number of nodes
        int SMPNodes = 2 << channelCount;

        //encode node ids
        int[] GrayCodes = getGrayCodes(SMPNodes);

        //----big ass cycle for init-ing stuff and things
        //iterate all nodes
        for (int i = 0; i < SMPNodes; i++) {
            int currentNodeID = GrayCodes[i];

            //for each node compute adjacent nodes
            int[] adjacentChannelIDs = new int[channelCount];
            for (int j = 0; j < channelCount; j++) {
                adjacentChannelIDs[j] = currentNodeID ^ (1 << j); //magic
            }

            //construct the router
            Router router = new Router(currentNodeID, adjacentChannelIDs);

            //populate input/output channel collections
            for (int j : adjacentChannelIDs) {
                InputChannel iChannel = new InputChannel(j, bufferCount);
                InputChannelCollection.push(currentNodeID, iChannel);

                OutputChannel oChannel = new OutputChannel(j, currentNodeID);
                OutputChannelCollection.push(currentNodeID, oChannel);
            }

            //include the bad boy in the network
            MPPNetwork.add(router);
        }
    }

    @Override
    public void run() {

        // Init phase
        int channelCount = 4;
        int bufferCount = 5;
        Context.getInstance().set("channelCount", channelCount); // TODO: get this from the interface!
        init(channelCount, bufferCount);
        //End of init

        System.out.println("Starting at... " + new Date());
        // Ticking....
        while(ticks < Long.MAX_VALUE){
            // Tick for each router
            //System.err.println("tick");
            ticks++;
        }
        System.out.println("Ended at... " + new Date());
        // Gather data
        // Write results in the context
        // More work
    }

}
