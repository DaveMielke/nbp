LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := InputService
LOCAL_SRC_FILES := uinput.c

include $(BUILD_SHARED_LIBRARY)

