/*
 * Standard JNI Interface Header for Fire OS / Android Native Bridge
 */
#ifndef _JAVASOFT_JNI_H_
#define _JAVASOFT_JNI_H_

#include <stdarg.h>
#include <stdint.h>
#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef uint8_t  jboolean;
typedef int8_t   jbyte;
typedef uint16_t jchar;
typedef int16_t  jshort;
typedef int32_t  jint;
typedef int64_t  jlong;
typedef float    jfloat;
typedef double   jdouble;
typedef jint     jsize;

#define JNI_FALSE 0
#define JNI_TRUE 1
#define JNI_VERSION_1_6 0x00010006
#define JNI_OK 0
#define JNI_ERR (-1)

struct _jobject;
typedef struct _jobject *jobject;
typedef jobject jclass;
typedef jobject jthrowable;
typedef jobject jstring;
typedef jobject jarray;
typedef jarray  jbooleanArray;
typedef jarray  jbyteArray;
typedef jarray  jcharArray;
typedef jarray  jshortArray;
typedef jarray  jintArray;
typedef jarray  jlongArray;
typedef jarray  jfloatArray;
typedef jarray  jdoubleArray;
typedef jarray  jobjectArray;

struct _jfieldID;
typedef struct _jfieldID *jfieldID;
struct _jmethodID;
typedef struct _jmethodID *jmethodID;

typedef union jvalue {
    jboolean z;
    jbyte    b;
    jchar    c;
    jshort   s;
    jint     i;
    jlong    j;
    jfloat   f;
    jdouble  d;
    jobject  l;
} jvalue;

struct JNINativeInterface;
struct JNIInvokeInterface;

#ifdef __cplusplus
typedef const struct JNINativeInterface *JNIEnv;
typedef const struct JNIInvokeInterface *JavaVM;
#else
typedef const struct JNINativeInterface *JNIEnv;
typedef const struct JNIInvokeInterface *JavaVM;
#endif

struct JNINativeInterface {
    void* reserved0;
    void* reserved1;
    void* reserved2;
    void* reserved3;
    jint (*GetVersion)(JNIEnv *);
    jclass (*DefineClass)(JNIEnv *, const char *, jobject, const jbyte *, jsize);
    jclass (*FindClass)(JNIEnv *, const char *);
    jmethodID (*FromReflectedMethod)(JNIEnv *, jobject);
    jfieldID (*FromReflectedField)(JNIEnv *, jobject);
    jobject (*ToReflectedMethod)(JNIEnv *, jclass, jmethodID, jboolean);
    jclass (*GetSuperclass)(JNIEnv *, jclass);
    jboolean (*IsAssignableFrom)(JNIEnv *, jclass, jclass);
    jobject (*ToReflectedField)(JNIEnv *, jclass, jfieldID, jboolean);
    jint (*Throw)(JNIEnv *, jthrowable);
    jint (*ThrowNew)(JNIEnv *, jclass, const char *);
    jthrowable (*ExceptionOccurred)(JNIEnv *);
    void (*ExceptionDescribe)(JNIEnv *);
    void (*ExceptionClear)(JNIEnv *);
    void (*FatalError)(JNIEnv *, const char *);
    jint (*PushLocalFrame)(JNIEnv *, jint);
    jobject (*PopLocalFrame)(JNIEnv *, jobject);
    jobject (*NewGlobalRef)(JNIEnv *, jobject);
    void (*DeleteGlobalRef)(JNIEnv *, jobject);
    void (*DeleteLocalRef)(JNIEnv *, jobject);
    jboolean (*IsSameObject)(JNIEnv *, jobject, jobject);
    jobject (*NewLocalRef)(JNIEnv *, jobject);
    jint (*EnsureLocalCapacity)(JNIEnv *, jint);
    jobject (*AllocObject)(JNIEnv *, jclass);
    jobject (*NewObject)(JNIEnv *, jclass, jmethodID, ...);
    jobject (*NewObjectA)(JNIEnv *, jclass, jmethodID, const jvalue *);
    jobject (*NewObjectV)(JNIEnv *, jclass, jmethodID, va_list);
    jclass (*GetObjectClass)(JNIEnv *, jobject);
    jboolean (*IsInstanceOf)(JNIEnv *, jobject, jclass);
    jmethodID (*GetMethodID)(JNIEnv *, jclass, const char *, const char *);
    void (*CallVoidMethod)(JNIEnv *, jobject, jmethodID, ...);
    jfieldID (*GetFieldID)(JNIEnv *, jclass, const char *, const char *);
    jmethodID (*GetStaticMethodID)(JNIEnv *, jclass, const char *, const char *);
    void (*CallStaticVoidMethod)(JNIEnv *, jclass, jmethodID, ...);
    jstring (*NewStringUTF)(JNIEnv *, const char *);
    const char* (*GetStringUTFChars)(JNIEnv *, jstring, jboolean *);
    void (*ReleaseStringUTFChars)(JNIEnv *, jstring, const char *);
    jsize (*GetStringUTFLength)(JNIEnv *, jstring);
    jshortArray (*NewShortArray)(JNIEnv *, jsize);
    void (*SetShortArrayRegion)(JNIEnv *, jshortArray, jsize, jsize, const jshort *);
    jshort* (*GetShortArrayElements)(JNIEnv *, jshortArray, jboolean *);
    void (*ReleaseShortArrayElements)(JNIEnv *, jshortArray, jshort *, jint);
};

struct JNIInvokeInterface {
    void* reserved0;
    void* reserved1;
    void* reserved2;
    jint (*DestroyJavaVM)(JavaVM *);
    jint (*AttachCurrentThread)(JavaVM *, void **, void *);
    jint (*DetachCurrentThread)(JavaVM *);
    jint (*GetEnv)(JavaVM *, void **, jint);
    jint (*AttachCurrentThreadAsDaemon)(JavaVM *, void **, void *);
};

#define JNIEXPORT __attribute__ ((visibility ("default")))
#define JNICALL

#ifdef __cplusplus
}
#endif

#endif /* _JAVASOFT_JNI_H_ */
