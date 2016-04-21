package org.cbsbh.ui.screens.graph_visualisation;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Created by Mihail Chilyashev on 4/19/16.
 * All rights reserved, unless otherwise noted.
 */
public class NodeController {

    EventHandler<Event> onEnterHandler;
    EventHandler<Event> onExitHandler;

    // FXML controls
    @FXML
    public Button nodeButton;
    // eo FXML controls


    public NodeController() {
    }

    public void setText(String text) {
        nodeButton.setText(text);
    }

    public void setTooltip(Tooltip tooltip) {
        nodeButton.setTooltip(tooltip);
    }

    public void setLocation(int x, int y) {
        nodeButton.setLayoutX(x);
        nodeButton.setLayoutY(y);
    }

    public void mouseEntered(Event event) {
        //nodeButton.setBlendMode(BlendMode.HARD_LIGHT);
        if (onEnterHandler != null) {
            onEnterHandler.handle(event);
        }
    }

    public void mouseExited(Event event) {
        //nodeButton.setBlendMode(null);
        if (onExitHandler != null) {
            onExitHandler.handle(event);
        }
    }

    public void setOnEnterHandler(EventHandler<Event> hoverHandler) {
        this.onEnterHandler = hoverHandler;
    }

    public void setOnExitHandler(EventHandler<Event> hoverHandler) {
        this.onExitHandler = hoverHandler;
    }

    public void resetColor() {
        nodeButton.setStyle(null);
    }
}
