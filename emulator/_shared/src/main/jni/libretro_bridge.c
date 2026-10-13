/*
 * FireEmu Native Libretro Bridge for Amazon Fire TV (Fire OS)
 * High-performance, low-latency frontend wrapper for Libretro emulation cores.
 * Optimized for ARM Cortex-A53 / PowerVR GE8300 (Fire OS 7 / 8)
 */

#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <stdarg.h>
#include <stdbool.h>
#include <stdint.h>

#define LOG_TAG "FireEmuBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/* Libretro Constants */
#define RETRO_DEVICE_ID_JOYPAD_B        0
#define RETRO_DEVICE_ID_JOYPAD_Y        1
#define RETRO_DEVICE_ID_JOYPAD_SELECT   2
#define RETRO_DEVICE_ID_JOYPAD_START    3
#define RETRO_DEVICE_ID_JOYPAD_UP       4
#define RETRO_DEVICE_ID_JOYPAD_DOWN     5
#define RETRO_DEVICE_ID_JOYPAD_LEFT     6
#define RETRO_DEVICE_ID_JOYPAD_RIGHT    7
#define RETRO_DEVICE_ID_JOYPAD_A        8
#define RETRO_DEVICE_ID_JOYPAD_X        9
#define RETRO_DEVICE_ID_JOYPAD_L       10
#define RETRO_DEVICE_ID_JOYPAD_R       11
#define RETRO_DEVICE_ID_JOYPAD_L2      12
#define RETRO_DEVICE_ID_JOYPAD_R2      13
#define RETRO_DEVICE_ID_JOYPAD_L3      14
#define RETRO_DEVICE_ID_JOYPAD_R3      15

#define RETRO_ENVIRONMENT_SET_ROTATION            1
#define RETRO_ENVIRONMENT_GET_OVERSCAN            2
#define RETRO_ENVIRONMENT_GET_CAN_DUPE            3
#define RETRO_ENVIRONMENT_SET_MESSAGE             6
#define RETRO_ENVIRONMENT_SHUTDOWN                7
#define RETRO_ENVIRONMENT_SET_PERFORMANCE_LEVEL   8
#define RETRO_ENVIRONMENT_GET_SYSTEM_DIRECTORY    9
#define RETRO_ENVIRONMENT_SET_PIXEL_FORMAT       10
#define RETRO_ENVIRONMENT_SET_INPUT_DESCRIPTORS  11
#define RETRO_ENVIRONMENT_SET_KEYBOARD_CALLBACK  12
#define RETRO_ENVIRONMENT_SET_DISK_CONTROL_INTERFACE 13
#define RETRO_ENVIRONMENT_SET_HW_RENDER          14
#define RETRO_ENVIRONMENT_GET_VARIABLE           15
#define RETRO_ENVIRONMENT_SET_VARIABLES          16
#define RETRO_ENVIRONMENT_GET_VARIABLE_UPDATE    17
#define RETRO_ENVIRONMENT_SET_SUPPORT_NO_GAME    18
#define RETRO_ENVIRONMENT_GET_LIBRETRO_PATH      19
#define RETRO_ENVIRONMENT_GET_LOG_INTERFACE      27
#define RETRO_ENVIRONMENT_GET_CORE_ASSETS_DIRECTORY 30
#define RETRO_ENVIRONMENT_GET_SAVE_DIRECTORY     31

#define RETRO_PIXEL_FORMAT_0RGB1555 0
#define RETRO_PIXEL_FORMAT_XRGB8888 1
#define RETRO_PIXEL_FORMAT_RGB565   2

enum retro_log_level {
    RETRO_LOG_DEBUG = 0,
    RETRO_LOG_INFO,
    RETRO_LOG_WARN,
    RETRO_LOG_ERROR
};

struct retro_log_callback {
    void (*log)(enum retro_log_level level, const char *fmt, ...);
};

struct retro_variable {
    const char *key;
    const char *value;
};

struct retro_game_info {
    const char *path;
    const void *data;
    size_t      size;
    const char *meta;
};

