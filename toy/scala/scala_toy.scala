import java.util.concurrent.{Callable, Executors, ExecutorService}
import java.util.ArrayList

object TestThreadSerial {
  val threads = new ArrayList[Callable[Unit]]()
  val numThreads = 16
  val pool: ExecutorService = Executors.newFixedThreadPool(numThreads)
  val numIters = 10000000

  def main(args: Array[String]) {
    var thrNo = 0
    for (thrNo <- 1 to numThreads) {
      threads.add(new ParallelWorker())
    }

    while (true) {
      doParallel()
      doSerial()
      sendRequest()
    }
  }

  def doParallel() {
    pool.invokeAll(threads)
  }

  def doSerial() {
    var sum:java.lang.Long = 0
    for(i <- 0 to numIters) {
      sum = sum + System.nanoTime()
    }
  }

  def sendRequest() {
    println("Iteration done")
    println("Line number: " + Thread.currentThread().getStackTrace()(2).getLineNumber())
  }

  class ParallelWorker extends Callable[Unit] {
    def call() {
      var sum:java.lang.Long = 0
        for(i <- 0 to numIters) {
          sum = sum + System.nanoTime()
        }
    }
  }
}

