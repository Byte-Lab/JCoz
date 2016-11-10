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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.vernetperronllc.jcoz.JCozVMDescriptor;
import com.vernetperronllc.jcoz.agent.JCozProfiler;
import com.vernetperronllc.jcoz.agent.JCozProfilerMBean;
import com.vernetperronllc.jcoz.agent.JCozProfilingErrorCodes;

/**
 * @author matt
 *
 */
public class JCozServiceImpl implements JCozServiceInterface {

	private static final String CONNECTOR_ADDRESS_PROPERTY_KEY = "com.sun.management.jmxremote.localConnectorAddress";
	
	// use a tree map so it is sorted
	public Map<Integer, JCozProfilerMBean> attachedVMs = new TreeMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernetperronllc.jcoz.service.JCozServiceInterface#
	 * getJavaProcessDescriptions()
	 */
	@Override
	public List<JCozVMDescriptor> getJavaProcessDescriptions()
			throws RemoteException {
		System.out.println("in get java process descriptions");
		ArrayList<JCozVMDescriptor> stringDesc = new ArrayList<>();
		for (VirtualMachineDescriptor desc : VirtualMachine.list()){
			stringDesc.add(new JCozVMDescriptor(Integer.parseInt(desc.id()), desc.displayName()));
		}
		return stringDesc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernetperronllc.jcoz.service.JCozServiceInterface#attachToProcess
	 * (int)
	 */
	@Override
	public int attachToProcess(int localProcessId)
			throws RemoteException {
		System.out.println("attach");
		try {
			for (VirtualMachineDescriptor desc : VirtualMachine.list()) {
				if (Integer.parseInt(desc.id()) == localProcessId) {
					VirtualMachine vm = VirtualMachine.attach(desc);
					vm.startLocalManagementAgent();
					Properties props = vm.getAgentProperties();
					String connectorAddress = props
							.getProperty(CONNECTOR_ADDRESS_PROPERTY_KEY);
					JMXServiceURL url = new JMXServiceURL(connectorAddress);
					JMXConnector connector = JMXConnectorFactory.connect(url);
					MBeanServerConnection mbeanConn = connector
							.getMBeanServerConnection();
					attachedVMs.put(localProcessId, JMX.newMXBeanProxy(mbeanConn,
							JCozProfiler.getMBeanName(),
							JCozProfilerMBean.class));
					return JCozProfilingErrorCodes.NORMAL_RETURN;
				}
			}
		} catch (IOException | NumberFormatException
				| AttachNotSupportedException exception) {
			exception.printStackTrace();
			throw new RemoteException("", exception);
			
		}
		return JCozProfilingErrorCodes.INVALID_JAVA_PROCESS;
	}

	/* (non-Javadoc)
	 * @see com.vernetperronllc.jcoz.service.JCozServiceInterface#startProfiling(int)
	 */
	@Override
	public int startProfiling(int pid) throws RemoteException {
		if(!attachedVMs.containsKey(pid)){
			throw new RemoteException("", new JCozException("JVM with pid ("+pid+") is not attached"));
		}
		return attachedVMs.get(pid).startProfiling();
	}

	/* (non-Javadoc)
	 * @see com.vernetperronllc.jcoz.service.JCozServiceInterface#endProfiling(int)
	 */
	@Override
	public int endProfiling(int pid) throws RemoteException {
		if(!attachedVMs.containsKey(pid)){
			throw new RemoteException("", new JCozException("JVM with pid ("+pid+") is not attached"));
		}
		return attachedVMs.get(pid).endProfiling();
	}

	/* (non-Javadoc)
	 * @see com.vernetperronllc.jcoz.service.JCozServiceInterface#setProgressPoint(int, java.lang.String, int)
	 */
	@Override
	public int setProgressPoint(int pid, String className, int lineNo)
			throws RemoteException {
		if(!attachedVMs.containsKey(pid)){
			throw new RemoteException("", new JCozException("JVM with pid ("+pid+") is not attached"));
		}
		return attachedVMs.get(pid).setProgressPoint(className, lineNo);
	}

	/* (non-Javadoc)
	 * @see com.vernetperronllc.jcoz.service.JCozServiceInterface#setScope(int, java.lang.String)
	 */
	@Override
	public int setScope(int pid, String scope) throws RemoteException {
		if(!attachedVMs.containsKey(pid)){
			throw new RemoteException("", new JCozException("JVM with pid ("+pid+") is not attached"));
		}
		return attachedVMs.get(pid).setScope(scope);
	}

	/* (non-Javadoc)
	 * @see com.vernetperronllc.jcoz.service.JCozServiceInterface#getProfilerOutput(int)
	 */
	@Override
	public byte[] getProfilerOutput(int pid) throws RemoteException {
		if(!attachedVMs.containsKey(pid)){
			throw new RemoteException("", new JCozException("JVM with pid ("+pid+") is not attached"));
		}
		try {
			return attachedVMs.get(pid).getProfilerOutput();
		} catch (IOException e) {
			throw new RemoteException("", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.vernetperronllc.jcoz.service.JCozServiceInterface#getCurrentScope(int)
	 */
	@Override
	public String getCurrentScope(int pid) throws RemoteException {
		if(!attachedVMs.containsKey(pid)){
			throw new RemoteException("", new JCozException("JVM with pid ("+pid+") is not attached"));
		}
		return attachedVMs.get(pid).getCurrentScope();
	}

	/* (non-Javadoc)
	 * @see com.vernetperronllc.jcoz.service.JCozServiceInterface#getProgressPoint(int)
	 */
	@Override
	public String getProgressPoint(int pid) throws RemoteException {
		if(!attachedVMs.containsKey(pid)){
			throw new RemoteException("", new JCozException("JVM with pid ("+pid+") is not attached"));
		}
		return attachedVMs.get(pid).getProgressPoint();
	}
}
