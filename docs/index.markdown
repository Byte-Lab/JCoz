---
layout: home
---

<head>
  <script src="//cdn.plot.ly/plotly-latest.min.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/regression/1.4.0/regression.min.js"></script>
  <script>
  const zip = (x, y) => x.map((k, i) => [k, y[i]]);
  function plot (x, y, r, title, div) {
    var div = document.getElementById(div);
    var data = [
      {
        x: x,
        y: y,
        showlegend: false,
        mode:'markers',
        name: '% speedup'
      },
      {
        x: x,
        y: r,
        name: 'best fit',
        showlegend: false,
      }
    ];
    var layout = {
      xaxis: {
        title: 'Line Speedup (%)'
      },
      yaxis: {
        title: 'Overall Runtime Speedup (%)',
        range: [-10, 50]
      },
      title: title
    };
    Plotly.plot( div, data, layout );
  }
  function plotBar(x, y, title, div) {
    var div = document.getElementById(div);
    var data = [
      {
       x: x,
       y: y,
        showlegend: false,
        type:'bar',
      },
    ];
    var layout = {
      yaxis: {
        title: 'Overall Runtime (seconds)',
      },
      title: title
    };
    Plotly.plot( div, data, layout );
  }
  </script>
</head>

# JCoz, the first Java Causal Profiler

>By Matthew Perron (mperron) and David Vernet (dcv)

## Summary
JCoz is the first ever causal Java profiler. That is, it is a profiler for Java programs that uses a technique called "causal profiling" to identify performance bottlenecks in complex, parallel programs that run on the JVM.

