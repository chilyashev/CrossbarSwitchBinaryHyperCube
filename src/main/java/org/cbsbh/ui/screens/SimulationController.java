package org.cbsbh.ui.screens;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import org.cbsbh.context.Context;
import org.cbsbh.ui.AbstractScreen;

/**
 * Description goes here
 * Date: 4/20/14 3:14 AM
 *
 * @author Mihail Chilyashev
 */
public class SimulationController extends AbstractScreen {


    // FXML controls

    @FXML
    public Label messageCount;
    @FXML
    public Label maxMessageSize;
    @FXML
    public Label maxFlitCount;
    @FXML
    public Label messageGenerationFrequency;
    @FXML
    public Label messageGenerationMethod;
    @FXML
    public Label channelCount;
    @FXML
    public Label smpCount;
    @FXML
    public Label fifoQueueCount;
    @FXML
    public Label workingTime;
    @FXML
    public Label nodeCount;
    @FXML
    public GridPane mainGrid;
    @FXML
    public Label transmissionMode;
    @FXML
    public Button resultsButton;
    @FXML
    private Label data;
    @FXML
    private ComboBox<String> msgGenerationCombo;
    @FXML
    public ProgressIndicator loader;
    @FXML
    public Label simulatingLabel;
    @FXML
    public Button backButton;
    // eo FXML controls


    private Context context;

    public SimulationController() {
        context = Context.getInstance();
        //...
    }

    @Override
    public void init() {
        loader.setVisible(true);
        simulatingLabel.setVisible(true);

        backButton.setVisible(false);
        resultsButton.setVisible(false);
        data.setVisible(false);

        nodeCount.setText(context.getString("nodeCount"));
        workingTime.setText(context.getString("workingTime"));
        messageCount.setText(context.getString("messageCount"));
        maxMessageSize.setText(context.getString("maxMessageSize"));
        maxFlitCount.setText(context.getString("maxPacketSize"));
        channelCount.setText(context.getString("channelCount"));
        fifoQueueCount.setText(context.getString("fifoQueueCount"));
        messageGenerationFrequency.setText(context.getString("messageGenerationFrequency"));


        // A miserable attempt to make rows have alternating backgrounds
        /*mainGrid.getChildren().stream().filter(node -> GridPane.getRowIndex(node) % 2 == 0).forEach(node -> {
            node.setStyle("-fx-background-color: whitesmoke");
        });*/

    }

    public void finishSimulation() {
        loader.setVisible(false);
        simulatingLabel.setVisible(false);

        backButton.setVisible(true);
        resultsButton.setVisible(true);

        data.setText("Симулацията завърши.");
        data.setVisible(true);
    }

    public void backToReality(ActionEvent actionEvent) {
        ((MainController) parent.getScreenController("main")).reset();
        this.init();
        parent.showScreen("main");
    }

    public void showSimulationResults(ActionEvent actionEvent) {
        parent.showScreen("simulation_results");
    }
}
