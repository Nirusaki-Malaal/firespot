# Android.mk for Fire OS mGBA
LOCAL_PATH := $(call my-dir)/../upstream

include $(CLEAR_VARS)

LOCAL_MODULE := retro_gba

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/src/core/*.c) \
                   $(wildcard $(LOCAL_PATH)/src/gba/*.c) \
                   $(wildcard $(LOCAL_PATH)/src/arm/*.c) \
                   $(wildcard $(LOCAL_PATH)/src/util/*.c) \
                   $(LOCAL_PATH)/src/platform/libretro/libretro.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
                    $(LOCAL_PATH)/src \
                    $(LOCAL_PATH)/src/platform/libretro

LOCAL_CFLAGS := -O3 -Wall -D__ANDROID__ -DFIREOS_SAMPLERATE=48000 -DHAVE_LOCALE
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
  LOCAL_CFLAGS += -march=armv7-a -mfpu=neon -mfloat-abi=softfp -DARM
  LOCAL_ARM_NEON := true
  LOCAL_ARM_MODE := arm
endif
ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
  LOCAL_CFLAGS += -march=armv8-a -DARM64
endif

LOCAL_LDLIBS := -llog -lz

include $(BUILD_SHARED_LIBRARY)
