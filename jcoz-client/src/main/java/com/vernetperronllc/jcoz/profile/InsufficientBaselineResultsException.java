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
package com.vernetperronllc.jcoz.profile;

public class InsufficientBaselineResultsException extends Exception {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = 7984069459706340151L;

	public InsufficientBaselineResultsException(){
		super();
	}

	public InsufficientBaselineResultsException(String message) {
		super(message);
	}
	
	public InsufficientBaselineResultsException(Throwable cause){
		super(cause);
	}
	
	public InsufficientBaselineResultsException(String message, Throwable cause){
		super(message, cause);
	}
}
