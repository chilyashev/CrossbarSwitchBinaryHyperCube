package org.cbsbh.ui.screens;

import org.cbsbh.ui.AbstractScreen;
import org.cbsbh.ui.context.Context;

/**
 * Description goes here
 * Date: 4/20/14 3:14 AM
 *
 * @author Mihail Chilyashev
 */
public class SimulationResultsController extends AbstractScreen {


    private Context context;

    public SimulationResultsController() {
        context = Context.getInstance();
        //...
    }

    @Override
    public void init() {
        System.out.println(context.get("something"));
    }

}
