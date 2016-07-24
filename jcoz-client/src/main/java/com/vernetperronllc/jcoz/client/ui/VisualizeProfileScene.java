package com.vernetperronllc.jcoz.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.vernetperronllc.jcoz.Experiment;
import com.vernetperronllc.jcoz.LineSpeedup;
import com.vernetperronllc.jcoz.client.cli.TargetProcessInterface;
import com.vernetperronllc.jcoz.service.JCozException;
import com.vernetperronllc.jcoz.service.VirtualMachineConnectionException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class VisualizeProfileScene {
    
	private static VisualizeProfileScene vpScene = null;
	
	private final GridPane grid = new GridPane();
	
	private final Scene scene;

	// Text elements
	private final Text processNameText = new Text();
	
	// Controls
	private final TextField experimentLength = new TextField();
	private final Button viewGraphButton = new Button();
	
	TargetProcessInterface client;
	
	/** Disable constructor */
	private VisualizeProfileScene(final Stage stage) {
		// Set layout of grid
		this.grid.setHgap(10);
        this.grid.setVgap(10);
        this.grid.setPadding(new Insets(25, 25, 25, 25));

        final Text scenetitle = new Text("Profiling process");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        int currRow = 0;
        this.grid.add(scenetitle, 0, currRow++, 2, 1);
        
        /*** Text elements ***/
        final Label processNameLabel = new Label("Process name");
        this.grid.add(processNameLabel, 0, currRow);
        this.grid.add(this.processNameText, 1, currRow);
        currRow++;
        
        /*** Controls ***/
        
        // Experiment length text element
        final Label lengthLabel = new Label("Experiment length:");
        this.experimentLength.setText("50");
        this.grid.add(lengthLabel, 0, currRow);
        this.grid.add(this.experimentLength, 1, currRow);
        currRow++;

        this.viewGraphButton.setText("View profile output");
        this.viewGraphButton.setOnAction(new EventHandler<ActionEvent>() { 
            @Override
            public void handle(ActionEvent event) {
            	List<Experiment> experiments = VisualizeProfileScene.getTestExperiments();
                //defining the axes
                final NumberAxis xAxis = new NumberAxis();
                final NumberAxis yAxis = new NumberAxis();
                xAxis.setLabel("Line Speedup %");
                yAxis.setLabel("Throughput improvement %");
                
                //creating the chart
                final LineChart<Number,Number> lineChart = 
                        new LineChart<Number,Number>(xAxis,yAxis);
                        
                lineChart.setTitle("Line xy Speedup");
                //defining a series
                XYChart.Series series = new XYChart.Series();
                series.setName("Line xy speedup");
                //populating the series with data
                LineSpeedup lineSpeedup = new LineSpeedup(experiments);
                Map<Double, Double> speedups = lineSpeedup.getSpeedupMap();
                for (double speedup : speedups.keySet()) {
                	double speedupActual = speedups.get(speedup) / lineSpeedup.getBaselineSpeedup();
                    series.getData().add(new XYChart.Data(speedup, speedupActual));
                }
                
                Scene scene  = new Scene(lineChart,800,600);
                lineChart.getData().add(series);
                grid.add(lineChart, 0, 5, 10, 10);
            }
        });
        this.grid.add(this.viewGraphButton, 0, 10);
        
        this.scene = new Scene(this.grid, 980, 600);
	}
	
	public Scene getScene() {
		return this.scene;
	}
	
	public void setExperimentLength(int length) {
		this.experimentLength.setText(length + "" );
	}
	
	public int getExperimentLength() {
		return Integer.parseInt(this.experimentLength.getText());
	}
	
	private void setClient(TargetProcessInterface client) {
		this.processNameText.setText("Set descriptor name");
	}
	
	public static Scene getVisualizeProfileScene(TargetProcessInterface client, Stage stage) {
		if (VisualizeProfileScene.vpScene == null) {
			VisualizeProfileScene.vpScene = new VisualizeProfileScene(stage);
		}
		vpScene.setClient(client);
		return VisualizeProfileScene.vpScene.getScene();
	}

	public static List<Experiment> getTestExperiments() {
		List<Experiment> experiments = new ArrayList<>();
		
		long duration = 100000L;
		long basePtsHit = 50;
		for (int i = 0; i < 1000; i++) {
			String name = "Experiment_" + i;
			int lineNo = 35;
			
			float speedup = (i / 50) * .05f;
			long currDuration = duration + (i / 10);
			Experiment newExp = new Experiment(name, lineNo, speedup, currDuration, basePtsHit + i);
			experiments.add(newExp);
		}
		
		return experiments;
	}
}

