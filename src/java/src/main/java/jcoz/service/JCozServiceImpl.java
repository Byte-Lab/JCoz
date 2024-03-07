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
package jcoz.service;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import jcoz.JCozVMDescriptor;
import jcoz.agent.JCozProfiler;
import jcoz.agent.JCozProfilerMBean;
import jcoz.agent.JCozProfilingErrorCodes;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matt
 */
public class JCozServiceImpl implements JCozServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(JCozServiceImpl.class);


    private static final String CONNECTOR_ADDRESS_PROPERTY_KEY = "com.sun.management.jmxremote.localConnectorAddress";

    // use a tree map so it is sorted
    private Map<Integer, JCozProfilerMBean> attachedVMs = new TreeMap<>();

    private static final String JVM_WITH_PID_IS_NOT_ATTACHED = "JVM with pid %d is not attached";

    /*
     * (non-Javadoc)
     *
     * @see jcoz.service.JCozServiceInterface#
     * getJavaProcessDescriptions()
     */
    @Override
    public List<JCozVMDescriptor> getJavaProcessDescriptions() throws RemoteException {
        logger.info("Getting Java Process Descriptions");
        ArrayList<JCozVMDescriptor> stringDesc = new ArrayList<>();
        for (VirtualMachineDescriptor desc : VirtualMachine.list()) {
            logger.debug("Adding description {} with id {}", desc.displayName(), desc.id());
            stringDesc.add(new JCozVMDescriptor(Integer.parseInt(desc.id()), desc.displayName()));
        }
        return stringDesc;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * jcoz.service.JCozServiceInterface#attachToProcess
     * (int)
     */
    @Override
    public int attachToProcess(int localProcessId) throws RemoteException {
        logger.info("Attaching to process {}", localProcessId);
        try {
            for (VirtualMachineDescriptor desc : VirtualMachine.list()) {
                if (Integer.parseInt(desc.id()) == localProcessId) {
                    VirtualMachine vm = VirtualMachine.attach(desc);
                    vm.startLocalManagementAgent();
                    Properties props = vm.getAgentProperties();
                    String connectorAddress = props
                        .getProperty(CONNECTOR_ADDRESS_PROPERTY_KEY);
                    JMXServiceURL url = new JMXServiceURL(connectorAddress);
                    JMXConnector connector = JMXConnectorFactory.connect(url);
                    MBeanServerConnection mbeanConn = connector
                        .getMBeanServerConnection();
                    attachedVMs.put(localProcessId, JMX.newMXBeanProxy(mbeanConn,
                                JCozProfiler.getMBeanName(),
                                JCozProfilerMBean.class));
                    return JCozProfilingErrorCodes.NORMAL_RETURN;
                }
            }
        } catch (IOException | NumberFormatException
                | AttachNotSupportedException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            logger.error("Got an exception during attachToProcess, stacktrace: {}", stringWriter);
            throw new RemoteException("", e);

                }
        return JCozProfilingErrorCodes.INVALID_JAVA_PROCESS;
    }

    /* (non-Javadoc)
     * @see jcoz.service.JCozServiceInterface#startProfiling(int)
     */
    @Override
    public int startProfiling(int pid) throws RemoteException {
        if (!attachedVMs.containsKey(pid)) {
            throw new RemoteException("", new JCozException(String.format(JVM_WITH_PID_IS_NOT_ATTACHED, pid)));
        }
        return attachedVMs.get(pid).startProfiling();
    }

    /* (non-Javadoc)
     * @see jcoz.service.JCozServiceInterface#endProfiling(int)
     */
    @Override
    public int endProfiling(int pid) throws RemoteException {
        if (!attachedVMs.containsKey(pid)) {
            throw new RemoteException("", new JCozException(String.format(JVM_WITH_PID_IS_NOT_ATTACHED, pid)));
        }
        return attachedVMs.get(pid).endProfiling();
    }

    /* (non-Javadoc)
     * @see jcoz.service.JCozServiceInterface#setProgressPoint(int, java.lang.String, int)
     */
    @Override
    public int setProgressPoint(int pid, String className, int lineNo) throws RemoteException {
        if (!attachedVMs.containsKey(pid)) {
            throw new RemoteException("", new JCozException(String.format(JVM_WITH_PID_IS_NOT_ATTACHED, pid)));
        }
        return attachedVMs.get(pid).setProgressPoint(className, lineNo);
    }

    /* (non-Javadoc)
     * @see jcoz.service.JCozServiceInterface#setScope(int, java.lang.String)
     */
    @Override
    public int setScope(int pid, String scope) throws RemoteException {
        if (!attachedVMs.containsKey(pid)) {
            throw new RemoteException("", new JCozException(String.format(JVM_WITH_PID_IS_NOT_ATTACHED, pid)));
        }
        return attachedVMs.get(pid).setScope(scope);
    }

    /* (non-Javadoc)
     * @see jcoz.service.JCozServiceInterface#getProfilerOutput(int)
     */
    @Override
    public byte[] getProfilerOutput(int pid) throws RemoteException {
        if (!attachedVMs.containsKey(pid)) {
            throw new RemoteException("", new JCozException(String.format(JVM_WITH_PID_IS_NOT_ATTACHED, pid)));
        }
        try {
            return attachedVMs.get(pid).getProfilerOutput();
        } catch (IOException e) {
            throw new RemoteException("", e);
        }
    }

    /* (non-Javadoc)
     * @see jcoz.service.JCozServiceInterface#getCurrentScope(int)
     */
    @Override
    public String getCurrentScope(int pid) throws RemoteException {
        if (!attachedVMs.containsKey(pid)) {
            throw new RemoteException("", new JCozException(String.format(JVM_WITH_PID_IS_NOT_ATTACHED, pid)));
        }
        return attachedVMs.get(pid).getCurrentScope();
    }

    /* (non-Javadoc)
     * @see jcoz.service.JCozServiceInterface#getProgressPoint(int)
     */
    @Override
    public String getProgressPoint(int pid) throws RemoteException {
        if (!attachedVMs.containsKey(pid)) {
            throw new RemoteException("", new JCozException(String.format(JVM_WITH_PID_IS_NOT_ATTACHED, pid)));
        }
        return attachedVMs.get(pid).getProgressPoint();
    }

}
