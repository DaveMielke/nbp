#include "utils.h"
MAKE_FILE_LOG_TAG;

#include <string.h>
#include <errno.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/ioctl.h>

#include <linux/input.h>

#define KEYBOARD_DEVICE_NAME "cp430_keypad"
#define NO_DEVICE -1

static char *
findEventDevice (const char *deviceName) {
  char *devicePath = NULL;
  char directoryPath[0X80];
  DIR *directory;

  snprintf(directoryPath, sizeof(directoryPath),
           "/sys/bus/platform/devices/%s/input", deviceName);

  if ((directory = opendir(directoryPath))) {
    struct dirent *entry;

    while ((entry = readdir(directory))) {
      unsigned int eventNumber;
      char extra;

      if (sscanf(entry->d_name, "input%u%c", &eventNumber, &extra) == 1) {
        char path[0X80];

        snprintf(path, sizeof(path), "/dev/input/event%u", eventNumber);
        if (!(devicePath = strdup(path))) logMallocError(LOG_TAG);
        break;
      }
    }

    closedir(directory);
  } else {
    LOG(ERROR, "event device input directory open error: %s: %s",
        directoryPath, strerror(errno));
  }

  return devicePath;
}

static int
openEventDevice (const char *deviceName) {
  char *devicePath = findEventDevice(deviceName);

  if (devicePath) {
    int deviceDescriptor = open(devicePath, O_RDONLY);

    if (deviceDescriptor != NO_DEVICE) {
      if (ioctl(deviceDescriptor, EVIOCGRAB, 1) != -1) {
        LOG(INFO, "Event Device Opened: %s: %s: fd=%d",
            deviceName, devicePath, deviceDescriptor);

        free(devicePath);
        return deviceDescriptor;
      } else {
        logSystemError(LOG_TAG, "ioctl[EVIOCGRAB]");
      }

      close(deviceDescriptor);
    } else {
      LOG(ERROR, "event device open error: %s: %s",
          devicePath, strerror(errno));
    }

    free(devicePath);
  }

  return NO_DEVICE;
}

JAVA_METHOD(
  org_nbp_b2g_ui_KeyboardMonitor, openDevice, jint
) {
  return openEventDevice(KEYBOARD_DEVICE_NAME);
}

JAVA_METHOD(
  org_nbp_b2g_ui_EventMonitor, closeDevice, void,
  jint device
) {
  close(device);
}

JAVA_METHOD(
  org_nbp_b2g_ui_EventMonitor, monitorDevice, void,
  jint device
) {
  jclass class = (*env)->GetObjectClass(env, this);
  const char *methodName = "onKeyEvent";
  const char *methodSignature = "(IZ)V";
  jmethodID method = (*env)->GetMethodID(env, class, methodName, methodSignature);

  if (method) {
    while (awaitInput(device)) {
      struct input_event event;
      size_t size = sizeof(event);
      ssize_t result = read(device, &event, size);

      if (result == -1) {
        logSystemError(LOG_TAG, "read[event]");
        break;
      }

      if (result == 0) {
        LOG(ERROR, "event end-of-file");
        break;
      }

      if (result != size) {
        LOG(ERROR, "unexpected event read length: %zu != %zu",
            (size_t)result, size);
        break;
      }

      if (event.type == EV_KEY) {
        jboolean press;

        if (event.value == 0) {
          press = JNI_FALSE;
        } else if (event.value == 1) {
          press = JNI_TRUE;
        } else {
          continue;
        }

        (*env)->CallVoidMethod(env, this, method, event.code, press);
        if (checkException(env)) break;
      }
    }
  } else {
    LOG(ERROR, "method not found: %s %s", methodName, methodSignature);
  }

  checkException(env);
}
