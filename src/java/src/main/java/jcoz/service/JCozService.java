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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matt
 */
public class JCozService {

    private static final Logger logger = LoggerFactory.getLogger(JCozService.class);

    public static final int DEFAULT_SERVICE_PORT = 2216; // for VP

    public static final String SERVICE_NAME = "JCozService";

    // first argument is port otherwise use default
    public static void main(String[] args) throws JCozException, RemoteException {

        int port = DEFAULT_SERVICE_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new JCozException("Invalid port: " + args[0], e);
            }
        }
        Registry registry = LocateRegistry.createRegistry(port);
        JCozServiceInterface engine = new JCozServiceImpl();
        JCozServiceInterface stub = (JCozServiceInterface) UnicastRemoteObject
            .exportObject(engine, 0);
        registry.rebind(SERVICE_NAME, stub);
        // wait forever
        logger.info("Started listening on port {}", port);
        synchronized (JCozService.class) {
            while (true) {
                try {
                    JCozService.class.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
