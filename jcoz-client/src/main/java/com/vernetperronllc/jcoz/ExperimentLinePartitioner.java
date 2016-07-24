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
package com.vernetperronllc.jcoz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Given a list of experiments returned from the profiler, partition them
 * into different LineSpeedup objects.
 * @author David
 */
public class ExperimentLinePartitioner {
	
	public static List<LineSpeedup> getLineSpeedups(List<Experiment> experiments) {
		Map<Integer, List<Experiment>> lineToExperimentMap = groupExperimentsByLine(experiments);
		
		return getLineSpeedupsFromExperiments(lineToExperimentMap);
	}
	
	private static Map<Integer, List<Experiment>> groupExperimentsByLine(List<Experiment> experiments) {
		Map<Integer, List<Experiment>> lineToExpMap = new TreeMap<>();
		for (Experiment exp : experiments) {
			int lineNumber = exp.getLineNo();
			if (!lineToExpMap.containsKey(lineNumber)) {
				lineToExpMap.put(lineNumber, new ArrayList<Experiment>());
			}
			lineToExpMap.get(lineNumber).add(exp);
		}

		return lineToExpMap;
	}
	
	private static List<LineSpeedup> getLineSpeedupsFromExperiments(
			Map<Integer, List<Experiment>> lineToExperimentMap) {
		List<LineSpeedup> lineSpeedups = new ArrayList<>();
		
		for (Integer lineNo : lineToExperimentMap.keySet()) {
			try {
				lineSpeedups.add(new LineSpeedup(lineNo, lineToExperimentMap.get(lineNo)));
			} catch (InsufficientBaselineResultsException e) {
				// NO-OP, just skip this line until there are enough results.
			}
		}
		
		return lineSpeedups;
	}
}
