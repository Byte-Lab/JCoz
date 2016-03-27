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

public class TestTwoLocksSerial {
	public static Object myLock = new Object();
	
	static class ThreadWaiting implements Runnable {
		
		public void run() {
			long sum = 0;
			synchronized(myLock) {
				for( long i = 0; i < 6000000000L; i++ ) {
					sum += 1;
				}
				
				printShit(sum);
			}
		}
	}
	
	public static void printShit(long sum) {
		System.out.println("Thread waiting sum: " + sum);
	}


	static class ThreadWithLock implements Runnable {
		
		public void run() {
			long sum = 0;
			synchronized(myLock) {
				for( long i = 0; i < 6000000000L; i++ ) {
					sum += 1;
				}
			}
			
			printShit2(sum);
		}
	}
	
	public static void printShit2(long sum) {
		System.out.println("Thread with lock sum: " + sum);
	}


	public static void main(String[] args) throws InterruptedException {
		
		Thread waiting_for_lock = new Thread(new ThreadWaiting());
		Thread has_lock = new Thread(new ThreadWithLock());

		has_lock.start();
		Thread.sleep(200);
		waiting_for_lock.start();
		
		waiting_for_lock.join();
		has_lock.join();
		
		System.out.println("Ending");
	}
}
