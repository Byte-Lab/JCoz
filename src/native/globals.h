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

#include <assert.h>
#include <dlfcn.h>
#include <jvmti.h>
#include <jni.h>
#include <stdint.h>
#ifdef __APPLE__
#include <pthread.h>
#endif


#ifndef GLOBALS_H
#define GLOBALS_H

#define AGENTEXPORT __attribute__((visibility("default"))) JNIEXPORT

// Gets us around -Wunused-parameter
#define IMPLICITLY_USE(x) (void) x;

// Wrap JVMTI functions in this in functions that expect a return
// value and require cleanup.
#define JVMTI_ERROR_CLEANUP_1(error, retval, cleanup)    \
  {                                                      \
    int err;                                             \
    if ((err = (error)) != JVMTI_ERROR_NONE) {           \
      fprintf(stderr, "JVMTI error %d\n", err);          \
      cleanup;                                           \
      return (retval);                                   \
    }                                                    \
  }

// Wrap JVMTI functions in this in functions that expect a return value.
#define JVMTI_ERROR_1(error, retval) \
  JVMTI_ERROR_CLEANUP_1(error, retval, /* nothing */)

// Wrap JVMTI functions in this in void functions.
#define JVMTI_ERROR(error) JVMTI_ERROR_CLEANUP(error, /* nothing */)

// Wrap JVMTI functions in this in void functions that require cleanup.
#define JVMTI_ERROR_CLEANUP(error, cleanup)      \
  {                                              \
    int err;                                     \
    if ((err = (error)) != JVMTI_ERROR_NONE) {   \
      fprintf(stderr, "JVMTI error %d\n", err);  \
      cleanup;                                   \
      return;                                    \
    }                                            \
  }

#define DISALLOW_COPY_AND_ASSIGN(TypeName) \
  TypeName(const TypeName&);               \
  void operator=(const TypeName&)

#define DISALLOW_IMPLICIT_CONSTRUCTORS(TypeName) \
  TypeName();                                    \
  DISALLOW_COPY_AND_ASSIGN(TypeName)

// Short version: reinterpret_cast produces undefined behavior in many
// cases where memcpy doesn't.
template <class Dest, class Source>
inline Dest bit_cast(const Source& source) {
  // Compile time assertion: sizeof(Dest) == sizeof(Source)
  // A compile error here means your Dest and Source have different sizes.
  typedef char VerifySizesAreEqual[sizeof(Dest) == sizeof(Source) ? 1 : -1]
    __attribute__ ((unused));

  Dest dest;
  memcpy(&dest, &source, sizeof(dest));
  return dest;
}

template<class T>
class JvmtiScopedPtr {
  public:
    explicit JvmtiScopedPtr(jvmtiEnv *jvmti)
      : jvmti_(jvmti),
      ref_(NULL) {}

    JvmtiScopedPtr(jvmtiEnv *jvmti, T *ref)
      : jvmti_(jvmti),
      ref_(ref) {}

    ~JvmtiScopedPtr() {
      if (NULL != ref_) {
        JVMTI_ERROR(jvmti_->Deallocate((unsigned char *)ref_));
      }
    }

    T **GetRef() {
      assert(ref_ == NULL);
      return &ref_;
    }

    T *Get() {
      return ref_;
    }

    void AbandonBecauseOfError() {
      ref_ = NULL;
    }

  private:
    jvmtiEnv *jvmti_;
    T *ref_;

    DISALLOW_IMPLICIT_CONSTRUCTORS(JvmtiScopedPtr);
};

// Accessors for a JNIEnv for this thread.
class Accessors {
  public:
#ifdef __APPLE__
    // As of 8/2013, Darwin doesn't support __thread.  We love you,
    // Darwin!
    static void SetCurrentJniEnv(JNIEnv *env) {
      static bool once = false;
      int err;
      if ((err = pthread_setspecific(key_, reinterpret_cast<void *>(env))) != 0 &&
          !once) {
        once = true;
        perror("Was not able to set JNIEnv for at least one thread: ");
      }
    }

    static JNIEnv *CurrentJniEnv() {
      JNIEnv *p = reinterpret_cast<JNIEnv *>(pthread_getspecific(key_));
      return p;
    }

    static void Init() {
      if (pthread_key_create(&key_, NULL) != 0) {
        perror("Unable to init thread-local storage.  Profiling won't work:");
      }
    }

    static void Destroy() {
      if (pthread_key_delete(key_) != 0) {
        // Meh.
      }
    }
#else
    static void SetCurrentJniEnv(JNIEnv *env) {
      env_ = env;
    }

    static JNIEnv *CurrentJniEnv() {
      return env_;
    }

    static void Init() {
    }

    static void Destroy() {
    }
#endif

    template <class FunctionType>
      static inline FunctionType GetJvmFunction(const char *function_name) {
        // get handle to library
#ifdef __APPLE__
        static void *handle = dlopen("libjvm.dylib", RTLD_LAZY);
#else
        static void *handle = dlopen("libjvm.so", RTLD_LAZY);
#endif
        if (handle == NULL) {
          return NULL;
        }

        // get address of function, return null if not found
        return bit_cast<FunctionType>(dlsym(handle, function_name));
      }

  private:
#ifdef __APPLE__
    static pthread_key_t key_;
#else
    // This is very dangerous.  __thread is not async-safe when used in
    // a shared library, because it calls malloc the first time a given
    // thread accesses it.  This is unlikely to cause problems in
    // straightforward Java apps, but a real fix involves either a fix
    // to glibc or to the Java launcher, and casual users will have a
    // hard time with this.
    static __thread JNIEnv *env_;
#endif
};

#if defined(__GNUC__) && (defined(i386) || defined(__x86_64))
#if defined(__x86_64__)
#define __CAS_INSTR "lock; cmpxchgq %1,%2"
#define __ADD_INSTR "lock; xaddq %0,%1"
#else  // defined(__x86_64__)
#define __CAS_INSTR "lock; cmpxchgl %1,%2"
#define __ADD_INSTR "lock; xaddl %0,%1"
#endif  // defined(__x86_64__)
#else  // defined(__GNUC__) && (defined(i386) || defined(__x86_64))
#error \
  "Cannot compile with non-x86.  Add support for atomic ops, if you want it"
#endif  // defined(__GNUC__) && (defined(i386) || defined(__x86_64))

inline intptr_t NoBarrier_CompareAndSwap(volatile intptr_t *ptr,
    intptr_t old_value,
    intptr_t new_value) {
  intptr_t prev;
  __asm__ __volatile__(__CAS_INSTR
      : "=a"(prev)
      : "q"(new_value), "m"(*ptr), "0"(old_value)
      : "cc", "memory");
  return prev;
}

inline intptr_t NoBarrier_AtomicIncrement(volatile intptr_t* ptr,
    intptr_t increment) {
  intptr_t temp = increment;
  __asm__ __volatile__(__ADD_INSTR
      : "+r" (temp), "+m" (*ptr)
      : : "cc", "memory");
  // temp now contains the previous value of *ptr
  return temp + increment;
}

#undef __CAS_INSTR
#undef __ADD_INSTR

// Things that should probably be user-configurable

// Number of times per second that we profile
static const int kNumInterrupts = 100;

// Maximum number of stack traces
static const int kMaxStackTraces = 3000;

// Maximum number of frames to store from the stack traces sampled.
static const int kMaxFramesToCapture = 128;

// Location where the data are dumped.
static const char kDefaultOutFile[] = "traces.txt";

class Globals {
  public:
    static FILE *OutFile;
};

#endif  // GLOBALS_H
