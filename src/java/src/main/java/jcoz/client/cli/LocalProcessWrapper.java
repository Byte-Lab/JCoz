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
package jcoz.client.cli;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import jcoz.agent.JCozProfiler;
import jcoz.agent.JCozProfilerMBean;
import jcoz.profile.Experiment;
import jcoz.service.InvalidWhenProfilerNotRunningException;
import jcoz.service.JCozException;
import jcoz.service.JCozExceptionFactory;
import jcoz.service.VirtualMachineConnectionException;

public class LocalProcessWrapper implements TargetProcessInterface{

    private VirtualMachine vm;

    private JCozProfilerMBean mbeanProxy;

    VirtualMachineDescriptor descriptor;

    private static final String CONNECTOR_ADDRESS_PROPERTY_KEY = "com.sun.management.jmxremote.localConnectorAddress";

    public LocalProcessWrapper(VirtualMachineDescriptor descriptor) throws VirtualMachineConnectionException{
        try{
            vm = VirtualMachine.attach(descriptor);
            vm.startLocalManagementAgent();
            Properties props = vm.getAgentProperties();
            String connectorAddress =
                props.getProperty(CONNECTOR_ADDRESS_PROPERTY_KEY);
            JMXServiceURL url = new JMXServiceURL(connectorAddress);
            JMXConnector connector = JMXConnectorFactory.connect(url);
            MBeanServerConnection mbeanConn = connector.getMBeanServerConnection();
            mbeanProxy = JMX.newMXBeanProxy(mbeanConn, 
                    JCozProfiler.getMBeanName(),  JCozProfilerMBean.class);
        } catch(IOException | AttachNotSupportedException e){
            throw new VirtualMachineConnectionException(e);
        }
    }


    public void startProfiling() throws JCozException{
        int returnCode = mbeanProxy.startProfiling();
        if(returnCode != 0){
            throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
        }
    }

    public void endProfiling() throws JCozException{
        int returnCode = mbeanProxy.endProfiling();
        if(returnCode != 0){
            throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
        }
    }

    public void setProgressPoint(String className, int lineNo) throws JCozException{
        int returnCode = mbeanProxy.setProgressPoint(className, lineNo);
        if(returnCode != 0){
            throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
        }
    }

    public void setScope(String scope) throws JCozException{
        int returnCode = mbeanProxy.setScope(scope);
        if(returnCode != 0){
            throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
        }
    }

    public List<Experiment> getProfilerOutput() throws JCozException{
        List<Experiment> experiments = new ArrayList<>();
        ObjectInputStream ois;

        try {
            byte[] profOutput = mbeanProxy.getProfilerOutput();
            if (profOutput == null){
                throw new InvalidWhenProfilerNotRunningException();
            }
            ois = new ObjectInputStream(new ByteArrayInputStream(profOutput));
            int numExperiments = ois.readInt();
            for (int j = 0; j < numExperiments; j++){
                experiments.add(Experiment.deserialize(ois));
            }
        } catch (IOException e) {
            throw new JCozException(e);
        }

        return experiments;
    }

    public String getCurrentScope(){
        return mbeanProxy.getCurrentScope();
    }

    public String getProgressPoint(){
        return mbeanProxy.getProgressPoint();
    }

    public VirtualMachineDescriptor getDescriptor() {
        return this.descriptor;
    }
}
