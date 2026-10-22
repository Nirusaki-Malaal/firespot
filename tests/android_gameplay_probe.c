/* GPL-3.0-or-later. Executes the shipped Android core on diagnostic content.
 * A passing integration probe is not a commercial-game or Fire TV performance claim. */
#include <dlfcn.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <unistd.h>
#include "libretro.h"
static unsigned pixel=0,held=0,width=0,height=0;static uint64_t hash=0,frames=0,samples=0,nonzero=0;
static const char *dir="/data/local/tmp/firespot-probes";
static void log_core(enum retro_log_level level,const char *fmt,...) {if(level<RETRO_LOG_WARN)return;va_list args;va_start(args,fmt);vfprintf(stderr,fmt,args);va_end(args);}
static bool env(unsigned cmd,void *data) {
 if(cmd==RETRO_ENVIRONMENT_SET_PIXEL_FORMAT){pixel=*(unsigned*)data;return pixel<=2;}
 if(cmd==RETRO_ENVIRONMENT_GET_SYSTEM_DIRECTORY || cmd==RETRO_ENVIRONMENT_GET_SAVE_DIRECTORY || cmd==RETRO_ENVIRONMENT_GET_CORE_ASSETS_DIRECTORY){*(const char**)data=dir;return true;}
 if(cmd==RETRO_ENVIRONMENT_GET_CAN_DUPE){*(bool*)data=true;return true;}
 if(cmd==RETRO_ENVIRONMENT_GET_VARIABLE_UPDATE){*(bool*)data=false;return true;}
 if(cmd==RETRO_ENVIRONMENT_GET_LOG_INTERFACE){((struct retro_log_callback*)data)->log=log_core;return true;}
 if(cmd==RETRO_ENVIRONMENT_SET_VARIABLES || cmd==RETRO_ENVIRONMENT_SET_PERFORMANCE_LEVEL)return true;
 return false;
}
static void video(const void *data,unsigned w,unsigned h,size_t pitch) {
 if(!data || w>2048 || h>2048)return;
 width=w;height=h;frames++;hash=1469598103934665603ULL;
 unsigned bpp=pixel==1?4:2;
 for(unsigned y=0;y<h;y++)for(unsigned x=0;x<w*bpp;x++){hash^=((const uint8_t*)data)[y*pitch+x];hash*=1099511628211ULL;}
}
static void sample(int16_t l,int16_t r){samples+=2;nonzero+=(l!=0)+(r!=0);}
static size_t batch(const int16_t *data,size_t n){samples+=n*2;for(size_t i=0;i<n*2;i++)nonzero+=data[i]!=0;return n;}
static void poll(void){}
static int16_t input(unsigned port,unsigned dev,unsigned index,unsigned id){return port==0 && dev==RETRO_DEVICE_JOYPAD && id<16 && (held&(1<<id))?1:0;}
#define LOAD(type,name) type name=(type)dlsym(lib,#name);if(!name){fprintf(stderr,"Missing %s\n",#name);return 3;}
int main(int argc,char **argv) {
 if(argc!=3)return 2;alarm(90);
 void *lib=dlopen(argv[1],RTLD_NOW|RTLD_LOCAL);if(!lib){fprintf(stderr,"%s\n",dlerror());return 3;}
 typedef void(*void_fn)(void);typedef void(*env_fn)(retro_environment_t);typedef void(*video_fn)(retro_video_refresh_t);typedef void(*sample_fn)(retro_audio_sample_t);typedef void(*batch_fn)(retro_audio_sample_batch_t);typedef void(*poll_fn)(retro_input_poll_t);typedef void(*input_fn)(retro_input_state_t);typedef void(*info_fn)(struct retro_system_info*);typedef bool(*load_fn)(const struct retro_game_info*);typedef size_t(*size_fn)(void);typedef bool(*state_fn)(void*,size_t);typedef bool(*restore_fn)(const void*,size_t);typedef void(*port_fn)(unsigned,unsigned);
 LOAD(env_fn,retro_set_environment);LOAD(video_fn,retro_set_video_refresh);LOAD(sample_fn,retro_set_audio_sample);LOAD(batch_fn,retro_set_audio_sample_batch);LOAD(poll_fn,retro_set_input_poll);LOAD(input_fn,retro_set_input_state);LOAD(void_fn,retro_init);LOAD(void_fn,retro_deinit);LOAD(info_fn,retro_get_system_info);LOAD(load_fn,retro_load_game);LOAD(void_fn,retro_unload_game);LOAD(void_fn,retro_run);LOAD(void_fn,retro_reset);LOAD(size_fn,retro_serialize_size);LOAD(state_fn,retro_serialize);LOAD(restore_fn,retro_unserialize);LOAD(port_fn,retro_set_controller_port_device);
 retro_set_environment(env);retro_set_video_refresh(video);retro_set_audio_sample(sample);retro_set_audio_sample_batch(batch);retro_set_input_poll(poll);retro_set_input_state(input);retro_init();
 struct retro_system_info info={0};retro_get_system_info(&info);
 FILE *f=fopen(argv[2],"rb");if(!f)return 4;fseek(f,0,SEEK_END);long len=ftell(f);rewind(f);void *rom=malloc(len);if(!rom || fread(rom,1,len,f)!=(size_t)len)return 4;fclose(f);
 struct retro_game_info game={argv[2],info.need_fullpath?NULL:rom,(size_t)len,NULL};
 if(!retro_load_game(&game)){fprintf(stderr,"Content rejected\n");return 5;}
 retro_set_controller_port_device(0,RETRO_DEVICE_JOYPAD);
 for(int i=0;i<180;i++)retro_run();uint64_t before=hash;
 held=(1<<RETRO_DEVICE_ID_JOYPAD_RIGHT);for(int i=0;i<30;i++)retro_run();held=0;uint64_t pressed=hash;
 size_t size=retro_serialize_size();void *state=size && size<64*1024*1024?malloc(size):NULL;
 bool saved=state && retro_serialize(state,size);uint64_t expected=0;bool restored=false;
 if(saved){for(int i=0;i<5;i++)retro_run();expected=hash;if(retro_unserialize(state,size)){for(int i=0;i<5;i++)retro_run();restored=expected==hash;}}
 retro_reset();for(int i=0;i<30;i++)retro_run();
 printf("{\"core\":\"%s\",\"scope\":\"Android x86_64 diagnostic integration\",\"frames\":%llu,\"width\":%u,\"height\":%u,\"audio_samples\":%llu,\"nonzero_audio\":%llu,\"input_changes_video\":%s,\"snapshot_roundtrip\":%s,\"state_bytes\":%zu,\"reset_runs\":true}\n",info.library_name,(unsigned long long)frames,width,height,(unsigned long long)samples,(unsigned long long)nonzero,before!=pressed?"true":"false",restored?"true":"false",size);
 bool ok=frames>0 && width>0 && height>0 && saved && restored;
 retro_unload_game();retro_deinit();free(state);free(rom);dlclose(lib);return ok?0:6;
}
