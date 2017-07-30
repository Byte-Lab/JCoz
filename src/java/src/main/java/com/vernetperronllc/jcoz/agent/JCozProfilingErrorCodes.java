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
package com.vernetperronllc.jcoz.agent;

/**
 * @author matt
 *
 */
public class JCozProfilingErrorCodes {
	/*
	 * error return codes
	 */
	public static final int NORMAL_RETURN = 0;
	public static final int NO_PROGRESS_POINT_SET = 1;
	public static final int NO_SCOPE_SET = 2;
	public static final int CANNOT_CALL_WHEN_RUNNING = 3;
	public static final int PROFILER_NOT_RUNNING = 4;
	public static final int INVALID_JAVA_PROCESS = 5;
}
