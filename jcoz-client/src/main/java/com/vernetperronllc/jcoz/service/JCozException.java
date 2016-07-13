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

/**
 * @author matt
 *
 */
public class JCozException extends Exception{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7912298271464056903L;

	public JCozException(){
		super();
	}

	public JCozException(String message) {
		super(message);
	}
	
	public JCozException(Throwable cause){
		super(cause);
	}
	
	public JCozException(String message, Throwable cause){
		super(message, cause);
	}

}
