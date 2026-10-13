# Android.mk for Fire OS NES Core (FCEUmm)
LOCAL_PATH := $(call my-dir)/../upstream

include $(CLEAR_VARS)

LOCAL_MODULE := retro_nes
LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/src/*.c) \
                   $(wildcard $(LOCAL_PATH)/src/boards/*.c) \
                   $(wildcard $(LOCAL_PATH)/src/input/*.c) \
                   $(wildcard $(LOCAL_PATH)/src/mappers/*.c) \
                   $(wildcard $(LOCAL_PATH)/src/sound/*.c) \
                   $(LOCAL_PATH)/src/drivers/libretro/libretro.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/src \
                    $(LOCAL_PATH)/src/drivers/libretro \
                    $(LOCAL_PATH)/src/sound

LOCAL_CFLAGS := -O3 -Wall -D__ANDROID__ -DHAVE_ASPRINTF -DPSS_STYLE=1 -DFIREOS_AUDIO_RATE=48000
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
