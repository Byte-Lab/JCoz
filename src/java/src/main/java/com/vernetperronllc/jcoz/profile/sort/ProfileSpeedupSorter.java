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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vernetperronllc.jcoz.profile.ClassSpeedup;
import com.vernetperronllc.jcoz.profile.InsufficientBaselineResultsException;
import com.vernetperronllc.jcoz.profile.LineSpeedup;

import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

/**
 * An interface for sorting profile speedups. A class that implements this
 * can be passed to {@code Profile#renderLineSpeedups} to create an ordered
 * list of series.
 * @author David
 */
public abstract class ProfileSpeedupSorter {
	protected List<XYChart.Series<Number, Number>> getSeriesFromClassSpeedups(
			Collection<ClassSpeedup> classSpeedups, int minSamples) {
		List<Series<Number, Number>> series = new ArrayList<>();
		for (ClassSpeedup classSpeedup : classSpeedups) {
			for (LineSpeedup lineSpeedup : classSpeedup.getLineSpeedups()) {
				try {
					series.add(lineSpeedup.renderSeries(minSamples));
				} catch (InsufficientBaselineResultsException e) {
					// Insufficient results -- NO-OP
				}
			}
		}
		
		return series;
	}
	
	public abstract List<XYChart.Series<Number, Number>> createCharts(
			Collection<ClassSpeedup> collection, int minSamples);
}
