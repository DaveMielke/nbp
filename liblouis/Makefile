include ../setup.mk
include $(BUILD_DIRECTORY)app.prologue.mk

APPLICATION_NAME = louis
PACKAGE_PATH = org.liblouis

NATIVE_SOURCES += liblouis-location/liblouis/config.h
NATIVE_SOURCES += liblouis-location/liblouis/liblouis.h
NATIVE_SOURCES += liblouis-location/liblouis/internal.h
NATIVE_SOURCES += liblouis-location/liblouis/compileTranslationTable.c
NATIVE_SOURCES += liblouis-location/liblouis/commonTranslationFunctions.c
NATIVE_SOURCES += liblouis-location/liblouis/lou_translateString.c
NATIVE_SOURCES += liblouis-location/liblouis/lou_backTranslateString.c
NATIVE_SOURCES += liblouis-location/liblouis/pattern.c
NATIVE_SOURCES += liblouis-location/liblouis/metadata.c
NATIVE_SOURCES += liblouis-location/liblouis/logging.c

NATIVE_SOURCES += lljni.h
NATIVE_SOURCES += log.c
NATIVE_SOURCES += translator.c
NATIVE_SOURCES += table.c
NATIVE_SOURCES += metadata.c
NATIVE_SOURCES += emphasis.c
NATIVE_SOURCES += louis.c

include $(BUILD_DIRECTORY)app.epilogue.mk
