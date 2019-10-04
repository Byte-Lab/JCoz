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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcoz.client.ui.VisualizeProfileScene;
import jcoz.profile.sort.ProfileSpeedupSorter;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

/**
 * A jcoz.profile for a given application. It contains all speedup data for
 * a given application, and manages the logic for rendering a speedup chart,
 * and logging experiments.
 * 
 * @author David
 */
public class Profile {
	private Map<String, ClassSpeedup> classSpeedups = new HashMap<>();

	private String process;
	
	private LineChart<Number,Number> lineChart;
	
	RandomAccessFile stream;
	
	public Profile(String process, LineChart<Number,Number> lineChart) {
		this.process = process;
		
		this.lineChart = lineChart;
		
		this.initializeProfileLogging();	
	}
	
	/**
	 * Render the line speedups for the current set of received experiments.
	 */
	public synchronized void renderLineSpeedups(
			int minSamples,
			int numSeries,
			ProfileSpeedupSorter sorter) {
		// Always clear data because the user may have chosen a different sorting.
		lineChart.getData().clear();
		List<XYChart.Series<Number, Number>> series =
				sorter.createCharts(this.classSpeedups.values(), minSamples);
		for (int i = 0; i< Math.min(numSeries, series.size()); i++) {
			lineChart.getData().add(series.get(i));
		}
	}
	
	/**
	 * Add a list of experiments to this jcoz.profile.
	 * @param experiments List of new experiments to add to jcoz.profile.
	 */
	public synchronized void addExperiments(List<Experiment> experiments) {
		for (Experiment exp : experiments) {
      System.out.println(exp);
			String classSig = exp.getClassSig();
			if (!this.classSpeedups.containsKey(classSig)) {
				this.classSpeedups.put(classSig, new ClassSpeedup(exp));
			} else {
				this.classSpeedups.get(classSig).addExperiment(exp);
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
	public synchronized List<Experiment> getExperiments() {
		List<Experiment> experiments = new ArrayList<>();
		for (ClassSpeedup classSpeedup : this.classSpeedups.values()) {
			experiments.addAll(classSpeedup.getExperiments());
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
	public synchronized void flushAndCloseLog(List<Experiment> experiments) {
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
			if (this.lineChart != null) {
				this.renderLineSpeedups(
						VisualizeProfileScene.DEFAULT_MIN_SAMPLES,
						VisualizeProfileScene.DEFAULT_NUM_SERIES,
						VisualizeProfileScene.DEFAULT_SORTER);
			}
		} catch (IOException e) {
			System.err.println("Unable to create jcoz.profile output file or read jcoz.profile output from file");
			e.printStackTrace();
		}
	}
	
	/**
	 * Read all of the experiments already contained in a jcoz.profile output.
	 * @throws IOException
	 */
	private void readExperimentsFromLogFile() throws IOException {
		// Iterate through every line in the file, and add 
		while (this.stream.getFilePointer() != this.stream.length()) {
			String line1 = this.stream.readLine();
			String line2 = this.stream.readLine();

			Experiment newExp = new Experiment(line1 + "\n" + line2);
      System.out.println(newExp);
			
			String classSig = newExp.getClassSig();
			if (!this.classSpeedups.containsKey(classSig)) {
				this.classSpeedups.put(classSig, new ClassSpeedup(newExp));
			} else {
				this.classSpeedups.get(classSig).addExperiment(newExp);
			}
		}
	}
}
