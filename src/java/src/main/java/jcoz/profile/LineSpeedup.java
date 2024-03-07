/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package jcoz.profile;

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

            this.updateSpeedupMap();
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
    private void updateSpeedupMap() throws InsufficientBaselineResultsException {
        this.baselineSpeedup = this.calculateBaselineSpeedup();
        this.speedupMap.clear();

        Map<Float, List<Experiment>> speedups = this.groupExperimentsBySpeedups();

        for (Map.Entry<Float, List<Experiment>> speedup : speedups.entrySet()) {
            List<Experiment> speedupExperiments = speedup.getValue();
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

    public List<Experiment> getExperiments() {
        return this.experiments;
    }
}
