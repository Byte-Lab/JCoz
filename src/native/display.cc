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

#include "display.h"

#include <inttypes.h>
#include <jni.h>
#include <jvmti.h>
#include <string.h>

#include <algorithm>
#include <string>
// The map used here doesn't need to be ordered, but unordered_map
// seems to cause problems out of the box on Ubuntu, and hash_map is
// non-standard.
#include <map>
#include <vector>

void StackTracesPrinter::PrintStackTraces(TraceData *traces, int length) {
  int count = 0;
  int total = 0;
  for (int i = 0; i < length; i++) {
    if (traces[i].count != 0) {
      total += traces[i].count;
      count++;
      fprintf(file_, "%" PRIdPTR " ", traces[i].count);
      PrintStackTrace(&traces[i]);
      fprintf(file_, "\n");
    }
  }
  fprintf(file_, "Total trace count = %d, Total traces = %d\n", total, count);
}

typedef std::pair<jint, jmethodID> PairCallFrame;
typedef std::pair<PairCallFrame, int> FrameCount;

struct Sorter {
  bool operator()(const FrameCount f1, const FrameCount f2) {
    return f1.second > f2.second;
  }
};

void StackTracesPrinter::PrintLeafHistogram(TraceData *traces, int length) {
  fprintf(file_, "\n\nHot methods:\n");
  std::map<PairCallFrame, int> hot_methods;
  for (int i = 0; i < length; i++) {
    if (traces[i].count != 0) {
      JVMPI_CallTrace *t = &(traces[i].trace);
      JVMPI_CallFrame *f = t->frames;
      JVMPI_CallFrame *last_frame = f + t->num_frames;
      while (f->lineno == -99 && f != last_frame) {
        f++;
      }
      if (f == last_frame) {
        continue;
      }

      PairCallFrame pair(f->lineno, f->method_id);
      hot_methods[pair] += traces[i].count;
    }
  }

  std::vector<FrameCount> sorted_methods;
  Sorter sorter;

  sorted_methods.reserve(hot_methods.size());

  for (auto method : hot_methods) {
    sorted_methods.emplace_back(method.first, method.second);
  }

  std::sort(sorted_methods.begin(), sorted_methods.end(), sorter);

  JVMPI_CallFrame last;
  last.method_id = NULL;
  last.lineno = 0;

  for (auto method : sorted_methods) {
    int count = method.second;
    PairCallFrame *f = &(method.first);
    JVMPI_CallFrame curr_frame;
    curr_frame.lineno = f->first;
    curr_frame.method_id = f->second;
    if (curr_frame.lineno == last.lineno &&
        curr_frame.method_id == last.method_id) {
      continue;
    }
    fprintf(file_, "%10d ", count);
    PrintStackFrame(&curr_frame);
    last = curr_frame;
  }
}

// This method changes the standard class signature "Lfoo/bar;" format
// to a more readable "foo.bar" format.
static void CleanJavaSignature(char *signature_ptr) {
  size_t signature_length = strlen(signature_ptr);  // ugh!
  if (signature_length < 3) {                    // I'm not going to even try.
    return;
  }

  signature_ptr[0] = ' ';
  for (size_t i = 1; i < signature_length - 1; ++i) {
    if (signature_ptr[i] == '/') {
      signature_ptr[i] = '.';
    }
  }
  signature_ptr[signature_length - 1] = '\0';
}

// Given a method and a location, this method gets the line number.
// Kind of expensive, comparatively.
jint StackTracesPrinter::GetLineNumber(jmethodID method, jlocation location) {
  jint entry_count;
  JvmtiScopedPtr<jvmtiLineNumberEntry> table_ptr_ctr(jvmti_);
  jint line_number = -1;

  // Shortcut for native methods.
  if (location == -1) {
    return -1;
  }

  int jvmti_error = jvmti_->GetLineNumberTable(method,
      &entry_count,
      table_ptr_ctr.GetRef());

  // Go through all the line numbers...
  if (JVMTI_ERROR_NONE != jvmti_error) {
    table_ptr_ctr.AbandonBecauseOfError();
  } else {
    jvmtiLineNumberEntry *table_ptr = table_ptr_ctr.Get();
    if (entry_count > 1) {
      jlocation last_location = table_ptr[0].start_location;
      for (int l = 1; l < entry_count; l++) {
        // ... and if you see one that is in the right place for your
        // location, you've found the line number!
        if ((location < table_ptr[l].start_location) &&
            (location >= last_location)) {
          line_number = table_ptr[l-1].line_number;
          return line_number;
        }
        last_location = table_ptr[l].start_location;
      }
      if (location >= last_location) {
        return table_ptr[entry_count - 1].line_number;
      }
    } else if (entry_count == 1) {
      line_number = table_ptr[0].line_number;
    }
  }
  return line_number;
}