struct retro_game_geometry {
    unsigned base_width;
    unsigned base_height;
    unsigned max_width;
    unsigned max_height;
    float    aspect_ratio;
};

struct retro_system_timing {
    double fps;
    double sample_rate;
};

struct retro_system_av_info {
    struct retro_game_geometry geometry;
    struct retro_system_timing timing;
};

typedef void (*retro_init_t)(void);
typedef void (*retro_deinit_t)(void);
typedef unsigned (*retro_api_version_t)(void);
typedef void (*retro_set_environment_t)(bool (*)(unsigned, void*));
typedef void (*retro_set_video_refresh_t)(void (*)(const void*, unsigned, unsigned, size_t));
typedef void (*retro_set_audio_sample_t)(void (*)(int16_t, int16_t));
typedef void (*retro_set_audio_sample_batch_t)(size_t (*)(const int16_t*, size_t));
typedef void (*retro_set_input_poll_t)(void (*)(void));
typedef void (*retro_set_input_state_t)(int16_t (*)(unsigned, unsigned, unsigned, unsigned));
typedef bool (*retro_load_game_t)(const struct retro_game_info*);
typedef void (*retro_unload_game_t)(void);
typedef void (*retro_run_t)(void);
typedef void (*retro_reset_t)(void);
typedef size_t (*retro_serialize_size_t)(void);
typedef bool (*retro_serialize_t)(void*, size_t);
typedef bool (*retro_unserialize_t)(const void*, size_t);
typedef void (*retro_get_system_av_info_t)(struct retro_system_av_info*);

/* Global State */
static void *g_core_handle = NULL;
static ANativeWindow *g_native_window = NULL;
static int g_current_input_bitmask = 0;
static int g_pixel_format = RETRO_PIXEL_FORMAT_RGB565;
static char g_system_dir[512] = "/sdcard/ROMs/bios";
static char g_save_dir[512] = "/sdcard/ROMs";

static JavaVM *g_jvm = NULL;
static jclass g_bridge_class = NULL;
static jmethodID g_audio_method = NULL;

/* Audio Accumulator Buffer to prevent JNI allocation stalls at 60 FPS */
#define AUDIO_BUF_SAMPLES 4096
static int16_t g_audio_accum[AUDIO_BUF_SAMPLES];
static size_t g_audio_accum_count = 0;
static jshortArray g_reusable_audio_array = NULL;

/* Libretro Core Function Pointers */
static retro_init_t core_init = NULL;
static retro_deinit_t core_deinit = NULL;
static retro_set_environment_t core_set_environment = NULL;
static retro_set_video_refresh_t core_set_video_refresh = NULL;
static retro_set_audio_sample_t core_set_audio_sample = NULL;
static retro_set_audio_sample_batch_t core_set_audio_batch = NULL;
static retro_set_input_poll_t core_set_input_poll = NULL;
static retro_set_input_state_t core_set_input_state = NULL;
static retro_load_game_t core_load_game = NULL;
static retro_unload_game_t core_unload_game = NULL;
static retro_run_t core_run = NULL;
static retro_reset_t core_reset = NULL;
static retro_serialize_size_t core_serialize_size = NULL;
static retro_serialize_t core_serialize = NULL;
static retro_unserialize_t core_unserialize = NULL;
static retro_get_system_av_info_t core_get_av_info = NULL;

static void core_log_cb(enum retro_log_level level, const char *fmt, ...) {
    char buf[1024];
    va_list args;
    va_start(args, fmt);
    vsnprintf(buf, sizeof(buf), fmt, args);
    va_end(args);

    switch (level) {
        case RETRO_LOG_DEBUG:
        case RETRO_LOG_INFO:
            LOGI("[Core] %s", buf);
            break;
        case RETRO_LOG_WARN:
            LOGW("[Core] %s", buf);
            break;
        case RETRO_LOG_ERROR:
        default:
            LOGE("[Core] %s", buf);
            break;
    }
}

