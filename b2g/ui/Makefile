include ../../setup.mk
include $(BUILD_DIRECTORY)app.prologue.mk
include $(BUILD_DIRECTORY)app.properties.mk

APPLICATION_NAME = B2G_UI
PACKAGE_PATH = org.nbp.b2g.ui

ACTION_SUMMARIES = res/values/actions.xml
LOCAL_FILES += $(ACTION_SUMMARIES)
action-summaries: $(ACTION_SUMMARIES)
$(ACTION_SUMMARIES):
	./mkactions >$@

NATIVE_SOURCES += utils.h
NATIVE_SOURCES += utils.c
NATIVE_SOURCES += fwver.c
NATIVE_SOURCES += uinput.c
NATIVE_SOURCES += keyboard.c
NATIVE_SOURCES += kbdmon.c
NATIVE_SOURCES += metec_flat20_ioctl.h
NATIVE_SOURCES += metec.c

REQUIRED_TARGETS += documentation
DOCUMENTATION_DIRECTORY = ../doc
documentation:
	$(MAKE) -C $(DOCUMENTATION_DIRECTORY) all

clean::
	$(MAKE) -C $(DOCUMENTATION_DIRECTORY) $@

include $(BUILD_DIRECTORY)app.epilogue.mk
