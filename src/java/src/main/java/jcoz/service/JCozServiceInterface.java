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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import jcoz.JCozVMDescriptor;

/**
 * @author matt
 *
 */
public interface JCozServiceInterface extends Remote{
	
	public List<JCozVMDescriptor> getJavaProcessDescriptions() throws RemoteException;
	
	public int attachToProcess(int pid) throws RemoteException;
	
	public int startProfiling(int pid) throws RemoteException;
	
	public int endProfiling(int pid) throws RemoteException;
	
	public int setProgressPoint(int pid, String className, int lineNo) throws RemoteException;
	
	public int setScope(int pid, String scope) throws RemoteException;
	
	public byte[] getProfilerOutput(int pid) throws RemoteException;
	
	public String getCurrentScope(int pid) throws RemoteException;
	
	public String getProgressPoint(int pid) throws RemoteException;
	

}