static bool core_environ_cb(unsigned cmd, void *data) {
    switch (cmd) {
        case RETRO_ENVIRONMENT_SET_PIXEL_FORMAT:
            if (data) {
                g_pixel_format = *(const int*)data;
                LOGI("Libretro environment: Pixel format set to %d", g_pixel_format);
                return true;
            }
            return false;

        case RETRO_ENVIRONMENT_GET_SYSTEM_DIRECTORY:
        case RETRO_ENVIRONMENT_GET_CORE_ASSETS_DIRECTORY:
            if (data) {
                *(const char**)data = g_system_dir;
                LOGI("Libretro environment: Returned system directory %s", g_system_dir);
                return true;
            }
            return false;

        case RETRO_ENVIRONMENT_GET_SAVE_DIRECTORY:
            if (data) {
                *(const char**)data = g_save_dir;
                return true;
            }
            return false;

        case RETRO_ENVIRONMENT_GET_CAN_DUPE:
            if (data) {
                *(bool*)data = true;
                return true;
            }
            return false;

        case RETRO_ENVIRONMENT_SET_PERFORMANCE_LEVEL:
            return true;

        case RETRO_ENVIRONMENT_GET_LOG_INTERFACE:
            if (data) {
                struct retro_log_callback *cb = (struct retro_log_callback*)data;
                cb->log = core_log_cb;
                return true;
            }
            return false;

        case RETRO_ENVIRONMENT_SET_VARIABLES:
            return true;

        case RETRO_ENVIRONMENT_GET_VARIABLE:
            if (data) {
                struct retro_variable *var = (struct retro_variable*)data;
                if (var && var->key) {
                    var->value = NULL;
                }
            }
            return false;

        case RETRO_ENVIRONMENT_GET_VARIABLE_UPDATE:
            if (data) {
                *(bool*)data = false;
                return true;
            }
            return false;

        case RETRO_ENVIRONMENT_SET_SUPPORT_NO_GAME:
            return true;

        default:
            return false;
    }
}

static void core_video_refresh_cb(const void *data, unsigned width, unsigned height, size_t pitch) {
    if (!g_native_window || !data || width == 0 || height == 0) return;

    ANativeWindow_Buffer buffer;
    if (ANativeWindow_lock(g_native_window, &buffer, NULL) < 0) {
        return;
    }

    uint32_t *dst = (uint32_t*)buffer.bits;
    int dst_w = buffer.width;
    int dst_h = buffer.height;
    int stride = buffer.stride;

    if (!dst || dst_w <= 0 || dst_h <= 0) {
        ANativeWindow_unlockAndPost(g_native_window);
        return;
    }

    /* Compute integer/uniform aspect ratio scale */
    int scale_x = dst_w / width;
    int scale_y = dst_h / height;
    int scale = (scale_x < scale_y ? scale_x : scale_y);
    if (scale < 1) scale = 1;

    int render_w = width * scale;
    int render_h = height * scale;
    int offset_x = (dst_w - render_w) / 2;
    int offset_y = (dst_h - render_h) / 2;
    if (offset_x < 0) offset_x = 0;
    if (offset_y < 0) offset_y = 0;

    if (g_pixel_format == RETRO_PIXEL_FORMAT_XRGB8888) {
        for (unsigned y = 0; y < height; y++) {
            const uint32_t *line_src = (const uint32_t*)((const uint8_t*)data + y * pitch);
            for (int sy = 0; sy < scale; sy++) {
                int out_y = offset_y + y * scale + sy;
                if (out_y >= dst_h) break;
                uint32_t *line_dst = dst + out_y * stride;
                for (unsigned x = 0; x < width; x++) {
                    uint32_t pix = line_src[x] | 0xFF000000;
                    for (int sx = 0; sx < scale; sx++) {
                        int out_x = offset_x + x * scale + sx;
                        if (out_x < dst_w) {
                            line_dst[out_x] = pix;
                        }
                    }
                }
            }
        }
    } else if (g_pixel_format == RETRO_PIXEL_FORMAT_0RGB1555) {
        for (unsigned y = 0; y < height; y++) {
            const uint16_t *line_src = (const uint16_t*)((const uint8_t*)data + y * pitch);
            for (int sy = 0; sy < scale; sy++) {
                int out_y = offset_y + y * scale + sy;
                if (out_y >= dst_h) break;
                uint32_t *line_dst = dst + out_y * stride;
                for (unsigned x = 0; x < width; x++) {
                    uint16_t bgr1555 = line_src[x];
                    uint32_t r = ((bgr1555 >> 10) & 0x1F) << 3;
                    uint32_t g = ((bgr1555 >> 5) & 0x1F) << 3;
                    uint32_t b = (bgr1555 & 0x1F) << 3;
                    uint32_t pixel = (0xFF << 24) | (r << 16) | (g << 8) | b;
                    for (int sx = 0; sx < scale; sx++) {
                        int out_x = offset_x + x * scale + sx;
                        if (out_x < dst_w) {
                            line_dst[out_x] = pixel;
                        }
                    }
                }
            }
        }
    } else { // RETRO_PIXEL_FORMAT_RGB565
        for (unsigned y = 0; y < height; y++) {
            const uint16_t *line_src = (const uint16_t*)((const uint8_t*)data + y * pitch);
            for (int sy = 0; sy < scale; sy++) {
                int out_y = offset_y + y * scale + sy;
                if (out_y >= dst_h) break;
                uint32_t *line_dst = dst + out_y * stride;
                for (unsigned x = 0; x < width; x++) {
                    uint16_t rgb565 = line_src[x];
                    uint32_t r = ((rgb565 >> 11) & 0x1F) << 3;
                    uint32_t g = ((rgb565 >> 5) & 0x3F) << 2;
                    uint32_t b = (rgb565 & 0x1F) << 3;
                    uint32_t pixel = (0xFF << 24) | (r << 16) | (g << 8) | b;
                    for (int sx = 0; sx < scale; sx++) {
                        int out_x = offset_x + x * scale + sx;
                        if (out_x < dst_w) {
                            line_dst[out_x] = pixel;
                        }
                    }
                }
            }
        }
    }

    ANativeWindow_unlockAndPost(g_native_window);
}

