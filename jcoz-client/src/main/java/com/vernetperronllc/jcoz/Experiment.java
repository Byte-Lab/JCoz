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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;

/**
 * Simple bean which represents a single experiment
 * @author matt
 *
 */
public class Experiment implements Comparable<Experiment> {

	/** 
	 * public constructor, pass all data for experiment
	 * @param classSig signature of experiment class
	 * @param lineNo experiment line number
	 * @param speedup selected experiment speedup
	 * @param duration length of experiment - pause time
	 * @param pointsHit number of time the progress point was hit in the experiment
	 */
	public Experiment(String classSig, int lineNo, float speedup,
			long duration, long pointsHit) {
		this.classSig = classSig;
		this.lineNo = lineNo;
		this.speedup = speedup;
		this.duration = duration;
		this.pointsHit = pointsHit;
	}
	
	/**
	 * Create an experiment object from a coz string in the form
	 * returned by Experiment.toString().
	 * @param cozFormatExperiment
	 */
	public Experiment (String exp) {
		int selectedIndex = exp.indexOf("selected=");
		int lineNoIndex = exp.indexOf(':', selectedIndex);
		int speedupIndex = exp.indexOf("\tspeedup=");
		int durationIndex = exp.indexOf("\tduration=");
		int firstLineEndIndex = exp.indexOf('\n');
		int pointsHitIndex = exp.indexOf("\tdelta=");
		
		this.classSig = exp.substring(selectedIndex + ("selected=").length(), lineNoIndex);
		this.lineNo = Integer.parseInt(exp.substring(lineNoIndex + 1, speedupIndex));
		this.speedup = Float.parseFloat(
				exp.substring(speedupIndex + "\tspeedup=".length(), durationIndex));
		this.duration = Long.parseLong(
				exp.substring(durationIndex + "\tduration=".length(), firstLineEndIndex));
		this.pointsHit = Long.parseLong(
				exp.substring(pointsHitIndex + "\tdelta=".length()));
	}
	
	/**
	 * experiment class signature
	 */
	private String classSig;
	/**
	 * experiment line number
	 */
	private int lineNo;
	/**
	 * experiment speedup
	 */
	private float speedup;
	/** 
	 * duration of experiment
	 */
	private long duration;
	/**
	 * number of times the progress point was hit
	 */
	private long pointsHit;
	
	
	/*
	 * getters
	 */
	public String getClassSig() {
		return classSig;
	}
	public int getLineNo() {
		return lineNo;
	}
	public float getSpeedup() {
		return speedup;
	}
	public long getDuration() {
		return duration;
	}
	public long getPointsHit() {
		return pointsHit;
	}
	
	/**
	 * serialize the object into an object output stream
	 */
	public void serialize(ObjectOutputStream oos) throws IOException{
		oos.writeUTF(classSig);
		oos.writeInt(lineNo);
		oos.writeFloat(speedup);
		oos.writeLong(duration);
		oos.writeLong(pointsHit);
	}
	
	/**
	 * output the experiment in the coz format
	 */
	@Override
	public String toString() {
		return "experiment\tselected=" + classSig + ":" + lineNo +
         "\tspeedup=" + speedup + "\tduration="
        + duration +"\n" + "progress-point\tname=end-to-end\ttype=source\tdelta="
        +pointsHit;
	}
	
	/**
	 * deserialize an Experiment form an object input stream
	 * @param ois input stream containing object
	 * @return next experiment in the input stream
	 * @throws IOException
	 */
	public static Experiment deserialize(ObjectInputStream ois) throws IOException{
		return new Experiment(ois.readUTF(), ois.readInt(), ois.readFloat(), ois.readLong(), ois.readLong());
	}
	
	@Override
	public int compareTo(Experiment arg0) {
		if (this.getSpeedup() < arg0.getSpeedup()) {
			return -1;
		} else if (this.getSpeedup() > arg0.getSpeedup()) {
			return 1;
		} else {
			if (this.getPointsHit() < arg0.getPointsHit()) {
				return -1;
			} else if (this.getPointsHit() > arg0.getPointsHit()) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}
