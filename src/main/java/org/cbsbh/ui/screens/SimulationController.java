package org.cbsbh.ui.screens;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.cbsbh.ui.AbstractScreen;
import org.cbsbh.ui.context.Context;

/**
 * Description goes here
 * Date: 4/20/14 3:14 AM
 *
 * @author Mihail Chilyashev
 */
public class SimulationController extends AbstractScreen {

    // FXML controls
    @FXML
    private Label data;
    @FXML
    private ComboBox<String> msgGenerationCombo;
    // eo FXML controls


    private Context context;

    public SimulationController() {
        context = Context.getInstance();
        //...
    }

    @Override
    public void init() {
        data.setText("" + context.get("workingTime"));
    }

}
