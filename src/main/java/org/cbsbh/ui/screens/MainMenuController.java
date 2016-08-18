package org.cbsbh.ui.screens;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.cbsbh.Main;
import org.cbsbh.ui.AbstractScreen;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by mihail.chilyashev on 18.8.2016 г..
 */
public class MainMenuController  extends AbstractScreen {

    // FXML Controls
    @FXML
    public Label titleText;
    // eo FXML Controls

    @Override
    public void init() {
        setTitle("Главно меню");
    }

    public void gotoStepByStep(ActionEvent actionEvent) {
        parent.showScreen(Main.SCREEN_STEP_BY_STEP);
    }

    public void gotoSimulation(ActionEvent actionEvent) {
        parent.showScreen(Main.SCREEN_MAIN);
    }

    public void gotoAbout(ActionEvent actionEvent) {
        throw new NotImplementedException();
    }

    public void exitApplication(ActionEvent actionEvent) {
        parent.getStage().close();
        Platform.exit();
    }
}