bool StackTracesPrinter::GetStackFrameElements(JVMPI_CallFrame *frame,
    string *file_name,
    string *class_name,
    string *method_name,
    int *line_number) {
  jint error;
  JvmtiScopedPtr<char> name_ptr(jvmti_);

  // Get method name, put it in name_ptr
  if ((error = jvmti_->GetMethodName(frame->method_id, name_ptr.GetRef(), NULL,
          NULL)) !=
      JVMTI_ERROR_NONE) {
    name_ptr.AbandonBecauseOfError();
    if (error == JVMTI_ERROR_INVALID_METHODID) {
      static int once = 0;
      if (!once) {
        once = 1;
        fprintf(stderr, "One of your monitoring interfaces "
            "is having trouble resolving its stack traces.  "
            "GetMethodName on a jmethodID involved in a stacktrace "
            "resulted in an INVALID_METHODID error which usually "
            "indicates its declaring class has been unloaded.\n");
        fprintf(stderr, "Unexpected JVMTI error %d in GetMethodName", error);
      }
    }
    return false;
  }

  // Get class name, put it in signature_ptr
  jclass declaring_class;
  JVMTI_ERROR_1(
      jvmti_->GetMethodDeclaringClass(frame->method_id, &declaring_class),
      false);

  JvmtiScopedPtr<char> signature_ptr2(jvmti_);
  JVMTI_ERROR_CLEANUP_1(
      jvmti_->GetClassSignature(declaring_class, signature_ptr2.GetRef(), NULL),
      false, signature_ptr2.AbandonBecauseOfError());

  // Get source file, put it in source_name_ptr
  char *filename;
  JvmtiScopedPtr<char> source_name_ptr(jvmti_);
  static char file_unknown[] = "UnknownFile";
  if (JVMTI_ERROR_NONE !=
      jvmti_->GetSourceFileName(declaring_class, source_name_ptr.GetRef())) {
    source_name_ptr.AbandonBecauseOfError();
    filename = file_unknown;
  } else {
    filename = source_name_ptr.Get();
  }

  CleanJavaSignature(signature_ptr2.Get());

  // CleanJavaSignature prepends a ' ' character
  *class_name = signature_ptr2.Get() + 1;
  *method_name = name_ptr.Get();
  *file_name = filename;

  if (line_number != NULL) {
    // TODO(jeremymanson): is frame->lineno correct?  GetLineNumber
    // expects a BCI.
    *line_number = GetLineNumber(frame->method_id, frame->lineno);
  }

  return true;
}

bool StackTracesPrinter::PrintStackFrame(JVMPI_CallFrame *frame) {
  if (frame->lineno == -99) {
    // This should never happen in a stock hotspot build
    return false;
  }

  string method_name, class_name, file_name;
  int line_num;
  GetStackFrameElements(frame, &file_name, &class_name, &method_name,
      &line_num);
  fprintf(file_, "\t%s.%s(%s:%d)\n", class_name.c_str(), method_name.c_str(),
      file_name.c_str(), line_num);
  return true;
}

void StackTracesPrinter::PrintStackTrace(TraceData *trace) {
  JVMPI_CallTrace *t = &(trace->trace);
  if (t->num_frames < 0) {
    // Error trace - don't bother to print it.
    return;
  }

  fprintf(file_, "%d ", t->num_frames);
  for (int i = 0; i < t->num_frames; i++) {
    JVMPI_CallFrame *curr_frame = &(t->frames[i]);
    PrintStackFrame(curr_frame);
  }
}
