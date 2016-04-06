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