This page describes in detail how causal profiling works, and shows some speedup results we were able to obtain using JCoz. In particular, it shows our results in optimizing the [Java H2 Database](http://www.h2database.com/html/main.html), a widely used, mature, in-memory Java database engine. Finally, it shows that JCoz has a runtime overhead of roughly 10-40% - matching major sampling profilers such as HPROF and JVisualVM.

## Background
Optimizing parallel programs is very difficult, and there are often many moving parts to consider. For example, if threads are write-sharing a global variable, it can cause excessive interconnect traffic and cache misses. Or perhaps [Ahmdal's law](https://en.wikipedia.org/wiki/Amdahl%27s_law) prevents us from reaching our performance goal before we even get started, causing days of optimization effort that will never improve the speed of your program.
Even worse, it could happen that speeding up certain parts of your code might even cause a performance *hit* - for example, when speeding up a line of code causes increased lock contention. Is it possible to handle all of this complexity in a profiler, and provide accurate results so programmers know exactly where to look to optimize their parallel programs? It is -- with causal profiling.

JCoz is a causal profiler for Java; that is, a tool for profiling multi-threaded Java programs. A causal profiler detects how changing a line would affect performance using *virtual speedup*; in short by slowing down the rest of the program to emulate the impact an optimization to that particular line would have.

At some frequency throughout the runtime of the program, we run an "experiment" in which we choose a line being executed among all threads (randomly), and a speedup amount between 0 - 100%, to measure how speeding up that line of code by the given speedup percent would affect overall program runtime.

Ideally, during this experiment, when any thread enters the selected line, all other threads are suspended for a period of time depending on the speedup chosen for that experiment. However, we instead utilize sampling to avoid the excessive runtime overhead from having every thread pause every time it hits the line. To actually measure line speedup, we record the throughput achieved during the experiment and use it to determine how speeding up the line would affect throughput / runtime. Thus, by freezing the other threads and measuring how throughput changes, we have "virtually" sped the line being executed by the given thread.

The following caption is a useful visualization of virtual speedup (credited to Charlie Curtsinger and Emery Berger's [paper](https://arxiv.org/pdf/1608.03676v1.pdf)):

{: style="text-align: center"}
![Virtual Speedup](assets/images/virtual_speedup.png)

The above approach to profiling multi-threaded programs has many benefits over the traditional performance monitoring profilers.

For example, observe the following toy program:

```java
public static void main(String[] args) throws InterruptedException {
  threads.add(new LongWorker());
  threads.add(new ShortWorker());

  while(true) {
    doParallel();
    System.out.println("Iteration complete");
  }
}

public static void doParallel() throws InterruptedException {
  executor.invokeAll(threads);
}

static class LongWorker implements Callable<Void> {
  public Void call() {
    long sum = 0;
    for (long i = 0; i < 20000000L; i++)
      sum += System.nanoTime() % 9999;
    System.out.println("Long done");
    return null;
  }
}

static class ShortWorker implements Callable<Void> {
  public Void call() {
    long sum = 0;
    for (long i = 0; i < 10000000L; i++)
      sum += System.nanoTime() % 9999;
    System.out.println("Short done");
    return null;
  }
}
```

For this program, Java VisualVM's sampling instrumentation profiler gives us the following output:

![Java VisualVM Sampling](assets/images/java_visualvm_sampling.png)

Though this profile provides a more accurate picture of where we're spending our time, it does not indicate that optimizing `ShortWorker` does nothing for runtime and that
after we speed up `LongWorker` by 50%, the program runtime will top off when `ShortWorker` becomes the bottleneck.

Now, observe the output of JCoz on the same program:

<div id="jcoz-longer"></div>

<script>
  var x = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100];
  var y = [0, -1, 2, 14, 17, 19, 20, 21, 24, 29, 30, 35, 32, 36, 33, 36, 40, 38, 43, 42, 41];
  var r = regression('polynomial', zip(x,y)).points.map( (x) => x[1] );

  plot(x, y, r, '"Long" thread causal profile shows no benefit of speedup after 50%', 'jcoz-longer')
</script>

<div id="jcoz-shorter"></div>

<script>
  var x = [0, 5, 10, 15, 20, 25, 30, 40, 45, 50, 55, 60, 65, 75, 85, 90, 95, 100];
  var y = [0, -1, -1, 1, -4, -2, -4, -9, -2, -7, 0, 2, 5, -10, 9, -3, -1, -5];
  var r = regression('polynomial', zip(x,y)).points.map((x) => x[1]);

  plot(x, y, r, '"Short" thread causal profile shows no speedup', 'jcoz-shorter')
</script>

Though there is some variance (as is expected with a Java program), JCoz correctly indicates that speeding up the longer worker linearly approaches a 50% speedup, and then at roughly a 50% line speedup, the overall runtime speedup stays around 50% no matter how much faster we make the line (because `ShortWorker` dominates runtime). Additionally, we see that the profile indicates that optimizing `ShortWorker` does essentially nothing for performance.

Here is another example of how JCoz provides more valuable profiling results than other profilers. Observe the following code:

```java
static class ThreadTest implements Callable<Void> {
  public Void call() {
    long sum = 0;
    for (long i = 0; i < LOOP_ITERS; i++) {
      sum += System.nanoTime() % 9999;
    }
    return null;
  }
}

public static void doParallel() throws InterruptedException {
  executor.invokeAll(threads);
}

public static void doSerial() throws InterruptedException {
  long sum = 0;
  for (long i = 0; i < LOOP_ITERS; i++) {
    sum += System.nanoTime() % 9999;
  }
}

public static void main(String[] args) throws InterruptedException {
  int numThreads = 16;
  for (int i = 0; i < numThreads; i++) {
    threads.add(new ThreadTest());
  }
  while (true) {
    doParallel();
    doSerial();
    System.out.println("Iteration complete");
  }
}
```

Java VisualVM's sampling profiler gives us misleading results:

![Java VisualVM Sampling](assets/images/java_visualvm_sampling_2.png)

As you can see, this profile indicates that there is essentially no opportunity for speedup in the serial portion of the code, while indicating that there is enormous (much more than 50%) speedup opportunity in the parallel portion of the code. Clearly, each portion of the code has equal opportunity for optimization, as each portion of that code comprises about 50% of the loop runtime. This result is given by JCoz when run on this toy example:

<div id="jcoz-parallel"></div>

<script>
  var x = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100];
  var y = [0, 2,  8,  7, 18,  2,  8,  9, 12, 11, 12, 12, 23, 21, 23, 27, 25, 29, 31, 32, 34];
  var r = regression('linear', zip(x,y)).points.map((x) => x[1]);

  plot(x, y, r, 'Line in parallel portion results in ~35% speedup', 'jcoz-parallel');
</script>

<div id="jcoz-serial"></div>

<script>
  var x = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100];
  var y = [0, 1,  4,  5, 6,   8,  9, 10, 11, 12, 15, 18, 17, 21, 22, 22, 26, 26, 29, 29, 30];
  var r = regression('linear', zip(x,y)).points.map((x) => x[1]);

  plot(x, y, r, 'Line in serial portion results in ~30% speedup', 'jcoz-serial');
</script>

As mentioned above, JCoz correctly indicates that optimizing either portion of the code will result in approximately equal throughput improvement.

## Approach

### Profiler Workflow

