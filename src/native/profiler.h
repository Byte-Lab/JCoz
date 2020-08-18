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

#include <signal.h>
#include <jvmti.h>
#include <unordered_set>
#include <unordered_map>
#include <vector>
#include <atomic>
#include <pthread.h>
#include <fstream>
#include <iostream>
#include <map>

#include "globals.h"
#include "stacktraces.h"
#include "spdlog/spdlog.h"
#ifdef SPDLOG_VERSION
#include "spdlog/sinks/basic_file_sink.h"
#endif

#ifndef PROFILER_H
#define PROFILER_H

struct Experiment {
  long points_hit = 0;
  float speedup;
  long delay;
  long duration = 0;
  jmethodID method_id;
  jint lineno;
  jint bci;
  std::pair<jint,jint> *location_ranges;
  int num_ranges;
};

struct UserThread {
  pthread_t thread;
  long local_delay = 0;
  long points_hit = 0;
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

    static std::shared_ptr<spdlog::logger> &getLogger() { return logger; };

    static std::vector<std::string>& get_search_scopes() { return search_scopes; }
    static std::vector<std::string>& get_ignored_scopes() { return ignored_scopes; }

    static std::unordered_set<void *> &getInScopeMethods() { return in_scope_ids; }

    static struct Experiment &getCurrentExperiment() { return current_experiment; }

    static bool inExperiment() { return in_experiment; }

    static std::unordered_set<struct UserThread*> &getUserThreads() { return user_threads; }

    static void runAgentThread(jvmtiEnv *jvmti_env, JNIEnv *jni_env, void *args);

    static void addUserThread(jthread thread);

    static void removeUserThread(jthread thread);

    void setJVMTI(jvmtiEnv *jvmti);

    jvmtiEnv * getJVMTI();

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

    void setScope(std::string package);

    void setProgressPoint(std::string class_name, jint line_no);

    void setMBeanObject(jobject mbean);

    jobject getMBeanObject();

    void clearMBeanObject();

    void setJNI(JNIEnv* jni);

    static void clearInScopeMethods();

    static bool isRunning();

    void init();

  private:

    jvmtiEnv *jvmti_;

    SignalHandler handler_;

    struct sigaction old_action_;

    static JNIEnv *jni_;

    static void Handle(int signum, siginfo_t *info, void *context);

    static bool inline inExperiment(JVMPI_CallFrame &curr_frame);
    static bool inline frameInScope(JVMPI_CallFrame &curr_frame);
    DISALLOW_COPY_AND_ASSIGN(Profiler);

    static void prepare_scope(std::string &scope);

    static void add_search_scope(std::string &scope);
    static void add_ignored_scope(std::string &scope);

    static jobject mbean;

    static jmethodID mbean_cache_method_id;

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

    static std::atomic_bool _running;

    static void runExperiment(JNIEnv * jnienv);

    static float calculate_random_speedup();

    static void signal_user_threads();

    static volatile bool end_to_end;

    static pthread_t agent_pthread;

    static char *getClassFromMethodIDLocation(jmethodID method_id);

    static volatile pthread_t in_scope_lock;

    static std::atomic_bool profile_done;

    static void cleanSignature(char *sig);

    static std::string package;

    static struct ProgressPoint *progress_point;

    static std::string progress_class;

    static unsigned long experiment_time;

    static unsigned long warmup_time;

    static bool prof_ready;

    static bool fix_exp;

    static std::vector<std::string> search_scopes;
    static std::vector<std::string> ignored_scopes;

    static std::shared_ptr<spdlog::logger> logger;
};

#endif  // PROFILER_H
