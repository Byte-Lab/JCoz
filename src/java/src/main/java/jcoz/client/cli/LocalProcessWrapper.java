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
