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

public class TestThreeOneFaster {
	
	static class ThreadTest1 implements Runnable {
		
		public void run() {
			long sum = 0;
			for( long i = 0; i < 1600000000L; i++ ) {
				sum += 1;
			}
			
			printShit(sum);
		}
	}
	
	public static void printShit(long sum) {
		System.out.println("Thread1 sum: " + sum);
	}


	static class ThreadTest2 implements Runnable {
		
		public void run() {
			long sum = 0;
			for( long i = 0; i < 1200000000L; i++ ) {
				sum += 1;
			}
			
			System.out.println("Thread2 sum: " + sum);
		}
	}
	

	public static void main(String[] args) throws InterruptedException {
		
		Thread longer = new Thread(new ThreadTest1());
		Thread shorter1 = new Thread(new ThreadTest2());
		Thread shorter2 = new Thread(new ThreadTest2());

		longer.start();
		shorter1.start();
		shorter2.start();
		
		longer.join();
		shorter1.join();
		shorter2.join();
		
		System.out.println("Ending");
	}
}
