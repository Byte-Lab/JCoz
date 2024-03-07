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
