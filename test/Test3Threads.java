package test;

public class Test3Threads {

	static class ThreadTest1 implements Runnable {

		public void run() {
			long sum = 0;
			for (long i = 0; i < 800000000L; i++) {
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
			for (long i = 0; i < 800000000L; i++) {
				sum += 1;
			}

			System.out.println("Thread2 sum: " + sum);
		}
	}

	static class ThreadTest3 implements Runnable {

		public void run() {
			long sum = 0;
			for (long i = 0; i < 800000000L; i++) {
				sum += 1;
			}

			System.out.println("Thread3 sum: " + sum);
		}
	}

	public static void main(String[] args) throws InterruptedException {

		Thread longer = new Thread(new ThreadTest1());
		Thread shorter1 = new Thread(new ThreadTest2());
		Thread shorter2 = new Thread(new ThreadTest3());

		longer.start();
		shorter1.start();
		shorter2.start();

		longer.join();
		shorter1.join();
		shorter2.join();

		System.out.println("Ending");
	}
}
