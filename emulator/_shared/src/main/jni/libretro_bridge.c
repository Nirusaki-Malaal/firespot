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
#include <unistd.h>
#include "libretro.h"

#define LOG_TAG "FireEmuBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/* Global State */
static void *g_core_handle = NULL;
static ANativeWindow *g_native_window = NULL;
static int g_current_input_bitmask = 0;
static int g_pixel_format = RETRO_PIXEL_FORMAT_0RGB1555;
static void *g_rom_data = NULL;
static bool g_game_loaded = false;
static bool g_initialized = false;
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
static void (*core_init)(void) = NULL;
static void (*core_deinit)(void) = NULL;
static void (*core_set_environment)(retro_environment_t) = NULL;
static void (*core_set_video_refresh)(retro_video_refresh_t) = NULL;
static void (*core_set_audio_sample)(retro_audio_sample_t) = NULL;
static void (*core_set_audio_batch)(retro_audio_sample_batch_t) = NULL;
static void (*core_set_input_poll)(retro_input_poll_t) = NULL;
static void (*core_set_input_state)(retro_input_state_t) = NULL;
static bool (*core_load_game)(const struct retro_game_info*) = NULL;
static void (*core_unload_game)(void) = NULL;
static void (*core_run)(void) = NULL;
static void (*core_reset)(void) = NULL;
static size_t (*core_serialize_size)(void) = NULL;
static bool (*core_serialize)(void*, size_t) = NULL;
static bool (*core_unserialize)(const void*, size_t) = NULL;
static void (*core_get_av_info)(struct retro_system_av_info*) = NULL;
static void *(*core_memory_data)(unsigned) = NULL;
static size_t (*core_memory_size)(unsigned) = NULL;
static void (*core_get_info)(struct retro_system_info*) = NULL;

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

static int g_scaling_mode = 0;
/* Reuse horizontal mapping; expand each source row once, copy repeated scaled rows. */
static unsigned g_x_map[8192];
static unsigned g_map_source_width = 0;
static int g_map_target_width = 0;
static void core_video_refresh_cb(const void *data, unsigned width, unsigned height, size_t pitch) {
    if (!g_native_window || !data || width == 0 || height == 0) return;
    ANativeWindow_Buffer buffer;
    if (ANativeWindow_lock(g_native_window, &buffer, NULL) < 0) return;
    if (!buffer.bits || buffer.width <= 0 || buffer.height <= 0) { ANativeWindow_unlockAndPost(g_native_window); return; }
    uint32_t *dst = buffer.bits;
    int rw = buffer.width, rh = buffer.height;
    if (g_scaling_mode != 2) {
        double sx = (double)buffer.width / width, sy = (double)buffer.height / height;
        double scale = sx < sy ? sx : sy;
        if (g_scaling_mode == 1 && scale >= 1) scale = (int)scale;
        rw = (int)(width * scale); rh = (int)(height * scale);
    }
    int ox = (buffer.width-rw)/2, oy = (buffer.height-rh)/2;
    if (rw <= 0 || rh <= 0 || rw > 8192) { ANativeWindow_unlockAndPost(g_native_window); return; }
    if (g_map_source_width != width || g_map_target_width != rw) {
        for (int x=0; x<rw; x++) g_x_map[x] = (unsigned)((size_t)x * width / rw);
        g_map_source_width=width; g_map_target_width=rw;
    }
    if (oy > 0) memset(dst, 0, (size_t)buffer.stride * oy * 4);
    if (oy+rh < buffer.height) memset(dst+(oy+rh)*buffer.stride, 0, (size_t)(buffer.height-oy-rh)*buffer.stride*4);
    unsigned previous_source_y = height;
    for (int y=0; y<rh; y++) {
        unsigned source_y=(unsigned)((size_t)y * height / rh);
        uint32_t *target=dst+(oy+y)*buffer.stride;
        if (ox > 0) memset(target,0,(size_t)ox*4);
        if (ox+rw < buffer.stride) memset(target+ox+rw,0,(size_t)(buffer.stride-ox-rw)*4);
        target+=ox;
        if (source_y == previous_source_y) {
            memcpy(target,target-buffer.stride,(size_t)rw*4);
            continue;
        }
        const uint8_t *row=(const uint8_t*)data + source_y*pitch;
        for (int x=0; x<rw; x++) {
            unsigned px=g_x_map[x];
            uint32_t r,g,b;
            if (g_pixel_format == RETRO_PIXEL_FORMAT_XRGB8888) {
                uint32_t v=((const uint32_t*)row)[px];r=(v>>16)&255;g=(v>>8)&255;b=v&255;
            } else {
                uint16_t v=((const uint16_t*)row)[px];
                if (g_pixel_format == RETRO_PIXEL_FORMAT_RGB565) {r=((v>>11)&31)*255/31;g=((v>>5)&63)*255/63;b=(v&31)*255/31;}
                else {r=((v>>10)&31)*255/31;g=((v>>5)&31)*255/31;b=(v&31)*255/31;}
            }
            target[x]=0xFF000000 | (b<<16) | (g<<8) | r;
        }
        previous_source_y=source_y;
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
    if (port == 0 && device == RETRO_DEVICE_JOYPAD && id < 16) {
        return (g_current_input_bitmask & (1 << id)) ? 1 : 0;
    }
    return 0;
}

/* JNI Bindings */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_jvm = vm;
    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK) return -1;

    jclass cls = (*env)->FindClass(env, "com/fireemu/nativebridge/LibretroBridge");
    if (!cls) {
        LOGE("Could not find LibretroBridge class");
        return -1;
    }
    g_bridge_class = (jclass)(*env)->NewGlobalRef(env, cls);
    g_audio_method = (*env)->GetStaticMethodID(env, g_bridge_class, "dispatchAudio", "([SI)V");
    return JNI_VERSION_1_6;
}

