package org.cbsbh.ui.screens.graph_visualisation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Created by Mihail Chilyashev on 4/20/16.
 * All rights reserved, unless otherwise noted.
 */
public class NodeDetailsController {

    // FXML controls
    @FXML
    public Label mainLabel;
    // eo FXML controls

    public void setText(String text) {
        mainLabel.setText(text);
    }
}
