#include <stdio.h>
#include <jvmti.h>
#include <string.h>

static
jvmtiEnv *jvmti = NULL;

jvmtiError error;

static void check_capabilities();

void JNICALL
myBreakpoint(jvmtiEnv *jvmti_env,
             JNIEnv *jni_env,
             jthread thread,
             jmethodID methodID,
             jlocation location) {

    printf("My breakpoint was called lolz\n");
}

void JNICALL
myVMInit(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread) {
    printf("Called VMInit\n");

    jclass cls = (*jni_env)->FindClass(jni_env, "Test");
    if( cls == NULL ) {
        printf("Unable to find class test\n");

        exit(1);
    }
    char *method = "main";
    jmethodID methodId = (*jni_env)->GetStaticMethodID(jni_env, cls, method, "([Ljava/lang/String;)V");

    if( methodId == NULL ) {
        printf("Unable to find method %s\n", method);
        exit(1);
    }

    jlocation start_location, end_location;
    (*jvmti_env)->GetMethodLocation(jvmti_env, methodId, &start_location, &end_location);

    jvmtiError error = (*jvmti_env)->SetBreakpoint(jvmti_env, methodId, start_location);
    if( error != JVMTI_ERROR_NONE ) {
        printf("Breakpoint not set: %d\n", error);
    } else {
        printf("Breakpoint set\n");
    }
}

void JNICALL
myVMDeath(jvmtiEnv *jvmti_env, JNIEnv *jni_env) {
    printf("Called VMDeath\n");
}

JNIEXPORT jint JNICALL
Agent_OnLoad(JavaVM *vm, char *options, void *reserved) {
    jvmtiCapabilities capabilities;

    jint rc;
    rc = (*vm)->GetEnv(vm, (void **)(&jvmti), JVMTI_VERSION);
    // @TODO: Add check

    // Set capabilities
    (*jvmti)->GetCapabilities(jvmti, &capabilities);
    capabilities.can_signal_thread = 1;
    capabilities.can_generate_breakpoint_events = 1;
    capabilities.can_generate_single_step_events = 1;
    capabilities.can_suspend = 1;
    capabilities.can_get_current_thread_cpu_time = 1;
    capabilities.can_get_thread_cpu_time = 1;
    capabilities.can_get_source_file_name = 1;
    capabilities.can_get_line_numbers = 1;
    jvmtiError error = (*jvmti)->AddCapabilities(jvmti, &capabilities);

    if( error != JVMTI_ERROR_NONE ) {
        printf("Error: %d\n", error);
        exit(0);
    }

    // Set callback methods
    jvmtiEventCallbacks callbacks;
    (void)memset(&callbacks, 0, sizeof(callbacks));
    callbacks.VMInit = &myVMInit; /* JVMTI_EVENT_VM_INIT */
    callbacks.VMDeath = &myVMDeath; /* JVMTI_EVENT_VM_DEATH */
    callbacks.Breakpoint = &myBreakpoint;
    (*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof(callbacks));

    // set notification mode
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, NULL);
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, NULL);
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_BREAKPOINT, NULL);

    return JNI_OK;
}

JNIEXPORT void JNICALL
Agent_OnUnLoad(JavaVM *vm) {
    printf("Called Agent_OnUnLoad()\n");
}

static
void check_capabilities() {
    jvmtiCapabilities currCap;
    (*jvmti)->GetCapabilities(jvmti, &currCap);
    if( currCap.can_generate_breakpoint_events ) {
        printf("Can set breakpoints\n");
    }

    if( currCap.can_signal_thread ) {
        printf("Can signal threads\n");
    }

    if( currCap.can_suspend ) {
        printf("Can suspend\n");
    }

    if( currCap.can_get_current_thread_cpu_time) {
        printf("Can current thread cpu time\n");
    }

    if( currCap.can_get_thread_cpu_time) {
        printf("Can get thread cpu time\n");
    }

    if( currCap.can_get_source_file_name ) {
        printf("Can get source file name\n");
    }

    if( currCap.can_get_line_numbers ) {
        printf("Can get line numbers\n");
    }
}
