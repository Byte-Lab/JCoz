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

#include <jvmti.h>
#include <stdio.h>

#include <string>

#include "globals.h"
#include "profiler.h"

#ifndef DISPLAY_H
#define DISPLAY_H

// Some platforms have a ::string class that is different from ::std::string
// (although the interface is the same, of course).  On other platforms,
// ::string is the same as ::std::string.
#ifndef HAS_GLOBAL_STRING
using std::string;
#endif

class StackTracesPrinter {
  public:
    StackTracesPrinter(FILE *file, jvmtiEnv *jvmti)
      : file_(file), jvmti_(jvmti) {}

    void PrintStackTraces(TraceData *traces, int length);

    void PrintLeafHistogram(TraceData *traces, int length);

  private:
    FILE *file_;

    jvmtiEnv *jvmti_;

    bool PrintStackFrame(JVMPI_CallFrame *frame);

    void PrintStackTrace(TraceData *trace);

    bool GetStackFrameElements(JVMPI_CallFrame *frame, string *method_name,
        string *class_name, string *file_name,
        int *line_number);

    jint GetLineNumber(jmethodID method, jlocation location);

    DISALLOW_COPY_AND_ASSIGN(StackTracesPrinter);
};

#endif  // DISPLAY_H
