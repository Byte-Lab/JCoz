/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
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
