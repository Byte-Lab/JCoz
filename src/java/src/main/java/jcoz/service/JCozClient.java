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
package jcoz.service;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import jcoz.agent.JCozProfiler;
import jcoz.agent.JCozProfilerMBean;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCozClient {

    private static final Logger logger = LoggerFactory.getLogger(JCozClient.class);

    private static final String CONNECTOR_ADDR_PROPERTY = "com.sun.management.jmxremote.localConnectorAddress";

    private VirtualMachine vm;

    private JCozProfilerMBean mxbeanProxy;

    private JMXConnector connector;

    public JCozClient(VirtualMachineDescriptor vmDesc) throws
            AttachNotSupportedException,
            IOException {
        this.vm = VirtualMachine.attach(vmDesc);
        this.vm.startLocalManagementAgent();
        this.connectToProfiler();
    }

    public void setProgressPoint(String progressPoint, int lineNo) {
        logger.debug("Setting progressPoint {} on line {}", progressPoint, lineNo);
        this.mxbeanProxy.setProgressPoint(progressPoint, lineNo);
    }

    public void setScope(String scope) {
        logger.info("Scope set to {}", scope);
        this.mxbeanProxy.setScope(scope);
    }

    public void startProfiling() {
        logger.info("Started Profiling");
        this.mxbeanProxy.startProfiling();
    }

    public void endProfiling() {
        logger.info("Profiling ended");
        this.mxbeanProxy.endProfiling();
    }

    public byte[] getProfilerOutput() throws IOException {
        return this.mxbeanProxy.getProfilerOutput();
    }

    /**
     * Debug helper function for printing out all of the attached agent VM properties.
     *
     * @throws Exception If you try to get the properties on a null VM.
     */
    public void printAgentProperties() throws Exception {
        Properties props = this.vm.getAgentProperties();
        for (Entry<Object, Object> entry : props.entrySet()) {
            logger.info("Entry: {}", entry);
        }
    }

    /**
     * Close all open connections on the client.
     *
     * @throws Exception If you try to detach a null VM.
     */
    public void closeConnection() throws Exception {
        logger.info("Closing connection to client");
        this.connector.close();
        this.vm.detach();
    }

    /**
     * Connect to the profiler on the attached VM.
     *
     * @throws MalformedURLException
     */
    private void connectToProfiler() throws IOException {

        Properties props = this.vm.getAgentProperties();
        String connectorAddress = props.getProperty(JCozClient.CONNECTOR_ADDR_PROPERTY);

        JMXServiceURL url = new JMXServiceURL(connectorAddress);
        this.connector = JMXConnectorFactory.connect(url);
        MBeanServerConnection mbeanConn = this.connector.getMBeanServerConnection();

        this.mxbeanProxy = JMX.newMXBeanProxy(mbeanConn,
                JCozProfiler.getMBeanName(), JCozProfilerMBean.class);
    }

    /**
     * Search through the list of running VMs on the localhost
     * and attach to a JCoz Profiler instance.
     *
     * @return Map<String, VirtualMachineDescriptor> A list of the VirtualMachines that
     * should be queried for being profilable.
     */
    public static Map<String, VirtualMachineDescriptor> getJCozVMList() {
        Map<String, VirtualMachineDescriptor> jcozVMs = new HashMap<>();
        for (VirtualMachineDescriptor vmDesc : VirtualMachine.list()) {
            if (vmDesc.displayName().endsWith("JCozProfiler")) {
                jcozVMs.put(vmDesc.displayName(), vmDesc);
            }
        }

        return jcozVMs;
    }
}
