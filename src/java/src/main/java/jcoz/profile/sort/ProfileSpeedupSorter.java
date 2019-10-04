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
package jcoz.profile.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jcoz.profile.ClassSpeedup;
import jcoz.profile.InsufficientBaselineResultsException;
import jcoz.profile.LineSpeedup;

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
