package test;

import java.util.concurrent.*;
import java.util.ArrayList;

public class TestThreadSerial {

	public static final long LOOP_ITERS = 10000000L;
	public static final int numThreads = 32;
	public static ExecutorService executor = Executors
			.newFixedThreadPool(numThreads);
	public static ArrayList<Callable<Void>> threads = new ArrayList<>();

	public static void main(String[] args) throws InterruptedException {
		for (int i = 0; i < numThreads; i++) 
			threads.add(new ParallelWorker());
		
		while (true) {
			doParallel();
			doSerial();
			System.out.println("Iteration done");
		}
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
