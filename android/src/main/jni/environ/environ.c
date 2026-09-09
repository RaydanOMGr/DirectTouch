#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "environ.h"

struct directtouch_environ_s *directtouch_environ;
__attribute__((constructor)) void env_init() {
    if(directtouch_environ) return;
    char* strptr_env = getenv("DIRECTTOUCH_ENVIRON");
    if(strptr_env == NULL) {
        printf("[DirectTouch/Environ] No environ found, creating...\n");
        directtouch_environ = malloc(sizeof(struct directtouch_environ_s));
        assert(directtouch_environ);
        memset(directtouch_environ, 0 , sizeof(struct directtouch_environ_s));
        if(asprintf(&strptr_env, "%p", directtouch_environ) == -1) abort();
        setenv("DIRECTTOUCH_ENVIRON", strptr_env, 1);
        free(strptr_env);
    }else{
        printf("[DirectTouch/Environ] Found existing environ: %s\n", strptr_env);
        directtouch_environ = (void*) strtoul(strptr_env, NULL, 0x10);
    }
}