# Android.mk for Fire OS PCSX ReARMed
LOCAL_PATH := $(call my-dir)/../upstream

include $(CLEAR_VARS)

LOCAL_MODULE := retro_ps1

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/libpcsxcore/*.c) \
                   $(wildcard $(LOCAL_PATH)/libpcsxcore/cdrom/*.c) \
                   $(wildcard $(LOCAL_PATH)/plugins/gpulib/*.c) \
                   $(wildcard $(LOCAL_PATH)/plugins/dfsound/*.c) \
                   $(wildcard $(LOCAL_PATH)/plugins/dfinput/*.c) \
                   $(LOCAL_PATH)/frontend/libretro.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
                    $(LOCAL_PATH)/libpcsxcore \
                    $(LOCAL_PATH)/plugins/gpulib \
                    $(LOCAL_PATH)/plugins/dfsound

LOCAL_CFLAGS := -O3 -Wall -D__ANDROID__ -DARM -D_ARM_ASSEM_ -DHAVE_NEON=1 -DFIREOS_HDMI_SAMPLERATE=48000
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
  LOCAL_CFLAGS += -march=armv7-a -mfpu=neon -mfloat-abi=softfp -DDYNAREC_ARI64
  LOCAL_SRC_FILES += $(wildcard $(LOCAL_PATH)/libpcsxcore/new_dynarec/*.c) \
                     $(wildcard $(LOCAL_PATH)/plugins/gpu_neon/*.c)
  LOCAL_ARM_NEON := true
  LOCAL_ARM_MODE := arm
endif
ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
  LOCAL_CFLAGS += -march=armv8-a -DARM64 -DDYNAREC_LIGHTREC
endif

LOCAL_LDLIBS := -llog -lz

include $(BUILD_SHARED_LIBRARY)
