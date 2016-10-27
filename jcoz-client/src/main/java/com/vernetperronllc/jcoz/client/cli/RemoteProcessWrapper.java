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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.vernetperronllc.jcoz.agent.JCozProfilingErrorCodes;
import com.vernetperronllc.jcoz.profile.Experiment;
import com.vernetperronllc.jcoz.service.InvalidWhenProfilerNotRunningException;
import com.vernetperronllc.jcoz.service.JCozException;
import com.vernetperronllc.jcoz.service.JCozExceptionFactory;
import com.vernetperronllc.jcoz.service.JCozServiceInterface;

/**
 * @author matt
 *
 */
public class RemoteProcessWrapper implements TargetProcessInterface {
	
	JCozServiceInterface service;
	int remotePid;
	
	/**
	 * 
	 */
	public RemoteProcessWrapper(JCozServiceInterface service, int pid) throws JCozException{
		this.service = service;
		this.remotePid = pid;
		int returnCode;
		try {
			returnCode = service.attachToProcess(pid);
		} catch (RemoteException e) {
			throw new JCozException(e);
		}
		if (returnCode != JCozProfilingErrorCodes.NORMAL_RETURN){
			throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
		}
	}


	public void startProfiling() throws JCozException{
		int returnCode;
		try {
			returnCode = service.startProfiling(remotePid);
		} catch (RemoteException e) {
			throw new JCozException(e);
		}
		if(returnCode != 0){
			throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
		}
		
	}
	
	public void endProfiling() throws JCozException{
		int returnCode;
		try {
			returnCode = service.endProfiling(remotePid);
		} catch (RemoteException e) {
			throw new JCozException(e);
		}
		if(returnCode != 0){
			throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
		}
	}
	
	public void setProgressPoint(String className, int lineNo) throws JCozException{
		int returnCode;
		try {
			returnCode = service.setProgressPoint(remotePid, className, lineNo);
		} catch (RemoteException e) {
			throw new JCozException(e);
		}
		if(returnCode != 0){
			throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
		}
	}
	
	public void setScope(String scope) throws JCozException{
		int returnCode;
		try {
			returnCode = service.setScope(remotePid, scope);
		} catch (RemoteException e) {
			throw new JCozException(e);
		}
		if(returnCode != 0){
			throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
		}
	}
	
	public List<Experiment> getProfilerOutput() throws JCozException{
		List<Experiment> experiments = new ArrayList<>();
		ObjectInputStream ois;
		
		try {
			byte[] profOutput = service.getProfilerOutput(remotePid);
			if (profOutput == null){
				throw new InvalidWhenProfilerNotRunningException();
			}
			ois = new ObjectInputStream(new ByteArrayInputStream(profOutput));
			int numExperiments = ois.readInt();
	    	for (int j = 0; j < numExperiments; j++){
	    		experiments.add(Experiment.deserialize(ois));
	    	}
		} catch (IOException e) {
			throw new JCozException(e);
		}
    	
    	return experiments;
	}
	
	public String getCurrentScope() throws JCozException{
		try {
			return service.getCurrentScope(remotePid);
		} catch (RemoteException e) {
			throw new JCozException(e);
		}
	}
	
	public String getProgressPoint() throws JCozException{
		try {
			return service.getProgressPoint(remotePid);
		} catch (RemoteException e) {
			throw new JCozException(e);
		}
	}


	public int logProgressPointHit() throws JCozException {
		try {
			return service.logProgressPointHit(remotePid);
		} catch (RemoteException e) {
			throw new JCozException(e);
		}
	}

}
