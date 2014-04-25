package org.cbsbh.ui.screens;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import org.cbsbh.model.ModelRunner;
import org.cbsbh.ui.AbstractScreen;
import org.cbsbh.ui.context.Context;

/**
 * Displays the main screen with the menus and whatnot
 * Date: 4/20/14 1:50 AM
 *
 * @author Mihail Chilyashev
 */
public class MainController extends AbstractScreen {

    Context context;

    // FXML controls
    @FXML private TextField modelWorkTime;
    @FXML private Label errorLabel;
    @FXML
    private Slider messageCountSlider;
    @FXML
    private TextField messageCountValue;
    // eo FXML controls

    public MainController() {
        context = Context.getInstance();
    }

    /**
     * Gets the user's input and starts the model
     * TODO: Validation.
     *
     * @param event An event. Or maybe THE event?
     */
    @FXML
    public void startSimulation(ActionEvent event) {
        boolean fine = true;
        double workingTime = 0;
        errorLabel.setText("");
        //ModelController model = new ModelController();
        ModelRunner runner = new ModelRunner(context, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                parent.showScreen("simulation_results");
            }
        });

        try {
            workingTime = Double.parseDouble(modelWorkTime.getText());
            context.set("workingTime", workingTime);
            // More contextual stuff


            Thread modelThread = new Thread(runner);
            modelThread.start();

        } catch (NumberFormatException e) {
            System.err.println("TODO: Handle this one!");
            errorLabel.setText("Невалидно число.");
            fine = false;
        }

        if (fine) {

            // Setting model parameters in nyah!
            context.set("workingTime", workingTime);
            parent.showScreen("simulation");
        } else {
            // Nothing for now.
            // Maybe later we'll show a popup with an error
        }
    }

    @Override
    public void init() {
        // crap
    }
}
