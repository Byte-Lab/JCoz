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

import jcoz.agent.JCozProfilingErrorCodes;

/**
 * generates exceptions based on return codes
 * @author matt
 *
 */
public class JCozExceptionFactory {

    private JCozExceptionFactory(){

    }

    public static final JCozExceptionFactory instance = new JCozExceptionFactory();

    public static JCozExceptionFactory getInstance(){
        return instance;
    }

    public JCozException getJCozExceptionFromErrorCode(int errorCode){
        switch(errorCode){
            case JCozProfilingErrorCodes.NO_PROGRESS_POINT_SET:
                return new NoProgressPointSetException();
            case JCozProfilingErrorCodes.NO_SCOPE_SET:
                return new NoScopeSetException();
            case JCozProfilingErrorCodes.CANNOT_CALL_WHEN_RUNNING:
                return new InvalidWhenProfilerRunningException();
            case JCozProfilingErrorCodes.PROFILER_NOT_RUNNING:
                return new InvalidWhenProfilerNotRunningException();
            default:
                return new JCozException("Unknown Exception occurred: "+errorCode);
        }
    }

}
