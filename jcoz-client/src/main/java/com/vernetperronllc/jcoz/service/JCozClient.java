/*
 * NOTICE
 *
 * Copyright (c) 2016 David C Vernet and Matthew J Perron. All rights reserved.
 *
 * Unless otherwise noted, all of the material in this file is Copyright (c) 2016
 * by David C Vernet and Matthew J Perron. All rights reserved. No part of this file
 * may be reproduced, published, distributed, displayed, performed, copied,
 * stored, modified, transmitted or otherwise used or viewed by anyone other
 * than the authors (David C Vernet and Matthew J Perron),
 * for either public or private use.
 *
 * No part of this file may be modified, changed, exploited, or in any way
 * used for derivative works or offered for sale without the express
 * written permission of the authors.
 *
 * This file has been modified from lightweight-java-profiler
 * (https://github.com/dcapwell/lightweight-java-profiler). See APACHE_LICENSE for
 * a copy of the license that was included with that original work.
 */

package com.vernetperronllc.jcoz.service;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.vernetperronllc.jcoz.agent.JCozProfiler;
import com.vernetperronllc.jcoz.Experiment;
import com.vernetperronllc.jcoz.agent.JCozProfilerMBean;
import java.io.IOException;

public class JCozClient {

    private static final String CONNECTOR_ADDR_PROPERTY = "com.sun.management.jmxremote.localConnectorAddress";

    private VirtualMachine attachedVM = null;

    private JCozProfilerMBean mxbeanProxy;

    private JMXConnector connector;

    public static void main(String[] args) throws 
    AttachNotSupportedException,
    IOException,
    AgentLoadException,
    AgentInitializationException,
    Exception {

        JCozClient client = new JCozClient();

        client.attachProfilerVM();
        client.connectToProfiler();

        try {
            // TODO(dcvernet): Loop here and communicate with the UI to control the profiling.
            // The client will listen to the UI, and execute whatever MBean commands the user chooses.
            client.profileProcess();
        } finally {
            client.closeConnection();
        }


//		java -agentpath:./build-64/liblagent.so=pkg=test_progress-point=test/Test:21_warmup=1000_slow-exp test.Test
    }

     
    public void profileProcess() throws IOException, InterruptedException {
        this.mxbeanProxy.setProgressPoint("test.TestThreadSerial", 38);
	this.mxbeanProxy.setScope("test");
        this.mxbeanProxy.startProfiling();
        for(int i = 0; i < 30; i++){
            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(this.mxbeanProxy.getProfilerOutput()));
            int numExperiments = ois.readInt();
            for (int j = 0; j < numExperiments; j++){
                    System.out.println(Experiment.deserialize(ois));
            }

            Thread.sleep(1000);
        }
        
        this.mxbeanProxy.endProfiling();
    }

    /**
     * Debug helper function for printing out all of the attached agent VM properties.
     * @throws Exception If you try to get the properties on a null VM.
     */
    private void printAgentProperties() throws Exception {
        if (this.attachedVM == null) {
            throw new Exception("You must first attach the profiler VM before you can query its agent properties.");
        }

        Properties props = this.attachedVM.getAgentProperties();
        for(Entry<Object, Object> e : props.entrySet()){
                System.out.println(e);
        }
    }

    /**
     * Close all open connections on the client.
     * @throws Exception If you try to detach a null VM.
     */
    public void closeConnection() throws Exception {
        if (this.attachedVM == null) {
            throw new Exception("You cannot detach a null agent.");
        }

        this.connector.close();
        this.attachedVM.detach();

        this.attachedVM = null;
    }

    /**
     * Connect to the profiler on the attached VM.
     */
    public void connectToProfiler() throws Exception {
        if (this.attachedVM == null) {
            throw new Exception("You must first attach the profiler VM before you can connect to the profiler.");
        }

        Properties props = this.attachedVM.getAgentProperties();
        String connectorAddress = props.getProperty(JCozClient.CONNECTOR_ADDR_PROPERTY);

        JMXServiceURL url = new JMXServiceURL(connectorAddress);
        this.connector = JMXConnectorFactory.connect(url);
        MBeanServerConnection mbeanConn = this.connector.getMBeanServerConnection();

        this.mxbeanProxy = JMX.newMXBeanProxy(mbeanConn, 
        JCozProfiler.getMBeanName(),  JCozProfilerMBean.class);
    }

    /**
     * Search through the list of running VMs on the localhost
     * and attach to a JCoz Profiler instance.
     */
    public void attachProfilerVM() throws Exception {
        // Get list of running JVMs on the local host and attach to the one
        // we want to profile.
        List<VirtualMachineDescriptor> vmList = VirtualMachine.list();
        VirtualMachine vm = null;
        for(VirtualMachineDescriptor vmDesc : vmList){
            if (vmDesc.displayName().endsWith("JCozProfiler")){
                this.attachedVM = VirtualMachine.attach(vmDesc);
                this.attachedVM.startLocalManagementAgent();
                return;
            }
        }

        throw new Exception("Unable to find JCozProfiler VM");
    }
}
