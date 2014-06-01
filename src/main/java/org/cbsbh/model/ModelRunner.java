package org.cbsbh.model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.cbsbh.context.Context;

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

    @Override
    public void run() {
        // Init phase
        // OutputChannelCollection.push(output_1....)
        //
        Context.getInstance().set("channelCount", 4); // TODO: get this from the interface!
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
