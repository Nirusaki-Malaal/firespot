# Android.mk for Fire OS PPSSPP
LOCAL_PATH := $(call my-dir)/../upstream

include $(CLEAR_VARS)

LOCAL_MODULE := retro_psp

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/Core/*.cpp) \
                   $(wildcard $(LOCAL_PATH)/Core/MIPS/*.cpp) \
                   $(wildcard $(LOCAL_PATH)/Core/MIPS/ARM/*.cpp) \
                   $(wildcard $(LOCAL_PATH)/Core/HLE/*.cpp) \
                   $(wildcard $(LOCAL_PATH)/GPU/*.cpp) \
                   $(wildcard $(LOCAL_PATH)/GPU/GLES/*.cpp) \
                   $(wildcard $(LOCAL_PATH)/Common/*.cpp) \
                   $(LOCAL_PATH)/libretro/libretro.cpp

LOCAL_C_INCLUDES := $(LOCAL_PATH) \
                    $(LOCAL_PATH)/Core \
                    $(LOCAL_PATH)/GPU \
                    $(LOCAL_PATH)/Common \
                    $(LOCAL_PATH)/ext

LOCAL_CFLAGS := -O3 -Wall -D__ANDROID__ -DUSING_GLES2 -DARM -D_ARCH_32
LOCAL_CPPFLAGS := -std=c++17 -fno-rtti -fexceptions

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
  LOCAL_CFLAGS += -march=armv7-a -mfpu=neon -mfloat-abi=softfp
  LOCAL_ARM_NEON := true
  LOCAL_ARM_MODE := arm
endif
ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
  LOCAL_CFLAGS += -march=armv8-a -D_ARCH_64
endif

LOCAL_LDLIBS := -llog -lGLESv2 -lEGL -lz

include $(BUILD_SHARED_LIBRARY)
