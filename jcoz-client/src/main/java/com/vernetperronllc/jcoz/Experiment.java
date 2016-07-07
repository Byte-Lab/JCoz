package com.vernetperronllc.jcoz;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Experiment{

	public Experiment(String classSig, int lineNo, float speedup,
			long duration, long pointsHit) {
		this.classSig = classSig;
		this.lineNo = lineNo;
		this.speedup = speedup;
		this.duration = duration;
		this.pointsHit = pointsHit;
	}
	
	
	private String classSig;
	private int lineNo;
	private float speedup;
	private long duration;
	private long pointsHit;
	
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
	
	public void serialize(ObjectOutputStream oos) throws IOException{
		oos.writeUTF(classSig);
		oos.writeInt(lineNo);
		oos.writeFloat(speedup);
		oos.writeLong(duration);
		oos.writeLong(pointsHit);
	}
	
	@Override
	public String toString() {
		return "experiment\tselected=" + classSig + ":" + lineNo +
         "\tspeedup=" + speedup + "\tduration="
        + duration +"\n" + "progress-point\tname=end-to-end\ttype=source\tdelta="
        +pointsHit;
	}
	
	
	public static Experiment deserialize(ObjectInputStream ois) throws IOException{
		return new Experiment(ois.readUTF(), ois.readInt(), ois.readFloat(), ois.readLong(), ois.readLong());
	}
}
