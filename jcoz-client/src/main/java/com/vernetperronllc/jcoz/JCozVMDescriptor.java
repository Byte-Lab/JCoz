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
package com.vernetperronllc.jcoz;

import java.io.Serializable;

/**
 * 
 * simple Bean for transporting VMDescriptor data
 * @author matt
 *
 */
public class JCozVMDescriptor implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1232246806223243256L;
	
	private int pid;
	private String displayName;
	
	
	/**
	 * @param pid
	 * @param displayName
	 */
	public JCozVMDescriptor(int pid, String displayName) {
		this.pid = pid;
		this.displayName = displayName;
	}
	
	/**
	 * @return the pid
	 */
	public int getPid() {
		return pid;
	}
	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

}
