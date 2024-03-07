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
package jcoz.profile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A jcoz.profile for a given application. It contains all speedup data for
 * a given application, and manages the logic for parsing an existing profile,
 * and logging experiments.
 *
 * @author David
 */
public class Profile {

    private static final Logger logger = LoggerFactory.getLogger(Profile.class);

    private Map<String, ClassSpeedup> classSpeedups = new HashMap<>();

    private String process;

    private RandomAccessFile stream;

    public Profile(String process) {
        this.process = process;

        this.initializeProfileLogging();
    }

    /**
     * Add a list of experiments to this jcoz.profile.
     *
     * @param experiments List of new experiments to add to jcoz.profile.
     */
    public synchronized void addExperiments(List<Experiment> experiments) {
        for (Experiment experiment : experiments) {
            logger.info("Adding experiment {}", experiment);
            String classSig = experiment.getClassSig();
            if (!this.classSpeedups.containsKey(classSig)) {
                this.classSpeedups.put(classSig, new ClassSpeedup(experiment));
            } else {
                this.classSpeedups.get(classSig).addExperiment(experiment);
            }
        }

        try {
            this.flushExperimentsToCozFile(experiments);
        } catch (IOException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            logger.error("Unable to flush new experiments to file, stacktrace: {}", stringWriter);
        }
    }

    /**
     * @return List of all experiments in this profile.
     */
    public synchronized List<Experiment> getExperiments() {
        List<Experiment> experiments = new ArrayList<>();
        for (ClassSpeedup classSpeedup : this.classSpeedups.values()) {
            experiments.addAll(classSpeedup.getExperiments());
        }

        return experiments;
    }

    /**
     * @return This profile's process name.
     */
    public String getProcess() {
        return this.process;
    }

    /**
     * Flush all pending experiments to the profile and
     * close the open file reader/writer.
     *
     * @param experiments Pending experiments to be written.
     */
    public synchronized void flushAndCloseLog(List<Experiment> experiments) {
        try {
            this.flushExperimentsToCozFile(experiments);
            this.stream.close();
        } catch (IOException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            logger.error("Unable to flush and close stream, stacktrace: {}", stringWriter);
        }
    }

    /**
     * Append a list of experiments to the current coz file.
     *
     * @param experiments
     * @throws IOException
     */
    private void flushExperimentsToCozFile(List<Experiment> experiments) throws IOException {
        StringBuilder profText = new StringBuilder();
        for (Experiment exp : experiments) {
            profText.append(exp.toString()).append("\n");
        }

        this.stream.writeBytes(profText.toString());
    }

    /**
     * Open a stream to a .coz file. If there are any existing experiments in
     * the file, add those experiments to the current chart.
     *
     * @TODO(david): Allow the user to truncate and archive existing profiles.
     */
    private void initializeProfileLogging() {
        File profile = new File(this.process + ".coz");
        logger.info("Creating profile {}", profile.getAbsolutePath());
        try {
            this.stream = new RandomAccessFile(profile, "rw");
            this.readExperimentsFromLogFile();
        } catch (IOException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            logger.error("Unable to create jcoz.profile output file or read jcoz.profile output from file, stacktrace: {}", stringWriter);
        }
    }

    /**
     * Read all of the experiments already contained in a jcoz.profile output.
     *
     * @throws IOException
     */
    private void readExperimentsFromLogFile() throws IOException {
        // Iterate through every line in the file, and add any existing
        // profile entries to the current profile.
        while (this.stream.getFilePointer() != this.stream.length()) {
            String line1 = this.stream.readLine();
            String line2 = this.stream.readLine();

            Experiment newExp = new Experiment(line1 + "\n" + line2);
            logger.info("Experiment {}", newExp);

            String classSig = newExp.getClassSig();
            if (!this.classSpeedups.containsKey(classSig)) {
                this.classSpeedups.put(classSig, new ClassSpeedup(newExp));
            } else {
                this.classSpeedups.get(classSig).addExperiment(newExp);
            }
        }
    }
}