static void flush_audio(void) {
    if (!g_jvm || !g_audio_method || g_audio_accum_count == 0) return;

    JNIEnv *env = NULL;
    if ((*g_jvm)->GetEnv(g_jvm, (void**)&env, JNI_VERSION_1_6) != JNI_OK || !env) {
        return;
    }

    if (!g_reusable_audio_array) {
        jshortArray local = (*env)->NewShortArray(env, AUDIO_BUF_SAMPLES);
        g_reusable_audio_array = (jshortArray)(*env)->NewGlobalRef(env, local);
        (*env)->DeleteLocalRef(env, local);
    }

    (*env)->SetShortArrayRegion(env, g_reusable_audio_array, 0, g_audio_accum_count, (const jshort*)g_audio_accum);
    (*env)->CallStaticVoidMethod(env, g_bridge_class, g_audio_method, g_reusable_audio_array, (jint)g_audio_accum_count);
    g_audio_accum_count = 0;
}

static void core_audio_sample_cb(int16_t left, int16_t right) {
    if (g_audio_accum_count + 2 >= AUDIO_BUF_SAMPLES) {
        flush_audio();
    }
    g_audio_accum[g_audio_accum_count++] = left;
    g_audio_accum[g_audio_accum_count++] = right;
}

static size_t core_audio_batch_cb(const int16_t *data, size_t frames) {
    if (!data || frames == 0) return frames;

    size_t samples = frames * 2;
    size_t offset = 0;
    while (offset < samples) {
        size_t available = AUDIO_BUF_SAMPLES - g_audio_accum_count;
        size_t chunk = (samples - offset < available) ? (samples - offset) : available;
        memcpy(&g_audio_accum[g_audio_accum_count], &data[offset], chunk * sizeof(int16_t));
        g_audio_accum_count += chunk;
        offset += chunk;
        if (g_audio_accum_count >= AUDIO_BUF_SAMPLES) {
            flush_audio();
        }
    }
    return frames;
}

static void core_input_poll_cb(void) {}

static int16_t core_input_state_cb(unsigned port, unsigned device, unsigned index, unsigned id) {
    if (port == 0 && id < 16) {
        return (g_current_input_bitmask & (1 << id)) ? 1 : 0;
    }
    return 0;
}

