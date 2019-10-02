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
