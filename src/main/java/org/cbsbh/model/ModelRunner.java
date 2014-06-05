package org.cbsbh.model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.cbsbh.context.Context;
import org.cbsbh.model.routing.InputChannel;
import org.cbsbh.model.routing.InputChannelCollection;
import org.cbsbh.model.routing.OutputChannel;
import org.cbsbh.model.routing.Router;

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

    public ModelRunner(Context context, EventHandler<ActionEvent> handler) {
        this.context = context;
        this.handler = handler;
    }

    void getGrayCodes(ArrayList<Integer> list, int n){
        if(!list.isEmpty()){

        }
    }

    @Override
    public void run() {
        // Init phase
        // OutputChannelCollection.push(output_1....)
        //

        int bufferCount = 5;

        int channelCountPerCommutator = 4;
        int commutators = 16;

        for (int i = 0; i < commutators; i++){
            // Gray code = wtf
            Router router = new Router();
            router.setId(i);
            for(int j = 0; j < channelCountPerCommutator; j++){
                InputChannel in = new InputChannel(j, 5);
                router.getInputChannels().add(in);
                InputChannelCollection.push(router.getId(), in);
            }
        }


        Context.getInstance().set("channelCount", channelCountPerCommutator); // TODO: get this from the interface!
        for(int i = 0; i < channelCountPerCommutator; i++){
            OutputChannel out = new OutputChannel();
            out.setId(i);
        }
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
