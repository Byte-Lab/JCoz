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
 */

package test;

public class TestTwoLocksThrash {
	public static Object myLock = new Object();
	public static long sum = 0;
	
	static class ThreadWaiting implements Runnable {
		
		public void run() {

			while( sum < 99999999999999L ) {
				expensiveOp();
			}
		}
	}
	
	public static synchronized void expensiveOp() {
		if(Math.random() >= 0.5) {
			for( long i = 0; i < 60000000L; i++ ) sum += 1;
		} else {
			for( long i = 0; i < 30000000L; i++ ) sum += 1;
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		Thread thread1 = new Thread(new ThreadWaiting());
		Thread thread2 = new Thread(new ThreadWaiting());

		thread1.start();
		thread2.start();
		
		thread1.join();
	}
}
