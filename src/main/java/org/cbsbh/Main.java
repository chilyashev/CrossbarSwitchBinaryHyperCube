
package org.cbsbh;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.cbsbh.ui.ScreenContainer;

/**
 * The project entry point
 * Date: 4/17/14 4:41 PM
 *
 * @author Mihail Chilyashev
 */
public class Main extends Application {

    public static final String SCREEN_MENU = "menu";
    public static final String SCREEN_MAIN = "main";
    public static final String SCREEN_SIMULATION = "simulation";
    public static final String SCREEN_SIMULATION_RESULTS = "simulation_results";
    public static final String SCREEN_STEP_BY_STEP = "svg_step_by_step";


    @Override
    public void start(Stage primaryStage) throws Exception {

        ScreenContainer mainScreen = new ScreenContainer();

        // Adding the screens
        mainScreen.loadScreen(SCREEN_MENU, "/screens/menu.fxml");
        mainScreen.loadScreen(SCREEN_MAIN, "/screens/main.fxml");
        mainScreen.loadScreen(SCREEN_SIMULATION, "/screens/simulation.fxml");
        mainScreen.loadScreen(SCREEN_SIMULATION_RESULTS, "/screens/simulation_results.fxml");
        // mainScreen.loadScreen("step_by_step", "/screens/simulation_step_by_step.fxml");
        mainScreen.loadScreen(SCREEN_STEP_BY_STEP, "/screens/svg_simulation_step_by_step.fxml");

        // Pass the stage
        mainScreen.setStage(primaryStage);

        // Showing the main screen
        mainScreen.showScreen(SCREEN_MENU);
        //mainScreen.showScreen("main");
        //mainScreen.showScreen("step_by_step");
        //mainScreen.showScreen("svg_step_by_step");

        primaryStage.setOnCloseRequest(event -> {
            mainScreen.closeScreen();
        });

        // Displaying the stage
        Group root = new Group();
        root.getChildren().addAll(mainScreen);
        Scene scene = new Scene(root, 800, 640);
        primaryStage.setScene(scene);
        //primaryStage.minHeightProperty().bind(mainScreen.heightProperty());
        //primaryStage.minWidthProperty().bind(mainScreen.widthProperty());

        AnchorPane.setTopAnchor(mainScreen, 0.0);
        AnchorPane.setRightAnchor(mainScreen, 0.0);
        AnchorPane.setBottomAnchor(mainScreen, 0.0);
        AnchorPane.setLeftAnchor(mainScreen, 0.0);
        primaryStage.setWidth(mainScreen.getWidth());
        primaryStage.setHeight(mainScreen.getHeight());

        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            root.prefWidth((double)newValue);
        });
        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            root.prefHeight((double)newValue);

        });
        mainScreen.minWidthProperty().bind(primaryStage.widthProperty());
        mainScreen.minHeightProperty().bind(primaryStage.heightProperty());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);



        /*Context context = Context.getInstance();
        ModelRunner runner = new ModelRunner(context, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                System.err.println("K. Starting Sirmulation");
            }
        });

        try {
            double workingTime = 500;
            context.set("workingTime", workingTime);
            // More contextual stuff

            Context.getInstance().set("channelCount", 4);
            Context.getInstance().set("nodeCount", 16);
            Context.getInstance().set("maxMessageSize", 3);
            Context.getInstance().set("bufferCountPerInputChannel", 3); // TODO!
            Context.getInstance().set("messageGenerationFrequency", 1); // TODO
            Context.getInstance().set("minMessageSizel", 4); // TODO
            Context.getInstance().set("fifoQueueCount", 3); // TODO


            Thread modelThread = new Thread(runner);
            modelThread.start();

        } catch (NumberFormatException e) {
            System.err.println("TODO: Handle this one!");

        }*/
    }
}
