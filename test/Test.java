package test;

import java.util.concurrent.*;
import java.util.ArrayList;

public class Test {

	public static final long LONG_LOOP_ITERS = 20000000L;
	public static final long SHORT_LOOP_ITERS = 10000000L;
	public static final int numThreads = 32;
	public static ExecutorService executor = Executors
			.newFixedThreadPool(numThreads);
	public static ArrayList<Callable<Void>> threads = new ArrayList<>();

	public static void main(String[] args) throws InterruptedException {
		threads.add(new LongWorker());
		threads.add(new ShortWorker());
		
		while (true) {
			doParallel();
			System.out.println("Iteration done");
		}
	}

	public static void doParallel() throws InterruptedException {
		executor.invokeAll(threads);
	}

	static class LongWorker implements Callable<Void> {
		public Void call() {
			long sum = 0;
			for (long i = 0; i < LONG_LOOP_ITERS; i++)
				sum += (System.nanoTime() % 9999);
			System.out.println("Long done");
			return null;
		}
	}
	
	static class ShortWorker implements Callable<Void> {
		public Void call() {
			long sum = 0;
			for (long i = 0; i < SHORT_LOOP_ITERS; i++)
				sum += (System.nanoTime() % 9999);
			System.out.println("Short done");
			return null;
		}
	}

}
