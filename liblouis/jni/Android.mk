LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := louis

LOCAL_C_INCLUDES := $(LOCAL_PATH) $(LOCAL_PATH)/liblouis-location/liblouis
LOCAL_CFLAGS := -std=c99 -Wall -DTABLESDIR=\"liblouis-location/tables\"
LOCAL_LDLIBS := -llog

LOCAL_SRC_FILES := \
   liblouis-location/liblouis/compileTranslationTable.c \
   liblouis-location/liblouis/commonTranslationFunctions.c \
   liblouis-location/liblouis/lou_translateString.c \
   liblouis-location/liblouis/lou_backTranslateString.c \
   liblouis-location/liblouis/pattern.c \
   liblouis-location/liblouis/metadata.c \
   liblouis-location/liblouis/logging.c \
   liblouis-location/liblouis/utils.c \
   log.c \
   translator.c \
   table.c \
   metadata.c \
   emphasis.c \
   louis.c

include $(BUILD_SHARED_LIBRARY)
