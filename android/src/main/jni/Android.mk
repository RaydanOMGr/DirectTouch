LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := directtouch
LOCAL_SRC_FILES := \
    main.c \
    environ/environ.c \
    pojavenviron/environ.c \
    dlfake/fake_dlfcn.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_CFLAGS := -Wall -O2

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
