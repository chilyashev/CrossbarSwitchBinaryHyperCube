package org.cbsbh.ui.screens;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import org.cbsbh.context.Context;
import org.cbsbh.ui.AbstractScreen;

/**
 * Description goes here
 * Date: 4/20/14 3:14 AM
 *
 * @author Mihail Chilyashev
 */
public class SimulationController extends AbstractScreen {

    public GridPane main;
    // FXML controls
    @FXML
    private Label data;
    @FXML
    private ComboBox<String> msgGenerationCombo;
    @FXML
    public ProgressIndicator loader;
    @FXML
    public Label simulatingLabel;
    @FXML
    public Button backButton;
    // eo FXML controls


    private Context context;

    public SimulationController() {
        context = Context.getInstance();
        //...
    }

    @Override
    public void init() {
        backButton.setVisible(false);
        data.setText("" + context.get("workingTime"));
        String ids[] = new String[]{
                "channelCount",
                "nodeCount",
                "maxMessageSize",
                "messageGenerationFrequency",
                "fifoQueueCount",
        };


    }

    public void finishSimulation() {
        loader.setVisible(false);
        simulatingLabel.setVisible(false);
        backButton.setVisible(true);
        data.setText("Симулацията завърши.");

    }

    public void backToReality(ActionEvent actionEvent) {
        ((MainController) parent.getScreenController("main")).reset();
        parent.showScreen("main");
    }
}
