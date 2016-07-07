package com.vernetperronllc.jcoz.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.vernetperronllc.jcoz.agent.JCozProfiler;
import com.vernetperronllc.jcoz.agent.JCozProfilerMBean;
import com.vernetperronllc.jcoz.Experiment;

public class JCozProcessWrapper {
	
	private VirtualMachine vm;
	
	private JCozProfilerMBean mbeanProxy;
	
	public JCozProcessWrapper(VirtualMachineDescriptor descriptor) throws Exception {
		vm = VirtualMachine.attach(descriptor);
		vm.startLocalManagementAgent();
		Properties props = vm.getAgentProperties();
		String connectorAddress =
		        props.getProperty("com.sun.management.jmxremote.localConnectorAddress");
	    System.out.println(connectorAddress);
	    JMXServiceURL url = new JMXServiceURL(connectorAddress);
	    JMXConnector connector = JMXConnectorFactory.connect(url);
        MBeanServerConnection mbeanConn = connector.getMBeanServerConnection();
        mbeanProxy = JMX.newMXBeanProxy(mbeanConn, 
        	    JCozProfiler.getMBeanName(),  JCozProfilerMBean.class);
	}
	
	// TODO these methods should through exceptions not only error conditions
	
	public int startProfiling(){
		
		return mbeanProxy.startProfiling();
	}
	
	public int endProfiling(){
		return mbeanProxy.endProfiling();
	}
	
	public int setProgressPoint(String className, int lineNo){
		return mbeanProxy.setProgressPoint(className, lineNo);
	}
	
	public int setScope(String scope){
		return mbeanProxy.setScope(scope);
	}
	
	public List<Experiment> getProfilerOutput() throws IOException{
		List<Experiment> experiments = new ArrayList<>();
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(mbeanProxy.getProfilerOutput()));
    	int numExperiments = ois.readInt();
    	for (int j = 0; j < numExperiments; j++){
    		experiments.add(Experiment.deserialize(ois));
    	}
    	return experiments;
	}
	
	public String getCurrentScope(){
		return mbeanProxy.getCurrentScope();
	}
	
	public String getProgressPoint(){
		return mbeanProxy.getProgressPoint();
	}
}
