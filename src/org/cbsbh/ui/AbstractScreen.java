package org.cbsbh.ui;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * An abstract class for the screens.
 * Date: 4/20/14 2:42 AM
 *
 * @author Mihail Chilyashev
 */
abstract public class AbstractScreen implements Initializable, ControlledScreen {
    /**
     * Used for communicating to the parent controller.
     */
    protected ScreenContainer parent;

    /**
     * TODO: This could be left abstract. We'll talk about it.
     *
     * @param url            ?
     * @param resourceBundle ?
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @Override
    public void setParent(ScreenContainer screen) {
        this.parent = screen;
    }
}
