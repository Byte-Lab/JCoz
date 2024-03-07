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

import java.util.List;

import jcoz.profile.Experiment;
import jcoz.service.JCozException;

/**
 * @author matt
 *
 */
public interface TargetProcessInterface {

    public void startProfiling() throws JCozException;

    public void endProfiling() throws JCozException;

    public void setProgressPoint(String className, int lineNo) throws JCozException;

    public void setScope(String scope) throws JCozException;

    public List<Experiment> getProfilerOutput() throws JCozException;

    public String getCurrentScope() throws JCozException;

    public String getProgressPoint() throws JCozException;
}
