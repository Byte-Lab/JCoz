/*
 * This file is part of JCoz.
 *
 * JCoz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JCoz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JCoz.  If not, see <https://www.gnu.org/licenses/>.
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
