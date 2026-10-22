/* GPL-3.0-or-later. Native initialization probe; this is not a gameplay test. */
#include <dlfcn.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include "libretro.h"
static bool environment(unsigned cmd, void *data) {
    static const char *directory="/data/local/tmp";
    if(cmd==RETRO_ENVIRONMENT_GET_SYSTEM_DIRECTORY || cmd==RETRO_ENVIRONMENT_GET_SAVE_DIRECTORY || cmd==RETRO_ENVIRONMENT_GET_CORE_ASSETS_DIRECTORY) {*(const char**)data=directory;return true;}
    if(cmd==RETRO_ENVIRONMENT_SET_PIXEL_FORMAT) return true;
    if(cmd==RETRO_ENVIRONMENT_GET_CAN_DUPE) {*(bool*)data=true;return true;}
    if(cmd==RETRO_ENVIRONMENT_GET_CORE_OPTIONS_VERSION) {*(unsigned*)data=0;return true;}
    return false;
}
static void video(const void *data,unsigned width,unsigned height,size_t pitch) {}
static void sample(int16_t l,int16_t r) {}
static size_t batch(const int16_t *data,size_t frames) {return frames;}
static void poll(void) {}
static int16_t input(unsigned port,unsigned device,unsigned index,unsigned id) {return 0;}
int main(int argc,char **argv) {
    if(argc!=2)return 2;
    void *handle=dlopen(argv[1],RTLD_NOW|RTLD_LOCAL);
    if(!handle){fprintf(stderr,"dlopen: %s\n",dlerror());return 3;}
#define FN(type,name) type name=dlsym(handle,#name);if(!name){fprintf(stderr,"Missing %s\n",#name);return 4;}
    typedef unsigned (*api_t)(void);typedef void (*void_t)(void);typedef void (*env_t)(retro_environment_t);typedef void (*video_t)(retro_video_refresh_t);typedef void (*sample_t)(retro_audio_sample_t);typedef void (*batch_t)(retro_audio_sample_batch_t);typedef void (*poll_t)(retro_input_poll_t);typedef void (*input_t)(retro_input_state_t);typedef void (*info_t)(struct retro_system_info*);
    FN(api_t,retro_api_version);FN(env_t,retro_set_environment);FN(video_t,retro_set_video_refresh);FN(sample_t,retro_set_audio_sample);FN(batch_t,retro_set_audio_sample_batch);FN(poll_t,retro_set_input_poll);FN(input_t,retro_set_input_state);FN(void_t,retro_init);FN(void_t,retro_deinit);FN(info_t,retro_get_system_info);
    if(retro_api_version()!=1)return 5;
    retro_set_environment(environment);retro_set_video_refresh(video);retro_set_audio_sample(sample);retro_set_audio_sample_batch(batch);retro_set_input_poll(poll);retro_set_input_state(input);
    retro_init();struct retro_system_info info={0};retro_get_system_info(&info);
    if(!info.library_name || !info.library_version)return 6;
    printf("PASS native init: %s / %s\n",info.library_name,info.library_version);
    retro_deinit();dlclose(handle);return 0;
}
