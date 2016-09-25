package org.cbsbh.ui.screens;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.cbsbh.Main;
import org.cbsbh.context.Context;
import org.cbsbh.model.statistics.DataCollector;
import org.cbsbh.ui.AbstractScreen;

import java.util.Random;

/**
 * Description goes here
 * Date: 4/20/14 3:14 AM
 *
 * @author Mihail Chilyashev
 */
public class SimulationResultsController extends AbstractScreen {

    // FXML controls
    @FXML
    public Label generatedMessageCount;
    @FXML
    public Label generatedPacketCount;
    @FXML
    public Label transmittedPacketCount;
    @FXML
    public Label droppedPacketCount;
    @FXML
    public LineChart<Integer, Integer> mainChart;
    // eo FXML controls

    private Context context;

    public SimulationResultsController() {
        context = Context.getInstance();
        //...
    }

    @Override
    public void init() {
        setTitle("Резултати от изпълнението на симулацията");

        // Some stats
        generatedPacketCount.setText(String.valueOf(context.getInteger("generatedPacketsCount")));

        // Charts
        DataCollector.generatedPackets.setName("Генерирани пакети по време");
        mainChart.getData().add(DataCollector.generatedPackets);
    }

    public void goBack(ActionEvent actionEvent) {
        parent.showScreen(Main.SCREEN_MAIN);
    }
}
