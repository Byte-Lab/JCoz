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

#include <signal.h>
#include <jvmti.h>
#include <unordered_set>
#include <unordered_map>
#include <vector>
#include <atomic>
#include <pthread.h>
#include <fstream>
#include <iostream>

#include "globals.h"
#include "stacktraces.h"

#ifndef PROFILER_H
#define PROFILER_H

struct Experiment {
    unsigned long points_hit = 0;
    float speedup;
    long delay;
    long duration = 0;
    jmethodID method_id;
    jint lineno;
    std::pair<jint,jint> *location_ranges;
    int num_ranges;
};

struct UserThread {
    pthread_t thread;
    long local_delay = 0;
    unsigned long points_hit = 0;
    unsigned int num_signals_received = 0;
    jthread java_thread;
};

struct ProgressPoint {
    jmethodID method_id;
    jint lineno;
    jlocation location;
};

class SignalHandler {
 public:
  SignalHandler() {}

  struct sigaction SetAction(void (*sigaction)(int, siginfo_t *, void *));

 private:
  DISALLOW_COPY_AND_ASSIGN(SignalHandler);
};

struct TraceData {
  intptr_t count;
  JVMPI_CallTrace trace;
};

class Profiler {
 public:
  explicit Profiler(jvmtiEnv *jvmti) : jvmti_(jvmti) {}

  void Start();

  void Stop();

  void ParseOptions(const char *options);

  static std::string &getPackage() { return package; }

  static std::string &getProgressClass() { return progress_class; }

  static std::unordered_set<void *> &getInScopeMethods() { return in_scope_ids; }

  static struct Experiment &getCurrentExperiment() { return current_experiment; }

  static bool inExperiment() { return in_experiment; }

  static std::unordered_set<struct UserThread*> &getUserThreads() { return user_threads; }

  static void runAgentThread(jvmtiEnv *jvmti_env, JNIEnv *jni_env, void *args);

  static void addUserThread(jthread thread);

  static void removeUserThread(jthread thread);

  static void setJVMTI(jvmtiEnv *jvmti);

  static void addInScopeMethods(jint method_count, jmethodID *methods);

  static void addProgressPoint(jint method_count, jmethodID *methods);

  static void clearProgressPoint();

  static void printInScopeLineNumberMapping();

  static void HandleBreakpoint(
          jvmtiEnv *jvmti,
          JNIEnv *jni_env,
          jthread thread,
          jmethodID method_id,
          jlocation location
  );

 private:
  jvmtiEnv *jvmti_;

  SignalHandler handler_;

  struct sigaction old_action_;

  static void Handle(int signum, siginfo_t *info, void *context);

  static bool inline inExperiment(JVMPI_CallFrame &curr_frame);
  static bool inline frameInScope(JVMPI_CallFrame &curr_frame);
  DISALLOW_COPY_AND_ASSIGN(Profiler);

  static std::unordered_set<void *> in_scope_ids;

  static struct Experiment current_experiment;

  static std::vector<JVMPI_CallFrame> call_frames;

  static volatile int frame_lock;

  static volatile int user_threads_lock;

  static volatile bool in_experiment;

  static std::atomic_ulong points_hit;

  static std::unordered_set<struct UserThread*> user_threads;

  static bool thread_in_main(jthread thread);

  static jvmtiEnv *jvmti;

  static std::atomic<long> global_delay;

  static volatile bool _running;

  static void runExperiment();

  static float calculate_random_speedup();

  static void signal_user_threads();

  static volatile bool end_to_end;

  static pthread_t agent_pthread;

  static char *getClassFromMethodIDLocation(jmethodID method_id);

  static volatile pthread_t in_scope_lock;

  static volatile bool profile_done;

  static void cleanSignature(char *sig);

  static std::string package;

  static void print_usage();

  static struct ProgressPoint *progress_point;

  static std::string progress_class;

  static std::ofstream prof_output;

  static unsigned long experiment_time;

  static unsigned long warmup_time;

  static bool prof_ready;

  static bool slow_exp;
};

#endif  // PROFILER_H
