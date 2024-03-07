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
