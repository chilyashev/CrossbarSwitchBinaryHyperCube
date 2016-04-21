package org.cbsbh.ui.screens.graph_visualisation;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Created by Mihail Chilyashev on 4/21/16.
 * All rights reserved, unless otherwise noted.
 */
public class PacketController {

    EventHandler<Event> onEnterHandler;
    EventHandler<Event> onExitHandler;

    // FXML controls
    @FXML
    public Button button;
    // eo FXML controls

    public void mouseEntered(Event event) {
        if (onEnterHandler != null) {
            onEnterHandler.handle(event);
        }
    }

    public void mouseExited(Event event) {
        if (onExitHandler != null) {
            onExitHandler.handle(event);
        }
    }

    public void setOnExitHandler(EventHandler<Event> onExitHandler) {
        this.onExitHandler = onExitHandler;
    }

    public void setOnEnterHandler(EventHandler<Event> onEnterHandler) {
        this.onEnterHandler = onEnterHandler;
    }
}
