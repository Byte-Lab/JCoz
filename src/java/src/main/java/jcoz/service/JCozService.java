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
