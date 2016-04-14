package org.cbsbh.ui.screens;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.cbsbh.context.Context;
import org.cbsbh.model.ModelRunner;
import org.cbsbh.ui.AbstractScreen;
import org.cbsbh.ui.SliderStringConverter;

/**
 * Displays the main screen with the menus and whatnot
 * Date: 4/20/14 1:50 AM
 *
 * @author Mihail Chilyashev
 */
public class MainController extends AbstractScreen {

    Context context;
    Thread modelThread = null;

    // FXML controls
    @FXML
    public Button simulationStartButton;
    @FXML
    private TextField modelWorkTime;
    @FXML
    private Label errorLabel;
    @FXML
    private Slider messageCountSlider;
    @FXML
    private TextField messageCountValue;
    @FXML
    public Slider packetCountSlider;
    @FXML
    public TextField packetCountValue;
    @FXML
    public ComboBox algorithmCombo;
    @FXML
    public TextField algorithmValue;
    @FXML
    public Slider smpCountSlider;
    @FXML
    public TextField smpCountValue;
    @FXML
    public Slider channelCountSlider;
    @FXML
    public TextField channelCountValue;
    @FXML
    public ComboBox messageGenerationMethod;
    @FXML
    public Slider flitCountSlider;
    @FXML
    public TextField flitCountValue;
    @FXML
    public Slider fifoQueueCountSlider;
    @FXML
    public TextField fifoQueueCountValue;
    // eo FXML controls

    public MainController() {
        context = Context.getInstance();
    }

    /**
     * Gets the user's input and starts the model
     * TODO: Validation.
     *
     * @param event An event. Or maybe THE event?
     */
    @FXML
    public void startSimulation(ActionEvent event) {
        boolean fine = true;
        double workingTime = 0;
        errorLabel.setText("");
        // Enterprise-grade input validation
        if (modelWorkTime.getText().length() < 1) {
            modelWorkTime.setText("500");
        }

        ModelRunner runner = new ModelRunner(context, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //parent.showScreen("simulation_results");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ((SimulationController) parent.getCurrentScreenController()).finishSimulation(); // Ugly af, but works, so stfu
                    }
                });

            }
        });

        // Set up the model properties
        try {
            workingTime = Double.parseDouble(modelWorkTime.getText());

            // Setting up some context stuff to be used in the model
            context.set("workingTime", workingTime);
            context.set("messageCount", messageCountSlider.valueProperty().intValue());
            context.set("channelCount", channelCountSlider.valueProperty().intValue());
            context.set("nodeCount", smpCountSlider.valueProperty().intValue());
            context.set("maxMessageSize", packetCountSlider.valueProperty().intValue());
            context.set("maxPacketSize", flitCountSlider.valueProperty().intValue());
            context.set("messageGenerationFrequency", Double.parseDouble(algorithmValue.getText()));
            System.err.println(context.get("messageGenerationFrequency"));
            context.set("fifoQueueCount", fifoQueueCountSlider.valueProperty().intValue());

        } catch (NumberFormatException e) {
            System.err.println("TODO: Handle this one!");
            errorLabel.setText("Невалидно число.");
            fine = false;
        }

        if (fine) {
            // Setting model parameters in nyah!
            parent.showScreen("simulation");
            modelThread = new Thread(runner);
            modelThread.start();
        } else {
            // Nothing for now.
            // Maybe later we'll show a popup with an error
        }
    }

    @Override
    public void init() {
        setTitle("Настройки на модела");
        /*messageCountSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            messageCountValue.setText(String.valueOf(newValue.intValue()));
        });

        packetCountSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            packetCountValue.setText(String.valueOf(newValue.intValue()));
        });

        flitCountSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            flitCountValue.setText(String.valueOf(newValue.intValue()));
        });

        smpCountSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            smpCountValue.setText(String.valueOf(newValue.intValue()));
        });

         */

        channelCountSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            //channelCountValue.setText(String.valueOf(newValue.intValue()));
            smpCountSlider.setValue(1 << newValue.intValue());
        });

        /*messageCountSlider.setValue(1);
        packetCountSlider.setValue(3);
        flitCountSlider.setValue(3);
        channelCountSlider.valueProperty().setValue(channelCountSlider.getMin());*/
        smpCountSlider.setValue(1 << (int) channelCountSlider.getValue());

        SliderStringConverter converter = new SliderStringConverter();
        // Binding of ~Isaac~ controls
        messageCountValue.textProperty().bindBidirectional(messageCountSlider.valueProperty(), converter);
        packetCountValue.textProperty().bindBidirectional(packetCountSlider.valueProperty(), converter);
        flitCountValue.textProperty().bindBidirectional(flitCountSlider.valueProperty(), converter);
        smpCountValue.textProperty().bindBidirectional(smpCountSlider.valueProperty(), converter);
        channelCountValue.textProperty().bindBidirectional(channelCountSlider.valueProperty(), converter);
        fifoQueueCountValue.textProperty().bindBidirectional(fifoQueueCountSlider.valueProperty(), converter);

    }

    public void reset() {
        if (modelThread != null) {
            try {
                modelThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
