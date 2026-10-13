#ifndef _BIONIC_STDLIB_H
#define _BIONIC_STDLIB_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

void *malloc(size_t size);
void *calloc(size_t nmemb, size_t size);
void *realloc(void *ptr, size_t size);
void free(void *ptr);
int atoi(const char *nptr);
long strtol(const char *nptr, char **endptr, int base);
void exit(int status);

#ifdef __cplusplus
}
#endif

#endif
