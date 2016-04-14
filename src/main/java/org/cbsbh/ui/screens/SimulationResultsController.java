package org.cbsbh.ui.screens;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.cbsbh.context.Context;
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
        Random r = new Random(); // Real science done RIGHT HERE!
        XYChart.Series<Integer, Integer> series = new XYChart.Series<>();
        series.setName("Пакети в движение в даден момент");

        for (int i = 0; i < 10; i++) {
            series.getData().add(new XYChart.Data<>(i + 1, r.nextInt(100)));
        }
        mainChart.getData().add(series);

        series = new XYChart.Series<>();
        series.setName("Нещо друго");

        for (int i = 0; i < 10; i++) {
            series.getData().add(new XYChart.Data<>(i + 1, r.nextInt(50)));
        }
        mainChart.getData().add(series);
    }

}
