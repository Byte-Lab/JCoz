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

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import jcoz.JCozVMDescriptor;
import jcoz.service.JCozException;
import jcoz.service.JCozService;
import jcoz.service.JCozServiceInterface;

/**
 * @author matt
 *
 */
public class RemoteServiceWrapper {

    JCozServiceInterface service;

    private String host;

    private int port;

    public RemoteServiceWrapper(String host, int port) throws JCozException {
        try {
            this.host = host;
            this.port = port;
            Registry reg = LocateRegistry.getRegistry(host, port);
            service = (JCozServiceInterface) reg
                .lookup(JCozService.SERVICE_NAME);
        } catch (RemoteException | NotBoundException e) {
            throw new JCozException(e);
        }
    }

    /**
     * uses the default JCozService port, 2216
     * 
     * @param host
     */
    public RemoteServiceWrapper(String host) throws JCozException {
        this(host, JCozService.DEFAULT_SERVICE_PORT);
    }

    public List<JCozVMDescriptor> listRemoteVirtualMachines() throws JCozException{
        try {
            return service.getJavaProcessDescriptions();
        } catch (RemoteException e) {
            throw new JCozException(e);
        }
    }

    public TargetProcessInterface attachToProcess(int remotePid) throws JCozException{
        return new RemoteProcessWrapper(service, remotePid);
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

}
