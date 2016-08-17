/*
 * NOTICE
 *
 * Copyright (c) 2016 David C Vernet and Matthew J Perron. All rights reserved.
 *
 * Unless otherwise noted, all of the material in this file is Copyright (c) 2016
 * by David C Vernet and Matthew J Perron. All rights reserved. No part of this file
 * may be reproduced, published, distributed, displayed, performed, copied,
 * stored, modified, transmitted or otherwise used or viewed by anyone other
 * than the authors (David C Vernet and Matthew J Perron),
 * for either public or private use.
 *
 * No part of this file may be modified, changed, exploited, or in any way
 * used for derivative works or offered for sale without the express
 * written permission of the authors.
 *
 * This file has been modified from lightweight-java-profiler
 * (https://github.com/dcapwell/lightweight-java-profiler). See APACHE_LICENSE for
 * a copy of the license that was included with that original work.
 */
package com.vernetperronllc.jcoz.client.ui;

import java.util.List;

import com.vernetperronllc.jcoz.client.cli.TargetProcessInterface;
import com.vernetperronllc.jcoz.profile.Experiment;
import com.vernetperronllc.jcoz.profile.Profile;
import com.vernetperronllc.jcoz.profile.sort.MaximizeThroughputSorter;
import com.vernetperronllc.jcoz.profile.sort.ProfileSpeedupSorter;
import com.vernetperronllc.jcoz.service.JCozException;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;

public class VisualizeProfileScene {

	private static VisualizeProfileScene vpScene = null;

	public static int DEFAULT_MIN_SAMPLES = 5;
	public static int DEFAULT_NUM_SERIES = 5;
	public static ProfileSpeedupSorter DEFAULT_SORTER = MaximizeThroughputSorter.getInstance();
	
	private final GridPane grid = new GridPane();

	private final Scene scene;

	// Text elements
	final Text scenetitle = new Text();

	// Controls
	private final Button stopProfilingButton = new Button("Stop profiling");
	private final Button experimentsConsoleButton = new Button("Print experiments to console");
	private final TextField minSamplesText = new TextField();

	// Visualization    
	private Timeline visualizationUpdateTimeline;
	private LineChart<Number,Number> lineChart;
	
	// Profile
	Profile profile;

	TargetProcessInterface client;



	/** Disable constructor */
	private VisualizeProfileScene(final Stage stage) {
		// Set layout of grid
		this.grid.setHgap(10);
		this.grid.setVgap(10);
		this.grid.setPadding(new Insets(25, 25, 25, 25));

		this.scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		int currRow = 0;
		this.grid.add(this.scenetitle, 0, currRow++, 2, 1);

		/*** Controls ***/
		currRow = this.setUpProfileControls(currRow, stage);
		
		/*** VISUALIZATION ***/
		currRow = this.setUpVisualizationSection(currRow);

		this.scene = new Scene(this.grid, 980, 600);
	}
	
