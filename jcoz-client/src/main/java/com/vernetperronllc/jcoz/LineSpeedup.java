package com.vernetperronllc.jcoz;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineSpeedup {
	double baselineSpeedup;
	
	List<Experiment> experiments = new ArrayList<Experiment>();
	
	Map<Double, Double> speedupMap = new HashMap<>();
	
	public LineSpeedup(List<Experiment> experiments) {
		this.experiments = new ArrayList<Experiment>(experiments);
		
		this.baselineSpeedup = this.calculateBaselineSpeedup();
		
		Map<Float, List<Experiment>> speedups = this.partitionExperiments();
		
		for (Float speedup : speedups.keySet()) {
			long totalDuration = 0;
			long pointsHit = 0;
			for (Experiment exp : speedups.get(speedup)) {
				pointsHit += exp.getPointsHit();
				totalDuration = exp.getDuration();
			}
			this.speedupMap.put((double)speedup, (double)totalDuration / (double)pointsHit);
		}
	}
	
	public Map<Double, Double> getSpeedupMap() {
		return this.speedupMap;
	}
	
	public double getBaselineSpeedup() {
		return this.baselineSpeedup;
	}
	
	private double calculateBaselineSpeedup() {
		double baselineDuration = 0;
		double baselinePointsHit = 0;
		
		for (Experiment exp : this.experiments) {
			if (exp.getSpeedup() == 0) {
				baselineDuration += exp.getDuration();
				baselinePointsHit += exp.getPointsHit();
			}
		}
		
		return (double)baselineDuration / (double)baselinePointsHit;
	}
	
	private Map<Float, List<Experiment>> partitionExperiments() {
		
		this.experiments.sort(new Comparator<Experiment>() {
			@Override
			public int compare(Experiment o1, Experiment o2) {
				return o1.compareTo(o2);
			}
		});

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
			output.append("Line speedup: " + speedup + ", throughput speedup: " + this.speedupMap.get(speedup) + "\n");
		}
		
		return output.toString();
	}
}
