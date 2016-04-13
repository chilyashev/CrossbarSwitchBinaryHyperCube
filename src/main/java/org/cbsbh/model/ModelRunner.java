package org.cbsbh.model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.cbsbh.Debug;
import org.cbsbh.context.Context;
import org.cbsbh.model.generator.BernoulliGenerator;
import org.cbsbh.model.routing.InputChannel;
import org.cbsbh.model.routing.MPPNetwork;
import org.cbsbh.model.routing.OutputChannel;
import org.cbsbh.model.routing.SMPNode;
import org.cbsbh.model.routing.packet.flit.Flit;

import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;


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
        Debug.println(getClass() + " init");
        //compute number of nodes
        int SMPNodes = 1 << channelCount;

        int arbiterCount = channelCount * bufferCount;

        //encode node ids
        int[] GrayCodes = getGrayCodes(SMPNodes);

        //----big ass cycle for init-ing stuff and things
        //iterate all nodes
        for (int i = 0; i < SMPNodes; i++) {
            int currentNodeID = GrayCodes[i];
            //construct the router
            SMPNode smp = new SMPNode(currentNodeID);
            //for each node compute adjacent nodes
            int[] adjacentChannelIDs = new int[channelCount];
            for (int j = 0; j < channelCount; j++) {
                adjacentChannelIDs[j] = currentNodeID ^ (1 << j); // bit guru magic
            }

            HashMap<Integer, InputChannel> ics = new HashMap<>();
            HashMap<Integer, OutputChannel> ocs = new HashMap<>();


            //populate input/output channel collections
            for (int j : adjacentChannelIDs) {
                InputChannel iChannel = new InputChannel(j, currentNodeID);
                ics.put(j, iChannel);
                OutputChannel oChannel = new OutputChannel(j, currentNodeID);
                ocs.put(j, oChannel);
            }
            smp.setInputChannels(ics);
            smp.setOutputChannels(ocs);
            MPPNetwork.push(smp);
        }

        for (int i = 0; i < SMPNodes; i++) {
            SMPNode smp = MPPNetwork.get(i);
            smp.init();
            // TODO: .init() every mofo.
        }
    }



/*
    (.)(.)
    (Y)
    // Входният канал за всички изходни канали на рутер XXXX е XXXX на рутер YYYY, където YYYY ID-то на изходния канал.
        +--------------------+                            +--------------------+
0001 -> |                    | -> 0001 ---------> 0000 -> |                    |
        |                    |                            |        0001        |
0010 -> |                    | -> 0010
        |        0000        |
0100 -> |                    | -> 0100
        |                    |
1000 -> |                    | -> 1000
        +--------------------+

 */

    @Override
    public void run() {

        // Init phase
        int channelCount = 4;
        int bufferCount = 10;
        Context.getInstance().set("channelCount", channelCount); // TODO: get this from the interface!
        Context.getInstance().set("nodeCount",  1 << channelCount);
        Context.getInstance().set("bufferCountPerInputChannel", bufferCount);
        Context.getInstance().set("messageGenerationFrequency", 1);
        Context.getInstance().set("minMessageSize", 4);
        Context.getInstance().set("maxMessageSize", 56);
        Context.getInstance().set("fifoQueueCount", 3);
        init(channelCount, bufferCount);
        //End of init

        Debug.println("Starting at... " + new Date());
        // Ticking....
        BernoulliGenerator g = new BernoulliGenerator();
        Scanner bblock = new Scanner(System.in);
        while (ticks < 2__0__0) {
            Debug.printf("\n\n====== TICKL-TOCKL №%d ======\n\n", ticks);
//            if(g.newValueReady()){
            if (ticks == 5) {
                // inject
                //Debug.println("New value is being injected!");
                MPPNetwork.get(0).generateMessage();
            }

            MPPNetwork.getInstance().calculateNewStates();
            MPPNetwork.getInstance().tick();
            // Tick for each SMP
            Debug.printf("\n\n====== NO MORE TICKL-TOCKL №%d ======\n\n", ticks);
            ticks++;
            // bblock.nextLine();
        }

        for(SMPNode node: MPPNetwork.getAll()) {
            if(node.sentFlits.size() < 1) {
                continue;
            }
            Debug.printf("Sent flits for %d: ", node.getId());
            for (Flit flit : node.sentFlits) {
                Debug.println("Flit jumps for " + flit.toString());
                for (String jump : flit.history) {
                    Debug.println("" + jump);
                }
                Debug.println("------");
            }
        }

        Debug.println("Ended at... " + new Date());
        Debug.endTheMisery();
        // Gather data
        // Write results in the context
        // More work
    }

}
