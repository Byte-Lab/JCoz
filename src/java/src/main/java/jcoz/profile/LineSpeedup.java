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
package jcoz.profile;

import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import jcoz.client.ui.VisualizeProfileScene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An object containing speedup information for a single line in a jcoz.profile.
 *
 * @author David
 */
public class LineSpeedup {
    private double baselineSpeedup;

    private List<Experiment> experiments;

    private Map<Double, Double> speedupMap = new HashMap<>();

    private int lineNo;

    public LineSpeedup(int lineNo, List<Experiment> experiments)
            throws InsufficientBaselineResultsException {
        this.experiments = new ArrayList<>(experiments);
        this.lineNo = lineNo;

        this.updateSpeedupMap(VisualizeProfileScene.DEFAULT_MIN_SAMPLES);
    }

    public LineSpeedup(Experiment exp) {
        this.experiments = new ArrayList<>();
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
     *
     * @throws InsufficientBaselineResultsException
     */
    private void updateSpeedupMap(int minSamples) throws InsufficientBaselineResultsException {
        this.baselineSpeedup = this.calculateBaselineSpeedup();
        this.speedupMap.clear();

        Map<Float, List<Experiment>> speedups = this.groupExperimentsBySpeedups();

        for (Map.Entry<Float, List<Experiment>> speedup : speedups.entrySet()) {

            List<Experiment> speedupExperiments = speedup.getValue();

            // Don't plot speedup measurements with fewer than the minimum required samples.
            if (speedupExperiments.size() < minSamples) {
                continue;
            }

            long totalDuration = 0;
            long pointsHit = 0;

            for (Experiment exp : speedupExperiments) {
                pointsHit += exp.getPointsHit();
                totalDuration += exp.getDuration();
            }

            // avoid divide-by-zero
            if ((pointsHit > 0) && (this.baselineSpeedup > 0)) {
                double preBaseSpeedup = (double) totalDuration / (double) pointsHit;
                double actualSpeedup = (this.baselineSpeedup - preBaseSpeedup) / this.baselineSpeedup;
                this.speedupMap.put((double) speedup.getKey(), actualSpeedup);
            }
        }
    }

    /**
     * Calculate the baseline speedup from the latest experiment data.
     *
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
     *
     * @return
     */
    private Map<Float, List<Experiment>> groupExperimentsBySpeedups() {
        Map<Float, List<Experiment>> partitionedExperiments = new HashMap<>();
        for (Experiment exp : this.experiments) {
            float speedup = exp.getSpeedup();
            if (!partitionedExperiments.containsKey(speedup)) {
                partitionedExperiments.put(speedup, new ArrayList<>());
            }
            partitionedExperiments.get(speedup).add(exp);
        }

        return partitionedExperiments;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        for (Map.Entry<Double, Double> entry : speedupMap.entrySet()) {
            output.append("Line speedup: ")
                    .append(entry.getKey())
                    .append(", throughput speedup: ")
                    .append(entry.getValue())
                    .append("\n");
        }

        return output.toString();
    }

    /**
     * Given a series to be displayed on a chart, render all of the data points
     * corresponding to speedup values for this LineSpeedup object.
     *
     * @throws InsufficientBaselineResultsException When there aren't a sufficient number
     *                                              of samples to render this series.
     */
    public Series<Number, Number> renderSeries(int minSamples) throws InsufficientBaselineResultsException {
        this.updateSpeedupMap(minSamples);

        //populating the series with data
        Series<Number, Number> series = new Series<>();
        String classSig = this.experiments.get(0).getClassSig();
        series.setName(classSig + ":" + this.getLineNo());

        // Populate list with data points.
        for (Map.Entry<Double, Double> entry : speedupMap.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        return series;
    }

    public List<Experiment> getExperiments() {
        return this.experiments;
    }
}
