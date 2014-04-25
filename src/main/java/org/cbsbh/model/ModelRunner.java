package org.cbsbh.model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.cbsbh.ui.context.Context;


/**
 * Description goes here
 * Date: 4/25/14 1:46 PM
 *
 * @author Mihail Chilyashev
 */
public class ModelRunner implements Runnable {
    Context context;
    EventHandler<ActionEvent> handler;

    public ModelRunner(Context context, EventHandler<ActionEvent> handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void run() {
        // Start model loop
        // Gather data
        // Write results in the context

        try{
            Thread.sleep(10000);
        }catch (Exception e){
            /// who cares
        }
        // More work
        handler.handle(new ActionEvent());
    }

}
