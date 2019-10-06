/*
 * This file is part of JCoz.
 *
 * JCoz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JCoz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JCoz.  If not, see <https://www.gnu.org/licenses/>.
 *
 * This file has been modified from lightweight-java-profiler
 * (https://github.com/dcapwell/lightweight-java-profiler). See APACHE_LICENSE for
 * a copy of the license that was included with that original work.
 */
package jcoz.agent;

import jcoz.profile.Experiment;

import javax.management.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the mbean, controls the underlying native profiler
 *
 * @author matt
 */
public class JCozProfiler implements JCozProfilerMBean {

    /**
     * is the mbean registered with the platform mbean server
     */
    private static boolean registered = false;
    /**
     * class of progress point
     */
    private String progressPointClass = null;
    /**
     * line number of progress point
     */
    private Integer progressPointLineNo = null;
    /**
     * scope to jcoz.profile
     */
    private String currentScope = null;
    /**
     * is an experiment running
     */
    private boolean experimentRunning = false;
    /**
     * last time results were fetched
     */
    private long lastCollectionMillis = System.currentTimeMillis();

    /**
     * end experiments after 30 seconds without fetch
     */
    private static final long INACTIVITY_THRESHOLD = 30000;

    /**
     * list of experiments run since last collected
     */
    private List<Experiment> cachedOutput = new ArrayList<>();

    /**
     * start profiling with the current scope and progress point
     */
    public synchronized int startProfiling() {
        if (experimentRunning) {
            return JCozProfilingErrorCodes.CANNOT_CALL_WHEN_RUNNING;
        }
        if (progressPointClass == null || progressPointLineNo == null) {
            return JCozProfilingErrorCodes.NO_PROGRESS_POINT_SET;
        }
        if (currentScope == null) {
            return JCozProfilingErrorCodes.NO_SCOPE_SET;
        }
        experimentRunning = true;
        lastCollectionMillis = System.currentTimeMillis();
        return startProfilingNative();
    }

    private native int startProfilingNative();

    /**
     * end the current profiling
     */
    public synchronized int endProfiling() {
        if (!experimentRunning) {
            return JCozProfilingErrorCodes.PROFILER_NOT_RUNNING;
        }
        int returnCode = endProfilingNative();
        experimentRunning = false;
        cachedOutput = new ArrayList<>();
        return returnCode;
    }

    private native int endProfilingNative();

    /**
     * set progress point
     */
    public synchronized int setProgressPoint(String className, int lineNo) {
        if (experimentRunning) {
            return JCozProfilingErrorCodes.CANNOT_CALL_WHEN_RUNNING;
        }
        //replace class name . with /
        String passedClassName = className.replace('.', '/');
        int returnCode = setProgressPointNative(passedClassName, lineNo);
        if (returnCode == JCozProfilingErrorCodes.NORMAL_RETURN) {
            this.progressPointClass = className;
            this.progressPointLineNo = lineNo;
        }
        return returnCode;

    }

    private native int setProgressPointNative(String className, int lineNo);

    /**
     * get the serialized output from recently run experiments
     */
    public synchronized byte[] getProfilerOutput() throws IOException {
        lastCollectionMillis = System.currentTimeMillis();
        if (!experimentRunning) {
            return new ByteArrayOutputStream().toByteArray();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeInt(cachedOutput.size());
        for (Experiment e : cachedOutput) {
            e.serialize(oos);
        }
        clearCachedOutput();
        oos.flush();
        return baos.toByteArray();
    }

    /**
     * clear the experiments in the buffer
     */
    private synchronized void clearCachedOutput() {
        cachedOutput.clear();
    }

    /**
     * method for the profiler to add an experiment to the buffer
     *
     * @param classSig
     * @param lineNo
     * @param speedup
     * @param duration
     * @param pointsHit
     */
    private synchronized void cacheOutput(String classSig, int lineNo,
                                          float speedup, long duration, long pointsHit) {
        cachedOutput.add(new Experiment(classSig, lineNo, speedup, duration,
                pointsHit));
        if (System.currentTimeMillis() - lastCollectionMillis > INACTIVITY_THRESHOLD) {
            new Thread(() -> endProfiling()).start();
        }
    }

    /**
     * get the current experiment scope
     */
    public synchronized String getCurrentScope() {
        return currentScope;
    }

    /**
     * set the scope to jcoz.profile
     */
    public synchronized int setScope(String scopePackage) {
        if (experimentRunning) {
            return JCozProfilingErrorCodes.CANNOT_CALL_WHEN_RUNNING;
        }
        int scopeReturn = setScopeNative(scopePackage);
        if (scopeReturn == 0) {
            currentScope = scopePackage;
        }
        return scopeReturn;
    }

    private native int setScopeNative(String scopePackage);

    /**
     * get the current progress point as a string class and line number are separated by a ':'
     */
    public synchronized String getProgressPoint() {
        return progressPointClass + ":" + progressPointLineNo;
    }

    /**
     * register the profiler with the Platform mbean server
     */
    public static synchronized void registerProfilerWithMBeanServer() {
        if (!registered) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            registered = true;
            try {
                JCozProfiler mbean = new JCozProfiler();
                synchronized (mbean) {
                    mbs.registerMBean(mbean, getMBeanName());
                }
            } catch (InstanceAlreadyExistsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MBeanRegistrationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NotCompliantMBeanException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * get the ObjectName of this mbean
     *
     * @return
     */
    public static ObjectName getMBeanName() {
        try {
            return new ObjectName(JCozProfiler.class.getPackage().getName()
                    + ":type=" + JCozProfiler.class.getSimpleName());
        } catch (MalformedObjectNameException e) {
            // do nothing, this should never be malformed
            throw new Error(e);
        }
    }
}
