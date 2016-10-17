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

import java.util.concurrent.*;
import java.util.ArrayList;

public class TestThreadSerial {

	public static long LOOP_ITERS = 50000000L;
	public static final int numThreads = 16;
	public static ExecutorService executor = Executors
			.newFixedThreadPool(numThreads);
	public static ArrayList<Callable<Void>> threads = new ArrayList<>();

	public static void main(String[] args) throws InterruptedException {
        if (args.length > 0) {
            switch (args[0]) {
                case "--fast":
                case "-s":
                    LOOP_ITERS = 1000000L;
                    break;

                case "-h":
                case "--help":
                    System.out.println("usage: java test.TestThreadSerial [--fast|-s]");
                    System.exit(0);
                
                default:
                    LOOP_ITERS = 50000000L;
                    break;
            }
        }

		for (int i = 0; i < numThreads; i++) 
			threads.add(new ParallelWorker());
		
    long start = System.currentTimeMillis();
		while (true) {
			doParallel();
			doSerial();
      sendRequest();
      if (System.currentTimeMillis() - start >= 3590000) {
        break;
      }
		}
	}


    public static void sendRequest() {
	    System.out.println("Iteration done");
    }

	public static void doSerial() throws InterruptedException {
		long sum = 0;
		for (long i = 0; i < LOOP_ITERS; i++)
			sum += (System.nanoTime() % 9999);
	}

	public static void doParallel() throws InterruptedException {
		executor.invokeAll(threads);
	}

	static class ParallelWorker implements Callable<Void> {
		public Void call() {
			long sum = 0;
			for (long i = 0; i < LOOP_ITERS; i++)
				sum += (System.nanoTime() % 9999);
			
			return null;
		}
	}

}
