package org.cbsbh.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Description goes here
 * Date: 4/25/14 2:40 PM
 *
 * @author Mihail Chilyashev
 */
public class MessageController {
    public static void showError(Scene parentScene, String title, String message, String details) {
        try {
            FXMLLoader loader = new FXMLLoader(MessageController.class.getResource("/screens/alertDialog.fxml"));
            Parent loadedScreen = (Parent) loader.load();
            //ControlledScreen controller = loader.getController();
            Group root = new Group();
            root.getChildren().addAll(loadedScreen);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            // don't give a rat's ass!
        }
    }
}
