//
// Created by maks on 24.09.2022.
//

#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "environ.h"

struct pojav_environ_s *pojav_environ;
__attribute__((constructor)) void pojenv_init() {
    char* strptr_env = getenv("POJAV_ENVIRON");
    if(strptr_env == NULL) {
        printf("[DirectTouch/Environ] No pojav environ found\n");
        return;
    }else{
        printf("[DirectTouch/Environ] Found existing pojav environ: %s\n", strptr_env);
        pojav_environ = (void*) strtoul(strptr_env, NULL, 0x10);
    }
}