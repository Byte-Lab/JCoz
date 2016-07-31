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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

/**
 * An object containing speedup information for a single line in a com.vernetperronllc.jcoz.profile.
 * @author David
 */
public class LineSpeedup {
	double baselineSpeedup;
	
	List<Experiment> experiments;
	
	Map<Double, Double> speedupMap = new HashMap<>();
	
	int lineNo;
	
	public LineSpeedup(int lineNo, List<Experiment> experiments)
			throws InsufficientBaselineResultsException {
		this.experiments = new ArrayList<Experiment>(experiments);
		
		this.lineNo = lineNo;
	
		this.updateSpeedupMap();
	}
	
	public LineSpeedup(Experiment exp) {
		this.experiments = new ArrayList<Experiment>();
		this.experiments.add(exp);
		
		this.lineNo = exp.getLineNo();
	}

	public Map<Double, Double> getSpeedupMap() {
		return this.speedupMap;
	}
	
	public double getBaselineSpeedup() {
		return this.baselineSpeedup;
	}
	
	public int getLineNo() {
		return this.lineNo;
	}
	
	public void addExperiment(Experiment exp) {
		this.experiments.add(exp);
	}
	
	/**
	 * Update the speedup map to use the latest experiment data.
	 * @throws InsufficientBaselineResultsException
	 */
	private void updateSpeedupMap() throws InsufficientBaselineResultsException {
		this.baselineSpeedup = this.calculateBaselineSpeedup();
		this.speedupMap.clear();
		
		Map<Float, List<Experiment>> speedups = this.groupExperimentsBySpeedups();
		
		for (Float speedup : speedups.keySet()) {
			long totalDuration = 0;
			long pointsHit = 0;
			for (Experiment exp : speedups.get(speedup)) {
				pointsHit += exp.getPointsHit();
				totalDuration += exp.getDuration();
			}
			double preBaseSpeedup = (double)totalDuration / (double)pointsHit;
			double actualSpeedup = (this.baselineSpeedup - preBaseSpeedup) / this.baselineSpeedup;
			this.speedupMap.put((double)speedup, actualSpeedup);
		}
	}
	
	/**
	 * Calculate the baseline speedup from the latest experiment data.
	 * @return The new baseline speedup.
	 * @throws InsufficientBaselineResultsException
	 */
	private double calculateBaselineSpeedup() throws InsufficientBaselineResultsException {
		double baselineDuration = 0;
		double baselinePointsHit = 0;
		
		for (Experiment exp : this.experiments) {
			if (exp.getSpeedup() == 0) {
				baselineDuration += exp.getDuration();
				baselinePointsHit += exp.getPointsHit();
			}
		}
		
		if (baselinePointsHit <= 5) {
			throw new InsufficientBaselineResultsException(
					"Insufficient baseline results. Expected at least 5, found: " + baselinePointsHit);
		}
		return baselineDuration / baselinePointsHit;
	}
	
	/**
	 * Group the list of experiments into a separate list by speedup.
	 * @return
	 */
	private Map<Float, List<Experiment>> groupExperimentsBySpeedups() {
		Map<Float, List<Experiment>> partitionedExperiments = new HashMap<>();
		for (Experiment exp : this.experiments) {
			float speedup = exp.getSpeedup();
			if (!partitionedExperiments.containsKey(speedup)) {
				partitionedExperiments.put(speedup, new ArrayList<Experiment>());
			}
			partitionedExperiments.get(speedup).add(exp);
		}
		
		return partitionedExperiments;
	}
	
	public String toString() {
		StringBuffer output = new StringBuffer();
		for (double speedup : this.speedupMap.keySet()) {
			output.append(
					"Line speedup: " + speedup +
					", throughput speedup: " + this.speedupMap.get(speedup) +
					"\n");
		}
		
		return output.toString();
	}
	
	/**
	 * Given a series to be displayed on a chart, render all of the data points
	 * corresponding to speedup values for this LineSpeedup object.
	 * @param series The series in which to render this LineSpeedup's data points. 
	 * @throws InsufficientBaselineResultsException 
	 */
	public void renderSeries(XYChart.Series<Number, Number> series) throws InsufficientBaselineResultsException {
        //populating the series with data
		List<XYChart.Data<Number, Number>> speedupList = new ArrayList<>();

		this.updateSpeedupMap();
		
		// Populate list with data points. 
        for (double speedup : speedupMap.keySet()) {
            speedupList.add(new XYChart.Data<Number, Number>(speedup, speedupMap.get(speedup)));
        }

        // Add data points to series.
        ObservableList<XYChart.Data<Number, Number>> dataPoints =
        		FXCollections.observableList(speedupList);
        series.setData(dataPoints);
	}

	public List<Experiment> getExperiments() {
		return this.experiments;
	}
}
