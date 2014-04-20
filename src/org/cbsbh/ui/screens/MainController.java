package org.cbsbh.ui.screens;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    // eo FXML controls

    public MainController() {
        context = Context.getInstance();
    }

    /**
     * Gets the user's input and starts the model
     * TODO: Validation.
     * @param event An event. Or maybe THE event?
     */
    @FXML
    public void startSimulation(ActionEvent event){
        boolean fine = true;
        double workingTime = 0;
        errorLabel.setText("");
        try{
            workingTime = Double.parseDouble(modelWorkTime.getText());
            context.set("workingTime", workingTime);
        }catch(NumberFormatException e){
            System.err.println("TODO: Handle this one!");
            errorLabel.setText("Невалидно число.");
            fine = false;
        }

        if(fine){
            // TODO: Figure out a way to tell the new screen it's being shown. Maybe an event listener?
            parent.showScreen("simulation");
        }else{
            // Nothing for now.
            // Maybe later we'll show a popup with an error
        }
    }
}
