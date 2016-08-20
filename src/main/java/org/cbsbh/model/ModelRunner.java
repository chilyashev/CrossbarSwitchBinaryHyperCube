package org.cbsbh.model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.cbsbh.Constants;
import org.cbsbh.Debug;
import org.cbsbh.context.Context;
import org.cbsbh.model.generator.BernoulliGenerator;
import org.cbsbh.model.routing.InputChannel;
import org.cbsbh.model.routing.MPPNetwork;
import org.cbsbh.model.routing.OutputChannel;
import org.cbsbh.model.routing.SMPNode;
import org.cbsbh.model.structures.Flit;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;


/**
 * Takes care of bootstrapping the model, running it, collecting the data and telling the controller that
 * the simulation has finished.
 * Date: 4/25/14 1:46 PM
 *
 * @author Mihail Chilyashev
 */
public class ModelRunner extends Thread {
    Context context;
    EventHandler<ActionEvent> handler;
    private long ticks = 0;
    private boolean waiting = false;
    private boolean initialized = false;
    private boolean stepByStepExecution = false;
    private boolean shouldStop = false;

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

    public void init(int channelCount) {
        if (initialized) {
            return;
        }
        initialized = true;

        Debug.startThePain();
        Debug.println(getClass() + " init");
        //compute number of nodes
        int SMPNodes = 1 << channelCount;

        //int arbiterCount = channelCount * bufferCount;

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
        int channelCount = Context.getInstance().getInteger("channelCount");
        // TODO: get this from the interface!
        /*Context.getInstance().set("channelCount", channelCount);
        Context.getInstance().set("nodeCount", 1 << channelCount);
        Context.getInstance().set("bufferCountPerInputChannel", bufferCount);
        Context.getInstance().set("messageGenerationFrequency", 1);
        Context.getInstance().set("minMessageSize", 4);
        Context.getInstance().set("maxMessageSize", 56);
        Context.getInstance().set("fifoQueueCount", 3);*/
        init(channelCount);
        //End of init

        Debug.println("Starting at... " + new Date());
        // Ticking....
        BernoulliGenerator g = new BernoulliGenerator();
        Random randomSource = new Random();
        int source;
        int msgCount = Context.getInstance().getInteger("messageCount");
        int messages = 0;
        int nodeCount = 1 << channelCount;
        try {
            int MAX_TICK_FOR_GENERATING_MESSAGE = Context.getInstance().getInteger("maxTickForGeneratingMessages");

            while (ticks < Context.getInstance().getInteger("workingTime")) {
                if (shouldStop) {
                    break;
                }
                System.err.println("Tick: " + ticks);
                Debug.printf("\n\n====== TICKL-TOCKL №%d ======\n\n", ticks);
                // Съобщения се пращат, ако:
                // 1. Има съобщение за пращане
                // 2. Моделът е работил достатъчно бързо, за да може да се пращат съобщения
                // 3. Все още не е достигнат максималният такт за пращане на съобщения
                if (g.newValueReady() && ticks > Constants.MODEL_INITIALIZATION_TICK_COUNT && ticks <= MAX_TICK_FOR_GENERATING_MESSAGE && msgCount > 0) {
                    source = randomSource.nextInt(nodeCount - 1); // Отговаря на псевдослучаен метод на изпращане на съобщения
                    if (MPPNetwork.get(source).getMessageToSend().size() == 0) {
                        Debug.printf("New value is being injected in %d!", source);
                        MPPNetwork.get(source).generateMessage();
                        messages++;
                        msgCount--;
                    }
                }

                MPPNetwork.getInstance().calculateNewStates();
                MPPNetwork.getInstance().tick();
                // Tick for each SMP
                Debug.printf("\n\n====== NO MORE TICKL-TOCKL №%d ======\n\n", ticks);
                ticks++;
                context.set("currentModelTick", ticks);

                // If step-by-step simulation is selected, block until someone wakes you up.
                if (stepByStepExecution) {
                    try {
                        waiting = true;
                        synchronized (this) {
                            while (waiting) {
                                wait();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Waiting failed");
                    }
                }
                // bblock.nextLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Simulation failed due to some stupid shit. Fix it.");
            handler.handle(new ActionEvent());
            //throw e;
        }

        System.err.println("End of ticks.");

        //
        int packets = 0, tails = 0, bodies = 0;
        for (SMPNode node : MPPNetwork.getAll()) {
            if (node.sentFlits.size() < 1) {
                continue;
            }
            Debug.printf("Sent flits for %d: ", node.getId());
            for (Flit flit : node.sentFlits) {
                if (flit.getFlitType() == Flit.FLIT_TYPE_HEADER) {
                    packets++;
                }
                if (flit.getFlitType() == Flit.FLIT_TYPE_TAIL) {
                    tails++;
                }
                if (flit.getFlitType() == Flit.FLIT_TYPE_BODY) {
                    bodies++;
                }
                Debug.println("Flit jumps for " + flit.toString());
                for (String jump : flit.history) {
                    Debug.println("" + jump);
                }
                Debug.println("------");
            }
        }
        Debug.printf("Total packets sent (doesn't mean, they got received): %d\n" +
                        "Total tail flits: %d\n" +
                        "Total body flits: %d\n",
                packets,
                tails,
                bodies
        );
        Debug.println("Total messages generated: " + messages);

        Debug.println("Ended at... " + new Date());
        Debug.endTheMisery();
        // Gather data
        // Write results in the context
        // More work
        this.handler.handle(new ActionEvent());
    }

    public synchronized void stopModel() {
        shouldStop = true;
    }

    public synchronized void wakeUp() {
        waiting = false;
        notifyAll();
    }

    public void setStepByStepExecution(boolean stepByStepExecution) {
        this.stepByStepExecution = stepByStepExecution;
    }
}
