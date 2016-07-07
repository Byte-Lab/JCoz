package com.vernetperronllc.jcoz.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.vernetperronllc.jcoz.Experiment;

public class JCozProfiler implements JCozProfilerMBean {
	
	//TODO organize this better and add proper error conditions and return values
	// for example, cannot start an experiment without progress point, scope set
	// 
	private String progressPointClass_ = null;
	private int progressPointLineNo = 0;
	private String currentScope = null;
	private static boolean registered = false;
	
	private List<Experiment> cachedOutput = new ArrayList<>();

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

	public synchronized byte[] getProfilerOutput() throws IOException {
		System.out.println("get Profiler output, numExperiments "+cachedOutput.size());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeInt(cachedOutput.size());
		for (Experiment e : cachedOutput){
			e.serialize(oos);
		}
		System.out.println();
		clearCachedOutput();
		oos.flush();
		return baos.toByteArray();
	}
	
	private synchronized void clearCachedOutput(){
		cachedOutput.clear();
	}
	
	private synchronized void cacheOutput(String classSig, int lineNo, float speedup, long duration, long pointsHit){
		cachedOutput.add(new Experiment(classSig, lineNo, speedup, duration, pointsHit));
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
