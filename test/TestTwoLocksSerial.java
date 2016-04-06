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
