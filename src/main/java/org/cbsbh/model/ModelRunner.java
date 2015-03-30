package org.cbsbh.model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.cbsbh.context.Context;
import org.cbsbh.model.routing.InputChannel;
import org.cbsbh.model.routing.MPPNetwork;
import org.cbsbh.model.routing.OutputStateStructure;
import org.cbsbh.model.routing.Router;
import org.cbsbh.model.structures.SMP;

import java.util.Date;
import java.util.HashMap;


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
    //private ArrayList<SMP> MPPNetwork;

    public ModelRunner(Context context, EventHandler<ActionEvent> handler) {
        this.context = context;
        this.handler = handler;
        //this.MPPNetwork = new ArrayList<>();
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
        int SMPNodes = 1 << channelCount;

        int arbiterCount = channelCount * bufferCount;

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

            HashMap<Integer, InputChannel> ics = new HashMap<>();
            HashMap<Integer, OutputStateStructure> ocs = new HashMap<>();
            //populate input/output channel collections
            for (int nextRouterId : adjacentChannelIDs) {
                OutputStateStructure oChannel = new OutputStateStructure(nextRouterId, currentNodeID, bufferCount, (1 << channelCount));
                ocs.put(nextRouterId, oChannel);
            }
/*
    (.)(.)
    (Y)
        +--------------------+
0001 -> |                    | -> 0001
        |                    |
0010 -> |                    | -> 0010
        |        0000        |
0100 -> |                    | -> 0100
        |                    |
1000 -> |                    | -> 1000
        +--------------------+

 */
            /*for (int j : adjacentChannelIDs) {
                InputChannel iChannel = new InputChannel(j, currentNodeID, ocs);
                ics.put(j, iChannel);
            }*/

            //construct the router
            Router router = new Router(ics, ocs);
            SMP smp = new SMP(currentNodeID, router, adjacentChannelIDs);
            //include the bad boy in the network
            MPPNetwork.push(smp);
        }
    }

    @Override
    public void run() {

        // Init phase
        int channelCount = 4;
        int bufferCount = 10;
        Context.getInstance().set("channelCount", channelCount); // TODO: get this from the interface!
        Context.getInstance().set("bufferCountPerInputChannel", bufferCount);
        Context.getInstance().set("messageGenerationFrequency", 1);
        Context.getInstance().set("minMessageSize", 4);
        Context.getInstance().set("maxMessageSize", 56);
        init(channelCount, bufferCount);
        //End of init

        System.out.println("Starting at... " + new Date());
        // Ticking....
        while (ticks < 1_000_00) {
            // Tick for each SMP

            ticks++;
        }

        // Gather data
        // Write results in the context
        // More work
    }

}