	private int setUpProfileControls(int currRow, final Stage stage) {
		this.experimentsConsoleButton.setOnAction(new EventHandler<ActionEvent>() { 
			@Override
			public void handle(ActionEvent event) {
				printExperimentsToConsole();
			}
		});
		this.grid.add(this.experimentsConsoleButton, 0, currRow++);
		this.stopProfilingButton.setTooltip(new Tooltip("End profiling and choose a new process"));
		this.stopProfilingButton.setOnAction(new EventHandler<ActionEvent>() { 
			@Override
			public void handle(ActionEvent event) {
				try {
					visualizationUpdateTimeline.stop();
					profile.flushAndCloseLog(client.getProfilerOutput());
					client.endProfiling();
				} catch (JCozException e) {
					System.err.println("Unable to end profiling");
					e.printStackTrace();
				}
				stage.setScene(PickProcessScene.getPickProcessScene(stage));
			}
		});
		this.grid.add(this.stopProfilingButton, 0, currRow++);

		final Label minSamplesLabel = new Label("Min samples:");
		this.grid.add(minSamplesLabel, 0, currRow);
		this.grid.add(this.minSamplesText, 1, currRow);
		this.minSamplesText.setTextFormatter(
        		new TextFormatter<>(new NumberStringConverter()));
		this.minSamplesText.setText(VisualizeProfileScene.DEFAULT_MIN_SAMPLES + "");
		
		// Listen for updates to min samples and re-render chart on changes.  
		this.minSamplesText.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable,
		            String oldValue, String newValue) {
		    	try {
		    		int minSamples = Integer.parseInt(newValue);
		    		profile.renderLineSpeedups(
		    				minSamples, VisualizeProfileScene.DEFAULT_NUM_SERIES,
		    				VisualizeProfileScene.DEFAULT_SORTER);
		    	} catch (NumberFormatException e) { /* NO-OP */ }
		    }
		});
		currRow++;

		return currRow;
	}
	
	/**
	 * Set up the chart /visualization section of the VisualizeProfileScene
	 * @param currRow The row where the chart should be placed.
	 * @return Next available row after setting scene section.
	 */
	private int setUpVisualizationSection(int currRow) {
		visualizationUpdateTimeline = new Timeline(new KeyFrame(
				Duration.millis(5000),
				new EventHandler<ActionEvent>() { 
					@Override
					public void handle(ActionEvent event) {
						updateGraphVisualization();
					}
				}));
		visualizationUpdateTimeline.setCycleCount(Animation.INDEFINITE);
				
		
		final NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel("Line Speedup %");
		xAxis.setUpperBound(1.0);
		xAxis.setLowerBound(0.0);

		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Throughput improvement %");
		yAxis.setUpperBound(1.0);
		yAxis.setLowerBound(-1.0);
		
		this.lineChart = new LineChart<Number,Number>(xAxis,yAxis);
		lineChart.setTitle("Speedup visualization");

		this.grid.add(this.lineChart, 0, currRow, 10, 10);
		currRow += 10;

		return currRow;
	}

	public Scene getScene() {
		return this.scene;
	}

	private void setClient(TargetProcessInterface client, String processName) {
		this.lineChart.getData().clear();
		this.profile = new Profile(processName, this.lineChart);
		this.scenetitle.setText("Profiling process -- " + profile.getProcess());
		this.client = client;
	}

	/**
	 * Update the currently displayed graph visualization.
	 * This is currently called from a timer task in the
	 * scene constructor.
	 */
	private synchronized void updateGraphVisualization() {
		try {
			List<Experiment> experiments = client.getProfilerOutput();
			
			// Don't re-render if we don't have any more experiments.
			if (experiments.size() == 0) {
				return;
			}
			
			String minSamplesStr = this.minSamplesText.getText();
			int minSamples = VisualizeProfileScene.DEFAULT_MIN_SAMPLES;
			try {
				if (minSamplesStr != null && !minSamplesStr.equals("")) {
					int minParse = Integer.parseInt(minSamplesStr);
					if (minParse >= 1) {
						minSamples = minParse;
					}
				}
			} catch (NumberFormatException e) { /* NO-OP if field is invalid */ }
			this.profile.addExperiments(experiments);
			this.profile.renderLineSpeedups(
					minSamples,
					VisualizeProfileScene.DEFAULT_NUM_SERIES,
					VisualizeProfileScene.DEFAULT_SORTER);
		} catch (JCozException e) {
			System.err.println("Unable to get profiler experiment outputs");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Helper function for debugging. Prints all current experiments to the console.
	 */
	private synchronized void printExperimentsToConsole() {
		List<Experiment> experiments = this.profile.getExperiments();
		for (Experiment exp : experiments) {
			System.out.println(exp);
		}
		System.out.println("Printed " + experiments.size() + " experiments to the console...");
	}

	public static Scene getVisualizeProfileScene(
			TargetProcessInterface client, Stage stage, String processName) {
		if (VisualizeProfileScene.vpScene == null) {
			VisualizeProfileScene.vpScene = new VisualizeProfileScene(stage);
		}
		vpScene.setClient(client, processName);
		vpScene.visualizationUpdateTimeline.play();
		return VisualizeProfileScene.vpScene.getScene();
	}
}

