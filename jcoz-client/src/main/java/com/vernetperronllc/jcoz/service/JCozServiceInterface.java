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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.vernetperronllc.jcoz.JCozVMDescriptor;

/**
 * @author matt
 *
 */
public interface JCozServiceInterface extends Remote {
	
	public List<JCozVMDescriptor> getJavaProcessDescriptions() throws RemoteException;
	
	public int attachToProcess(int pid) throws RemoteException;
	
	public int startProfiling(int pid) throws RemoteException;
	
	public int endProfiling(int pid) throws RemoteException;
	
	public int setProgressPoint(int pid, String className, int lineNo) throws RemoteException;
	
	public int setScope(int pid, String scope) throws RemoteException;
	
	public byte[] getProfilerOutput(int pid) throws RemoteException;
	
	public String getCurrentScope(int pid) throws RemoteException;
	
	public String getProgressPoint(int pid) throws RemoteException;

	public int logProgressPointHit(int pid) throws RemoteException;
}
