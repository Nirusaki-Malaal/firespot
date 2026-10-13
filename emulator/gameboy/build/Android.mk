# Android.mk for Fire OS Gambatte (GB/GBC)
LOCAL_PATH := $(call my-dir)/../upstream

include $(CLEAR_VARS)

LOCAL_MODULE := retro_gameboy

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/libgambatte/src/*.cpp) \
                   $(wildcard $(LOCAL_PATH)/libgambatte/src/mem/*.cpp) \
                   $(wildcard $(LOCAL_PATH)/libgambatte/src/sound/*.cpp) \
                   $(wildcard $(LOCAL_PATH)/libgambatte/src/video/*.cpp) \
                   $(LOCAL_PATH)/libgambatte/libretro/libretro.cpp

LOCAL_C_INCLUDES := $(LOCAL_PATH)/libgambatte/include \
                    $(LOCAL_PATH)/libgambatte/src \
                    $(LOCAL_PATH)/libgambatte/libretro

LOCAL_CFLAGS := -O3 -Wall -D__ANDROID__ -DHAVE_STDINT_H -DFIREOS_AUDIO_RATE=48000
LOCAL_CPPFLAGS := -std=c++11 -fno-rtti -fno-exceptions

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
  LOCAL_CFLAGS += -march=armv7-a -mfpu=neon -mfloat-abi=softfp -DARM
  LOCAL_ARM_NEON := true
  LOCAL_ARM_MODE := arm
endif
ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
  LOCAL_CFLAGS += -march=armv8-a -DARM64
endif

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
