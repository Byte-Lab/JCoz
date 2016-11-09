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
package com.vernetperronllc.jcoz.client.cli;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.vernetperronllc.jcoz.client.ui.VisualizeProfileScene;
import com.vernetperronllc.jcoz.profile.Experiment;
import com.vernetperronllc.jcoz.profile.Profile;
import com.vernetperronllc.jcoz.service.JCozException;
import com.vernetperronllc.jcoz.service.VirtualMachineConnectionException;

/**
 * @author matt
 *
 */
public class JCozCLI {
	public static TargetProcessInterface process = null;
    public static boolean profilingStarted = false;

	public static void main(String[] args) throws ParseException, VirtualMachineConnectionException, JCozException, IOException, InterruptedException{
		Options ops = new Options();
		
		Option ppClassOption = new Option("c", "ppClass", true, "Class of ProgressPoint");
		ppClassOption.setRequired(true);
		ops.addOption(ppClassOption);
		
		Option ppLineNoOption = new Option("l", "ppLineNo", true, "Line number of progress point");
		ppLineNoOption.setRequired(true);
		ops.addOption(ppLineNoOption);
		
		Option pidOption = new Option("p", "pid", true, "ProcessID to com.vernetperronllc.jcoz.profile");
		pidOption.setRequired(true);
		ops.addOption(pidOption);
		
		Option scopeOption = new Option("s", "scope", true, "scope to com.vernetperronllc.jcoz.profile (package)");
		scopeOption.setRequired(true);
		ops.addOption(scopeOption);
		
		Option remoteHostOption = new Option("h", "host", true, "Remote hostname containing JVM process to profile");
		remoteHostOption.setRequired(false);
		ops.addOption(remoteHostOption);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cl = parser.parse(ops, args);
		String ppClass = cl.getOptionValue('c');
		String scopePkg = cl.getOptionValue('s');
		int ppLineNo = -1;
		int pid = -1;
		try{
			ppLineNo = Integer.parseInt(cl.getOptionValue('l'));
		}catch(NumberFormatException e){
			System.err.println("Invalid Line Number : "+ cl.getOptionValue('l'));
			System.exit(-1);
		}
		try{
			pid = Integer.parseInt(cl.getOptionValue('p'));
		}catch(NumberFormatException e){
			System.err.println("Invalid pid : "+ cl.getOptionValue('l'));
			System.exit(-1);
		}
		
		String remoteHost = cl.getOptionValue('h');
		boolean isRemote = remoteHost != null && !remoteHost.equals("");
		if (isRemote) {
			try {
				Profile profile = new Profile(remoteHost, null);
				final RemoteServiceWrapper remoteService = new RemoteServiceWrapper(remoteHost);
				JCozCLI.process = remoteService.attachToProcess(pid);
				JCozCLI.process.setProgressPoint(ppClass, ppLineNo);
				JCozCLI.process.setScope(scopePkg);
				JCozCLI.process.startProfiling();
                JCozCLI.profilingStarted = true;

				while (true) {
					// Sleep for 2 seconds
					Thread.sleep(2000);
					
					List<Experiment> experiments = JCozCLI.process.getProfilerOutput();
					profile.addExperiments(experiments);
				}
			} catch (JCozException e) {
				System.err.println("Unable to connect to target process.");
				e.printStackTrace();
			}
		} else {
			VirtualMachineDescriptor descriptor = null;
			for(VirtualMachineDescriptor vmDesc : VirtualMachine.list()){
				if (vmDesc.id().equals(Integer.toString(pid))){
					descriptor = vmDesc;
					break;
				}
			}
			if(descriptor == null){
				System.err.println("Could not find java process with pid : "+ pid);
				return;
			}
			
			JCozCLI.process = new LocalProcessWrapper(descriptor);
			//catch SIGINT and end profiling
			Runtime.getRuntime().addShutdownHook(new Thread()
	        {
	            @Override
	            public void run()
	            {
	                try {
	                	JCozCLI.process.endProfiling();
						System.exit(0);
					} catch (JCozException e) {
						// we are dying, do nothing
					}
	            }
	        });
			JCozCLI.process.setProgressPoint(ppClass, ppLineNo);
			JCozCLI.process.setScope(scopePkg);
			JCozCLI.process.startProfiling();
			while(true){
				for(Experiment e : JCozCLI.process.getProfilerOutput()){
					System.out.println(e.toString());
				}
				Thread.sleep(1000);
			}
			
			// search through and 
		}
	}


    public static void tryToLogPPHit() throws Exception {
        System.out.println("Called tryToLogPPHit");
        if (JCozCLI.profilingStarted) {
            System.out.println("Called tryToLogPPHit when profiling was started");
            JCozCLI.process.logProgressPointHit();
        }
    }
}
