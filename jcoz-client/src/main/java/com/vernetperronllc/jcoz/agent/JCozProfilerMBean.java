package com.vernetperronllc.jcoz.agent;

public interface JCozProfilerMBean {
	
	public int startProfiling();
	
	public int endProfiling();
	
	public int setProgressPoint(String className, int lineNo);
	
	public int setScope(String scope);
	
	public String getProfilerOutput();
	
	public String getCurrentScope();
	
	public String getProgressPoint();
}
