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
package test;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.vernetperronllc.jcoz.JCozVMDescriptor;
import com.vernetperronllc.jcoz.client.cli.RemoteServiceWrapper;
import com.vernetperronllc.jcoz.client.cli.TargetProcessInterface;
import com.vernetperronllc.jcoz.profile.Experiment;
import com.vernetperronllc.jcoz.service.JCozException;


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
