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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSpeedup {
    private Map<Integer, LineSpeedup> lineSpeedups = new HashMap<>();

    public ClassSpeedup(Experiment exp) {
        lineSpeedups.put(exp.getLineNo(), new LineSpeedup(exp));
    }

    public void addExperiment(Experiment exp) {
        int lineNo = exp.getLineNo();
        if (!this.lineSpeedups.containsKey(lineNo)) {
            this.lineSpeedups.put(lineNo, new LineSpeedup(exp));
        } else {
            this.lineSpeedups.get(lineNo).addExperiment(exp);
        }
    }

    public Collection<? extends Experiment> getExperiments() {
        List<Experiment> experiments = new ArrayList<>();
        for (LineSpeedup lineSpeedup : this.lineSpeedups.values()) {
            experiments.addAll(lineSpeedup.getExperiments());
        }

        return experiments;
    }

    public Collection<LineSpeedup> getLineSpeedups() {
        return this.lineSpeedups.values();
    }
}
