// Main.java

package jyinterface;

import jyinterface.factories.WorkerFactory;
import jyinterface.interfaces.Worker;
import org.python.core.PyObject;
import org.python.core.PyList;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        int numThreads = 16;
        ArrayList<Callable<Void>> threads = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        WorkerFactory factory = new WorkerFactory();
        int id = 1;
        for(int i = 0; i < numThreads; i++) {
          threads.add(factory.createWorker((id++) + ""));
        }

        Worker serialWorker = factory.createWorker((id++) + "");

        while(true) {
          executor.invokeAll(threads); // Parallel portion
          serialWorker.call();         // Serial portion
          sendRequest();               // Send muh request.
        }
    }

    public static void sendRequest() {
      System.out.println("Loop done!");
    }
}

