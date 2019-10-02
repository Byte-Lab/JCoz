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
package test;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.vernetperronllc.jcoz.JCozVMDescriptor;
import com.vernetperronllc.jcoz.client.cli.RemoteServiceWrapper;
import com.vernetperronllc.jcoz.client.cli.TargetProcessInterface;
import com.vernetperronllc.jcoz.profile.Experiment;
import com.vernetperronllc.jcoz.service.JCozException;


/**
 * @author matt
 *
 */
public class JCozServiceTest {
	

	public static void main(String[] args) throws RemoteException, NotBoundException, JCozException, InterruptedException{
		RemoteServiceWrapper service = new RemoteServiceWrapper("localhost");
		
		TargetProcessInterface remote = null;
		for (JCozVMDescriptor desc : service.listRemoteVirtualMachines()){
			if (desc.getDisplayName().contains("TestThreadSerial")){
				remote = service.attachToProcess(desc.getPid());
			}
		}
		remote.setProgressPoint("test.TestThreadSerial", 38);
		remote.setScope("test");
		remote.startProfiling();
		for(int i =0; i < 30; i++){
			for(Experiment exp: remote.getProfilerOutput()){
				System.out.println(exp);
			}
			Thread.sleep(1000);
		}
		remote.endProfiling();
	}
}
