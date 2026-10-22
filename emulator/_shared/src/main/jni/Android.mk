LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := retro_bridge
LOCAL_SRC_FILES := libretro_bridge.c

LOCAL_CFLAGS := -O3 -Wall -D__ANDROID__
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
  LOCAL_CFLAGS += -march=armv7-a -mfpu=neon -mfloat-abi=softfp
  LOCAL_ARM_NEON := true
  LOCAL_ARM_MODE := arm
endif

LOCAL_LDLIBS := -llog -landroid -ldl

include $(BUILD_SHARED_LIBRARY)
