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
