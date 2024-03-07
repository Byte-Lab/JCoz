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
package test;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import jcoz.JCozVMDescriptor;
import jcoz.client.cli.RemoteServiceWrapper;
import jcoz.client.cli.TargetProcessInterface;
import jcoz.profile.Experiment;
import jcoz.service.JCozException;


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
