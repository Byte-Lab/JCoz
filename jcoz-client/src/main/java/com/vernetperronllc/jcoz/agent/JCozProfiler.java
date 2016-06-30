package com.vernetperronllc.jcoz.agent;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class JCozProfiler implements JCozProfilerMBean {

	private String progressPointClass_ = null;
	private int progressPointLineNo = 0;
	private String currentScope = null;
	private static boolean registered = false;
	
	private StringBuilder cachedOutput = new StringBuilder();

	public synchronized int startProfiling(){
		return startProfilingNative();
	}
	
	public native int startProfilingNative();

	public synchronized int endProfiling(){
		return endProfilingNative();
	}
	
	public native int endProfilingNative();

	public synchronized int setProgressPoint(String className, int lineNo){
		return setProgressPointNative(className, lineNo);
	}
	
	public native int setProgressPointNative(String className, int lineNo);

	public synchronized String getProfilerOutput() {
		return cachedOutput.toString();
	}
	
	private synchronized void clearCachedOutput(){
		cachedOutput = new StringBuilder();
	}
	
	private synchronized void cacheOutput(String output){
		cachedOutput.append(output);
	}
	
	public synchronized String getCurrentScope(){
		return currentScope;
	}
	
	public synchronized int setScope(String scopePackage){
		int scopeReturn = setScopeNative(scopePackage);
		if(scopeReturn == 0){
			currentScope = scopePackage;
		}
		return scopeReturn;
	}
	
	public native int setScopeNative(String scopePackage);
	
	public synchronized String getProgressPoint(){
		return progressPointClass_ +":"+progressPointLineNo;
	}
	
	public synchronized static void registerProfilerWithMBeanServer(){
		if(!registered){
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			JCozProfiler mbean = new JCozProfiler(); 
	        mbs.registerMBean(mbean, getMBeanName()); 
		} catch (InstanceAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		}
	}
	
	public static void main(String[] args) throws InterruptedException{
		while(true){
			System.out.println("Sleeping");
			Thread.sleep(5000);
			
		}
	}
	
	public static ObjectName getMBeanName(){
		try {
			return new ObjectName(JCozProfiler.class.getPackage().getName()+":type="+JCozProfiler.class.getSimpleName());
		} catch (MalformedObjectNameException e) {
			// do nothing, this should never be malformed
			throw new Error(e);
		}
		
	}
}
