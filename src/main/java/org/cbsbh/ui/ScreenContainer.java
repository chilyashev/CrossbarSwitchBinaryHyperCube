package org.cbsbh.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;

/**
 * All the screens get handled by this class.
 * It keeps them stored in a HashMap instead of keeping them stacked and having to redraw everything all the time.
 * Date: 4/20/14 2:17 AM
 *
 * @author Mihail Chilyashev
 */
public class ScreenContainer extends StackPane {
    /**
     * A HashMap containing the current screens.
     * The key is the screen id, the value is the screen itself
     */
    private HashMap<String, Node> screens = screens = new HashMap<>();

    /**
     * Keeps references to all the controllers
     */
    private HashMap<String, ControlledScreen> controllers = new HashMap<>();

    /**
     * The primary stage. Currently only used to set the title.
     */
    private Stage stage;

    /**
     * Used to get stuff related to the current screen being displayed
     */
    private String currentScreenId;

    /**
     * Loads a screen and adds it to the screen map
     *
     * @param screenId The screen's ID
     * @param resource the screen's FXML
     * @return true if the screen got loaded successfully
     */
    public boolean loadScreen(String screenId, String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Parent loadedScreen = (Parent) loader.load();
            ControlledScreen controller = loader.getController();
            if (controller == null) {
                // TODO: Handle exceptions in a nicer way (with error dialogs perhaps?). Screw this boolean if-checking nonsense!
                throw new Exception("All screens must have a controller. What the hell do you think you are doing?");
            }
            controller.setParent(this);
            addScreen(screenId, loadedScreen, controller);
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(1);
            return false;
        }
    }

    /**
     * @param name
     * @param screen
     * @param controller
     */
    public void addScreen(String name, Node screen, ControlledScreen controller) {
        screens.put(name, screen);
        controllers.put(name, controller);
    }

    /**
     * Switches to the specified screen.
     *
     * @param screenId the ID of the screen to be shown
     * @return true if screen exists and gets shown successfully
     */
    public boolean showScreen(final String screenId) {
        currentScreenId = screenId;
        if (screens.get(screenId) != null) { //screen loaded
            final DoubleProperty opacity = opacityProperty();

            /*
                If there is a screen currently being shown, we fade it out, add the new one, and fade it in.
                By doing this the StackPane used as a base keeps only one screen at any time, thus enhancing performance.
             */
            if (!getChildren().isEmpty()) {
                Timeline fade = new Timeline(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(opacity, 1.0)),
                        new KeyFrame(new Duration(30), new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                // Remove the currently displayed screen after it finishes fading out
                                getChildren().remove(0);
                                // Add the new screen at the old one's place
                                getChildren().add(0, screens.get(screenId));
                                // tell the controller to do whatever it should do when the screen is starting
                                //controllers.get(screenId).init();
                                //((Node)screens.get(screenId)).getScene().getRoot()
                                Timeline fadeIn = new Timeline(
                                        new KeyFrame(Duration.ZERO,
                                                new KeyValue(opacity, 0.0)),
                                        new KeyFrame(new Duration(30),
                                                new KeyValue(opacity, 1.0)));
                                fadeIn.play();
                            }
                        }, new KeyValue(opacity, 0.0)));
                fade.play();
            } else {
                // No screens are currently being displayed, so we just add the new one and fad it in
                setOpacity(0.0);
                getChildren().add(screens.get(screenId));
                Timeline fadeIn = new Timeline(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(opacity, 0.0)),
                        new KeyFrame(new Duration(10),
                                new KeyValue(opacity, 1.0)));
                fadeIn.play();
            }

            // tell the controller to do whatever it should do when the screen is starting
            ControlledScreen controlledScreen = controllers.get(screenId);
            controlledScreen.init();
            if (stage != null && controlledScreen.getTitle() != null && controlledScreen.getTitle().length() > 0) {
                stage.setTitle(controlledScreen.getTitle());
            }
            return true;
        } else { // Something went wront
            // TODO: Seriously... the exceptions thing?
            System.err.println("Something went wrong and the screen didn't get loaded\n");
            return false;
        }
    }

    public ControlledScreen getCurrentScreenController(){
        return controllers.get(currentScreenId);
    }
    public ControlledScreen getScreenController(String screenId){
        return controllers.get(screenId);
    }

    public Node getCurrentScreen() {
        return screens.get(currentScreenId);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void closeScreen() {
        //getCurrentScreenController().close()
    }
}
