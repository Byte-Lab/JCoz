/*
 * This file is part of JCoz.
 *
 * JCoz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JCoz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JCoz.  If not, see <https://www.gnu.org/licenses/>.
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
