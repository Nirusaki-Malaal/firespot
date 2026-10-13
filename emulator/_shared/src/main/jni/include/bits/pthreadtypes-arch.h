#ifndef _BITS_PTHREADTYPES_ARCH_H
#define _BITS_PTHREADTYPES_ARCH_H 1

#include <bits/wordsize.h>

#ifdef __arm__
# define __SIZEOF_PTHREAD_MUTEX_T 24
# define __SIZEOF_PTHREAD_ATTR_T 36
# define __SIZEOF_PTHREAD_RWLOCK_T 32
# define __SIZEOF_PTHREAD_BARRIER_T 20
# define __cleanup_fct_attribute
#elif defined(__aarch64__)
# define __SIZEOF_PTHREAD_MUTEX_T 40
# define __SIZEOF_PTHREAD_ATTR_T 64
# define __SIZEOF_PTHREAD_RWLOCK_T 56
# define __SIZEOF_PTHREAD_BARRIER_T 32
# define __cleanup_fct_attribute
#elif defined(__x86_64__)
# define __SIZEOF_PTHREAD_MUTEX_T 40
# define __SIZEOF_PTHREAD_ATTR_T 56
# define __SIZEOF_PTHREAD_RWLOCK_T 56
# define __SIZEOF_PTHREAD_BARRIER_T 32
# define __cleanup_fct_attribute
#else
# define __SIZEOF_PTHREAD_MUTEX_T 24
# define __SIZEOF_PTHREAD_ATTR_T 36
# define __SIZEOF_PTHREAD_RWLOCK_T 32
# define __SIZEOF_PTHREAD_BARRIER_T 20
# define __cleanup_fct_attribute
#endif

#define __SIZEOF_PTHREAD_MUTEXATTR_T 4
#define __SIZEOF_PTHREAD_COND_T 48
#define __SIZEOF_PTHREAD_CONDATTR_T 4
#define __SIZEOF_PTHREAD_RWLOCKATTR_T 8
#define __SIZEOF_PTHREAD_BARRIERATTR_T 4

#define __LOCK_ALIGNMENT
#define __ONCE_ALIGNMENT

#endif
