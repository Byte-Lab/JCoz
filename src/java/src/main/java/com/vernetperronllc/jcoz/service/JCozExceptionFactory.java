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

import com.vernetperronllc.jcoz.agent.JCozProfilingErrorCodes;

/**
 * generates exceptions based on return codes
 * @author matt
 *
 */
public class JCozExceptionFactory {
	
	private JCozExceptionFactory(){
		
	}
	
	public static final JCozExceptionFactory instance = new JCozExceptionFactory();
	
	public static JCozExceptionFactory getInstance(){
		return instance;
	}
	
	public JCozException getJCozExceptionFromErrorCode(int errorCode){
		switch(errorCode){
		case JCozProfilingErrorCodes.NO_PROGRESS_POINT_SET:
			return new NoProgressPointSetException();
		case JCozProfilingErrorCodes.NO_SCOPE_SET:
			return new NoScopeSetException();
		case JCozProfilingErrorCodes.CANNOT_CALL_WHEN_RUNNING:
			return new InvalidWhenProfilerRunningException();
		case JCozProfilingErrorCodes.PROFILER_NOT_RUNNING:
			return new InvalidWhenProfilerNotRunningException();
		default:
			return new JCozException("Unknown Exception occurred: "+errorCode);
		}
	}
	
}
