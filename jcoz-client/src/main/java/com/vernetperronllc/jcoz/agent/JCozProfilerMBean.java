package com.vernetperronllc.jcoz.agent;

import java.io.IOException;

public interface JCozProfilerMBean {
	
	public int startProfiling();
	
	public int endProfiling();
	
	public int setProgressPoint(String className, int lineNo);
	
	public int setScope(String scope);
	
	public byte[] getProfilerOutput() throws IOException;
	
	public String getCurrentScope();
	
	public String getProgressPoint();
}
