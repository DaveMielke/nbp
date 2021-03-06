Firmware Features
=================

Status Alerts
-------------

A high-pitched beep (|beep frequency|) is used to alert you to a number of
firmware-detected states.

.. table:: Beep Lengths

   ======  ====================
   Length  Duration
   ------  --------------------
   Short   |short beep length|
   Medium  |medium beep length|
   Long    |long beep length|
   ======  ====================

One Short Beep
  The system has started booting. This occurs immediately after the Power
  switch has been turned on while the system is shut down. The braille display
  is not powered up until later.

Two Short Beeps
  The kernel has finished initializing. This occurs part way through the
  system's boot sequence. The braille display is powered up at this point.
  It will say ``Starting`` for a few seconds, and then go blank. Eventually,
  when the User Interface starts, regular screen content will appear.

Three Short Beeps
  The battery is too low to boot the system. This check is made when the Power
  switch is turned on while the system is shut down.

Two Medium Beeps
  The battery is full. This occurs when the charger is connected, and indicates
  that further charging is unnecessary.

One Long Beep
  The device has been successfully reset (see `The Reset Button`_).

Four Long Beeps
  The system is about to reboot. This occurs after the
  `VolumeDown+Dot4+Dot5+Dot6+Dot8`_ firmware key binding has been pressed.

Key Bindings
------------

Simultaneously pressing VolumeDown, Dot1, Dot2, Dot3, and Dot7:
  If the system is running then go to the Power Off screen. This is
  equivalent to a medium press of the Power button on an Android device.

.. _VolumeDown+Dot4+Dot5+Dot6+Dot8:

Simultaneously pressing VolumeDown, Dot4, Dot5, Dot6, and Dot8:
  If the system is running then reset the main processor and then boot the
  system. This is equivalent to a long press of the Power button on an
  Android device. You should hear four long beeps.

Switching the power on:

  * If the system is shut down then boot it. This is equivalent to a long
    press of the Power button on an Android device.

  * If the system is running then wake it up. This is equivalent to a short
    press of the Power button on an Android device.

Switching the power off:
  If the system is running then put it to sleep. This is equivalent to a
  short press of the Power button on an Android device. The system itself
  will stay up, but most of the peripherals (the braille display, the keyboard,
  etc) will be shut down in order to save power.

Holding VolumeDown while switching the power on:
  If the system is shut down then boot into `Recovery Mode`_. This is equivalent
  to a simultaneous long press of the Power button and the Volume Up key on an
  Android device.