Our profiler follows this workflow when profiling a program:

 - Start the program, and set a breakpoint (aka "progress point") at a line in the program that was chosen by the user to measure throughput
 - Wait for a given warm-up period to avoid overhead during the initial part of a program where the progress point will not be hit
 - Starting running experiments. An experiment includes the following steps:
    - Choose a line for speedup randomly among the currently executing threads, as well as a random speedup between 0 - 100%
    - Every 1ms, send a [SIGPROF](https://www.gnu.org/software/libc/manual/html_node/Alarm-Signals.html) signal to all user threads, in which they check whether they are on the experiment line. If so, all other threads are frozen for a period of time dependent on the speedup chosen for this experiment, and the given thread continues to execute
 - Throughout the runtime of the experiment, keep track of how many times a given "progress point" line is hit by all threads. This progress point line is how we measure how throughput changes when we virtually speed up a line
 - After a certain amount of time, end the experiment and write the metrics to a file buffer
 - When the program has finished running, flush the file buffer

A program can be run more than once -- the results are simply appended to the given profile output.

### Platform

JCoz is written in C++ as a Java Agent, and targets Linux machines with a JDK with Java >= 1.7 installed. Other than that, our project has no dependencies other than the standard C and C++ libraries. To interpret a profile output from JCoz, we used the [original COZ profile plotter](http://plasma-umass.github.io/coz/).

### Implementation

In building JCoz, we leveraged the [JVM Tool Interface (JVMTI)](https://docs.oracle.com/javase/8/docs/platform/jvmti/jvmti.html) to:
 - Get thread stack traces
 - Listen for when new user threads are created and existing threads are killed
 - Map byte code instructions to source file line numbers, and
 - Set breakpoints

Though much of the interface was relatively straightforward and easy to use, we ran into a hurdle in obtaining stack traces in real-time from the program. `JVMTI` only publicly exposes a [GetStackTrace](https://docs.oracle.com/javase/8/docs/platform/jvmti/jvmti.html#GetStackTrace) function that returns stack traces at "safe points". Had we been constrained to use this "safe" thread stack function, it would have severely degraded the utility of JCoz because it may have skipped important lines to profile, or caused us to wait for some indefinite amount of time before being given the stack trace - which would have caused our sampling logic to be completely unpredictable. For example, we observed that no safe points were being added in tight, expensive loops. To get around this issue, we use the undocumented [AsyncGetStackTrace](https://github.com/openjdk/jdk/blob/master/src/hotspot/share/prims/forte.cpp) function to get stack traces in non-safe points.

Another technique we use in JCoz is sampling. It would have required far too much overhead to listen to each instruction for every thread to see if a thread was executing an experiment line. Instead, we send all threads a `SIGPROF` signal every 1ms. In the signal handler, each thread determines if it is executing the experiment line by calling `AsyncGetStackTrace`. If so, it notifies other threads to sleep by incrementing a global sleep counter. This provides a nice tuning knob for configuring the profiler, as we can easily adjust experiment duration and sampling frequency to tune the runtime overhead and profiling granularity of JCoz.

## Results

We chose to measure performance with three different metrics:
 - Does JCoz give us accurate results for toy programs that exhibited various examples of parallel programming (i.e. the example given above, threads thrashing on locks, Ahmdal's law with serial execution, etc)?
 - How much overhead is incurred from using JCoz?
 - Can we use the profiler to find optimizations in complex, mature, and widely used libraries?

We were successful in all three of the above metrics, and cover each in more detail below.

Our first metric of success was JCoz's performance on simple Java programs that exhibited various paradigms of parallel programming. We were able to measure this by running JCoz on many different, easy to understand, toy examples, and verifying that the profiler gave us the output we expected for all programs. We found that all examples were correctly profiled, though in some instances, many iterations of the program was required to build a large enough profile to have useful results.

We ran JCoz on the following types of toy examples:

 - Two non-contending threads with different runtimes (shown above)
 - Multiple non-contending threads with exactly the same runtimes (equal speedup opportunities in all threads)
 - Two threads with the same runtimes where one is waiting on a lock (essentially serial execution -- equal speedup opportunities found)
 - Multiple threads thrashing on a single lock (significant speedup found for the small section of code where a thread held the lock)

We found that the profile outputs matched our expectations for all of the aforementioned examples. Going forward, we intend to manufacture more toy examples; including examples with threads that exhibit poor cache locality, threads that write to a global atomic variable, and threads that flood the interconnect with excessive traffic. We are confident that JCoz can at least correctly identify poor cache locality, as we were able to identify a bottleneck in the [Universal Java Matrix Package](https://ujmp.org/) that exhibited very poor cache locality when multiplying dense matrices (though we did not have time to implement an optimization).

Perhaps most importantly, we were able to optimize real libraries with JCoz. Specifically, we were able to optimize the [Java H2 Database](http://www.h2database.com/html/main.html) by 19% using the standard [Dacapo Benchmark](http://dacapobench.org/) suite, and we also identified an opportunity for optimization on the high performance, highly parallel [Universal Java Matrix Package](https://github.com/ujmp/universal-java-matrix-package) by using blocking to take better advantage of the cache.

JCoz joins a community of existing Java profilers, including
 - [HPROF](http://docs.oracle.com/javase/7/docs/technotes/samples/HPROF.html)
 - [Java VisualVM](http://docs.oracle.com/javase/6/docs/technotes/tools/share/jvisualvm.html)

Another metric of success for JCoz was the amount of runtime overhead incurred from using it. We found that JCoz was very lightweight, incurring roughly 10-40% runtime overhead for all programs that we profiled. Below is a comparison of the overhead of running different Java Profilers on the Dacapo H2 Benchmarking tool.

<div id="profiler-comparison"></div>

<script>
var x = ["JCoz", "No Profiling", "VisualVM Sampling", "HPROF Sampling", "JVisualVM Profiling"];
var y = [98.596, 93.365, 93.365, 102.541, 1434.668];
plotBar(x, y, JCoz sampler runtime compared to other profilers', 'profiler-comparison')

</script>

Since JCoz does not use the heavyweight instrumentation that is used by the JVisualVM profiling tool, its overhead is similar to HPROF and JVisualVM profiling.

One of our main goals for this project was to take an application of which we had little knowledge, run JCoz, and make a meaningful improvment with little development time. We chose to profile the H2 TPCC benchmark using the Dacapo Java Benchmarking Suite. Our profile displayed the line as the best opportunity for throughput improvement with the following graph. We ran on muir, a machine with 264GB of DRAM and 4 Intel Intel Xeon E5-4650 2.70 GHz processors.

<div id='jcoz-optimization-h2'></div>

<script>
  var x = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100];
  var y = [0, 1, 8, 4, 11, 8, 13, 20, 19, 15, 21, 26, 23, 30, 27, 35, 33, 42, 45, 48, 51];
  var r = regression('linear', zip(x,y)).points.map((x) => x[1]);

  plot(x, y, r, 'JCoz profile shows optimization opportunity in H2', 'jcoz-optimization-h2')
</script>

Using the above profile, we discovered that a `Thread.sleep(...)` call, meant to implement backoff of a failed transaction before retrying, was causing an significant drop in throughput performance. By reducing the wait time from a random time between 1 and 10 ms to between 1 and 10 *microseconds*, we observed a throughput increase of around 19%. We had no knowledge of this codebase before running JCoz, and this allowed us to improve the throughput of the benchmark in about an hour after viewing the profile results.

Here is the section of code we identified for speedup (it was the `Thread.sleep(...)` call):

```java
Database database = session.getDatabase();
int sleep = 1 + MathUtils.randomInt(10);
while (true) {
  try {
    if (database.isMultiThreaded()) {
      Thread.sleep(sleep);
    } else {
      database.wait(sleep);
    }
  } catch (InterruptedException e1) {
    // ignore
  }
  long slept = System.nanoTime() / 1000000 - now);
  if (slept >= sleep) {
    break;
  }
}
```

To speed up the line of code, we simply lowered the amount of time to randomly sleep on a failed transaction:

```java
Database database = session.getDatabase();
// sleep for 1-10 microseconds
int sleep = 1000 * (1 + MathUtils.randomInt(10));
while (true) {
  try {
    if (database.isMultiThreaded()) {
      Thread.sleep(0, sleep);
    } else {
      database.wait(0, sleep);
    }
  } catch (InterruptedException e1) {
    // ignore
  }
  long slept = System.nanoTime() - now);
  if (slept >= sleep) {
    break;
  }
}
```

The results of the end to end runtime of the benchmark before and after optimization are displayed below.

<div id='h2-result'></div>

<script>
var x = ["Unoptimized", "Optimized"];
var y = [86.24, 72.34];
plotBar(x, y, 'Optimization yields 20% improvement', 'h2-result')
</script>

We feel this significant improvement, with little development effort, demonstrates the utility of this form of profiling, and we are excited to continue improving JCoz.
