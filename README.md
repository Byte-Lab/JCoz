[![Join the chat at https://gitter.im/JCoz-profiler/community](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/JCoz-profiler/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Overview
JCoz is the world's first causal profiler for Java (and eventually all JVM) programs. It was inspired by [coz](https://github.com/plasma-umass/coz), the original causal profiler.

For documentation, including installing, building, and using JCoz, please see our [Wiki page](https://github.com/Decave/JCoz/wiki) page.

## Dependencies

 - [spdlog](https://github.com/gabime/spdlog) (`0.11.0` or higher)
   - `apt-get install libspdlog-dev` for debian/ubuntu
   - `yum install spdlog-devel` for fedora/rhel/centos
 - make
 - jdk, of course

# Getting Started Tutorial

## Build and shakeout

You can drive a basic test use case through the Makefile.

Start by building everything from scratch:
```
$ make clean
$ make all
```

Now, to get started, open three terminal windows:

```
(1) $ make run-rmi-host
(2) $ make run-workload
(3) $ make run-profiler
```

From the third (profiler) window, after a few moments you will see some output
appear:
```
(3)
experiment	selected=test.TestThreadSerial:67	speedup=0.0	duration=20003047916
progress-point	name=end-to-end	type=source	delta=0
```
This is the coz flat file format. Leave the application to run for a
period of time, and you will see more profiling samples collected.

If you've made it this far, congrats, you can proceed to running the CLI for
a proper profiling run! The 'run-profiler' process should terminate after
about 30 seconds.

Unfortunately we do not have enough datapoints from a 30 second run to get
sufficient confidence for coz to recommend the lines of code to improve.

## Running the CLI

Using the CLI, we can collect as many datapoints as we like. Keeping the RMI
host and workload running, start the CLI. Check the PID of the monitored host
with ps.
```
(3)
$ CLIENT_JAR=./src/java/target/client-0.0.1-jar-with-dependencies.jar
$ TOOLS_JAR=/usr/lib/jvm/java-8-openjdk-amd64/lib/tools.jar
$ java -cp ${CLIENT_JAR}:${TOOLS_JAR} \
    jcoz.client.cli.JCozCLI \
    -c test.TestThreadSerial \
    -l 57 \
    -s test \
    -p $PID_OF_WORKLOAD
...
experiment	selected=test.TestThreadSerial:62	speedup=0.6	duration=4747080912
progress-point	name=end-to-end	type=source	delta=98
experiment	selected=test.TestThreadSerial:73	speedup=0.45	duration=4647873501
progress-point	name=end-to-end	type=source	delta=96
experiment	selected=test.TestThreadSerial:73	speedup=0.0	duration=5002572016
progress-point	name=end-to-end	type=source	delta=100
experiment	selected=test.TestThreadSerial:62	speedup=0.35	duration=4858058541
progress-point	name=end-to-end	type=source	delta=99
...
```

Results will start appearing in the profile output.

## Getting a profiling visualisation

Save the results you previously captured to a file `foo.coz`.

Open the [coz UI here](https://plasma-umass.org/coz/), and upload the file and review the output.

## Profiling a real application

You should now be in a position to profile a real application. Use the JCozCLI
and capture some samples!

Be aware for that a real sized application there will be lots of code and lots
of experiments that coz needs to run. You should plan to keep coz running for
some hours to be confident in the results.
