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
