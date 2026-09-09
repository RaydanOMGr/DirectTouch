#ifndef DIRECTTOUCH_ENVIRON_H
#define DIRECTTOUCH_ENVIRON_H

#include <jni.h>

struct directtouch_environ_s {
    JavaVM *minecraft_jvm;
    JavaVM *launcher_dvm;

    char *dex_data;
    size_t dex_size;

    jobject object_classLoader_dvm;
    jmethodID method_loadClass_dvm;

    jclass class_mainClass_dvm;
    jmethodID method_setupTouchController_dvm;
    jmethodID method_receiveMessage_dvm;
    jmethodID method_close_dvm;

    jclass class_androidNative_jvm;
    jmethodID method_receiveMessage_jvm;
};
extern struct directtouch_environ_s *directtouch_environ;

#endif //DIRECTTOUCH_ENVIRON_H
