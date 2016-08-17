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
package com.vernetperronllc.jcoz.profile.sort;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vernetperronllc.jcoz.profile.ClassSpeedup;

import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

public class MaximizeThroughputSorter extends ProfileSpeedupSorter {
	private static final MaximizeThroughputSorter INSTANCE = new MaximizeThroughputSorter();
	
	private MaximizeThroughputSorter() {}
	
	@Override
	public List<Series<Number, Number>> createCharts(
			Collection<ClassSpeedup> classSpeedups, int minSamples) {

		List<Series<Number, Number>> series =
				this.getSeriesFromClassSpeedups(classSpeedups, minSamples);
		
		// Sort on the series, ordering primarily on the maximum throughput increase, and
		// secondarily on the minimum effort required to achieve that throughput increase.
		Collections.sort(series, new Comparator<Series<Number, Number>>() {
			@Override
			public int compare(Series<Number, Number> series0, Series<Number, Number> series1) {
				List<XYChart.Data<Number, Number>> series0Data = series0.getData();
				List<XYChart.Data<Number, Number>> series1Data = series1.getData();
				
				int series0Max = -1;
				int series0XValue = -1;
				int series1Max = -1;
				int series1XValue = -1;
				for (XYChart.Data<Number, Number> data : series0Data) {
					Number yValue = data.getYValue();
					if (yValue.intValue() > series0Max) {
						series0Max = yValue.intValue();
						series0XValue = data.getXValue().intValue();
					}
				}
				for (XYChart.Data<Number, Number> data : series1Data) {
					Number yValue = data.getYValue();
					if (yValue.intValue() > series1Max) {
						series1Max = yValue.intValue();
						series1XValue = data.getXValue().intValue();
					}
				}
				
				// If the throughput increase is the same, elevate the series
				// that requires the least effort to get there (in other words,
				// series0 should be weighted higher than series1 if series0's x value
				// is less than series1's x value).
				if (series0Max == series1Max) {
					return series1XValue - series0XValue;
				}
				
				return series0Max - series1Max;
			}
		});
		
		return series;
	}

	public static ProfileSpeedupSorter getInstance() {
		return MaximizeThroughputSorter.INSTANCE;
	}
}
