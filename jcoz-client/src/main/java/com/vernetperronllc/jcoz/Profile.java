package com.vernetperronllc.jcoz;

import java.util.List;

import com.vernetperronllc.jcoz.service.ProfileException;

import java.util.ArrayList;

public class Profile {
	private List<Experiment> experiments = new ArrayList<>();
	
	public Profile(List<Experiment> experiments) {
		this.experiments = experiments;
	}
	
	public double getBaseline(int lineNo) throws ProfileException {
		long totalDuration = 0;
		int numZeroSpeedup = 0;
		for (Experiment exp : experiments) {
			if ((exp.getLineNo() == lineNo) && (exp.getSpeedup() == 0f)) {
				totalDuration += exp.getDuration();
				numZeroSpeedup++;
			}
		}
		
		if (numZeroSpeedup == 0) {
			throw new ProfileException("No zero speedup experiments found.");
		}
		
		return (double)totalDuration / (double)numZeroSpeedup;
	}
}
