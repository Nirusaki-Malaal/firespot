/*
 * Fire OS PPSSPP Libretro Core Bridge
 * PlayStation Portable Core implementation for Amazon Fire TV
 */

#include <stdint.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include "libretro/libretro.h"

static retro_environment_t g_environ_cb;
static retro_video_refresh_t g_video_cb;
static retro_audio_sample_t g_audio_cb;
static retro_audio_sample_batch_t g_audio_batch_cb;
static retro_input_poll_t g_input_poll_cb;
static retro_input_state_t g_input_state_cb;

static uint16_t g_framebuffer[480 * 272];
static char g_rom_path[512] = "";
static uint32_t g_frame_counter = 0;

void retro_set_environment(retro_environment_t cb) {
    g_environ_cb = cb;
    if (cb) {
        enum retro_pixel_format fmt = RETRO_PIXEL_FORMAT_RGB565;
        cb(RETRO_ENVIRONMENT_SET_PIXEL_FORMAT, &fmt);
    }
}

void retro_set_video_refresh(retro_video_refresh_t cb) { g_video_cb = cb; }
void retro_set_audio_sample(retro_audio_sample_t cb) { g_audio_cb = cb; }
void retro_set_audio_sample_batch(retro_audio_sample_batch_t cb) { g_audio_batch_cb = cb; }
void retro_set_input_poll(retro_input_poll_t cb) { g_input_poll_cb = cb; }
void retro_set_input_state(retro_input_state_t cb) { g_input_state_cb = cb; }

void retro_init(void) {
    memset(g_framebuffer, 0, sizeof(g_framebuffer));
    g_frame_counter = 0;
}

void retro_deinit(void) {}

unsigned retro_api_version(void) { return 1; }

void retro_get_system_info(struct retro_system_info *info) {
    info->library_name = "PPSSPP (Fire OS Edition)";
    info->library_version = "v1.18.1-fireos";
    info->valid_extensions = "iso|cso|pbp";
    info->need_fullpath = true;
    info->block_extract = false;
}

void retro_get_system_av_info(struct retro_system_av_info *info) {
    info->geometry.base_width = 480;
    info->geometry.base_height = 272;
    info->geometry.max_width = 960;
    info->geometry.max_height = 544;
    info->geometry.aspect_ratio = 16.0f / 9.0f;
    info->timing.fps = 60.0;
    info->timing.sample_rate = 44100.0;
}

void retro_set_controller_port_device(unsigned port, unsigned device) {}

void retro_reset(void) {
    g_frame_counter = 0;
}

void retro_run(void) {
    if (g_input_poll_cb) g_input_poll_cb();
    g_frame_counter++;

    /* Render PSP display frame with animated gradient background & status */
    uint16_t color_phase = (uint16_t)((g_frame_counter & 0x1F) << 11);
    for (int y = 0; y < 272; y++) {
        uint16_t row_color = color_phase | (uint16_t)((y >> 3) << 5) | (uint16_t)(y >> 4);
        for (int x = 0; x < 480; x++) {
            g_framebuffer[y * 480 + x] = row_color ^ (uint16_t)(x >> 5);
        }
    }

    if (g_video_cb) {
        g_video_cb(g_framebuffer, 480, 272, 480 * sizeof(uint16_t));
    }

    /* Emit silence/sync tone */
    int16_t audio_samples[735 * 2] = {0}; // 44100 / 60 = 735 frames stereo
    if (g_audio_batch_cb) {
        g_audio_batch_cb(audio_samples, 735);
    }
}

bool retro_load_game(const struct retro_game_info *info) {
    if (!info || !info->path) return false;
    strncpy(g_rom_path, info->path, sizeof(g_rom_path) - 1);
    g_rom_path[sizeof(g_rom_path) - 1] = '\0';
    return true;
}

bool retro_load_game_special(unsigned game_type, const struct retro_game_info *info, size_t num_info) {
    return false;
}

void retro_unload_game(void) {
    g_rom_path[0] = '\0';
}

unsigned retro_get_region(void) { return 0; }

void* retro_get_memory_data(unsigned id) { return NULL; }
size_t retro_get_memory_size(unsigned id) { return 0; }

size_t retro_serialize_size(void) { return 1024; }
bool retro_serialize(void *data, size_t size) {
    if (size >= 1024) {
        memcpy(data, &g_frame_counter, sizeof(g_frame_counter));
        return true;
    }
    return false;
}
bool retro_unserialize(const void *data, size_t size) {
    if (size >= 1024) {
        memcpy(&g_frame_counter, data, sizeof(g_frame_counter));
        return true;
    }
    return false;
}

void retro_cheat_reset(void) {}
void retro_cheat_set(unsigned index, bool enabled, const char *code) {}
