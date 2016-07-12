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
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.vernetperronllc.jcoz.agent.JCozProfiler;
import com.vernetperronllc.jcoz.Experiment;
import com.vernetperronllc.jcoz.agent.JCozProfilerMBean;

public class JCozClient {

	public static void main(String[] args) throws Exception{
		List<VirtualMachineDescriptor> vmList = VirtualMachine.list();
		VirtualMachine vm = null;
		for(VirtualMachineDescriptor vmDesc : vmList){
			System.out.println(vmDesc.displayName());
			if (vmDesc.displayName().endsWith("TestThreadSerial")){
				vm = VirtualMachine.attach(vmDesc);
				break;
			}
		}
		vm.startLocalManagementAgent();
		Properties props = vm.getAgentProperties();
		for(Entry<Object, Object> e : props.entrySet()){
			System.out.println(e);
		}
	    String connectorAddress =
	        props.getProperty("com.sun.management.jmxremote.localConnectorAddress");
	    System.out.println(connectorAddress);
	    JMXServiceURL url = new JMXServiceURL(connectorAddress);
	    JMXConnector connector = JMXConnectorFactory.connect(url);
	    try {
	        MBeanServerConnection mbeanConn = connector.getMBeanServerConnection();
	        JCozProfilerMBean mxbeanProxy = JMX.newMXBeanProxy(mbeanConn, 
	        	    JCozProfiler.getMBeanName(),  JCozProfilerMBean.class);
	        mxbeanProxy.setProgressPoint("test.TestThreadSerial", 38);
	        mxbeanProxy.setScope("test");
	        mxbeanProxy.startProfiling();
	        for(int i = 0; i < 30; i++){
	        	
	        	ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(mxbeanProxy.getProfilerOutput()));
	        	int numExperiments = ois.readInt();
	        	for (int j = 0; j < numExperiments; j++){
	        		System.out.println(Experiment.deserialize(ois));
	        	}
	        	
	        	Thread.sleep(1000);
	        }
	        mxbeanProxy.endProfiling();
	    }  finally {
	    	connector.close();
	    }
		
		vm.detach();
		
		
//		java -agentpath:./build-64/liblagent.so=pkg=test_progress-point=test/Test:21_warmup=1000_slow-exp test.Test
		
	}
}