/* JNI Bindings */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_jvm = vm;
    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK) return -1;

    jclass cls = (*env)->FindClass(env, "com/fireemu/launcher/jni/LibretroBridge");
    if (!cls) {
        LOGE("Could not find LibretroBridge class");
        return -1;
    }
    g_bridge_class = (jclass)(*env)->NewGlobalRef(env, cls);
    g_audio_method = (*env)->GetStaticMethodID(env, g_bridge_class, "dispatchAudio", "([SI)V");
    return JNI_VERSION_1_6;
}

JNIEXPORT jboolean JNICALL Java_com_fireemu_launcher_jni_LibretroBridge_nativeInitCore(
        JNIEnv *env, jclass cls, jstring coreLibPath) {
    const char *path = (*env)->GetStringUTFChars(env, coreLibPath, NULL);
    LOGI("Loading Libretro core: %s", path);
    g_core_handle = dlopen(path, RTLD_LAZY | RTLD_LOCAL);
    (*env)->ReleaseStringUTFChars(env, coreLibPath, path);

    if (!g_core_handle) {
        LOGE("Failed to dlopen core: %s", dlerror());
        return JNI_FALSE;
    }

    core_init = (retro_init_t)dlsym(g_core_handle, "retro_init");
    core_deinit = (retro_deinit_t)dlsym(g_core_handle, "retro_deinit");
    core_set_environment = (retro_set_environment_t)dlsym(g_core_handle, "retro_set_environment");
    core_set_video_refresh = (retro_set_video_refresh_t)dlsym(g_core_handle, "retro_set_video_refresh");
    core_set_audio_sample = (retro_set_audio_sample_t)dlsym(g_core_handle, "retro_set_audio_sample");
    core_set_audio_batch = (retro_set_audio_sample_batch_t)dlsym(g_core_handle, "retro_set_audio_sample_batch");
    core_set_input_poll = (retro_set_input_poll_t)dlsym(g_core_handle, "retro_set_input_poll");
    core_set_input_state = (retro_set_input_state_t)dlsym(g_core_handle, "retro_set_input_state");
    core_load_game = (retro_load_game_t)dlsym(g_core_handle, "retro_load_game");
    core_unload_game = (retro_unload_game_t)dlsym(g_core_handle, "retro_unload_game");
    core_run = (retro_run_t)dlsym(g_core_handle, "retro_run");
    core_reset = (retro_reset_t)dlsym(g_core_handle, "retro_reset");
    core_serialize_size = (retro_serialize_size_t)dlsym(g_core_handle, "retro_serialize_size");
    core_serialize = (retro_serialize_t)dlsym(g_core_handle, "retro_serialize");
    core_unserialize = (retro_unserialize_t)dlsym(g_core_handle, "retro_unserialize");
    core_get_av_info = (retro_get_system_av_info_t)dlsym(g_core_handle, "retro_get_system_av_info");

    if (core_set_environment) core_set_environment(core_environ_cb);
    if (core_set_video_refresh) core_set_video_refresh(core_video_refresh_cb);
    if (core_set_audio_sample) core_set_audio_sample(core_audio_sample_cb);
    if (core_set_audio_batch) core_set_audio_batch(core_audio_batch_cb);
    if (core_set_input_poll) core_set_input_poll(core_input_poll_cb);
    if (core_set_input_state) core_set_input_state(core_input_state_cb);
    if (core_init) core_init();

    LOGI("Libretro core initialized successfully.");
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_fireemu_launcher_jni_LibretroBridge_nativeSetSurface(
        JNIEnv *env, jclass cls, jobject surface, jint width, jint height) {
    if (g_native_window) {
        ANativeWindow_release(g_native_window);
        g_native_window = NULL;
    }
    if (surface) {
        g_native_window = ANativeWindow_fromSurface(env, surface);
        ANativeWindow_setBuffersGeometry(g_native_window, width, height, WINDOW_FORMAT_RGBA_8888);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_fireemu_launcher_jni_LibretroBridge_nativeLoadGame(
        JNIEnv *env, jclass cls, jstring romPath, jstring biosPath) {
    if (!core_load_game) return JNI_FALSE;

    if (biosPath) {
        const char *c_bios = (*env)->GetStringUTFChars(env, biosPath, NULL);
        if (c_bios) {
            strncpy(g_system_dir, c_bios, sizeof(g_system_dir) - 1);
            g_system_dir[sizeof(g_system_dir) - 1] = '\0';
            (*env)->ReleaseStringUTFChars(env, biosPath, c_bios);
        }
    }

    const char *c_path = (*env)->GetStringUTFChars(env, romPath, NULL);
    if (c_path) {
        // Derive save directory from rom path
        strncpy(g_save_dir, c_path, sizeof(g_save_dir) - 1);
        char *last_slash = strrchr(g_save_dir, '/');
        if (last_slash) *last_slash = '\0';
    }

    struct retro_game_info info = {
        .path = c_path,
        .data = NULL,
        .size = 0,
        .meta = NULL
    };
    bool ok = core_load_game(&info);
    if (c_path) {
        (*env)->ReleaseStringUTFChars(env, romPath, c_path);
    }
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_fireemu_launcher_jni_LibretroBridge_nativeRunFrame(
        JNIEnv *env, jclass cls, jint inputBitmask) {
    g_current_input_bitmask = inputBitmask;
    if (core_run) {
        core_run();
    }
    flush_audio();
}

JNIEXPORT jboolean JNICALL Java_com_fireemu_launcher_jni_LibretroBridge_nativeSaveState(
        JNIEnv *env, jclass cls, jstring stateFilePath) {
    if (!core_serialize || !core_serialize_size) return JNI_FALSE;
    size_t sz = core_serialize_size();
    if (sz == 0) return JNI_FALSE;

    void *data = malloc(sz);
    if (!data) return JNI_FALSE;

    bool ok = core_serialize(data, sz);
    if (ok) {
        const char *c_file = (*env)->GetStringUTFChars(env, stateFilePath, NULL);
        FILE *f = fopen(c_file, "wb");
        if (f) {
            fwrite(data, 1, sz, f);
            fclose(f);
        } else {
            ok = false;
        }
        (*env)->ReleaseStringUTFChars(env, stateFilePath, c_file);
    }
    free(data);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_fireemu_launcher_jni_LibretroBridge_nativeLoadState(
        JNIEnv *env, jclass cls, jstring stateFilePath) {
    if (!core_unserialize || !core_serialize_size) return JNI_FALSE;
    size_t sz = core_serialize_size();
    if (sz == 0) return JNI_FALSE;

    const char *c_file = (*env)->GetStringUTFChars(env, stateFilePath, NULL);
    FILE *f = fopen(c_file, "rb");
    if (!f) {
        (*env)->ReleaseStringUTFChars(env, stateFilePath, c_file);
        return JNI_FALSE;
    }

    void *data = malloc(sz);
    size_t read_bytes = fread(data, 1, sz, f);
    fclose(f);
    (*env)->ReleaseStringUTFChars(env, stateFilePath, c_file);

    bool ok = false;
    if (read_bytes == sz) {
        ok = core_unserialize(data, sz);
    }
    free(data);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_fireemu_launcher_jni_LibretroBridge_nativeReset(
        JNIEnv *env, jclass cls) {
    if (core_reset) core_reset();
}

JNIEXPORT void JNICALL Java_com_fireemu_launcher_jni_LibretroBridge_nativeDestroy(
        JNIEnv *env, jclass cls) {
    if (core_unload_game) core_unload_game();
    if (core_deinit) core_deinit();
    if (g_native_window) {
        ANativeWindow_release(g_native_window);
        g_native_window = NULL;
    }
    if (g_core_handle) {
        dlclose(g_core_handle);
        g_core_handle = NULL;
    }
    if (g_reusable_audio_array && env) {
        (*env)->DeleteGlobalRef(env, g_reusable_audio_array);
        g_reusable_audio_array = NULL;
    }
    g_audio_accum_count = 0;
}
