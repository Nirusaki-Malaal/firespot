#ifndef _ANDROID_NATIVE_WINDOW_JNI_H
#define _ANDROID_NATIVE_WINDOW_JNI_H

#include <jni.h>
#include <android/native_window.h>

#ifdef __cplusplus
extern "C" {
#endif

ANativeWindow* ANativeWindow_fromSurface(JNIEnv* env, jobject surface);

#ifdef __cplusplus
}
#endif

#endif
