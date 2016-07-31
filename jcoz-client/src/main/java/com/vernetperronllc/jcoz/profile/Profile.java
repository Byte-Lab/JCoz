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
package com.vernetperronllc.jcoz.profile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

/**
 * A com.vernetperronllc.jcoz.profile for a given application. It contains all speedup data for
 * a given application, and manages the logic for rendering a speedup chart,
 * and logging experiments.
 * 
 * @author David
 */
public class Profile {
	private Map<Integer, LineSpeedup> lineData = new HashMap<>();

	private String process;
	
	private LineChart<Number,Number> lineChart;
	
	RandomAccessFile stream;
	
	private final Map<Integer, XYChart.Series<Number, Number>> seriesMap = new TreeMap<>();
		
	public Profile(String process, LineChart<Number,Number> lineChart) {
		this.process = process;
		
		this.lineChart = lineChart;
		
		this.initializeProfileLogging();	
	}
	
	/**
	 * Render the line speedups for the current set of received experiments.
	 */
	public void renderLineSpeedups() {
		for (LineSpeedup lineSpeedup : lineData.values()) {
			int lineNo = lineSpeedup.getLineNo();
			if (!this.seriesMap.containsKey(lineNo)) {
				XYChart.Series<Number, Number> newSeries = new XYChart.Series<>();
				newSeries.setName("Line " + lineNo);
				this.seriesMap.put(lineNo, newSeries);
				lineChart.getData().add(newSeries);
			}
			XYChart.Series<Number, Number> currSeries = this.seriesMap.get(lineNo);
			try {
				lineSpeedup.renderSeries(currSeries);
			} catch (InsufficientBaselineResultsException e) {
				// Insufficient results -- NO-OP
			}
		}
	}
	
	/**
	 * Add a list of experiments to this com.vernetperronllc.jcoz.profile.
	 * @param experiments List of new experiments to add to com.vernetperronllc.jcoz.profile.
	 */
	public void addExperiments(List<Experiment> experiments) {
		for (Experiment exp : experiments) {
			int lineNo = exp.getLineNo();
			if (!lineData.containsKey(lineNo)) {
				lineData.put(lineNo, new LineSpeedup(exp));
			} else {
				lineData.get(lineNo).addExperiment(exp);
			}
		}
		
		try {
			this.flushExperimentsToCozFile(experiments);
		} catch (IOException e) {
			System.err.println("Unable to flush new experiments to file.");
			e.printStackTrace();
		}
	}

	/**
	 * @return List of all experiments in this profile.
	 */
	public List<Experiment> getExperiments() {
		List<Experiment> experiments = new ArrayList<>();
		for (LineSpeedup lineSpeedup : this.lineData.values()) {
			experiments.addAll(lineSpeedup.getExperiments());
		}
		
		return experiments;
	}

	/**
	 * @return This profile's process name.
	 */
	public String getProcess() {
		return this.process;
	}
	

	/**
	 * @return This profile's line chart.
	 */
	public LineChart<Number,Number> getLineChart() {
		return this.lineChart;
	}

	/**
	 * Flush all pending experiments to the profile and
	 * close the open file reader/writer.
	 * @param experiments Pending experiments to be written.
	 */
	public void flushAndCloseLog(List<Experiment> experiments) {
		try {
			this.flushExperimentsToCozFile(experiments);
			this.stream.close();
		} catch (IOException e) {
			System.err.println("Unable to flush and close stream");
			e.printStackTrace();
		}
	}

	/**
	 * Append a list of experiments to the current coz file.
	 * @param experiments
	 * @throws IOException
	 */
	private void flushExperimentsToCozFile(List<Experiment> experiments) throws IOException {
		StringBuffer profText = new StringBuffer();
		for (Experiment exp : experiments) {
			profText.append(exp.toString() + "\n");
		}
		
		this.stream.writeBytes(profText.toString());
	}
	
	/**
	 * Open a stream to a .coz file. If there are any existing experiments in
	 * the file, add those experiments to the current chart.
	 * @TODO(david): Allow the user to truncate and archive existing profiles.
	 */
	private void initializeProfileLogging() {
		File profile = new File(this.process + ".coz");
		try {
			this.stream = new RandomAccessFile(profile, "rw");
			this.readExperimentsFromLogFile();
			this.renderLineSpeedups();
		} catch (IOException e) {
			System.err.println("Unable to create com.vernetperronllc.jcoz.profile output file or read com.vernetperronllc.jcoz.profile output from file");
			e.printStackTrace();
		}
	}
	
	/**
	 * Read all of the experiments already contained in a com.vernetperronllc.jcoz.profile output.
	 * @throws IOException
	 */
	private void readExperimentsFromLogFile() throws IOException {
		// Iterate through every line in the file, and add 
		while (this.stream.getFilePointer() != this.stream.length()) {
			String line1 = this.stream.readLine();
			String line2 = this.stream.readLine();
			Experiment newExp = new Experiment(line1 + "\n" + line2);
			
			int lineNo = newExp.getLineNo();
			if (!lineData.containsKey(lineNo)) {
				lineData.put(lineNo, new LineSpeedup(newExp));
			} else {
				lineData.get(lineNo).addExperiment(newExp);
			}
		}
	}
}
