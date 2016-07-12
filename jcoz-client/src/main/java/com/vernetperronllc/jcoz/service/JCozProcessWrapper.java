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

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.vernetperronllc.jcoz.agent.JCozProfiler;
import com.vernetperronllc.jcoz.agent.JCozProfilerMBean;
import com.vernetperronllc.jcoz.Experiment;

public class JCozProcessWrapper {
	
	private VirtualMachine vm;
	
	private JCozProfilerMBean mbeanProxy;
	
	public JCozProcessWrapper(VirtualMachineDescriptor descriptor) throws VirtualMachineConnectionException{
		try{
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
		}catch(IOException | AttachNotSupportedException e){
			throw new VirtualMachineConnectionException(e);
		}
	}
	
	
	public void startProfiling() throws JCozException{
		int returnCode = mbeanProxy.startProfiling();
		if(returnCode != 0){
			throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
		}
		
	}
	
	public void endProfiling() throws JCozException{
		int returnCode = mbeanProxy.endProfiling();
		if(returnCode != 0){
			throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
		}
	}
	
	public void setProgressPoint(String className, int lineNo) throws JCozException{
		int returnCode = mbeanProxy.setProgressPoint(className, lineNo);
		if(returnCode != 0){
			throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
		}
	}
	
	public void setScope(String scope) throws JCozException{
		int returnCode = mbeanProxy.setScope(scope);
		if(returnCode != 0){
			throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
		}
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
