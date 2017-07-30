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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSpeedup {
	private Map<Integer, LineSpeedup> lineSpeedups = new HashMap<>();
	
	public ClassSpeedup(Experiment exp) {
		lineSpeedups.put(exp.getLineNo(), new LineSpeedup(exp));
	}

	public void addExperiment(Experiment exp) {
		int lineNo = exp.getLineNo();
		if (!this.lineSpeedups.containsKey(lineNo)) {
			this.lineSpeedups.put(lineNo, new LineSpeedup(exp));
		} else {
			this.lineSpeedups.get(lineNo).addExperiment(exp);
		}
	}

	public Collection<? extends Experiment> getExperiments() {
		List<Experiment> experiments = new ArrayList<>();
		for (LineSpeedup lineSpeedup : this.lineSpeedups.values()) {
			experiments.addAll(lineSpeedup.getExperiments());
		}
		
		return experiments;
	}

	public Collection<LineSpeedup> getLineSpeedups() {
		return this.lineSpeedups.values();
	}
}
