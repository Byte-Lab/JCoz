/*
 * NOTICE
 *
 * Copyright (c) 2016 David C Vernet and Matthew J Perron. All rights reserved.
 *
 * Unless otherwise noted, all of the material in this file is Copyright (c) 2016
 * by David C Vernet and Matthew J Perron. All rights reserved. No part of this file
 * may be reproduced, published, distributed, displayed, performed, copied,
 * stored, modified, transmitted or otherwise used or viewed by anyone other
 * than the authors (David C Vernet and Matthew J Perron),
 * for either public or private use.
 *
 * No part of this file may be modified, changed, exploited, or in any way
 * used for derivative works or offered for sale without the express
 * written permission of the authors.
 *
 * This file has been modified from lightweight-java-profiler
 * (https://github.com/dcapwell/lightweight-java-profiler). See APACHE_LICENSE for
 * a copy of the license that was included with that original work.
 */
package com.vernetperronllc.jcoz.service;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author matt
 *
 */
public class JCozService {
	public static final int DEFAULT_SERVICE_PORT = 2216; // for VP
	
	public static final String SERVICE_NAME = "JCozService";

	// first element is port otherwise use default
	public static void main(String[] args) throws JCozException,
			RemoteException {
		int port = DEFAULT_SERVICE_PORT;
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				throw new JCozException("Invalid port : " + args[0], e);
			}
		}
		Registry registry = LocateRegistry.createRegistry(port);
		JCozServiceInterface engine = new JCozServiceImpl();
		JCozServiceInterface stub = (JCozServiceInterface) UnicastRemoteObject
				.exportObject(engine, 0);
		registry.rebind(SERVICE_NAME, stub);
		// wait forever
		synchronized (JCozService.class) {
			while (true) {
				try {
					JCozService.class.wait();
				} catch (InterruptedException e) {
					//do nothing
				}
			}
		}

	}

}
