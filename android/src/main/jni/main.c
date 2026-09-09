//
// Created by Andreas on 22.10.2025.
//

#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <dlfcn.h>
#include <string.h>
#include <android/log.h>
#include "pojavenviron/environ.h"
#include "environ/environ.h"
#include "dlfake/fake_dlfcn.h"

#define INIT_SUCCESS 0x00
#define INIT_GENERIC_ERROR 0xFFFFFFFF
#define INIT_DEX_NOT_INITIALIZED 0xFFFFFFFE
#define INIT_DVM_NOT_FOUND 0xFFFFFFFD
#define INIT_METHOD_NOT_INITIALIZED 0xFFFFFFFC

#define DIE(msg) do { printf(msg); exit(EXIT_FAILURE); } while(0)
#define DIE_JVM(env, msg) do { \
    jclass runtime_exception = (*env)->FindClass(env, "java/lang/RuntimeException"); \
    (*env)->ThrowNew(env, runtime_exception, msg);                               \
} while(0)
#define CHECK_EXCEPTION(env) do { \
    if((*env)->ExceptionCheck(env)) { \
        (*env)->ExceptionDescribe(env); \
        (*env)->ExceptionClear(env); \
    } } while(0)

typedef  jint(*jni_getvms_t)(JavaVM**, jsize, jsize*);

// get vm functions stolen from https://github.com/artdeell/AutoWax4C
jni_getvms_t getVMFunction_fake() {
    void* library = fake_dlopen("libart.so", 0);
    if(library == NULL) {
        printf("[PojIntegr/VMFinder] Google trolling failed. Giving up.\n");
        return NULL;
    }
    void* sym = fake_dlsym(library, "JNI_GetCreatedJavaVMs");
    fake_dlclose(library);
    return (jni_getvms_t)sym;
}
jni_getvms_t getVMFunction() {
    void* library = dlopen("libnativehelper.so", RTLD_LAZY);
    if(library == NULL) {
        printf("[PojIntegr/VMFinder] Time to troll Google!\n");
        return getVMFunction_fake();
    }
    void* sym = dlsym(library, "JNI_GetCreatedJavaVMs");
    dlclose(library);
    return sym == NULL ? getVMFunction_fake() : (jni_getvms_t) sym;
}

jclass LoadClass(JNIEnv* env, const char* name) {
    jclass clazz = (jclass) (*env)->CallObjectMethod(env, directtouch_environ->object_classLoader_dvm, directtouch_environ->method_loadClass_dvm,
                                                     (*env)->NewStringUTF(env, name));
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        printf("[PojIntegr/LoadClass] Failed to load %s\n", name);
        return NULL;
    }
    return clazz;
}

jobject getAppClassLoader(JNIEnv* env) {
    jclass activityThreadClass = (*env)->FindClass(env, "android/app/ActivityThread");
    if (!activityThreadClass) return NULL;

    jmethodID currentActivityThreadMethod = (*env)->GetStaticMethodID(env, activityThreadClass,
                                                                      "currentActivityThread",
                                                                      "()Landroid/app/ActivityThread;");
    if (!currentActivityThreadMethod) return NULL;

    jobject activityThread = (*env)->CallStaticObjectMethod(env, activityThreadClass,
                                                            currentActivityThreadMethod);
    if (!activityThread) return NULL;

    jmethodID getApplicationMethod = (*env)->GetMethodID(env, activityThreadClass, "getApplication", "()Landroid/app/Application;");
    if(!getApplicationMethod) return NULL;

    jobject application = (*env)->CallObjectMethod(env, activityThread, getApplicationMethod);
    if(!application) return NULL;

    jclass contextClass = (*env)->FindClass(env, "android/content/Context");
    if(!contextClass) return NULL;

    jmethodID getClassLoaderMethod = (*env)->GetMethodID(env, contextClass, "getClassLoader", "()Ljava/lang/ClassLoader;");
    if(!getClassLoaderMethod) return NULL;

    return (*env)->CallObjectMethod(env, application, getClassLoaderMethod);
}

