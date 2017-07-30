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
package com.vernetperronllc.jcoz.client.cli;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import com.vernetperronllc.jcoz.JCozVMDescriptor;
import com.vernetperronllc.jcoz.service.JCozException;
import com.vernetperronllc.jcoz.service.JCozService;
import com.vernetperronllc.jcoz.service.JCozServiceInterface;

/**
 * @author matt
 *
 */
public class RemoteServiceWrapper {

	JCozServiceInterface service;
	
	private String host;
	
	private int port;

	public RemoteServiceWrapper(String host, int port) throws JCozException {
		try {
			this.host = host;
			this.port = port;
			Registry reg = LocateRegistry.getRegistry(host, port);
			service = (JCozServiceInterface) reg
					.lookup(JCozService.SERVICE_NAME);
		} catch (RemoteException | NotBoundException e) {
			throw new JCozException(e);
		}
	}

	/**
	 * uses the default JCozService port, 2216
	 * 
	 * @param host
	 */
	public RemoteServiceWrapper(String host) throws JCozException {
		this(host, JCozService.DEFAULT_SERVICE_PORT);
	}
	
	public List<JCozVMDescriptor> listRemoteVirtualMachines() throws JCozException{
		try {
			return service.getJavaProcessDescriptions();
		} catch (RemoteException e) {
			throw new JCozException(e);
		}
	}
	
	public TargetProcessInterface attachToProcess(int remotePid) throws JCozException{
		return new RemoteProcessWrapper(service, remotePid);
	}
	
	public String getHost() {
		return this.host;
	}
	
	public int getPort() {
		return this.port;
	}

}
