package com.vernetperronllc.jcoz.service;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.vernetperronllc.jcoz.agent.JCozProfiler;
import com.vernetperronllc.jcoz.agent.JCozProfilerMBean;

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

    public void profileProcess() {
        this.mxbeanProxy.startProfiling();
        this.mxbeanProxy.endProfiling();
        this.mxbeanProxy.setProgressPoint("test.class", 12345);
        this.mxbeanProxy.setScope("test.scope");
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
			System.out.println(vmDesc.displayName());
			if (vmDesc.displayName().endsWith("JCozProfiler")){
				this.attachedVM = VirtualMachine.attach(vmDesc);
                this.attachedVM.startLocalManagementAgent();
                return;
			}
		}

        throw new Exception("Unable to find JCozProfiler VM");
    }
}