JNIEXPORT jint JNICALL
Java_me_andreasmelone_directtouch_pojav_DirectTouchAndroidNative_init(JNIEnv *env, jclass clazz, jstring own_path) {
    (*env)->GetJavaVM(env, &directtouch_environ->minecraft_jvm);
    if(!directtouch_environ->dex_data || directtouch_environ->dex_size == 0) {
        return INIT_DEX_NOT_INITIALIZED;
    }

    JNIEnv *dalvik_env;

    jsize cnt;
    jni_getvms_t JNI_GetCreatedJavaVMs_p = getVMFunction();
    JavaVM *dalvik_vm;
    if(!JNI_GetCreatedJavaVMs_p || JNI_GetCreatedJavaVMs_p(&directtouch_environ->launcher_dvm, 1, &cnt) != JNI_OK || cnt == 0) {
        printf("[DirectTouch/Init] Failed to find a JVM\n");
        if(pojav_environ == NULL || pojav_environ->dalvikJavaVMPtr == NULL) return INIT_DVM_NOT_FOUND;
        directtouch_environ->launcher_dvm = pojav_environ->dalvikJavaVMPtr;
        printf("[DirectTouch/Init] If the game crashes after this message, the pojav_environ is incompatible with the mod. "
               "This means that you should remove the DirectTouch mod, switch to a different launcher or try using a different device/Android version.\n");
    }
    dalvik_vm = directtouch_environ->launcher_dvm;

    jint result = (*dalvik_vm)->GetEnv(dalvik_vm, (void**)&dalvik_env, JNI_VERSION_1_6);
    if (result == JNI_EDETACHED) {
        result = (*dalvik_vm)->AttachCurrentThread(dalvik_vm, &dalvik_env, NULL);
    }
    if (result != JNI_OK) {
        printf("[DirectTouch/Init] Can't get a JNIEnv\n");
        return INIT_DVM_NOT_FOUND;
    }

    jclass class_InMemoryClassLoader = (*dalvik_env)->FindClass(dalvik_env, "dalvik/system/InMemoryDexClassLoader");
    if(!class_InMemoryClassLoader || (*dalvik_env)->ExceptionCheck(dalvik_env)) {
        (*dalvik_env)->ExceptionDescribe(dalvik_env);
        printf("[DirectTouch/Init] Can't find the class loader\n");
        return INIT_GENERIC_ERROR;
    }

    jmethodID constructor_InMemoryClassLoader = (*dalvik_env)->GetMethodID(dalvik_env, class_InMemoryClassLoader, "<init>", "(Ljava/nio/ByteBuffer;Ljava/lang/ClassLoader;)V");
    directtouch_environ->method_loadClass_dvm = (*dalvik_env)->GetMethodID(dalvik_env, class_InMemoryClassLoader, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    if(!constructor_InMemoryClassLoader || !directtouch_environ->method_loadClass_dvm){
        printf("[DirectTouch/Init] Can't find the class methods\n");
        return INIT_GENERIC_ERROR;
    }
    jobject byteBuffer = (*dalvik_env)->NewDirectByteBuffer(dalvik_env, directtouch_environ->dex_data, directtouch_environ->dex_size);
    if(!byteBuffer) {
        printf("[DirectTouch/Init] Can't create the byte buffer\n");
        return INIT_GENERIC_ERROR;
    }
    jobject appClassLoader = getAppClassLoader(dalvik_env);
    if(appClassLoader == NULL) {
        printf("[DirectTouch/Init] Failed to retrieve app classloader!\n");
        return INIT_GENERIC_ERROR;
    }
    directtouch_environ->object_classLoader_dvm = (*dalvik_env)->NewObject(dalvik_env, class_InMemoryClassLoader, constructor_InMemoryClassLoader, byteBuffer, appClassLoader);
    if((*dalvik_env)->ExceptionCheck(dalvik_env)) {
        (*dalvik_env)->ExceptionDescribe(dalvik_env);
        printf("[DirectTouch/Init] Failed to create class loader\n");
        return INIT_GENERIC_ERROR;
    }
    directtouch_environ->object_classLoader_dvm = (*dalvik_env)->NewGlobalRef(dalvik_env, directtouch_environ->object_classLoader_dvm);
    jclass mainClass = LoadClass(dalvik_env, "me.andreasmelone.directtouch.pojav.DirectTouchAndroid");
    if(!mainClass) {
        printf("[DirectTouch/Init] Failed to load main class\n");
        return INIT_GENERIC_ERROR;
    }
    printf("[DirectTouch/Init] ClassLoader class = %p\n", mainClass);
    directtouch_environ->class_mainClass_dvm = (jclass) (*dalvik_env)->NewGlobalRef(dalvik_env, mainClass);
    directtouch_environ->method_setupTouchController_dvm = (*dalvik_env)->GetStaticMethodID(dalvik_env, directtouch_environ->class_mainClass_dvm, "setupTouchController", "(Ljava/lang/String;)V");
    if(!directtouch_environ->method_setupTouchController_dvm) {
        printf("[DirectTouch/Init] Failed to get setupTouchController(Ljava/lang/String;)V dvm method\n");
        return INIT_METHOD_NOT_INITIALIZED;
    }

    jsize path_length = (*env)->GetStringLength(env, own_path);
    const jchar *path_chars = (*env)->GetStringChars(env, own_path, NULL);

    jstring path_copy_dvm = (*dalvik_env)->NewString(dalvik_env, path_chars, path_length);

    (*env)->ReleaseStringChars(env, own_path, path_chars);

    (*dalvik_env)->CallStaticVoidMethod(dalvik_env, directtouch_environ->class_mainClass_dvm, directtouch_environ->method_setupTouchController_dvm, path_copy_dvm);
    CHECK_EXCEPTION(dalvik_env);

    directtouch_environ->method_receiveMessage_dvm = (*dalvik_env)->GetStaticMethodID(dalvik_env, directtouch_environ->class_mainClass_dvm, "receiveMessage", "([B)V");
    if(!directtouch_environ->method_receiveMessage_dvm) {
        printf("[DirectTouch/Init] Failed to get receiveMessage([B)V dvm method\n");
        return INIT_GENERIC_ERROR;
    }

    directtouch_environ->method_close_dvm = (*dalvik_env)->GetStaticMethodID(dalvik_env, directtouch_environ->class_mainClass_dvm, "close", "()V");
    if(!directtouch_environ->method_close_dvm) {
        printf("[DirectTouch/Init] Failed to get close()V dvm method\n");
        return INIT_GENERIC_ERROR;
    }

    directtouch_environ->class_androidNative_jvm = (*env)->NewGlobalRef(env, clazz);
    directtouch_environ->method_receiveMessage_jvm = (*env)->GetStaticMethodID(env, directtouch_environ->class_androidNative_jvm, "receiveMessage", "([B)V");
    if(!directtouch_environ->method_receiveMessage_jvm) {
        printf("[DirectTouch/Init] Failed to get receiveMessage([B)V jvm method\n");
        return INIT_GENERIC_ERROR;
    }

    return INIT_SUCCESS;
}

JNIEXPORT void JNICALL
Java_me_andreasmelone_directtouch_pojav_DirectTouchAndroidNative_setDexData(JNIEnv *env, jclass clazz, jbyteArray data) {
    if(!data) {
        jclass exceptionClass = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
        (*env)->ThrowNew(env, exceptionClass, "Null data passed!");
        return;
    }

    if(directtouch_environ->dex_data) {
        free(directtouch_environ->dex_data);
        directtouch_environ->dex_data = NULL;
    }

    jboolean isCopy;
    jbyte* b = (*env)->GetByteArrayElements(env, data, &isCopy);
    if(!b) {
        DIE_JVM(env, "[DirectTouch/SetDexData] Failed to get byte array, exiting\n");

    }
    jint size = (*env)->GetArrayLength(env, data);

    directtouch_environ->dex_size = size;
    printf("[DirectTouch/SetDexData] New byte array size: %zu\n", directtouch_environ->dex_size);
    directtouch_environ->dex_data = malloc(size);
    if(!directtouch_environ->dex_data) {
        DIE_JVM(env, "[DirectTouch/SetDexData] Failed to allocate memory, exiting\n");
    }
    memcpy(directtouch_environ->dex_data, b, size);

    (*env)->ReleaseByteArrayElements(env, data, b, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_me_andreasmelone_directtouch_pojav_DirectTouchAndroidNative_sendMessage(JNIEnv *env, jclass clazz, jint length, jobject message_bytes) {
    if(!directtouch_environ->class_mainClass_dvm || !directtouch_environ->method_receiveMessage_dvm) {
        DIE("[DirectTouch/SetKeyboardState] Main class or receive message not initialized!\n");
    }

    JNIEnv *dvm;
    jint result = (*directtouch_environ->launcher_dvm)->GetEnv(directtouch_environ->launcher_dvm, (void**)&dvm, JNI_VERSION_1_6);
    if (result == JNI_EDETACHED) {
        result = (*directtouch_environ->launcher_dvm)->AttachCurrentThread(directtouch_environ->launcher_dvm, &dvm, NULL);
    }
    if (result != JNI_OK) {
        DIE_JVM(env, "[DirectTouch/SendMessage] Can't get a JNIEnv\n");
        return;
    }

    jbyte *bytes = (*env)->GetDirectBufferAddress(env, message_bytes);
    if(!bytes) {
        DIE_JVM(env, "[DirectTouch/SendMessage] Can't get bytes from DirectByteBuffer");
        return;
    }

    jbyteArray copied_bytes = (*dvm)->NewByteArray(dvm, length);
    if(!copied_bytes) {
        DIE_JVM(env, "[DirectTouch/SendMessage] Can't create a new byte array");
        return;
    }
    (*dvm)->SetByteArrayRegion(dvm, copied_bytes, 0, length, bytes);

    (*dvm)->CallStaticVoidMethod(dvm, directtouch_environ->class_mainClass_dvm, directtouch_environ->method_receiveMessage_dvm, copied_bytes);
    CHECK_EXCEPTION(dvm);
}

JNIEXPORT void JNICALL
Java_me_andreasmelone_directtouch_pojav_PojavDirectTouchNative_sendMessage(JNIEnv *env,
                                                                           jclass clazz, jint size,
                                                                           jint offset, jobject buffer) {
    if(!directtouch_environ->class_androidNative_jvm || !directtouch_environ->method_receiveMessage_jvm) {
        DIE_JVM(env, "[DirectTouch/SetKeyboardState] AndroidNative class or receive message not initialized!\n");
    }

    JNIEnv *jvm;
    jint result = (*directtouch_environ->minecraft_jvm)->GetEnv(directtouch_environ->minecraft_jvm, (void**)&jvm, JNI_VERSION_1_6);
    if (result == JNI_EDETACHED) {
        result = (*directtouch_environ->minecraft_jvm)->AttachCurrentThread(directtouch_environ->minecraft_jvm, &jvm, NULL);
    }
    if (result != JNI_OK) {
        DIE_JVM(env, "[DirectTouch/SendMessage] Can't get a JNIEnv\n");
        return;
    }

    jbyte *bytes = (*env)->GetDirectBufferAddress(env, buffer);
    if(!bytes) {
        DIE_JVM(env, "[DirectTouch/SendMessage] Can't get bytes from DirectByteBuffer");
        return;
    }

    jbyteArray copied_bytes = (*jvm)->NewByteArray(jvm, size);
    if(!copied_bytes) {
        DIE_JVM(env, "[DirectTouch/SendMessage] Can't create a new byte array");
        return;
    }
    (*jvm)->SetByteArrayRegion(jvm, copied_bytes, 0, size, bytes + offset);

    (*jvm)->CallStaticVoidMethod(jvm, directtouch_environ->class_androidNative_jvm, directtouch_environ->method_receiveMessage_jvm, copied_bytes);
    CHECK_EXCEPTION(jvm);
}

JNIEXPORT void JNICALL
Java_me_andreasmelone_directtouch_pojav_DirectTouchAndroidNative_close(JNIEnv *env, jclass clazz) {
    if(!directtouch_environ->class_mainClass_dvm || !directtouch_environ->method_close_dvm) {
        DIE_JVM(env, "[DirectTouch/Close] AndroidNative class or close not initialized!\n");
    }

    JNIEnv *dvm;
    jint result = (*directtouch_environ->launcher_dvm)->GetEnv(directtouch_environ->launcher_dvm, (void**)&dvm, JNI_VERSION_1_6);
    if (result == JNI_EDETACHED) {
        result = (*directtouch_environ->launcher_dvm)->AttachCurrentThread(directtouch_environ->launcher_dvm, &dvm, NULL);
    }
    if (result != JNI_OK) {
        DIE_JVM(env, "[DirectTouch/Close] Can't get a JNIEnv\n");
        return;
    }

    (*dvm)->CallStaticVoidMethod(dvm, directtouch_environ->class_mainClass_dvm, directtouch_environ->method_close_dvm);
    CHECK_EXCEPTION(dvm);
}