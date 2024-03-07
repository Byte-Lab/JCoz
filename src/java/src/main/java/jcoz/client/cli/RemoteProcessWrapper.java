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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import jcoz.agent.JCozProfilingErrorCodes;
import jcoz.profile.Experiment;
import jcoz.service.InvalidWhenProfilerNotRunningException;
import jcoz.service.JCozException;
import jcoz.service.JCozExceptionFactory;
import jcoz.service.JCozServiceInterface;

/**
 * @author matt
 *
 */
public class RemoteProcessWrapper implements TargetProcessInterface {

    JCozServiceInterface service;
    int remotePid;

    /**
     * 
     */
    public RemoteProcessWrapper(JCozServiceInterface service, int pid) throws JCozException{
        this.service = service;
        this.remotePid = pid;
        int returnCode;
        try {
            returnCode = service.attachToProcess(pid);
        } catch (RemoteException e) {
            throw new JCozException(e);
        }
        if (returnCode != JCozProfilingErrorCodes.NORMAL_RETURN){
            throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
        }
    }


    public void startProfiling() throws JCozException{
        int returnCode;
        try {
            returnCode = service.startProfiling(remotePid);
        } catch (RemoteException e) {
            throw new JCozException(e);
        }
        if(returnCode != 0){
            throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
        }

    }

    public void endProfiling() throws JCozException{
        int returnCode;
        try {
            returnCode = service.endProfiling(remotePid);
        } catch (RemoteException e) {
            throw new JCozException(e);
        }
        if(returnCode != 0){
            throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
        }
    }

    public void setProgressPoint(String className, int lineNo) throws JCozException{
        int returnCode;
        try {
            returnCode = service.setProgressPoint(remotePid, className, lineNo);
        } catch (RemoteException e) {
            throw new JCozException(e);
        }
        if(returnCode != 0){
            throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
        }
    }

    public void setScope(String scope) throws JCozException{
        int returnCode;
        try {
            returnCode = service.setScope(remotePid, scope);
        } catch (RemoteException e) {
            throw new JCozException(e);
        }
        if(returnCode != 0){
            throw JCozExceptionFactory.getInstance().getJCozExceptionFromErrorCode(returnCode);
        }
    }

    public List<Experiment> getProfilerOutput() throws JCozException{
        List<Experiment> experiments = new ArrayList<>();
        ObjectInputStream ois;

        try {
            byte[] profOutput = service.getProfilerOutput(remotePid);
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

    public String getCurrentScope() throws JCozException{
        try {
            return service.getCurrentScope(remotePid);
        } catch (RemoteException e) {
            throw new JCozException(e);
        }
    }

    public String getProgressPoint() throws JCozException{
        try {
            return service.getProgressPoint(remotePid);
        } catch (RemoteException e) {
            throw new JCozException(e);
        }
    }

}
