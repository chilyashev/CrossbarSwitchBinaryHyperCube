
package org.cbsbh;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cbsbh.ui.ScreenContainer;

/**
 * The project entry point
 * Date: 4/17/14 4:41 PM
 *
 * @author Mihail Chilyashev
 */
public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {

        ScreenContainer mainScreen = new ScreenContainer();

        // Adding the screens
        mainScreen.loadScreen("main", "/screens/main.fxml");
        mainScreen.loadScreen("simulation", "/screens/simulation.fxml");
        mainScreen.loadScreen("simulation_results", "/screens/simulation_results.fxml");


        // Showing the main screen
        mainScreen.showScreen("main");

        // Displaying the stage
        Group root = new Group();
        root.getChildren().addAll(mainScreen);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String[] args) {
        boolean a = false,
        b = true;
        System.out.println(a & b);
        System.out.println(a && b);


        //launch(args);
    }
}