JNIEXPORT jboolean JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeInitCore(
        JNIEnv *env, jclass cls, jstring coreLibPath) {
    const char *path = (*env)->GetStringUTFChars(env, coreLibPath, NULL);
    LOGI("Loading Libretro core: %s", path);
    if (g_core_handle) {
        (*env)->ReleaseStringUTFChars(env, coreLibPath, path);
        return JNI_FALSE;
    }
    g_core_handle = dlopen(path, RTLD_NOW | RTLD_LOCAL);
    (*env)->ReleaseStringUTFChars(env, coreLibPath, path);

    if (!g_core_handle) {
        LOGE("Failed to dlopen core: %s", dlerror());
        return JNI_FALSE;
    }

    unsigned (*api_version)(void) = dlsym(g_core_handle, "retro_api_version");
    if (!api_version || api_version() != RETRO_API_VERSION) {
        LOGE("Unsupported Libretro API version");
        dlclose(g_core_handle);
        g_core_handle = NULL;
        return JNI_FALSE;
    }
    g_pixel_format = RETRO_PIXEL_FORMAT_0RGB1555;

    core_memory_data = dlsym(g_core_handle, "retro_get_memory_data");
    core_memory_size = dlsym(g_core_handle, "retro_get_memory_size");
    core_get_info = dlsym(g_core_handle, "retro_get_system_info");
    core_init = dlsym(g_core_handle, "retro_init");
    core_deinit = dlsym(g_core_handle, "retro_deinit");
    core_set_environment = dlsym(g_core_handle, "retro_set_environment");
    core_set_video_refresh = dlsym(g_core_handle, "retro_set_video_refresh");
    core_set_audio_sample = dlsym(g_core_handle, "retro_set_audio_sample");
    core_set_audio_batch = dlsym(g_core_handle, "retro_set_audio_sample_batch");
    core_set_input_poll = dlsym(g_core_handle, "retro_set_input_poll");
    core_set_input_state = dlsym(g_core_handle, "retro_set_input_state");
    core_load_game = dlsym(g_core_handle, "retro_load_game");
    core_unload_game = dlsym(g_core_handle, "retro_unload_game");
    core_run = dlsym(g_core_handle, "retro_run");
    core_reset = dlsym(g_core_handle, "retro_reset");
    core_serialize_size = dlsym(g_core_handle, "retro_serialize_size");
    core_serialize = dlsym(g_core_handle, "retro_serialize");
    core_unserialize = dlsym(g_core_handle, "retro_unserialize");
    core_get_av_info = dlsym(g_core_handle, "retro_get_system_av_info");

    if (!core_get_info || !core_get_av_info || !core_init || !core_deinit ||
        !core_set_environment || !core_set_video_refresh || !core_set_audio_sample ||
        !core_set_audio_batch || !core_set_input_poll || !core_set_input_state ||
        !core_load_game || !core_unload_game || !core_run || !core_reset ||
        !core_serialize_size || !core_serialize || !core_unserialize) {
        LOGE("Core is missing required Libretro entry points");
        dlclose(g_core_handle);
        g_core_handle = NULL;
        return JNI_FALSE;
    }
    core_set_environment(core_environ_cb);
    if (core_set_video_refresh) core_set_video_refresh(core_video_refresh_cb);
    if (core_set_audio_sample) core_set_audio_sample(core_audio_sample_cb);
    if (core_set_audio_batch) core_set_audio_batch(core_audio_batch_cb);
    if (core_set_input_poll) core_set_input_poll(core_input_poll_cb);
    if (core_set_input_state) core_set_input_state(core_input_state_cb);
    core_init();
    g_initialized = true;

    LOGI("Libretro core initialized successfully.");
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeSetSurface(
        JNIEnv *env, jclass cls, jobject surface, jint width, jint height) {
    if (g_native_window) {
        ANativeWindow_release(g_native_window);
        g_native_window = NULL;
    }
    if (surface) {
        g_native_window = ANativeWindow_fromSurface(env, surface);
        if (!g_native_window || ANativeWindow_setBuffersGeometry(g_native_window, width, height, WINDOW_FORMAT_RGBA_8888) != 0) {
            if (g_native_window) ANativeWindow_release(g_native_window);
            g_native_window = NULL;
            return JNI_FALSE;
        }
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeLoadGame(
        JNIEnv *env, jclass cls, jstring romPath, jstring biosPath) {
    if (!g_initialized || g_game_loaded) return JNI_FALSE;

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
    struct retro_system_info system_info = {0};
    core_get_info(&system_info);
    if (!system_info.need_fullpath) {
        FILE *rom = fopen(c_path, "rb");
        long size = -1;
        if (rom && fseek(rom, 0, SEEK_END) == 0) size = ftell(rom);
        if (!rom || size <= 0 || size > 64 * 1024 * 1024 || fseek(rom, 0, SEEK_SET) != 0) {
            if (rom) fclose(rom);
            (*env)->ReleaseStringUTFChars(env, romPath, c_path);
            return JNI_FALSE;
        }
        g_rom_data = malloc((size_t)size);
        bool read_ok = g_rom_data && fread(g_rom_data, 1, size, rom) == (size_t)size;
        fclose(rom);
        if (!read_ok) {
            free(g_rom_data); g_rom_data = NULL;
            (*env)->ReleaseStringUTFChars(env, romPath, c_path);
            return JNI_FALSE;
        }
        info.data = g_rom_data;
        info.size = size;
    }
    bool ok = core_load_game(&info);
    g_game_loaded = ok;
    if (!ok) { free(g_rom_data); g_rom_data = NULL; }
    if (c_path) {
        (*env)->ReleaseStringUTFChars(env, romPath, c_path);
    }
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeRunFrame(
        JNIEnv *env, jclass cls, jint inputBitmask) {
    g_current_input_bitmask = inputBitmask;
    if (g_game_loaded) {
        core_run();
    }
    flush_audio();
}

JNIEXPORT jboolean JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeSaveState(
        JNIEnv *env, jclass cls, jstring stateFilePath) {
    if (!g_game_loaded || !core_serialize || !core_serialize_size) return JNI_FALSE;
    size_t sz = core_serialize_size();
    if (sz == 0) return JNI_FALSE;

    void *data = malloc(sz);
    if (!data) return JNI_FALSE;

    bool ok = core_serialize(data, sz);
    if (ok) {
        const char *c_file = (*env)->GetStringUTFChars(env, stateFilePath, NULL);
        char temp[1024];
        snprintf(temp, sizeof(temp), "%s.tmp", c_file);
        FILE *f = fopen(temp, "wb");
        if (f) {
            ok = fwrite(data, 1, sz, f) == sz;
            if (fflush(f) != 0 || fsync(fileno(f)) != 0) ok = false;
            if (fclose(f) != 0) ok = false;
            if (ok) ok = rename(temp, c_file) == 0;
            if (!ok) remove(temp);
        } else {
            ok = false;
        }
        (*env)->ReleaseStringUTFChars(env, stateFilePath, c_file);
    }
    free(data);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeLoadState(
        JNIEnv *env, jclass cls, jstring stateFilePath) {
    if (!g_game_loaded || !core_unserialize || !core_serialize_size) return JNI_FALSE;
    size_t sz = core_serialize_size();
    if (sz == 0) return JNI_FALSE;

    const char *c_file = (*env)->GetStringUTFChars(env, stateFilePath, NULL);
    FILE *f = fopen(c_file, "rb");
    if (!f) {
        (*env)->ReleaseStringUTFChars(env, stateFilePath, c_file);
        return JNI_FALSE;
    }

    void *data = malloc(sz);
    if (!data) { fclose(f); return JNI_FALSE; }
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

/* Cartridge battery RAM is independent of emulator snapshots. */
JNIEXPORT jboolean JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeBatteryRam(
        JNIEnv *env, jclass cls, jstring path, jboolean saving) {
    if (!g_game_loaded || !core_memory_data || !core_memory_size) return JNI_FALSE;
    size_t size = core_memory_size(RETRO_MEMORY_SAVE_RAM);
    void *data = core_memory_data(RETRO_MEMORY_SAVE_RAM);
    if (!data || !size || size > 8 * 1024 * 1024) return JNI_FALSE;
    const char *name = (*env)->GetStringUTFChars(env, path, NULL);
    if (!name) return JNI_FALSE;
    bool ok = false;
    if (saving) {
        char temp[1024];
        if (snprintf(temp, sizeof(temp), "%s.tmp", name) < (int)sizeof(temp)) {
            FILE *f = fopen(temp, "wb");
            if (f) {
                ok = fwrite(data, 1, size, f) == size;
                if (fflush(f) || fsync(fileno(f))) ok = false;
                if (fclose(f)) ok = false;
                if (ok) ok = rename(temp, name) == 0;
                if (!ok) remove(temp);
            }
        }
    } else {
        FILE *f = fopen(name, "rb");
        if (f) {
            if (fseek(f, 0, SEEK_END) == 0 && ftell(f) == (long)size && fseek(f, 0, SEEK_SET) == 0)
                ok = fread(data, 1, size, f) == size;
            fclose(f);
        }
    }
    (*env)->ReleaseStringUTFChars(env, path, name);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeReset(
        JNIEnv *env, jclass cls) {
    if (g_game_loaded) core_reset();
}

JNIEXPORT void JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeDestroy(
        JNIEnv *env, jclass cls) {
    if (g_game_loaded) core_unload_game();
    if (g_initialized) core_deinit();
    g_game_loaded = false;
    g_initialized = false;
    free(g_rom_data); g_rom_data = NULL;
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

JNIEXPORT jdoubleArray JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeGetTiming(
        JNIEnv *env, jobject self) {
    if (!g_game_loaded) return NULL;
    struct retro_system_av_info info = {0};
    core_get_av_info(&info);
    jdouble values[2] = {info.timing.fps, info.timing.sample_rate};
    jdoubleArray result = (*env)->NewDoubleArray(env, 2);
    if (result) (*env)->SetDoubleArrayRegion(env, result, 0, 2, values);
    return result;
}

JNIEXPORT void JNICALL Java_com_fireemu_nativebridge_LibretroBridge_nativeSetScaling(JNIEnv *env, jobject self, jint mode) { if (mode>=0 && mode<=2) g_scaling_mode=mode; }
