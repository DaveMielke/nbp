/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyCharacterMap;
import android.view.KeyCharacterMap.KeyData;

/**
 * Object used to report key and button events.
 * <p>
 * Each key press is described by a sequence of key events.  A key press
 * starts with a key event with {@link #ACTION_DOWN}.  If the key is held
 * sufficiently long that it repeats, then the initial down is followed
 * additional key events with {@link #ACTION_DOWN} and a non-zero value for
 * {@link #getRepeatCount()}.  The last key event is a {@link #ACTION_UP}
 * for the key up.  If the key press is canceled, the key up event will have the
 * {@link #FLAG_CANCELED} flag set.
 * </p><p>
 * Key events are generally accompanied by a key code ({@link #getKeyCode()}),
 * scan code ({@link #getScanCode()}) and meta state ({@link #getMetaState()}).
 * Key code constants are defined in this class.  Scan code constants are raw
 * device-specific codes obtained from the OS and so are not generally meaningful
 * to applications unless interpreted using the {@link KeyCharacterMap}.
 * Meta states describe the pressed state of key modifiers
 * such as {@link #META_SHIFT_ON} or {@link #META_ALT_ON}.
 * </p><p>
 * Key codes typically correspond one-to-one with individual keys on an input device.
 * Many keys and key combinations serve quite different functions on different
 * input devices so care must be taken when interpreting them.  Always use the
 * {@link KeyCharacterMap} associated with the input device when mapping keys
 * to characters.  Be aware that there may be multiple key input devices active
 * at the same time and each will have its own key character map.
 * </p><p>
 * As soft input methods can use multiple and inventive ways of inputting text,
 * there is no guarantee that any key press on a soft keyboard will generate a key
 * event: this is left to the IME's discretion, and in fact sending such events is
 * discouraged.  You should never rely on receiving KeyEvents for any key on a soft
 * input method.  In particular, the default software keyboard will never send any
 * key event to any application targetting Jelly Bean or later, and will only send
 * events for some presses of the delete and return keys to applications targetting
 * Ice Cream Sandwich or earlier.  Be aware that other software input methods may
 * never send key events regardless of the version.  Consider using editor actions
 * like {@link android.view.inputmethod.EditorInfo#IME_ACTION_DONE} if you need
 * specific interaction with the software keyboard, as it gives more visibility to
 * the user as to how your application will react to key presses.
 * </p><p>
 * When interacting with an IME, the framework may deliver key events
 * with the special action {@link #ACTION_MULTIPLE} that either specifies
 * that single repeated key code or a sequence of characters to insert.
 * </p><p>
 * In general, the framework cannot guarantee that the key events it delivers
 * to a view always constitute complete key sequences since some events may be dropped
 * or modified by containing views before they are delivered.  The view implementation
 * should be prepared to handle {@link #FLAG_CANCELED} and should tolerate anomalous
 * situations such as receiving a new {@link #ACTION_DOWN} without first having
 * received an {@link #ACTION_UP} for the prior key press.
 * </p><p>
 * Refer to {@link InputDevice} for more information about how different kinds of
 * input devices and sources represent keys and buttons.
 * </p>
 */
public class KeyEvent extends InputEvent implements Parcelable {
    /** Key code constant: Unknown key code. */
    public static final int KEYCODE_UNKNOWN         = 0;
    /** Key code constant: Soft Left key.
     * Usually situated below the display on phones and used as a multi-function
     * feature key for selecting a software defined function shown on the bottom left
     * of the display. */
    public static final int KEYCODE_SOFT_LEFT       = 1;
    /** Key code constant: Soft Right key.
     * Usually situated below the display on phones and used as a multi-function
     * feature key for selecting a software defined function shown on the bottom right
     * of the display. */
    public static final int KEYCODE_SOFT_RIGHT      = 2;
    /** Key code constant: Home key.
     * This key is handled by the framework and is never delivered to applications. */
    public static final int KEYCODE_HOME            = 3;
    /** Key code constant: Back key. */
    public static final int KEYCODE_BACK            = 4;
    /** Key code constant: Call key. */
    public static final int KEYCODE_CALL            = 5;
    /** Key code constant: End Call key. */
    public static final int KEYCODE_ENDCALL         = 6;
    /** Key code constant: '0' key. */
    public static final int KEYCODE_0               = 7;
    /** Key code constant: '1' key. */
    public static final int KEYCODE_1               = 8;
    /** Key code constant: '2' key. */
    public static final int KEYCODE_2               = 9;
    /** Key code constant: '3' key. */
    public static final int KEYCODE_3               = 10;
    /** Key code constant: '4' key. */
    public static final int KEYCODE_4               = 11;
    /** Key code constant: '5' key. */
    public static final int KEYCODE_5               = 12;
    /** Key code constant: '6' key. */
    public static final int KEYCODE_6               = 13;
    /** Key code constant: '7' key. */
    public static final int KEYCODE_7               = 14;
    /** Key code constant: '8' key. */
    public static final int KEYCODE_8               = 15;
    /** Key code constant: '9' key. */
    public static final int KEYCODE_9               = 16;
    /** Key code constant: '*' key. */
    public static final int KEYCODE_STAR            = 17;
    /** Key code constant: '#' key. */
    public static final int KEYCODE_POUND           = 18;
    /** Key code constant: Directional Pad Up key.
     * May also be synthesized from trackball motions. */
    public static final int KEYCODE_DPAD_UP         = 19;
    /** Key code constant: Directional Pad Down key.
     * May also be synthesized from trackball motions. */
    public static final int KEYCODE_DPAD_DOWN       = 20;
    /** Key code constant: Directional Pad Left key.
     * May also be synthesized from trackball motions. */
    public static final int KEYCODE_DPAD_LEFT       = 21;
    /** Key code constant: Directional Pad Right key.
     * May also be synthesized from trackball motions. */
    public static final int KEYCODE_DPAD_RIGHT      = 22;
    /** Key code constant: Directional Pad Center key.
     * May also be synthesized from trackball motions. */
    public static final int KEYCODE_DPAD_CENTER     = 23;
    /** Key code constant: Volume Up key.
     * Adjusts the speaker volume up. */
    public static final int KEYCODE_VOLUME_UP       = 24;
    /** Key code constant: Volume Down key.
     * Adjusts the speaker volume down. */
    public static final int KEYCODE_VOLUME_DOWN     = 25;
    /** Key code constant: Power key. */
    public static final int KEYCODE_POWER           = 26;
    /** Key code constant: Camera key.
     * Used to launch a camera application or take pictures. */
    public static final int KEYCODE_CAMERA          = 27;
    /** Key code constant: Clear key. */
    public static final int KEYCODE_CLEAR           = 28;
    /** Key code constant: 'A' key. */
    public static final int KEYCODE_A               = 29;
    /** Key code constant: 'B' key. */
    public static final int KEYCODE_B               = 30;
    /** Key code constant: 'C' key. */
    public static final int KEYCODE_C               = 31;
    /** Key code constant: 'D' key. */
    public static final int KEYCODE_D               = 32;
    /** Key code constant: 'E' key. */
    public static final int KEYCODE_E               = 33;
    /** Key code constant: 'F' key. */
    public static final int KEYCODE_F               = 34;
    /** Key code constant: 'G' key. */
    public static final int KEYCODE_G               = 35;
    /** Key code constant: 'H' key. */
    public static final int KEYCODE_H               = 36;
    /** Key code constant: 'I' key. */
    public static final int KEYCODE_I               = 37;
    /** Key code constant: 'J' key. */
    public static final int KEYCODE_J               = 38;
    /** Key code constant: 'K' key. */
    public static final int KEYCODE_K               = 39;
    /** Key code constant: 'L' key. */
    public static final int KEYCODE_L               = 40;
    /** Key code constant: 'M' key. */
    public static final int KEYCODE_M               = 41;
    /** Key code constant: 'N' key. */
    public static final int KEYCODE_N               = 42;
    /** Key code constant: 'O' key. */
    public static final int KEYCODE_O               = 43;
    /** Key code constant: 'P' key. */
    public static final int KEYCODE_P               = 44;
    /** Key code constant: 'Q' key. */
    public static final int KEYCODE_Q               = 45;
    /** Key code constant: 'R' key. */
    public static final int KEYCODE_R               = 46;
    /** Key code constant: 'S' key. */
    public static final int KEYCODE_S               = 47;
    /** Key code constant: 'T' key. */
    public static final int KEYCODE_T               = 48;
    /** Key code constant: 'U' key. */
    public static final int KEYCODE_U               = 49;
    /** Key code constant: 'V' key. */
    public static final int KEYCODE_V               = 50;
    /** Key code constant: 'W' key. */
    public static final int KEYCODE_W               = 51;
    /** Key code constant: 'X' key. */
    public static final int KEYCODE_X               = 52;
    /** Key code constant: 'Y' key. */
    public static final int KEYCODE_Y               = 53;
    /** Key code constant: 'Z' key. */
    public static final int KEYCODE_Z               = 54;
    /** Key code constant: ',' key. */
    public static final int KEYCODE_COMMA           = 55;
    /** Key code constant: '.' key. */
    public static final int KEYCODE_PERIOD          = 56;
    /** Key code constant: Left Alt modifier key. */
    public static final int KEYCODE_ALT_LEFT        = 57;
    /** Key code constant: Right Alt modifier key. */
    public static final int KEYCODE_ALT_RIGHT       = 58;
    /** Key code constant: Left Shift modifier key. */
    public static final int KEYCODE_SHIFT_LEFT      = 59;
    /** Key code constant: Right Shift modifier key. */
    public static final int KEYCODE_SHIFT_RIGHT     = 60;
    /** Key code constant: Tab key. */
    public static final int KEYCODE_TAB             = 61;
    /** Key code constant: Space key. */
    public static final int KEYCODE_SPACE           = 62;
    /** Key code constant: Symbol modifier key.
     * Used to enter alternate symbols. */
    public static final int KEYCODE_SYM             = 63;
    /** Key code constant: Explorer special function key.
     * Used to launch a browser application. */
    public static final int KEYCODE_EXPLORER        = 64;
    /** Key code constant: Envelope special function key.
     * Used to launch a mail application. */
    public static final int KEYCODE_ENVELOPE        = 65;
    /** Key code constant: Enter key. */
    public static final int KEYCODE_ENTER           = 66;
    /** Key code constant: Backspace key.
     * Deletes characters before the insertion point, unlike {@link #KEYCODE_FORWARD_DEL}. */
    public static final int KEYCODE_DEL             = 67;
    /** Key code constant: '`' (backtick) key. */
    public static final int KEYCODE_GRAVE           = 68;
    /** Key code constant: '-'. */
    public static final int KEYCODE_MINUS           = 69;
    /** Key code constant: '=' key. */
    public static final int KEYCODE_EQUALS          = 70;
    /** Key code constant: '[' key. */
    public static final int KEYCODE_LEFT_BRACKET    = 71;
    /** Key code constant: ']' key. */
    public static final int KEYCODE_RIGHT_BRACKET   = 72;
    /** Key code constant: '\' key. */
    public static final int KEYCODE_BACKSLASH       = 73;
    /** Key code constant: ';' key. */
    public static final int KEYCODE_SEMICOLON       = 74;
    /** Key code constant: ''' (apostrophe) key. */
    public static final int KEYCODE_APOSTROPHE      = 75;
    /** Key code constant: '/' key. */
    public static final int KEYCODE_SLASH           = 76;
    /** Key code constant: '@' key. */
    public static final int KEYCODE_AT              = 77;
    /** Key code constant: Number modifier key.
     * Used to enter numeric symbols.
     * This key is not Num Lock; it is more like {@link #KEYCODE_ALT_LEFT} and is
     * interpreted as an ALT key by {@link android.text.method.MetaKeyKeyListener}. */
    public static final int KEYCODE_NUM             = 78;
    /** Key code constant: Headset Hook key.
     * Used to hang up calls and stop media. */
    public static final int KEYCODE_HEADSETHOOK     = 79;
    /** Key code constant: Camera Focus key.
     * Used to focus the camera. */
    public static final int KEYCODE_FOCUS           = 80;   // *Camera* focus
    /** Key code constant: '+' key. */
    public static final int KEYCODE_PLUS            = 81;
    /** Key code constant: Menu key. */
    public static final int KEYCODE_MENU            = 82;
    /** Key code constant: Notification key. */
    public static final int KEYCODE_NOTIFICATION    = 83;
    /** Key code constant: Search key. */
    public static final int KEYCODE_SEARCH          = 84;
    /** Key code constant: Play/Pause media key. */
    public static final int KEYCODE_MEDIA_PLAY_PAUSE= 85;
    /** Key code constant: Stop media key. */
    public static final int KEYCODE_MEDIA_STOP      = 86;
    /** Key code constant: Play Next media key. */
    public static final int KEYCODE_MEDIA_NEXT      = 87;
    /** Key code constant: Play Previous media key. */
    public static final int KEYCODE_MEDIA_PREVIOUS  = 88;
    /** Key code constant: Rewind media key. */
    public static final int KEYCODE_MEDIA_REWIND    = 89;
    /** Key code constant: Fast Forward media key. */
    public static final int KEYCODE_MEDIA_FAST_FORWARD = 90;
    /** Key code constant: Mute key.
     * Mutes the microphone, unlike {@link #KEYCODE_VOLUME_MUTE}. */
    public static final int KEYCODE_MUTE            = 91;
    /** Key code constant: Page Up key. */
    public static final int KEYCODE_PAGE_UP         = 92;
    /** Key code constant: Page Down key. */
    public static final int KEYCODE_PAGE_DOWN       = 93;
    /** Key code constant: Picture Symbols modifier key.
     * Used to switch symbol sets (Emoji, Kao-moji). */
    public static final int KEYCODE_PICTSYMBOLS     = 94;   // switch symbol-sets (Emoji,Kao-moji)
    /** Key code constant: Switch Charset modifier key.
     * Used to switch character sets (Kanji, Katakana). */
    public static final int KEYCODE_SWITCH_CHARSET  = 95;   // switch char-sets (Kanji,Katakana)
    /** Key code constant: A Button key.
     * On a game controller, the A button should be either the button labeled A
     * or the first button on the upper row of controller buttons. */
    public static final int KEYCODE_BUTTON_A        = 96;
    /** Key code constant: B Button key.
     * On a game controller, the B button should be either the button labeled B
     * or the second button on the upper row of controller buttons. */
    public static final int KEYCODE_BUTTON_B        = 97;
    /** Key code constant: C Button key.
     * On a game controller, the C button should be either the button labeled C
     * or the third button on the upper row of controller buttons. */
    public static final int KEYCODE_BUTTON_C        = 98;
    /** Key code constant: X Button key.
     * On a game controller, the X button should be either the button labeled X
     * or the first button on the lower row of controller buttons. */
    public static final int KEYCODE_BUTTON_X        = 99;
    /** Key code constant: Y Button key.
     * On a game controller, the Y button should be either the button labeled Y
     * or the second button on the lower row of controller buttons. */
    public static final int KEYCODE_BUTTON_Y        = 100;
    /** Key code constant: Z Button key.
     * On a game controller, the Z button should be either the button labeled Z
     * or the third button on the lower row of controller buttons. */
    public static final int KEYCODE_BUTTON_Z        = 101;
    /** Key code constant: L1 Button key.
     * On a game controller, the L1 button should be either the button labeled L1 (or L)
     * or the top left trigger button. */
    public static final int KEYCODE_BUTTON_L1       = 102;
    /** Key code constant: R1 Button key.
     * On a game controller, the R1 button should be either the button labeled R1 (or R)
     * or the top right trigger button. */
    public static final int KEYCODE_BUTTON_R1       = 103;
    /** Key code constant: L2 Button key.
     * On a game controller, the L2 button should be either the button labeled L2
     * or the bottom left trigger button. */
    public static final int KEYCODE_BUTTON_L2       = 104;
    /** Key code constant: R2 Button key.
     * On a game controller, the R2 button should be either the button labeled R2
     * or the bottom right trigger button. */
    public static final int KEYCODE_BUTTON_R2       = 105;
    /** Key code constant: Left Thumb Button key.
     * On a game controller, the left thumb button indicates that the left (or only)
     * joystick is pressed. */
    public static final int KEYCODE_BUTTON_THUMBL   = 106;
    /** Key code constant: Right Thumb Button key.
     * On a game controller, the right thumb button indicates that the right
     * joystick is pressed. */
    public static final int KEYCODE_BUTTON_THUMBR   = 107;
    /** Key code constant: Start Button key.
     * On a game controller, the button labeled Start. */
    public static final int KEYCODE_BUTTON_START    = 108;
    /** Key code constant: Select Button key.
     * On a game controller, the button labeled Select. */
    public static final int KEYCODE_BUTTON_SELECT   = 109;
    /** Key code constant: Mode Button key.
     * On a game controller, the button labeled Mode. */
    public static final int KEYCODE_BUTTON_MODE     = 110;
    /** Key code constant: Escape key. */
    public static final int KEYCODE_ESCAPE          = 111;
    /** Key code constant: Forward Delete key.
     * Deletes characters ahead of the insertion point, unlike {@link #KEYCODE_DEL}. */
    public static final int KEYCODE_FORWARD_DEL     = 112;
    /** Key code constant: Left Control modifier key. */
    public static final int KEYCODE_CTRL_LEFT       = 113;
    /** Key code constant: Right Control modifier key. */
    public static final int KEYCODE_CTRL_RIGHT      = 114;
    /** Key code constant: Caps Lock key. */
    public static final int KEYCODE_CAPS_LOCK       = 115;
    /** Key code constant: Scroll Lock key. */
    public static final int KEYCODE_SCROLL_LOCK     = 116;
    /** Key code constant: Left Meta modifier key. */
    public static final int KEYCODE_META_LEFT       = 117;
    /** Key code constant: Right Meta modifier key. */
    public static final int KEYCODE_META_RIGHT      = 118;
    /** Key code constant: Function modifier key. */
    public static final int KEYCODE_FUNCTION        = 119;
    /** Key code constant: System Request / Print Screen key. */
    public static final int KEYCODE_SYSRQ           = 120;
    /** Key code constant: Break / Pause key. */
    public static final int KEYCODE_BREAK           = 121;
    /** Key code constant: Home Movement key.
     * Used for scrolling or moving the cursor around to the start of a line
     * or to the top of a list. */
    public static final int KEYCODE_MOVE_HOME       = 122;
    /** Key code constant: End Movement key.
     * Used for scrolling or moving the cursor around to the end of a line
     * or to the bottom of a list. */
    public static final int KEYCODE_MOVE_END        = 123;
    /** Key code constant: Insert key.
     * Toggles insert / overwrite edit mode. */
    public static final int KEYCODE_INSERT          = 124;
    /** Key code constant: Forward key.
     * Navigates forward in the history stack.  Complement of {@link #KEYCODE_BACK}. */
    public static final int KEYCODE_FORWARD         = 125;
    /** Key code constant: Play media key. */
    public static final int KEYCODE_MEDIA_PLAY      = 126;
    /** Key code constant: Pause media key. */
    public static final int KEYCODE_MEDIA_PAUSE     = 127;
    /** Key code constant: Close media key.
     * May be used to close a CD tray, for example. */
    public static final int KEYCODE_MEDIA_CLOSE     = 128;
    /** Key code constant: Eject media key.
     * May be used to eject a CD tray, for example. */
    public static final int KEYCODE_MEDIA_EJECT     = 129;
    /** Key code constant: Record media key. */
    public static final int KEYCODE_MEDIA_RECORD    = 130;
    /** Key code constant: F1 key. */
    public static final int KEYCODE_F1              = 131;
    /** Key code constant: F2 key. */
    public static final int KEYCODE_F2              = 132;
    /** Key code constant: F3 key. */
    public static final int KEYCODE_F3              = 133;
    /** Key code constant: F4 key. */
    public static final int KEYCODE_F4              = 134;
    /** Key code constant: F5 key. */
    public static final int KEYCODE_F5              = 135;
    /** Key code constant: F6 key. */
    public static final int KEYCODE_F6              = 136;
    /** Key code constant: F7 key. */
    public static final int KEYCODE_F7              = 137;
    /** Key code constant: F8 key. */
    public static final int KEYCODE_F8              = 138;
    /** Key code constant: F9 key. */
    public static final int KEYCODE_F9              = 139;
    /** Key code constant: F10 key. */
    public static final int KEYCODE_F10             = 140;
    /** Key code constant: F11 key. */
    public static final int KEYCODE_F11             = 141;
    /** Key code constant: F12 key. */
    public static final int KEYCODE_F12             = 142;
    /** Key code constant: Num Lock key.
     * This is the Num Lock key; it is different from {@link #KEYCODE_NUM}.
     * This key alters the behavior of other keys on the numeric keypad. */
    public static final int KEYCODE_NUM_LOCK        = 143;
    /** Key code constant: Numeric keypad '0' key. */
    public static final int KEYCODE_NUMPAD_0        = 144;
    /** Key code constant: Numeric keypad '1' key. */
    public static final int KEYCODE_NUMPAD_1        = 145;
    /** Key code constant: Numeric keypad '2' key. */
    public static final int KEYCODE_NUMPAD_2        = 146;
    /** Key code constant: Numeric keypad '3' key. */
    public static final int KEYCODE_NUMPAD_3        = 147;
    /** Key code constant: Numeric keypad '4' key. */
    public static final int KEYCODE_NUMPAD_4        = 148;
    /** Key code constant: Numeric keypad '5' key. */
    public static final int KEYCODE_NUMPAD_5        = 149;
    /** Key code constant: Numeric keypad '6' key. */
    public static final int KEYCODE_NUMPAD_6        = 150;
    /** Key code constant: Numeric keypad '7' key. */
    public static final int KEYCODE_NUMPAD_7        = 151;
    /** Key code constant: Numeric keypad '8' key. */
    public static final int KEYCODE_NUMPAD_8        = 152;
    /** Key code constant: Numeric keypad '9' key. */
    public static final int KEYCODE_NUMPAD_9        = 153;
    /** Key code constant: Numeric keypad '/' key (for division). */
    public static final int KEYCODE_NUMPAD_DIVIDE   = 154;
    /** Key code constant: Numeric keypad '*' key (for multiplication). */
    public static final int KEYCODE_NUMPAD_MULTIPLY = 155;
    /** Key code constant: Numeric keypad '-' key (for subtraction). */
    public static final int KEYCODE_NUMPAD_SUBTRACT = 156;
    /** Key code constant: Numeric keypad '+' key (for addition). */
    public static final int KEYCODE_NUMPAD_ADD      = 157;
    /** Key code constant: Numeric keypad '.' key (for decimals or digit grouping). */
    public static final int KEYCODE_NUMPAD_DOT      = 158;
    /** Key code constant: Numeric keypad ',' key (for decimals or digit grouping). */
    public static final int KEYCODE_NUMPAD_COMMA    = 159;
    /** Key code constant: Numeric keypad Enter key. */
    public static final int KEYCODE_NUMPAD_ENTER    = 160;
    /** Key code constant: Numeric keypad '=' key. */
    public static final int KEYCODE_NUMPAD_EQUALS   = 161;
    /** Key code constant: Numeric keypad '(' key. */
    public static final int KEYCODE_NUMPAD_LEFT_PAREN = 162;
    /** Key code constant: Numeric keypad ')' key. */
    public static final int KEYCODE_NUMPAD_RIGHT_PAREN = 163;
    /** Key code constant: Volume Mute key.
     * Mutes the speaker, unlike {@link #KEYCODE_MUTE}.
     * This key should normally be implemented as a toggle such that the first press
     * mutes the speaker and the second press restores the original volume. */
    public static final int KEYCODE_VOLUME_MUTE     = 164;
    /** Key code constant: Info key.
     * Common on TV remotes to show additional information related to what is
     * currently being viewed. */
    public static final int KEYCODE_INFO            = 165;
    /** Key code constant: Channel up key.
     * On TV remotes, increments the television channel. */
    public static final int KEYCODE_CHANNEL_UP      = 166;
    /** Key code constant: Channel down key.
     * On TV remotes, decrements the television channel. */
    public static final int KEYCODE_CHANNEL_DOWN    = 167;
    /** Key code constant: Zoom in key. */
    public static final int KEYCODE_ZOOM_IN         = 168;
    /** Key code constant: Zoom out key. */
    public static final int KEYCODE_ZOOM_OUT        = 169;
    /** Key code constant: TV key.
     * On TV remotes, switches to viewing live TV. */
    public static final int KEYCODE_TV              = 170;
    /** Key code constant: Window key.
     * On TV remotes, toggles picture-in-picture mode or other windowing functions. */
    public static final int KEYCODE_WINDOW          = 171;
    /** Key code constant: Guide key.
     * On TV remotes, shows a programming guide. */
    public static final int KEYCODE_GUIDE           = 172;
    /** Key code constant: DVR key.
     * On some TV remotes, switches to a DVR mode for recorded shows. */
    public static final int KEYCODE_DVR             = 173;
    /** Key code constant: Bookmark key.
     * On some TV remotes, bookmarks content or web pages. */
    public static final int KEYCODE_BOOKMARK        = 174;
    /** Key code constant: Toggle captions key.
     * Switches the mode for closed-captioning text, for example during television shows. */
    public static final int KEYCODE_CAPTIONS        = 175;
    /** Key code constant: Settings key.
     * Starts the system settings activity. */
    public static final int KEYCODE_SETTINGS        = 176;
    /** Key code constant: TV power key.
     * On TV remotes, toggles the power on a television screen. */
    public static final int KEYCODE_TV_POWER        = 177;
    /** Key code constant: TV input key.
     * On TV remotes, switches the input on a television screen. */
    public static final int KEYCODE_TV_INPUT        = 178;
    /** Key code constant: Set-top-box power key.
     * On TV remotes, toggles the power on an external Set-top-box. */
    public static final int KEYCODE_STB_POWER       = 179;
    /** Key code constant: Set-top-box input key.
     * On TV remotes, switches the input mode on an external Set-top-box. */
    public static final int KEYCODE_STB_INPUT       = 180;
    /** Key code constant: A/V Receiver power key.
     * On TV remotes, toggles the power on an external A/V Receiver. */
    public static final int KEYCODE_AVR_POWER       = 181;
    /** Key code constant: A/V Receiver input key.
     * On TV remotes, switches the input mode on an external A/V Receiver. */
    public static final int KEYCODE_AVR_INPUT       = 182;
    /** Key code constant: Red "programmable" key.
     * On TV remotes, acts as a contextual/programmable key. */
    public static final int KEYCODE_PROG_RED        = 183;
    /** Key code constant: Green "programmable" key.
     * On TV remotes, actsas a contextual/programmable key. */
    public static final int KEYCODE_PROG_GREEN      = 184;
    /** Key code constant: Yellow "programmable" key.
     * On TV remotes, acts as a contextual/programmable key. */
    public static final int KEYCODE_PROG_YELLOW     = 185;
    /** Key code constant: Blue "programmable" key.
     * On TV remotes, acts as a contextual/programmable key. */
    public static final int KEYCODE_PROG_BLUE       = 186;
    /** Key code constant: App switch key.
     * Should bring up the application switcher dialog. */
    public static final int KEYCODE_APP_SWITCH      = 187;
    /** Key code constant: Generic Game Pad Button #1.*/
    public static final int KEYCODE_BUTTON_1        = 188;
    /** Key code constant: Generic Game Pad Button #2.*/
    public static final int KEYCODE_BUTTON_2        = 189;
    /** Key code constant: Generic Game Pad Button #3.*/
    public static final int KEYCODE_BUTTON_3        = 190;
    /** Key code constant: Generic Game Pad Button #4.*/
    public static final int KEYCODE_BUTTON_4        = 191;
    /** Key code constant: Generic Game Pad Button #5.*/
    public static final int KEYCODE_BUTTON_5        = 192;
    /** Key code constant: Generic Game Pad Button #6.*/
    public static final int KEYCODE_BUTTON_6        = 193;
    /** Key code constant: Generic Game Pad Button #7.*/
    public static final int KEYCODE_BUTTON_7        = 194;
    /** Key code constant: Generic Game Pad Button #8.*/
    public static final int KEYCODE_BUTTON_8        = 195;
    /** Key code constant: Generic Game Pad Button #9.*/
    public static final int KEYCODE_BUTTON_9        = 196;
    /** Key code constant: Generic Game Pad Button #10.*/
    public static final int KEYCODE_BUTTON_10       = 197;
    /** Key code constant: Generic Game Pad Button #11.*/
    public static final int KEYCODE_BUTTON_11       = 198;
    /** Key code constant: Generic Game Pad Button #12.*/
    public static final int KEYCODE_BUTTON_12       = 199;
    /** Key code constant: Generic Game Pad Button #13.*/
    public static final int KEYCODE_BUTTON_13       = 200;
    /** Key code constant: Generic Game Pad Button #14.*/
    public static final int KEYCODE_BUTTON_14       = 201;
    /** Key code constant: Generic Game Pad Button #15.*/
    public static final int KEYCODE_BUTTON_15       = 202;
    /** Key code constant: Generic Game Pad Button #16.*/
    public static final int KEYCODE_BUTTON_16       = 203;
    /** Key code constant: Language Switch key.
     * Toggles the current input language such as switching between English and Japanese on
     * a QWERTY keyboard.  On some devices, the same function may be performed by
     * pressing Shift+Spacebar. */
    public static final int KEYCODE_LANGUAGE_SWITCH = 204;
    /** Key code constant: Manner Mode key.
     * Toggles silent or vibrate mode on and off to make the device behave more politely
     * in certain settings such as on a crowded train.  On some devices, the key may only
     * operate when long-pressed. */
    public static final int KEYCODE_MANNER_MODE     = 205;
    /** Key code constant: 3D Mode key.
     * Toggles the display between 2D and 3D mode. */
    public static final int KEYCODE_3D_MODE         = 206;
    /** Key code constant: Contacts special function key.
     * Used to launch an address book application. */
    public static final int KEYCODE_CONTACTS        = 207;
    /** Key code constant: Calendar special function key.
     * Used to launch a calendar application. */
    public static final int KEYCODE_CALENDAR        = 208;
    /** Key code constant: Music special function key.
     * Used to launch a music player application. */
    public static final int KEYCODE_MUSIC           = 209;
    /** Key code constant: Calculator special function key.
     * Used to launch a calculator application. */
    public static final int KEYCODE_CALCULATOR      = 210;
    /** Key code constant: Japanese full-width / half-width key. */
    public static final int KEYCODE_ZENKAKU_HANKAKU = 211;
    /** Key code constant: Japanese alphanumeric key. */
    public static final int KEYCODE_EISU            = 212;
    /** Key code constant: Japanese non-conversion key. */
    public static final int KEYCODE_MUHENKAN        = 213;
    /** Key code constant: Japanese conversion key. */
    public static final int KEYCODE_HENKAN          = 214;
    /** Key code constant: Japanese katakana / hiragana key. */
    public static final int KEYCODE_KATAKANA_HIRAGANA = 215;
    /** Key code constant: Japanese Yen key. */
    public static final int KEYCODE_YEN             = 216;
    /** Key code constant: Japanese Ro key. */
    public static final int KEYCODE_RO              = 217;
    /** Key code constant: Japanese kana key. */
    public static final int KEYCODE_KANA            = 218;
    /** Key code constant: Assist key.
     * Launches the global assist activity.  Not delivered to applications. */
    public static final int KEYCODE_ASSIST          = 219;

    /*{KW} 256 Braille key combinations*/    
  	public static final int KEYCODE_BRL_DOTS_7	=   257;
	public static final int KEYCODE_BRL_DOTS_3	=   258;
	public static final int KEYCODE_BRL_DOTS_37	=   259;
	public static final int KEYCODE_BRL_DOTS_2	=   260;
	public static final int KEYCODE_BRL_DOTS_27	=   261;
	public static final int KEYCODE_BRL_DOTS_23	=   262;
	public static final int KEYCODE_BRL_DOTS_237	=   263;
	public static final int KEYCODE_BRL_DOTS_1	=   264;
	public static final int KEYCODE_BRL_DOTS_17	=   265;
	public static final int KEYCODE_BRL_DOTS_13	=   266;
	public static final int KEYCODE_BRL_DOTS_137	=   267;
	public static final int KEYCODE_BRL_DOTS_12	=   268;
	public static final int KEYCODE_BRL_DOTS_127	=   269;
	public static final int KEYCODE_BRL_DOTS_123	=   270;
	public static final int KEYCODE_BRL_DOTS_1237	=   271;
	public static final int KEYCODE_BRL_DOTS_4	=   272;
	public static final int KEYCODE_BRL_DOTS_47	=   273;
	public static final int KEYCODE_BRL_DOTS_34	=   274;
	public static final int KEYCODE_BRL_DOTS_347	=   275;
	public static final int KEYCODE_BRL_DOTS_24	=   276;
	public static final int KEYCODE_BRL_DOTS_247	=   277;
	public static final int KEYCODE_BRL_DOTS_234	=   278;
	public static final int KEYCODE_BRL_DOTS_2347	=   279;
	public static final int KEYCODE_BRL_DOTS_14	=   280;
	public static final int KEYCODE_BRL_DOTS_147	=   281;
	public static final int KEYCODE_BRL_DOTS_134	=   282;
	public static final int KEYCODE_BRL_DOTS_1347	=   283;
	public static final int KEYCODE_BRL_DOTS_124	=   284;
	public static final int KEYCODE_BRL_DOTS_1247	=   285;
	public static final int KEYCODE_BRL_DOTS_1234	=   286;
	public static final int KEYCODE_BRL_DOTS_12347	=   287;
	public static final int KEYCODE_BRL_DOTS_5	=   288;
	public static final int KEYCODE_BRL_DOTS_57	=   289;
	public static final int KEYCODE_BRL_DOTS_35	=   290;
	public static final int KEYCODE_BRL_DOTS_357	=   291;
	public static final int KEYCODE_BRL_DOTS_25	=   292;
	public static final int KEYCODE_BRL_DOTS_257	=   293;
	public static final int KEYCODE_BRL_DOTS_235	=   294;
	public static final int KEYCODE_BRL_DOTS_2357	=   295;
	public static final int KEYCODE_BRL_DOTS_15	=   296;
	public static final int KEYCODE_BRL_DOTS_157	=   297;
	public static final int KEYCODE_BRL_DOTS_135	=   298;
	public static final int KEYCODE_BRL_DOTS_1357	=   299;
	public static final int KEYCODE_BRL_DOTS_125	=   300;
	public static final int KEYCODE_BRL_DOTS_1257	=   301;
	public static final int KEYCODE_BRL_DOTS_1235	=   302;
	public static final int KEYCODE_BRL_DOTS_12357	=   303;
	public static final int KEYCODE_BRL_DOTS_45	=   304;
	public static final int KEYCODE_BRL_DOTS_457	=   305;
	public static final int KEYCODE_BRL_DOTS_345	=   306;
	public static final int KEYCODE_BRL_DOTS_3457	=   307;
	public static final int KEYCODE_BRL_DOTS_245	=   308;
	public static final int KEYCODE_BRL_DOTS_2457	=   309;
	public static final int KEYCODE_BRL_DOTS_2345	=   310;
	public static final int KEYCODE_BRL_DOTS_23457	=   311;
	public static final int KEYCODE_BRL_DOTS_145	=   312;
	public static final int KEYCODE_BRL_DOTS_1457	=   313;
	public static final int KEYCODE_BRL_DOTS_1345	=   314;
	public static final int KEYCODE_BRL_DOTS_13457	=   315;
	public static final int KEYCODE_BRL_DOTS_1245	=   316;
	public static final int KEYCODE_BRL_DOTS_12457	=   317;
	public static final int KEYCODE_BRL_DOTS_12345	=   318;
	public static final int KEYCODE_BRL_DOTS_123457	=   319;
	public static final int KEYCODE_BRL_DOTS_6	=   320;
	public static final int KEYCODE_BRL_DOTS_67	=   321;
	public static final int KEYCODE_BRL_DOTS_36	=   322;
	public static final int KEYCODE_BRL_DOTS_367	=   323;
	public static final int KEYCODE_BRL_DOTS_26	=   324;
	public static final int KEYCODE_BRL_DOTS_267	=   325;
	public static final int KEYCODE_BRL_DOTS_236	=   326;
	public static final int KEYCODE_BRL_DOTS_2367	=   327;
	public static final int KEYCODE_BRL_DOTS_16	=   328;
	public static final int KEYCODE_BRL_DOTS_167	=   329;
	public static final int KEYCODE_BRL_DOTS_136	=   330;
	public static final int KEYCODE_BRL_DOTS_1367	=   331;
	public static final int KEYCODE_BRL_DOTS_126	=   332;
	public static final int KEYCODE_BRL_DOTS_1267	=   333;
	public static final int KEYCODE_BRL_DOTS_1236	=   334;
	public static final int KEYCODE_BRL_DOTS_12367	=   335;
	public static final int KEYCODE_BRL_DOTS_46	=   336;
	public static final int KEYCODE_BRL_DOTS_467	=   337;
	public static final int KEYCODE_BRL_DOTS_346	=   338;
	public static final int KEYCODE_BRL_DOTS_3467	=   339;
	public static final int KEYCODE_BRL_DOTS_246	=   340;
	public static final int KEYCODE_BRL_DOTS_2467	=   341;
	public static final int KEYCODE_BRL_DOTS_2346	=   342;
	public static final int KEYCODE_BRL_DOTS_23467	=   343;
	public static final int KEYCODE_BRL_DOTS_146	=   344;
	public static final int KEYCODE_BRL_DOTS_1467	=   345;
	public static final int KEYCODE_BRL_DOTS_1346	=   346;
	public static final int KEYCODE_BRL_DOTS_13467	=   347;
	public static final int KEYCODE_BRL_DOTS_1246	=   348;
	public static final int KEYCODE_BRL_DOTS_12467	=   349;
	public static final int KEYCODE_BRL_DOTS_12346	=   350;
	public static final int KEYCODE_BRL_DOTS_123467	=   351;
	public static final int KEYCODE_BRL_DOTS_56	=   352;
	public static final int KEYCODE_BRL_DOTS_567	=   353;
	public static final int KEYCODE_BRL_DOTS_356	=   354;
	public static final int KEYCODE_BRL_DOTS_3567	=   355;
	public static final int KEYCODE_BRL_DOTS_256	=   356;
	public static final int KEYCODE_BRL_DOTS_2567	=   357;
	public static final int KEYCODE_BRL_DOTS_2356	=   358;
	public static final int KEYCODE_BRL_DOTS_23567	=   359;
	public static final int KEYCODE_BRL_DOTS_156	=   360;
	public static final int KEYCODE_BRL_DOTS_1567	=   361;
	public static final int KEYCODE_BRL_DOTS_1356	=   362;
	public static final int KEYCODE_BRL_DOTS_13567	=   363;
	public static final int KEYCODE_BRL_DOTS_1256	=   364;
	public static final int KEYCODE_BRL_DOTS_12567	=   365;
	public static final int KEYCODE_BRL_DOTS_12356	=   366;
	public static final int KEYCODE_BRL_DOTS_123567	=   367;
	public static final int KEYCODE_BRL_DOTS_456	=   368;
	public static final int KEYCODE_BRL_DOTS_4567	=   369;
	public static final int KEYCODE_BRL_DOTS_3456	=   370;
	public static final int KEYCODE_BRL_DOTS_34567	=   371;
	public static final int KEYCODE_BRL_DOTS_2456	=   372;
	public static final int KEYCODE_BRL_DOTS_24567	=   373;
	public static final int KEYCODE_BRL_DOTS_23456	=   374;
	public static final int KEYCODE_BRL_DOTS_234567	=   375;
	public static final int KEYCODE_BRL_DOTS_1456	=   376;
	public static final int KEYCODE_BRL_DOTS_14567	=   377;
	public static final int KEYCODE_BRL_DOTS_13456	=   378;
	public static final int KEYCODE_BRL_DOTS_134567	=   379;
	public static final int KEYCODE_BRL_DOTS_12456	=   380;
	public static final int KEYCODE_BRL_DOTS_124567 =   381;
	public static final int KEYCODE_BRL_DOTS_123456	=   382;
	public static final int KEYCODE_BRL_DOTS_1234567	=   383;
        public static final int KEYCODE_BRL_DOTS_8	=   384;
	public static final int KEYCODE_BRL_DOTS_78	=   385;
	public static final int KEYCODE_BRL_DOTS_38	=   386;
	public static final int KEYCODE_BRL_DOTS_378	=   387;
	public static final int KEYCODE_BRL_DOTS_28	=   388;
	public static final int KEYCODE_BRL_DOTS_278	=   389;
	public static final int KEYCODE_BRL_DOTS_238	=   390;
	public static final int KEYCODE_BRL_DOTS_2378	=   391;
	public static final int KEYCODE_BRL_DOTS_18	=   392;
	public static final int KEYCODE_BRL_DOTS_178	=   393;
	public static final int KEYCODE_BRL_DOTS_138	=   394;
	public static final int KEYCODE_BRL_DOTS_1378	=   395;
	public static final int KEYCODE_BRL_DOTS_128	=   396;
	public static final int KEYCODE_BRL_DOTS_1278	=   397;
	public static final int KEYCODE_BRL_DOTS_1238	=   398;
	public static final int KEYCODE_BRL_DOTS_12378	=   399;
	public static final int KEYCODE_BRL_DOTS_48	=   400;
	public static final int KEYCODE_BRL_DOTS_478	=   401;
	public static final int KEYCODE_BRL_DOTS_348	=   402;
	public static final int KEYCODE_BRL_DOTS_3478	=   403;
	public static final int KEYCODE_BRL_DOTS_248	=   404;
	public static final int KEYCODE_BRL_DOTS_2478	=   405;
	public static final int KEYCODE_BRL_DOTS_2348	=   406;
	public static final int KEYCODE_BRL_DOTS_23478	=   407;
	public static final int KEYCODE_BRL_DOTS_148	=   408;
	public static final int KEYCODE_BRL_DOTS_1478	=   409;
	public static final int KEYCODE_BRL_DOTS_1348	=   410;
	public static final int KEYCODE_BRL_DOTS_13478	=   411;
	public static final int KEYCODE_BRL_DOTS_1248	=   412;
	public static final int KEYCODE_BRL_DOTS_12478	=   413;
	public static final int KEYCODE_BRL_DOTS_12348	=   414;
	public static final int KEYCODE_BRL_DOTS_123478	=   415;
	public static final int KEYCODE_BRL_DOTS_58	=   416;
	public static final int KEYCODE_BRL_DOTS_578	=   417;
	public static final int KEYCODE_BRL_DOTS_358	=   418;
	public static final int KEYCODE_BRL_DOTS_3578	=   419;
	public static final int KEYCODE_BRL_DOTS_258	=   420;
	public static final int KEYCODE_BRL_DOTS_2578	=   421;
	public static final int KEYCODE_BRL_DOTS_2358	=   422;
	public static final int KEYCODE_BRL_DOTS_23578	=   423;
	public static final int KEYCODE_BRL_DOTS_158	=   424;
	public static final int KEYCODE_BRL_DOTS_1578	=   425;
	public static final int KEYCODE_BRL_DOTS_1358	=   426;
	public static final int KEYCODE_BRL_DOTS_13578	=   427;
	public static final int KEYCODE_BRL_DOTS_1258	=   428;
	public static final int KEYCODE_BRL_DOTS_12578	=   429;
	public static final int KEYCODE_BRL_DOTS_12358	=   430;
	public static final int KEYCODE_BRL_DOTS_123578	=   431;
	public static final int KEYCODE_BRL_DOTS_458	=   432;
	public static final int KEYCODE_BRL_DOTS_4578	=   433;
	public static final int KEYCODE_BRL_DOTS_3458	=   434;
	public static final int KEYCODE_BRL_DOTS_34578	=   435;
	public static final int KEYCODE_BRL_DOTS_2458	=   436;
	public static final int KEYCODE_BRL_DOTS_24578	=   437;
	public static final int KEYCODE_BRL_DOTS_23458	=   438;
	public static final int KEYCODE_BRL_DOTS_234578	=   439;
	public static final int KEYCODE_BRL_DOTS_1458	=   440;
	public static final int KEYCODE_BRL_DOTS_14578	=   441;
	public static final int KEYCODE_BRL_DOTS_13458	=   442;
	public static final int KEYCODE_BRL_DOTS_134578	=   443;
	public static final int KEYCODE_BRL_DOTS_12458	=   444;
	public static final int KEYCODE_BRL_DOTS_124578	=   445;
	public static final int KEYCODE_BRL_DOTS_123458	=   446;
	public static final int KEYCODE_BRL_DOTS_1234578	=   447;
	public static final int KEYCODE_BRL_DOTS_68	=   448;
	public static final int KEYCODE_BRL_DOTS_678	=   449;
	public static final int KEYCODE_BRL_DOTS_368	=   450;
	public static final int KEYCODE_BRL_DOTS_3678	=   451;
	public static final int KEYCODE_BRL_DOTS_268	=   452;
	public static final int KEYCODE_BRL_DOTS_2678	=   453;
	public static final int KEYCODE_BRL_DOTS_2368	=   454;
	public static final int KEYCODE_BRL_DOTS_23678	=   455;
	public static final int KEYCODE_BRL_DOTS_168	=   456;
	public static final int KEYCODE_BRL_DOTS_1678	=   457;
	public static final int KEYCODE_BRL_DOTS_1368	=   458;
	public static final int KEYCODE_BRL_DOTS_13678	=   459;
	public static final int KEYCODE_BRL_DOTS_1268	=   460;
	public static final int KEYCODE_BRL_DOTS_12678	=   461;
	public static final int KEYCODE_BRL_DOTS_12368	=   462;
	public static final int KEYCODE_BRL_DOTS_123678	=   463;
	public static final int KEYCODE_BRL_DOTS_468	=   464;
	public static final int KEYCODE_BRL_DOTS_4678	=   465;
	public static final int KEYCODE_BRL_DOTS_3468	=   466;
	public static final int KEYCODE_BRL_DOTS_34678	=   467;
	public static final int KEYCODE_BRL_DOTS_2468	=   468;
	public static final int KEYCODE_BRL_DOTS_24678	=   469;
	public static final int KEYCODE_BRL_DOTS_23468	=   470;
	public static final int KEYCODE_BRL_DOTS_234678	=   471;
	public static final int KEYCODE_BRL_DOTS_1468	=   472;
	public static final int KEYCODE_BRL_DOTS_14678	=   473;
	public static final int KEYCODE_BRL_DOTS_13468	=   474;
	public static final int KEYCODE_BRL_DOTS_134678	=   475;
	public static final int KEYCODE_BRL_DOTS_12468	=   476;
	public static final int KEYCODE_BRL_DOTS_124678	=   477;
	public static final int KEYCODE_BRL_DOTS_123468	=   478;
	public static final int KEYCODE_BRL_DOTS_1234678	=   479;
	public static final int KEYCODE_BRL_DOTS_568	=   480;
	public static final int KEYCODE_BRL_DOTS_5678	=   481;
	public static final int KEYCODE_BRL_DOTS_3568	=   482;
	public static final int KEYCODE_BRL_DOTS_35678	=   483;
	public static final int KEYCODE_BRL_DOTS_2568	=   484;
	public static final int KEYCODE_BRL_DOTS_25678	=   485;
	public static final int KEYCODE_BRL_DOTS_23568	=   486;
	public static final int KEYCODE_BRL_DOTS_235678	=   487;
	public static final int KEYCODE_BRL_DOTS_1568	=   488;
	public static final int KEYCODE_BRL_DOTS_15678	=   489;
	public static final int KEYCODE_BRL_DOTS_13568	=   490;
	public static final int KEYCODE_BRL_DOTS_135678	=   491;
	public static final int KEYCODE_BRL_DOTS_12568	=   492;
	public static final int KEYCODE_BRL_DOTS_125678	=   493;
	public static final int KEYCODE_BRL_DOTS_123568	=   494;
	public static final int KEYCODE_BRL_DOTS_1235678	=   495;
	public static final int KEYCODE_BRL_DOTS_4568	=   496;
	public static final int KEYCODE_BRL_DOTS_45678	=   497;
	public static final int KEYCODE_BRL_DOTS_34568	=   498;
	public static final int KEYCODE_BRL_DOTS_345678	=   499;
	public static final int KEYCODE_BRL_DOTS_24568	=   500;
	public static final int KEYCODE_BRL_DOTS_245678	=   501;
	public static final int KEYCODE_BRL_DOTS_234568	=   502;
	public static final int KEYCODE_BRL_DOTS_2345678	=   503;
	public static final int KEYCODE_BRL_DOTS_14568	=   504;
	public static final int KEYCODE_BRL_DOTS_145678	=   505;
	public static final int KEYCODE_BRL_DOTS_134568	=   506;
	public static final int KEYCODE_BRL_DOTS_1345678	=   507;
	public static final int KEYCODE_BRL_DOTS_124568	=   508;
	public static final int KEYCODE_BRL_DOTS_1245678	=   509;
	public static final int KEYCODE_BRL_DOTS_1234568	=   510;
	public static final int KEYCODE_BRL_DOTS_12345678	=   511;
    /*{KW} end */
	
	/*{KW} 256 Chorded key combinations*/
	public static final int KEYCODE_BRL_CHORD_7	=   513;
  	public static final int KEYCODE_BRL_CHORD_3	=   514;
	public static final int KEYCODE_BRL_CHORD_37	=   515;
	public static final int KEYCODE_BRL_CHORD_2	=   516;
	public static final int KEYCODE_BRL_CHORD_27	=   517;
	public static final int KEYCODE_BRL_CHORD_23	=   518;
	public static final int KEYCODE_BRL_CHORD_237	=   519;
  	public static final int KEYCODE_BRL_CHORD_1	=   520;
	public static final int KEYCODE_BRL_CHORD_17	=   521;
	public static final int KEYCODE_BRL_CHORD_13	=   522;
	public static final int KEYCODE_BRL_CHORD_137	=   523;
  	public static final int KEYCODE_BRL_CHORD_12	=   524;
	public static final int KEYCODE_BRL_CHORD_127	=   525;
	public static final int KEYCODE_BRL_CHORD_123	=   526;
	public static final int KEYCODE_BRL_CHORD_1237	=   527;
  	public static final int KEYCODE_BRL_CHORD_4	=   528;
	public static final int KEYCODE_BRL_CHORD_47	=   529;
	public static final int KEYCODE_BRL_CHORD_34	=   530;
	public static final int KEYCODE_BRL_CHORD_347	=   531;
	public static final int KEYCODE_BRL_CHORD_24	=   532;
	public static final int KEYCODE_BRL_CHORD_247	=   533;
	public static final int KEYCODE_BRL_CHORD_234	=   534;
	public static final int KEYCODE_BRL_CHORD_2347	=   535;
	public static final int KEYCODE_BRL_CHORD_14	=   536;
	public static final int KEYCODE_BRL_CHORD_147	=   537;
  	public static final int KEYCODE_BRL_CHORD_134	=   538;
	public static final int KEYCODE_BRL_CHORD_1347	=   539;
  	public static final int KEYCODE_BRL_CHORD_124	=   540;
	public static final int KEYCODE_BRL_CHORD_1247	=   541;
	public static final int KEYCODE_BRL_CHORD_1234	=   542;
	public static final int KEYCODE_BRL_CHORD_12347	=   543;
	public static final int KEYCODE_BRL_CHORD_5	=   544;
	public static final int KEYCODE_BRL_CHORD_57	=   545;
	public static final int KEYCODE_BRL_CHORD_35	=   546;
	public static final int KEYCODE_BRL_CHORD_357	=   547;
	public static final int KEYCODE_BRL_CHORD_25	=   548;
	public static final int KEYCODE_BRL_CHORD_257	=   549;
	public static final int KEYCODE_BRL_CHORD_235	=   550;
	public static final int KEYCODE_BRL_CHORD_2357	=   551;
	public static final int KEYCODE_BRL_CHORD_15	=   552;
	public static final int KEYCODE_BRL_CHORD_157	=   553;
	public static final int KEYCODE_BRL_CHORD_135	=   554;
	public static final int KEYCODE_BRL_CHORD_1357	=   555;
	public static final int KEYCODE_BRL_CHORD_125	=   556;
	public static final int KEYCODE_BRL_CHORD_1257	=   557;
	public static final int KEYCODE_BRL_CHORD_1235	=   558;
	public static final int KEYCODE_BRL_CHORD_12357	=   559;
  	public static final int KEYCODE_BRL_CHORD_45	=   560;
	public static final int KEYCODE_BRL_CHORD_457	=   561;
	public static final int KEYCODE_BRL_CHORD_345	=   562;
	public static final int KEYCODE_BRL_CHORD_3457	=   563;
	public static final int KEYCODE_BRL_CHORD_245	=   564;
	public static final int KEYCODE_BRL_CHORD_2457	=   565;
	public static final int KEYCODE_BRL_CHORD_2345	=   566;
	public static final int KEYCODE_BRL_CHORD_23457	=   567;
  	public static final int KEYCODE_BRL_CHORD_145	=   568;
	public static final int KEYCODE_BRL_CHORD_1457	=   569;
  	public static final int KEYCODE_BRL_CHORD_1345	=   570;
	public static final int KEYCODE_BRL_CHORD_13457	=   571;
	public static final int KEYCODE_BRL_CHORD_1245	=   572;
	public static final int KEYCODE_BRL_CHORD_12457	=   573;
	public static final int KEYCODE_BRL_CHORD_12345	=   574;
	public static final int KEYCODE_BRL_CHORD_123457	=   575;
  	public static final int KEYCODE_BRL_CHORD_6	=   576;
	public static final int KEYCODE_BRL_CHORD_67	=   577;
	public static final int KEYCODE_BRL_CHORD_36	=   578;
	public static final int KEYCODE_BRL_CHORD_367	=   579;
	public static final int KEYCODE_BRL_CHORD_26	=   580;
	public static final int KEYCODE_BRL_CHORD_267	=   581;
	public static final int KEYCODE_BRL_CHORD_236	=   582;
	public static final int KEYCODE_BRL_CHORD_2367	=   583;
	public static final int KEYCODE_BRL_CHORD_16	=   584;
	public static final int KEYCODE_BRL_CHORD_167	=   585;
	public static final int KEYCODE_BRL_CHORD_136	=   586;
	public static final int KEYCODE_BRL_CHORD_1367	=   587;
	public static final int KEYCODE_BRL_CHORD_126	=   588;
	public static final int KEYCODE_BRL_CHORD_1267	=   589;
	public static final int KEYCODE_BRL_CHORD_1236	=   590;
	public static final int KEYCODE_BRL_CHORD_12367	=   591;
	public static final int KEYCODE_BRL_CHORD_46	=   592;
	public static final int KEYCODE_BRL_CHORD_467	=   593;
	public static final int KEYCODE_BRL_CHORD_346	=   594;
	public static final int KEYCODE_BRL_CHORD_3467	=   595;
	public static final int KEYCODE_BRL_CHORD_246	=   596;
	public static final int KEYCODE_BRL_CHORD_2467	=   597;
	public static final int KEYCODE_BRL_CHORD_2346	=   598;
	public static final int KEYCODE_BRL_CHORD_23467	=   599;
	public static final int KEYCODE_BRL_CHORD_146	=   600;
	public static final int KEYCODE_BRL_CHORD_1467	=   601;
	public static final int KEYCODE_BRL_CHORD_1346	=   602;
	public static final int KEYCODE_BRL_CHORD_13467	=   603;
	public static final int KEYCODE_BRL_CHORD_1246	=   604;
	public static final int KEYCODE_BRL_CHORD_12467	=   605;
	public static final int KEYCODE_BRL_CHORD_12346	=   606;
	public static final int KEYCODE_BRL_CHORD_123467	=   607;
	public static final int KEYCODE_BRL_CHORD_56	=   608;
	public static final int KEYCODE_BRL_CHORD_567	=   609;
	public static final int KEYCODE_BRL_CHORD_356	=   610;
	public static final int KEYCODE_BRL_CHORD_3567	=   611;
	public static final int KEYCODE_BRL_CHORD_256	=   612;
	public static final int KEYCODE_BRL_CHORD_2567	=   613;
	public static final int KEYCODE_BRL_CHORD_2356	=   614;
	public static final int KEYCODE_BRL_CHORD_23567	=   615;
	public static final int KEYCODE_BRL_CHORD_156	=   616;
	public static final int KEYCODE_BRL_CHORD_1567	=   617;
	public static final int KEYCODE_BRL_CHORD_1356	=   618;
	public static final int KEYCODE_BRL_CHORD_13567	=   619;
	public static final int KEYCODE_BRL_CHORD_1256	=   620;
	public static final int KEYCODE_BRL_CHORD_12567	=   621;
	public static final int KEYCODE_BRL_CHORD_12356	=   622;
	public static final int KEYCODE_BRL_CHORD_123567	=   623;
	public static final int KEYCODE_BRL_CHORD_456	=   624;
	public static final int KEYCODE_BRL_CHORD_4567	=   625;
	public static final int KEYCODE_BRL_CHORD_3456	=   626;
	public static final int KEYCODE_BRL_CHORD_34567	=   627;
	public static final int KEYCODE_BRL_CHORD_2456	=   628;
	public static final int KEYCODE_BRL_CHORD_24567	=   629;
	public static final int KEYCODE_BRL_CHORD_23456	=   630;
	public static final int KEYCODE_BRL_CHORD_234567	=   631;
  	public static final int KEYCODE_BRL_CHORD_1456	=   632;
	public static final int KEYCODE_BRL_CHORD_14567	=   633;
	public static final int KEYCODE_BRL_CHORD_13456	=   634;
	public static final int KEYCODE_BRL_CHORD_134567	=   635;
	public static final int KEYCODE_BRL_CHORD_12456	=   636;
	public static final int KEYCODE_BRL_CHORD_124567	=   637;
  	public static final int KEYCODE_BRL_CHORD_123456	=   638;
	public static final int KEYCODE_BRL_CHORD_1234567	=   639;
	public static final int KEYCODE_BRL_CHORD_8	=   640;
	public static final int KEYCODE_BRL_CHORD_78	=   641;
	public static final int KEYCODE_BRL_CHORD_38	=   642;
	public static final int KEYCODE_BRL_CHORD_378	=   643;
	public static final int KEYCODE_BRL_CHORD_28	=   644;
	public static final int KEYCODE_BRL_CHORD_278	=   645;
	public static final int KEYCODE_BRL_CHORD_238	=   646;
	public static final int KEYCODE_BRL_CHORD_2378	=   647;
	public static final int KEYCODE_BRL_CHORD_18	=   648;
	public static final int KEYCODE_BRL_CHORD_178	=   649;
	public static final int KEYCODE_BRL_CHORD_138	=   650;
	public static final int KEYCODE_BRL_CHORD_1378	=   651;
	public static final int KEYCODE_BRL_CHORD_128	=   652;
	public static final int KEYCODE_BRL_CHORD_1278	=   653;
	public static final int KEYCODE_BRL_CHORD_1238	=   654;
	public static final int KEYCODE_BRL_CHORD_12378	=   655;
	public static final int KEYCODE_BRL_CHORD_48	=   656;
	public static final int KEYCODE_BRL_CHORD_478	=   657;
	public static final int KEYCODE_BRL_CHORD_348	=   658;
	public static final int KEYCODE_BRL_CHORD_3478	=   659;
	public static final int KEYCODE_BRL_CHORD_248	=   660;
	public static final int KEYCODE_BRL_CHORD_2478	=   661;
	public static final int KEYCODE_BRL_CHORD_2348	=   662;
	public static final int KEYCODE_BRL_CHORD_23478	=   663;
	public static final int KEYCODE_BRL_CHORD_148	=   664;
	public static final int KEYCODE_BRL_CHORD_1478	=   665;
	public static final int KEYCODE_BRL_CHORD_1348	=   666;
	public static final int KEYCODE_BRL_CHORD_13478	=   667;
	public static final int KEYCODE_BRL_CHORD_1248	=   668;
	public static final int KEYCODE_BRL_CHORD_12478	=   669;
	public static final int KEYCODE_BRL_CHORD_12348	=   670;
	public static final int KEYCODE_BRL_CHORD_123478	=   671;
	public static final int KEYCODE_BRL_CHORD_58	=   672;
	public static final int KEYCODE_BRL_CHORD_578	=   673;
	public static final int KEYCODE_BRL_CHORD_358	=   674;
	public static final int KEYCODE_BRL_CHORD_3578	=   675;
	public static final int KEYCODE_BRL_CHORD_258	=   676;
	public static final int KEYCODE_BRL_CHORD_2578	=   677;
	public static final int KEYCODE_BRL_CHORD_2358	=   678;
	public static final int KEYCODE_BRL_CHORD_23578	=   679;
	public static final int KEYCODE_BRL_CHORD_158	=   680;
	public static final int KEYCODE_BRL_CHORD_1578	=   681;
	public static final int KEYCODE_BRL_CHORD_1358	=   682;
	public static final int KEYCODE_BRL_CHORD_13578	=   683;
	public static final int KEYCODE_BRL_CHORD_1258	=   684;
	public static final int KEYCODE_BRL_CHORD_12578	=   685;
	public static final int KEYCODE_BRL_CHORD_12358	=   686;
	public static final int KEYCODE_BRL_CHORD_123578	=   687;
	public static final int KEYCODE_BRL_CHORD_458	=   688;
	public static final int KEYCODE_BRL_CHORD_4578	=   689;
	public static final int KEYCODE_BRL_CHORD_3458	=   690;
	public static final int KEYCODE_BRL_CHORD_34578	=   691;
	public static final int KEYCODE_BRL_CHORD_2458	=   692;
	public static final int KEYCODE_BRL_CHORD_24578	=   693;
	public static final int KEYCODE_BRL_CHORD_23458	=   694;
	public static final int KEYCODE_BRL_CHORD_234578	=   695;
	public static final int KEYCODE_BRL_CHORD_1458	=   696;
	public static final int KEYCODE_BRL_CHORD_14578	=   697;
	public static final int KEYCODE_BRL_CHORD_13458	=   698;
	public static final int KEYCODE_BRL_CHORD_134578	=   699;
	public static final int KEYCODE_BRL_CHORD_12458	=   700;
	public static final int KEYCODE_BRL_CHORD_124578	=   701;
	public static final int KEYCODE_BRL_CHORD_123458	=   702;
	public static final int KEYCODE_BRL_CHORD_1234578	=   703;
	public static final int KEYCODE_BRL_CHORD_68	=   704;
	public static final int KEYCODE_BRL_CHORD_678	=   705;
	public static final int KEYCODE_BRL_CHORD_368	=   706;
	public static final int KEYCODE_BRL_CHORD_3678	=   707;
	public static final int KEYCODE_BRL_CHORD_268	=   708;
	public static final int KEYCODE_BRL_CHORD_2678	=   709;
	public static final int KEYCODE_BRL_CHORD_2368	=   710;
	public static final int KEYCODE_BRL_CHORD_23678	=   711;
	public static final int KEYCODE_BRL_CHORD_168	=   712;
	public static final int KEYCODE_BRL_CHORD_1678	=   713;
	public static final int KEYCODE_BRL_CHORD_1368	=   714;
	public static final int KEYCODE_BRL_CHORD_13678	=   715;
	public static final int KEYCODE_BRL_CHORD_1268	=   716;
	public static final int KEYCODE_BRL_CHORD_12678	=   717;
	public static final int KEYCODE_BRL_CHORD_12368	=   718;
	public static final int KEYCODE_BRL_CHORD_123678	=   719;
	public static final int KEYCODE_BRL_CHORD_468	=   720;
	public static final int KEYCODE_BRL_CHORD_4678	=   721;
	public static final int KEYCODE_BRL_CHORD_3468	=   722;
	public static final int KEYCODE_BRL_CHORD_34678	=   723;
	public static final int KEYCODE_BRL_CHORD_2468	=   724;
	public static final int KEYCODE_BRL_CHORD_24678	=   725;
	public static final int KEYCODE_BRL_CHORD_23468	=   726;
	public static final int KEYCODE_BRL_CHORD_234678	=   727;
	public static final int KEYCODE_BRL_CHORD_1468	=   728;
	public static final int KEYCODE_BRL_CHORD_14678	=   729;
	public static final int KEYCODE_BRL_CHORD_13468	=   730;
	public static final int KEYCODE_BRL_CHORD_134678	=   731;
	public static final int KEYCODE_BRL_CHORD_12468	=   732;
	public static final int KEYCODE_BRL_CHORD_124678	=   733;
	public static final int KEYCODE_BRL_CHORD_123468	=   734;
	public static final int KEYCODE_BRL_CHORD_1234678	=   735;
	public static final int KEYCODE_BRL_CHORD_568	=   736;
	public static final int KEYCODE_BRL_CHORD_5678	=   737;
	public static final int KEYCODE_BRL_CHORD_3568	=   738;
	public static final int KEYCODE_BRL_CHORD_35678	=   739;
	public static final int KEYCODE_BRL_CHORD_2568	=   740;
	public static final int KEYCODE_BRL_CHORD_25678	=   741;
	public static final int KEYCODE_BRL_CHORD_23568	=   742;
	public static final int KEYCODE_BRL_CHORD_235678	=   743;
	public static final int KEYCODE_BRL_CHORD_1568	=   744;
	public static final int KEYCODE_BRL_CHORD_15678	=   745;
	public static final int KEYCODE_BRL_CHORD_13568	=   746;
	public static final int KEYCODE_BRL_CHORD_135678	=   747;
	public static final int KEYCODE_BRL_CHORD_12568	=   748;
	public static final int KEYCODE_BRL_CHORD_125678	=   749;
	public static final int KEYCODE_BRL_CHORD_123568	=   750;
	public static final int KEYCODE_BRL_CHORD_1235678	=   751;
	public static final int KEYCODE_BRL_CHORD_4568	=   752;
	public static final int KEYCODE_BRL_CHORD_45678	=   753;
	public static final int KEYCODE_BRL_CHORD_34568	=   754;
	public static final int KEYCODE_BRL_CHORD_345678	=   755;
	public static final int KEYCODE_BRL_CHORD_24568	=   756;
	public static final int KEYCODE_BRL_CHORD_245678	=   757;
	public static final int KEYCODE_BRL_CHORD_234568	=   758;
	public static final int KEYCODE_BRL_CHORD_2345678	=   759;
	public static final int KEYCODE_BRL_CHORD_14568	=   760;
	public static final int KEYCODE_BRL_CHORD_145678	=   761;
	public static final int KEYCODE_BRL_CHORD_134568	=   762;
	public static final int KEYCODE_BRL_CHORD_1345678	=   763;
	public static final int KEYCODE_BRL_CHORD_124568	=   764;
	public static final int KEYCODE_BRL_CHORD_1245678	=   765;
	public static final int KEYCODE_BRL_CHORD_1234568	=   766;
	public static final int KEYCODE_BRL_CHORD_12345678	=   767;
	/*{KW} end*/

	/*{KW} Individual Braille keys */
	
	public static final int KEYCODE_BRL_DOT_7     =  769;
	public static final int KEYCODE_BRL_DOT_3     =  770;
	public static final int KEYCODE_BRL_DOT_2     =  771;
	public static final int KEYCODE_BRL_DOT_1     =  772;
	public static final int KEYCODE_BRL_DOT_4     =  773;
	public static final int KEYCODE_BRL_DOT_5     =  774;
	public static final int KEYCODE_BRL_DOT_6     =  775;
	public static final int KEYCODE_BRL_DOT_8     =  776;
	public static final int KEYCODE_BRL_SPACE     =  777;
		
		/*{KW} Cursor Routing keys block 1*/
	public static final int KEYCODE_BRL_CURSOR_0  =  778;
	public static final int KEYCODE_BRL_CURSOR_1  =  779;
	public static final int KEYCODE_BRL_CURSOR_2  =  780;
	public static final int KEYCODE_BRL_CURSOR_3  =  781;
	public static final int KEYCODE_BRL_CURSOR_4  =  782;
	public static final int KEYCODE_BRL_CURSOR_5  =  783;
	public static final int KEYCODE_BRL_CURSOR_6  =  784;
	public static final int KEYCODE_BRL_CURSOR_7  =  785;
	public static final int KEYCODE_BRL_CURSOR_8  =  786;
	public static final int KEYCODE_BRL_CURSOR_9  =  787;
	public static final int KEYCODE_BRL_CURSOR_10 =  788;
	public static final int KEYCODE_BRL_CURSOR_11 =  789;
	public static final int KEYCODE_BRL_CURSOR_12 =  790;
	public static final int KEYCODE_BRL_CURSOR_13 =  791;
	public static final int KEYCODE_BRL_CURSOR_14 =  792;
	public static final int KEYCODE_BRL_CURSOR_15 =  793;
	public static final int KEYCODE_BRL_CURSOR_16 =  794;
	public static final int KEYCODE_BRL_CURSOR_17 =  795;
	public static final int KEYCODE_BRL_CURSOR_18 =  796;
	public static final int KEYCODE_BRL_CURSOR_19 =  797;
	
	/*{KW} Cursor Routing keys block 2*/
	public static final int KEYCODE_BRL_CURSOR_20 =  798;
	public static final int KEYCODE_BRL_CURSOR_21 =  799;
	public static final int KEYCODE_BRL_CURSOR_22 =  800;
	public static final int KEYCODE_BRL_CURSOR_23 =  801;
	public static final int KEYCODE_BRL_CURSOR_24 =  802;
	public static final int KEYCODE_BRL_CURSOR_25 =  803;
	public static final int KEYCODE_BRL_CURSOR_26 =  804;
	public static final int KEYCODE_BRL_CURSOR_27 =  805;
	public static final int KEYCODE_BRL_CURSOR_28 =  806;
	public static final int KEYCODE_BRL_CURSOR_29 =  807;
	public static final int KEYCODE_BRL_CURSOR_30 =  808;
	public static final int KEYCODE_BRL_CURSOR_31 =  809;
	public static final int KEYCODE_BRL_CURSOR_32 =  810;
	public static final int KEYCODE_BRL_CURSOR_33 =  811;
	public static final int KEYCODE_BRL_CURSOR_34 =  812;
	public static final int KEYCODE_BRL_CURSOR_35 =  813;
	public static final int KEYCODE_BRL_CURSOR_36 =  814;
	public static final int KEYCODE_BRL_CURSOR_37 =  815;
	public static final int KEYCODE_BRL_CURSOR_38 =  816;
	public static final int KEYCODE_BRL_CURSOR_39 =  817;
	
	/*{KW} Backward, Forward keys*/
	public static final int KEYCODE_BRL_BACKWARD  =  818;
	public static final int KEYCODE_BRL_FORWARD   =  819;
	
	/*{KW}: 256 additional key codes spared (820-1075) */
	
	/*{KW} Unmapped keycode for wakeup from suspend */
	public static final int KEYCODE_DUMMY_WAKEUP     =   1076;
 
	// {RD} overiding LAST_KEYCODE for JB
	//private static final int LAST_KEYCODE           = KEYCODE_ASSIST;
    private static final int LAST_KEYCODE           = KEYCODE_DUMMY_WAKEUP;    

    // NOTE: If you add a new keycode here you must also add it to:
    //  isSystem()
    //  native/include/android/keycodes.h
    //  frameworks/base/include/ui/KeycodeLabels.h
    //  external/webkit/WebKit/android/plugins/ANPKeyCodes.h
    //  frameworks/base/core/res/res/values/attrs.xml
    //  emulator?
    //  LAST_KEYCODE
    //  KEYCODE_SYMBOLIC_NAMES
    //
    //  Also Android currently does not reserve code ranges for vendor-
    //  specific key codes.  If you have new key codes to have, you
    //  MUST contribute a patch to the open source project to define
    //  those new codes.  This is intended to maintain a consistent
    //  set of key code definitions across all Android devices.

    // Symbolic names of all key codes.
    private static final SparseArray<String> KEYCODE_SYMBOLIC_NAMES = new SparseArray<String>();
    private static void populateKeycodeSymbolicNames() {
        SparseArray<String> names = KEYCODE_SYMBOLIC_NAMES;
        names.append(KEYCODE_UNKNOWN, "KEYCODE_UNKNOWN");
        names.append(KEYCODE_SOFT_LEFT, "KEYCODE_SOFT_LEFT");
        names.append(KEYCODE_SOFT_RIGHT, "KEYCODE_SOFT_RIGHT");
        names.append(KEYCODE_HOME, "KEYCODE_HOME");
        names.append(KEYCODE_BACK, "KEYCODE_BACK");
        names.append(KEYCODE_CALL, "KEYCODE_CALL");
        names.append(KEYCODE_ENDCALL, "KEYCODE_ENDCALL");
        names.append(KEYCODE_0, "KEYCODE_0");
        names.append(KEYCODE_1, "KEYCODE_1");
        names.append(KEYCODE_2, "KEYCODE_2");
        names.append(KEYCODE_3, "KEYCODE_3");
        names.append(KEYCODE_4, "KEYCODE_4");
        names.append(KEYCODE_5, "KEYCODE_5");
        names.append(KEYCODE_6, "KEYCODE_6");
        names.append(KEYCODE_7, "KEYCODE_7");
        names.append(KEYCODE_8, "KEYCODE_8");
        names.append(KEYCODE_9, "KEYCODE_9");
        names.append(KEYCODE_STAR, "KEYCODE_STAR");
        names.append(KEYCODE_POUND, "KEYCODE_POUND");
        names.append(KEYCODE_DPAD_UP, "KEYCODE_DPAD_UP");
        names.append(KEYCODE_DPAD_DOWN, "KEYCODE_DPAD_DOWN");
        names.append(KEYCODE_DPAD_LEFT, "KEYCODE_DPAD_LEFT");
        names.append(KEYCODE_DPAD_RIGHT, "KEYCODE_DPAD_RIGHT");
        names.append(KEYCODE_DPAD_CENTER, "KEYCODE_DPAD_CENTER");
        names.append(KEYCODE_VOLUME_UP, "KEYCODE_VOLUME_UP");
        names.append(KEYCODE_VOLUME_DOWN, "KEYCODE_VOLUME_DOWN");
        names.append(KEYCODE_POWER, "KEYCODE_POWER");
        names.append(KEYCODE_CAMERA, "KEYCODE_CAMERA");
        names.append(KEYCODE_CLEAR, "KEYCODE_CLEAR");
        names.append(KEYCODE_A, "KEYCODE_A");
        names.append(KEYCODE_B, "KEYCODE_B");
        names.append(KEYCODE_C, "KEYCODE_C");
        names.append(KEYCODE_D, "KEYCODE_D");
        names.append(KEYCODE_E, "KEYCODE_E");
        names.append(KEYCODE_F, "KEYCODE_F");
        names.append(KEYCODE_G, "KEYCODE_G");
        names.append(KEYCODE_H, "KEYCODE_H");
        names.append(KEYCODE_I, "KEYCODE_I");
        names.append(KEYCODE_J, "KEYCODE_J");
        names.append(KEYCODE_K, "KEYCODE_K");
        names.append(KEYCODE_L, "KEYCODE_L");
        names.append(KEYCODE_M, "KEYCODE_M");
        names.append(KEYCODE_N, "KEYCODE_N");
        names.append(KEYCODE_O, "KEYCODE_O");
        names.append(KEYCODE_P, "KEYCODE_P");
        names.append(KEYCODE_Q, "KEYCODE_Q");
        names.append(KEYCODE_R, "KEYCODE_R");
        names.append(KEYCODE_S, "KEYCODE_S");
        names.append(KEYCODE_T, "KEYCODE_T");
        names.append(KEYCODE_U, "KEYCODE_U");
        names.append(KEYCODE_V, "KEYCODE_V");
        names.append(KEYCODE_W, "KEYCODE_W");
        names.append(KEYCODE_X, "KEYCODE_X");
        names.append(KEYCODE_Y, "KEYCODE_Y");
        names.append(KEYCODE_Z, "KEYCODE_Z");
        names.append(KEYCODE_COMMA, "KEYCODE_COMMA");
        names.append(KEYCODE_PERIOD, "KEYCODE_PERIOD");
        names.append(KEYCODE_ALT_LEFT, "KEYCODE_ALT_LEFT");
        names.append(KEYCODE_ALT_RIGHT, "KEYCODE_ALT_RIGHT");
        names.append(KEYCODE_SHIFT_LEFT, "KEYCODE_SHIFT_LEFT");
        names.append(KEYCODE_SHIFT_RIGHT, "KEYCODE_SHIFT_RIGHT");
        names.append(KEYCODE_TAB, "KEYCODE_TAB");
        names.append(KEYCODE_SPACE, "KEYCODE_SPACE");
        names.append(KEYCODE_SYM, "KEYCODE_SYM");
        names.append(KEYCODE_EXPLORER, "KEYCODE_EXPLORER");
        names.append(KEYCODE_ENVELOPE, "KEYCODE_ENVELOPE");
        names.append(KEYCODE_ENTER, "KEYCODE_ENTER");
        names.append(KEYCODE_DEL, "KEYCODE_DEL");
        names.append(KEYCODE_GRAVE, "KEYCODE_GRAVE");
        names.append(KEYCODE_MINUS, "KEYCODE_MINUS");
        names.append(KEYCODE_EQUALS, "KEYCODE_EQUALS");
        names.append(KEYCODE_LEFT_BRACKET, "KEYCODE_LEFT_BRACKET");
        names.append(KEYCODE_RIGHT_BRACKET, "KEYCODE_RIGHT_BRACKET");
        names.append(KEYCODE_BACKSLASH, "KEYCODE_BACKSLASH");
        names.append(KEYCODE_SEMICOLON, "KEYCODE_SEMICOLON");
        names.append(KEYCODE_APOSTROPHE, "KEYCODE_APOSTROPHE");
        names.append(KEYCODE_SLASH, "KEYCODE_SLASH");
        names.append(KEYCODE_AT, "KEYCODE_AT");
        names.append(KEYCODE_NUM, "KEYCODE_NUM");
        names.append(KEYCODE_HEADSETHOOK, "KEYCODE_HEADSETHOOK");
        names.append(KEYCODE_FOCUS, "KEYCODE_FOCUS");
        names.append(KEYCODE_PLUS, "KEYCODE_PLUS");
        names.append(KEYCODE_MENU, "KEYCODE_MENU");
        names.append(KEYCODE_NOTIFICATION, "KEYCODE_NOTIFICATION");
        names.append(KEYCODE_SEARCH, "KEYCODE_SEARCH");
        names.append(KEYCODE_MEDIA_PLAY_PAUSE, "KEYCODE_MEDIA_PLAY_PAUSE");
        names.append(KEYCODE_MEDIA_STOP, "KEYCODE_MEDIA_STOP");
        names.append(KEYCODE_MEDIA_NEXT, "KEYCODE_MEDIA_NEXT");
        names.append(KEYCODE_MEDIA_PREVIOUS, "KEYCODE_MEDIA_PREVIOUS");
        names.append(KEYCODE_MEDIA_REWIND, "KEYCODE_MEDIA_REWIND");
        names.append(KEYCODE_MEDIA_FAST_FORWARD, "KEYCODE_MEDIA_FAST_FORWARD");
        names.append(KEYCODE_MUTE, "KEYCODE_MUTE");
        names.append(KEYCODE_PAGE_UP, "KEYCODE_PAGE_UP");
        names.append(KEYCODE_PAGE_DOWN, "KEYCODE_PAGE_DOWN");
        names.append(KEYCODE_PICTSYMBOLS, "KEYCODE_PICTSYMBOLS");
        names.append(KEYCODE_SWITCH_CHARSET, "KEYCODE_SWITCH_CHARSET");
        names.append(KEYCODE_BUTTON_A, "KEYCODE_BUTTON_A");
        names.append(KEYCODE_BUTTON_B, "KEYCODE_BUTTON_B");
        names.append(KEYCODE_BUTTON_C, "KEYCODE_BUTTON_C");
        names.append(KEYCODE_BUTTON_X, "KEYCODE_BUTTON_X");
        names.append(KEYCODE_BUTTON_Y, "KEYCODE_BUTTON_Y");
        names.append(KEYCODE_BUTTON_Z, "KEYCODE_BUTTON_Z");
        names.append(KEYCODE_BUTTON_L1, "KEYCODE_BUTTON_L1");
        names.append(KEYCODE_BUTTON_R1, "KEYCODE_BUTTON_R1");
        names.append(KEYCODE_BUTTON_L2, "KEYCODE_BUTTON_L2");
        names.append(KEYCODE_BUTTON_R2, "KEYCODE_BUTTON_R2");
        names.append(KEYCODE_BUTTON_THUMBL, "KEYCODE_BUTTON_THUMBL");
        names.append(KEYCODE_BUTTON_THUMBR, "KEYCODE_BUTTON_THUMBR");
        names.append(KEYCODE_BUTTON_START, "KEYCODE_BUTTON_START");
        names.append(KEYCODE_BUTTON_SELECT, "KEYCODE_BUTTON_SELECT");
        names.append(KEYCODE_BUTTON_MODE, "KEYCODE_BUTTON_MODE");
        names.append(KEYCODE_ESCAPE, "KEYCODE_ESCAPE");
        names.append(KEYCODE_FORWARD_DEL, "KEYCODE_FORWARD_DEL");
        names.append(KEYCODE_CTRL_LEFT, "KEYCODE_CTRL_LEFT");
        names.append(KEYCODE_CTRL_RIGHT, "KEYCODE_CTRL_RIGHT");
        names.append(KEYCODE_CAPS_LOCK, "KEYCODE_CAPS_LOCK");
        names.append(KEYCODE_SCROLL_LOCK, "KEYCODE_SCROLL_LOCK");
        names.append(KEYCODE_META_LEFT, "KEYCODE_META_LEFT");
        names.append(KEYCODE_META_RIGHT, "KEYCODE_META_RIGHT");
        names.append(KEYCODE_FUNCTION, "KEYCODE_FUNCTION");
        names.append(KEYCODE_SYSRQ, "KEYCODE_SYSRQ");
        names.append(KEYCODE_BREAK, "KEYCODE_BREAK");
        names.append(KEYCODE_MOVE_HOME, "KEYCODE_MOVE_HOME");
        names.append(KEYCODE_MOVE_END, "KEYCODE_MOVE_END");
        names.append(KEYCODE_INSERT, "KEYCODE_INSERT");
        names.append(KEYCODE_FORWARD, "KEYCODE_FORWARD");
        names.append(KEYCODE_MEDIA_PLAY, "KEYCODE_MEDIA_PLAY");
        names.append(KEYCODE_MEDIA_PAUSE, "KEYCODE_MEDIA_PAUSE");
        names.append(KEYCODE_MEDIA_CLOSE, "KEYCODE_MEDIA_CLOSE");
        names.append(KEYCODE_MEDIA_EJECT, "KEYCODE_MEDIA_EJECT");
        names.append(KEYCODE_MEDIA_RECORD, "KEYCODE_MEDIA_RECORD");
        names.append(KEYCODE_F1, "KEYCODE_F1");
        names.append(KEYCODE_F2, "KEYCODE_F2");
        names.append(KEYCODE_F3, "KEYCODE_F3");
        names.append(KEYCODE_F4, "KEYCODE_F4");
        names.append(KEYCODE_F5, "KEYCODE_F5");
        names.append(KEYCODE_F6, "KEYCODE_F6");
        names.append(KEYCODE_F7, "KEYCODE_F7");
        names.append(KEYCODE_F8, "KEYCODE_F8");
        names.append(KEYCODE_F9, "KEYCODE_F9");
        names.append(KEYCODE_F10, "KEYCODE_F10");
        names.append(KEYCODE_F11, "KEYCODE_F11");
        names.append(KEYCODE_F12, "KEYCODE_F12");
        names.append(KEYCODE_NUM_LOCK, "KEYCODE_NUM_LOCK");
        names.append(KEYCODE_NUMPAD_0, "KEYCODE_NUMPAD_0");
        names.append(KEYCODE_NUMPAD_1, "KEYCODE_NUMPAD_1");
        names.append(KEYCODE_NUMPAD_2, "KEYCODE_NUMPAD_2");
        names.append(KEYCODE_NUMPAD_3, "KEYCODE_NUMPAD_3");
        names.append(KEYCODE_NUMPAD_4, "KEYCODE_NUMPAD_4");
        names.append(KEYCODE_NUMPAD_5, "KEYCODE_NUMPAD_5");
        names.append(KEYCODE_NUMPAD_6, "KEYCODE_NUMPAD_6");
        names.append(KEYCODE_NUMPAD_7, "KEYCODE_NUMPAD_7");
        names.append(KEYCODE_NUMPAD_8, "KEYCODE_NUMPAD_8");
        names.append(KEYCODE_NUMPAD_9, "KEYCODE_NUMPAD_9");
        names.append(KEYCODE_NUMPAD_DIVIDE, "KEYCODE_NUMPAD_DIVIDE");
        names.append(KEYCODE_NUMPAD_MULTIPLY, "KEYCODE_NUMPAD_MULTIPLY");
        names.append(KEYCODE_NUMPAD_SUBTRACT, "KEYCODE_NUMPAD_SUBTRACT");
        names.append(KEYCODE_NUMPAD_ADD, "KEYCODE_NUMPAD_ADD");
        names.append(KEYCODE_NUMPAD_DOT, "KEYCODE_NUMPAD_DOT");
        names.append(KEYCODE_NUMPAD_COMMA, "KEYCODE_NUMPAD_COMMA");
        names.append(KEYCODE_NUMPAD_ENTER, "KEYCODE_NUMPAD_ENTER");
        names.append(KEYCODE_NUMPAD_EQUALS, "KEYCODE_NUMPAD_EQUALS");
        names.append(KEYCODE_NUMPAD_LEFT_PAREN, "KEYCODE_NUMPAD_LEFT_PAREN");
        names.append(KEYCODE_NUMPAD_RIGHT_PAREN, "KEYCODE_NUMPAD_RIGHT_PAREN");
        names.append(KEYCODE_VOLUME_MUTE, "KEYCODE_VOLUME_MUTE");
        names.append(KEYCODE_INFO, "KEYCODE_INFO");
        names.append(KEYCODE_CHANNEL_UP, "KEYCODE_CHANNEL_UP");
        names.append(KEYCODE_CHANNEL_DOWN, "KEYCODE_CHANNEL_DOWN");
        names.append(KEYCODE_ZOOM_IN, "KEYCODE_ZOOM_IN");
        names.append(KEYCODE_ZOOM_OUT, "KEYCODE_ZOOM_OUT");
        names.append(KEYCODE_TV, "KEYCODE_TV");
        names.append(KEYCODE_WINDOW, "KEYCODE_WINDOW");
        names.append(KEYCODE_GUIDE, "KEYCODE_GUIDE");
        names.append(KEYCODE_DVR, "KEYCODE_DVR");
        names.append(KEYCODE_BOOKMARK, "KEYCODE_BOOKMARK");
        names.append(KEYCODE_CAPTIONS, "KEYCODE_CAPTIONS");
        names.append(KEYCODE_SETTINGS, "KEYCODE_SETTINGS");
        names.append(KEYCODE_TV_POWER, "KEYCODE_TV_POWER");
        names.append(KEYCODE_TV_INPUT, "KEYCODE_TV_INPUT");
        names.append(KEYCODE_STB_INPUT, "KEYCODE_STB_INPUT");
        names.append(KEYCODE_STB_POWER, "KEYCODE_STB_POWER");
        names.append(KEYCODE_AVR_POWER, "KEYCODE_AVR_POWER");
        names.append(KEYCODE_AVR_INPUT, "KEYCODE_AVR_INPUT");
        names.append(KEYCODE_PROG_RED, "KEYCODE_PROG_RED");
        names.append(KEYCODE_PROG_GREEN, "KEYCODE_PROG_GREEN");
        names.append(KEYCODE_PROG_YELLOW, "KEYCODE_PROG_YELLOW");
        names.append(KEYCODE_PROG_BLUE, "KEYCODE_PROG_BLUE");
        names.append(KEYCODE_APP_SWITCH, "KEYCODE_APP_SWITCH");
        names.append(KEYCODE_BUTTON_1, "KEYCODE_BUTTON_1");
        names.append(KEYCODE_BUTTON_2, "KEYCODE_BUTTON_2");
        names.append(KEYCODE_BUTTON_3, "KEYCODE_BUTTON_3");
        names.append(KEYCODE_BUTTON_4, "KEYCODE_BUTTON_4");
        names.append(KEYCODE_BUTTON_5, "KEYCODE_BUTTON_5");
        names.append(KEYCODE_BUTTON_6, "KEYCODE_BUTTON_6");
        names.append(KEYCODE_BUTTON_7, "KEYCODE_BUTTON_7");
        names.append(KEYCODE_BUTTON_8, "KEYCODE_BUTTON_8");
        names.append(KEYCODE_BUTTON_9, "KEYCODE_BUTTON_9");
        names.append(KEYCODE_BUTTON_10, "KEYCODE_BUTTON_10");
        names.append(KEYCODE_BUTTON_11, "KEYCODE_BUTTON_11");
        names.append(KEYCODE_BUTTON_12, "KEYCODE_BUTTON_12");
        names.append(KEYCODE_BUTTON_13, "KEYCODE_BUTTON_13");
        names.append(KEYCODE_BUTTON_14, "KEYCODE_BUTTON_14");
        names.append(KEYCODE_BUTTON_15, "KEYCODE_BUTTON_15");
        names.append(KEYCODE_BUTTON_16, "KEYCODE_BUTTON_16");
        names.append(KEYCODE_LANGUAGE_SWITCH, "KEYCODE_LANGUAGE_SWITCH");
        names.append(KEYCODE_MANNER_MODE, "KEYCODE_MANNER_MODE");
        names.append(KEYCODE_3D_MODE, "KEYCODE_3D_MODE");
        names.append(KEYCODE_CONTACTS, "KEYCODE_CONTACTS");
        names.append(KEYCODE_CALENDAR, "KEYCODE_CALENDAR");
        names.append(KEYCODE_MUSIC, "KEYCODE_MUSIC");
        names.append(KEYCODE_CALCULATOR, "KEYCODE_CALCULATOR");
        names.append(KEYCODE_ZENKAKU_HANKAKU, "KEYCODE_ZENKAKU_HANKAKU");
        names.append(KEYCODE_EISU, "KEYCODE_EISU");
        names.append(KEYCODE_MUHENKAN, "KEYCODE_MUHENKAN");
        names.append(KEYCODE_HENKAN, "KEYCODE_HENKAN");
        names.append(KEYCODE_KATAKANA_HIRAGANA, "KEYCODE_KATAKANA_HIRAGANA");
        names.append(KEYCODE_YEN, "KEYCODE_YEN");
        names.append(KEYCODE_RO, "KEYCODE_RO");
        names.append(KEYCODE_KANA, "KEYCODE_KANA");
        names.append(KEYCODE_ASSIST, "KEYCODE_ASSIST");
		names.append(KEYCODE_BRL_DOTS_7, "KEYCODE_BRL_DOTS_7");
		names.append(KEYCODE_BRL_DOTS_3, "KEYCODE_BRL_DOTS_3");
		names.append(KEYCODE_BRL_DOTS_37, "KEYCODE_BRL_DOTS_37");
		names.append(KEYCODE_BRL_DOTS_2, "KEYCODE_BRL_DOTS_2");
		names.append(KEYCODE_BRL_DOTS_27, "KEYCODE_BRL_DOTS_27");
		names.append(KEYCODE_BRL_DOTS_23, "KEYCODE_BRL_DOTS_23");
		names.append(KEYCODE_BRL_DOTS_237, "KEYCODE_BRL_DOTS_237");
		names.append(KEYCODE_BRL_DOTS_1, "KEYCODE_BRL_DOTS_1");
		names.append(KEYCODE_BRL_DOTS_17, "KEYCODE_BRL_DOTS_17");
		names.append(KEYCODE_BRL_DOTS_13, "KEYCODE_BRL_DOTS_13");
		names.append(KEYCODE_BRL_DOTS_137, "KEYCODE_BRL_DOTS_137");
		names.append(KEYCODE_BRL_DOTS_12, "KEYCODE_BRL_DOTS_12");
		names.append(KEYCODE_BRL_DOTS_127, "KEYCODE_BRL_DOTS_127");
		names.append(KEYCODE_BRL_DOTS_123, "KEYCODE_BRL_DOTS_123");
		names.append(KEYCODE_BRL_DOTS_1237, "KEYCODE_BRL_DOTS_1237");
		names.append(KEYCODE_BRL_DOTS_4, "KEYCODE_BRL_DOTS_4");
		names.append(KEYCODE_BRL_DOTS_47, "KEYCODE_BRL_DOTS_47");
		names.append(KEYCODE_BRL_DOTS_34, "KEYCODE_BRL_DOTS_34");
		names.append(KEYCODE_BRL_DOTS_347, "KEYCODE_BRL_DOTS_347");
		names.append(KEYCODE_BRL_DOTS_24, "KEYCODE_BRL_DOTS_24");
		names.append(KEYCODE_BRL_DOTS_247, "KEYCODE_BRL_DOTS_247");
		names.append(KEYCODE_BRL_DOTS_234, "KEYCODE_BRL_DOTS_234");
		names.append(KEYCODE_BRL_DOTS_2347, "KEYCODE_BRL_DOTS_2347");
		names.append(KEYCODE_BRL_DOTS_14, "KEYCODE_BRL_DOTS_14");
		names.append(KEYCODE_BRL_DOTS_147, "KEYCODE_BRL_DOTS_147");
		names.append(KEYCODE_BRL_DOTS_134, "KEYCODE_BRL_DOTS_134");
		names.append(KEYCODE_BRL_DOTS_1347, "KEYCODE_BRL_DOTS_1347");
		names.append(KEYCODE_BRL_DOTS_124, "KEYCODE_BRL_DOTS_124");
		names.append(KEYCODE_BRL_DOTS_1247, "KEYCODE_BRL_DOTS_1247");
		names.append(KEYCODE_BRL_DOTS_1234, "KEYCODE_BRL_DOTS_1234");
		names.append(KEYCODE_BRL_DOTS_12347, "KEYCODE_BRL_DOTS_12347");
		names.append(KEYCODE_BRL_DOTS_5, "KEYCODE_BRL_DOTS_5");
		names.append(KEYCODE_BRL_DOTS_57, "KEYCODE_BRL_DOTS_57");
		names.append(KEYCODE_BRL_DOTS_35, "KEYCODE_BRL_DOTS_35");
		names.append(KEYCODE_BRL_DOTS_357, "KEYCODE_BRL_DOTS_357");
		names.append(KEYCODE_BRL_DOTS_25, "KEYCODE_BRL_DOTS_25");
		names.append(KEYCODE_BRL_DOTS_257, "KEYCODE_BRL_DOTS_257");
		names.append(KEYCODE_BRL_DOTS_235, "KEYCODE_BRL_DOTS_235");
		names.append(KEYCODE_BRL_DOTS_2357, "KEYCODE_BRL_DOTS_2357");
		names.append(KEYCODE_BRL_DOTS_15, "KEYCODE_BRL_DOTS_15");
		names.append(KEYCODE_BRL_DOTS_157, "KEYCODE_BRL_DOTS_157");
		names.append(KEYCODE_BRL_DOTS_135, "KEYCODE_BRL_DOTS_135");
		names.append(KEYCODE_BRL_DOTS_1357, "KEYCODE_BRL_DOTS_1357");
		names.append(KEYCODE_BRL_DOTS_125, "KEYCODE_BRL_DOTS_125");
		names.append(KEYCODE_BRL_DOTS_1257, "KEYCODE_BRL_DOTS_1257");
		names.append(KEYCODE_BRL_DOTS_1235, "KEYCODE_BRL_DOTS_1235");
		names.append(KEYCODE_BRL_DOTS_12357, "KEYCODE_BRL_DOTS_12357");
		names.append(KEYCODE_BRL_DOTS_45, "KEYCODE_BRL_DOTS_45");
		names.append(KEYCODE_BRL_DOTS_457, "KEYCODE_BRL_DOTS_457");
		names.append(KEYCODE_BRL_DOTS_345, "KEYCODE_BRL_DOTS_345");
		names.append(KEYCODE_BRL_DOTS_3457, "KEYCODE_BRL_DOTS_3457");
		names.append(KEYCODE_BRL_DOTS_245, "KEYCODE_BRL_DOTS_245");
		names.append(KEYCODE_BRL_DOTS_2457, "KEYCODE_BRL_DOTS_2457");
		names.append(KEYCODE_BRL_DOTS_2345, "KEYCODE_BRL_DOTS_2345");
		names.append(KEYCODE_BRL_DOTS_23457, "KEYCODE_BRL_DOTS_23457");
		names.append(KEYCODE_BRL_DOTS_145, "KEYCODE_BRL_DOTS_145");
		names.append(KEYCODE_BRL_DOTS_1457, "KEYCODE_BRL_DOTS_1457");
		names.append(KEYCODE_BRL_DOTS_1345, "KEYCODE_BRL_DOTS_1345");
		names.append(KEYCODE_BRL_DOTS_13457, "KEYCODE_BRL_DOTS_13457");
		names.append(KEYCODE_BRL_DOTS_1245, "KEYCODE_BRL_DOTS_1245");
		names.append(KEYCODE_BRL_DOTS_12457, "KEYCODE_BRL_DOTS_12457");
		names.append(KEYCODE_BRL_DOTS_12345, "KEYCODE_BRL_DOTS_12345");
		names.append(KEYCODE_BRL_DOTS_123457, "KEYCODE_BRL_DOTS_123457");
		names.append(KEYCODE_BRL_DOTS_6, "KEYCODE_BRL_DOTS_6");
		names.append(KEYCODE_BRL_DOTS_67, "KEYCODE_BRL_DOTS_67");
		names.append(KEYCODE_BRL_DOTS_36, "KEYCODE_BRL_DOTS_36");
		names.append(KEYCODE_BRL_DOTS_367, "KEYCODE_BRL_DOTS_367");
		names.append(KEYCODE_BRL_DOTS_26, "KEYCODE_BRL_DOTS_26");
		names.append(KEYCODE_BRL_DOTS_267, "KEYCODE_BRL_DOTS_267");
		names.append(KEYCODE_BRL_DOTS_236, "KEYCODE_BRL_DOTS_236");
		names.append(KEYCODE_BRL_DOTS_2367, "KEYCODE_BRL_DOTS_2367");
		names.append(KEYCODE_BRL_DOTS_16, "KEYCODE_BRL_DOTS_16");
		names.append(KEYCODE_BRL_DOTS_167, "KEYCODE_BRL_DOTS_167");
		names.append(KEYCODE_BRL_DOTS_136, "KEYCODE_BRL_DOTS_136");
		names.append(KEYCODE_BRL_DOTS_1367, "KEYCODE_BRL_DOTS_1367");
		names.append(KEYCODE_BRL_DOTS_126, "KEYCODE_BRL_DOTS_126");
		names.append(KEYCODE_BRL_DOTS_1267, "KEYCODE_BRL_DOTS_1267");
		names.append(KEYCODE_BRL_DOTS_1236, "KEYCODE_BRL_DOTS_1236");
		names.append(KEYCODE_BRL_DOTS_12367, "KEYCODE_BRL_DOTS_12367");
		names.append(KEYCODE_BRL_DOTS_46, "KEYCODE_BRL_DOTS_46");
		names.append(KEYCODE_BRL_DOTS_467, "KEYCODE_BRL_DOTS_467");
		names.append(KEYCODE_BRL_DOTS_346, "KEYCODE_BRL_DOTS_346");
		names.append(KEYCODE_BRL_DOTS_3467, "KEYCODE_BRL_DOTS_3467");
		names.append(KEYCODE_BRL_DOTS_246, "KEYCODE_BRL_DOTS_246");
		names.append(KEYCODE_BRL_DOTS_2467, "KEYCODE_BRL_DOTS_2467");
		names.append(KEYCODE_BRL_DOTS_2346, "KEYCODE_BRL_DOTS_2346");
		names.append(KEYCODE_BRL_DOTS_23467, "KEYCODE_BRL_DOTS_23467");
		names.append(KEYCODE_BRL_DOTS_146, "KEYCODE_BRL_DOTS_146");
		names.append(KEYCODE_BRL_DOTS_1467, "KEYCODE_BRL_DOTS_1467");
		names.append(KEYCODE_BRL_DOTS_1346, "KEYCODE_BRL_DOTS_1346");
		names.append(KEYCODE_BRL_DOTS_13467, "KEYCODE_BRL_DOTS_13467");
		names.append(KEYCODE_BRL_DOTS_1246, "KEYCODE_BRL_DOTS_1246");
		names.append(KEYCODE_BRL_DOTS_12467, "KEYCODE_BRL_DOTS_12467");
		names.append(KEYCODE_BRL_DOTS_12346, "KEYCODE_BRL_DOTS_12346");
		names.append(KEYCODE_BRL_DOTS_123467, "KEYCODE_BRL_DOTS_123467");
		names.append(KEYCODE_BRL_DOTS_56, "KEYCODE_BRL_DOTS_56");
		names.append(KEYCODE_BRL_DOTS_567, "KEYCODE_BRL_DOTS_567");
		names.append(KEYCODE_BRL_DOTS_356, "KEYCODE_BRL_DOTS_356");
		names.append(KEYCODE_BRL_DOTS_3567, "KEYCODE_BRL_DOTS_3567");
		names.append(KEYCODE_BRL_DOTS_256, "KEYCODE_BRL_DOTS_256");
		names.append(KEYCODE_BRL_DOTS_2567, "KEYCODE_BRL_DOTS_2567");
		names.append(KEYCODE_BRL_DOTS_2356, "KEYCODE_BRL_DOTS_2356");
		names.append(KEYCODE_BRL_DOTS_23567, "KEYCODE_BRL_DOTS_23567");
		names.append(KEYCODE_BRL_DOTS_156, "KEYCODE_BRL_DOTS_156");
		names.append(KEYCODE_BRL_DOTS_1567, "KEYCODE_BRL_DOTS_1567");
		names.append(KEYCODE_BRL_DOTS_1356, "KEYCODE_BRL_DOTS_1356");
		names.append(KEYCODE_BRL_DOTS_13567, "KEYCODE_BRL_DOTS_13567");
		names.append(KEYCODE_BRL_DOTS_1256, "KEYCODE_BRL_DOTS_1256");
		names.append(KEYCODE_BRL_DOTS_12567, "KEYCODE_BRL_DOTS_12567");
		names.append(KEYCODE_BRL_DOTS_12356, "KEYCODE_BRL_DOTS_12356");
		names.append(KEYCODE_BRL_DOTS_123567, "KEYCODE_BRL_DOTS_123567");
		names.append(KEYCODE_BRL_DOTS_456, "KEYCODE_BRL_DOTS_456");
		names.append(KEYCODE_BRL_DOTS_4567, "KEYCODE_BRL_DOTS_4567");
		names.append(KEYCODE_BRL_DOTS_3456, "KEYCODE_BRL_DOTS_3456");
		names.append(KEYCODE_BRL_DOTS_34567, "KEYCODE_BRL_DOTS_34567");
		names.append(KEYCODE_BRL_DOTS_2456, "KEYCODE_BRL_DOTS_2456");
		names.append(KEYCODE_BRL_DOTS_24567, "KEYCODE_BRL_DOTS_24567");
		names.append(KEYCODE_BRL_DOTS_23456, "KEYCODE_BRL_DOTS_23456");
		names.append(KEYCODE_BRL_DOTS_234567, "KEYCODE_BRL_DOTS_234567");
		names.append(KEYCODE_BRL_DOTS_1456, "KEYCODE_BRL_DOTS_1456");
		names.append(KEYCODE_BRL_DOTS_14567, "KEYCODE_BRL_DOTS_14567");
		names.append(KEYCODE_BRL_DOTS_13456, "KEYCODE_BRL_DOTS_13456");
		names.append(KEYCODE_BRL_DOTS_134567, "KEYCODE_BRL_DOTS_134567");
		names.append(KEYCODE_BRL_DOTS_12456, "KEYCODE_BRL_DOTS_12456");
		names.append(KEYCODE_BRL_DOTS_124567, "KEYCODE_BRL_DOTS_124567");
		names.append(KEYCODE_BRL_DOTS_123456, "KEYCODE_BRL_DOTS_123456");
		names.append(KEYCODE_BRL_DOTS_1234567, "KEYCODE_BRL_DOTS_1234567");
		names.append(KEYCODE_BRL_DOTS_8, "KEYCODE_BRL_DOTS_8");
		names.append(KEYCODE_BRL_DOTS_78, "KEYCODE_BRL_DOTS_78");
		names.append(KEYCODE_BRL_DOTS_38, "KEYCODE_BRL_DOTS_38");
		names.append(KEYCODE_BRL_DOTS_378, "KEYCODE_BRL_DOTS_378");
		names.append(KEYCODE_BRL_DOTS_28, "KEYCODE_BRL_DOTS_28");
		names.append(KEYCODE_BRL_DOTS_278, "KEYCODE_BRL_DOTS_278");
		names.append(KEYCODE_BRL_DOTS_238, "KEYCODE_BRL_DOTS_238");
		names.append(KEYCODE_BRL_DOTS_2378, "KEYCODE_BRL_DOTS_2378");
		names.append(KEYCODE_BRL_DOTS_18, "KEYCODE_BRL_DOTS_18");
		names.append(KEYCODE_BRL_DOTS_178, "KEYCODE_BRL_DOTS_178");
		names.append(KEYCODE_BRL_DOTS_138, "KEYCODE_BRL_DOTS_138");
		names.append(KEYCODE_BRL_DOTS_1378, "KEYCODE_BRL_DOTS_1378");
		names.append(KEYCODE_BRL_DOTS_128, "KEYCODE_BRL_DOTS_128");
		names.append(KEYCODE_BRL_DOTS_1278, "KEYCODE_BRL_DOTS_1278");
		names.append(KEYCODE_BRL_DOTS_1238, "KEYCODE_BRL_DOTS_1238");
		names.append(KEYCODE_BRL_DOTS_12378, "KEYCODE_BRL_DOTS_12378");
		names.append(KEYCODE_BRL_DOTS_48, "KEYCODE_BRL_DOTS_48");
		names.append(KEYCODE_BRL_DOTS_478, "KEYCODE_BRL_DOTS_478");
		names.append(KEYCODE_BRL_DOTS_348, "KEYCODE_BRL_DOTS_348");
		names.append(KEYCODE_BRL_DOTS_3478, "KEYCODE_BRL_DOTS_3478");
		names.append(KEYCODE_BRL_DOTS_248, "KEYCODE_BRL_DOTS_248");
		names.append(KEYCODE_BRL_DOTS_2478, "KEYCODE_BRL_DOTS_2478");
		names.append(KEYCODE_BRL_DOTS_2348, "KEYCODE_BRL_DOTS_2348");
		names.append(KEYCODE_BRL_DOTS_23478, "KEYCODE_BRL_DOTS_23478");
		names.append(KEYCODE_BRL_DOTS_148, "KEYCODE_BRL_DOTS_148");
		names.append(KEYCODE_BRL_DOTS_1478, "KEYCODE_BRL_DOTS_1478");
		names.append(KEYCODE_BRL_DOTS_1348, "KEYCODE_BRL_DOTS_1348");
		names.append(KEYCODE_BRL_DOTS_13478, "KEYCODE_BRL_DOTS_13478");
		names.append(KEYCODE_BRL_DOTS_1248, "KEYCODE_BRL_DOTS_1248");
		names.append(KEYCODE_BRL_DOTS_12478, "KEYCODE_BRL_DOTS_12478");
		names.append(KEYCODE_BRL_DOTS_12348, "KEYCODE_BRL_DOTS_12348");
		names.append(KEYCODE_BRL_DOTS_123478, "KEYCODE_BRL_DOTS_123478");
		names.append(KEYCODE_BRL_DOTS_58, "KEYCODE_BRL_DOTS_58");
		names.append(KEYCODE_BRL_DOTS_578, "KEYCODE_BRL_DOTS_578");
		names.append(KEYCODE_BRL_DOTS_358, "KEYCODE_BRL_DOTS_358");
		names.append(KEYCODE_BRL_DOTS_3578, "KEYCODE_BRL_DOTS_3578");
		names.append(KEYCODE_BRL_DOTS_258, "KEYCODE_BRL_DOTS_258");
		names.append(KEYCODE_BRL_DOTS_2578, "KEYCODE_BRL_DOTS_2578");
		names.append(KEYCODE_BRL_DOTS_2358, "KEYCODE_BRL_DOTS_2358");
		names.append(KEYCODE_BRL_DOTS_23578, "KEYCODE_BRL_DOTS_23578");
		names.append(KEYCODE_BRL_DOTS_158, "KEYCODE_BRL_DOTS_158");
		names.append(KEYCODE_BRL_DOTS_1578, "KEYCODE_BRL_DOTS_1578");
		names.append(KEYCODE_BRL_DOTS_1358, "KEYCODE_BRL_DOTS_1358");
		names.append(KEYCODE_BRL_DOTS_13578, "KEYCODE_BRL_DOTS_13578");
		names.append(KEYCODE_BRL_DOTS_1258, "KEYCODE_BRL_DOTS_1258");
		names.append(KEYCODE_BRL_DOTS_12578, "KEYCODE_BRL_DOTS_12578");
		names.append(KEYCODE_BRL_DOTS_12358, "KEYCODE_BRL_DOTS_12358");
		names.append(KEYCODE_BRL_DOTS_123578, "KEYCODE_BRL_DOTS_123578");
		names.append(KEYCODE_BRL_DOTS_458, "KEYCODE_BRL_DOTS_458");
		names.append(KEYCODE_BRL_DOTS_4578, "KEYCODE_BRL_DOTS_4578");
		names.append(KEYCODE_BRL_DOTS_3458, "KEYCODE_BRL_DOTS_3458");
		names.append(KEYCODE_BRL_DOTS_34578, "KEYCODE_BRL_DOTS_34578");
		names.append(KEYCODE_BRL_DOTS_2458, "KEYCODE_BRL_DOTS_2458");
		names.append(KEYCODE_BRL_DOTS_24578, "KEYCODE_BRL_DOTS_24578");
		names.append(KEYCODE_BRL_DOTS_23458, "KEYCODE_BRL_DOTS_23458");
		names.append(KEYCODE_BRL_DOTS_234578, "KEYCODE_BRL_DOTS_234578");
		names.append(KEYCODE_BRL_DOTS_1458, "KEYCODE_BRL_DOTS_1458");
		names.append(KEYCODE_BRL_DOTS_14578, "KEYCODE_BRL_DOTS_14578");
		names.append(KEYCODE_BRL_DOTS_13458, "KEYCODE_BRL_DOTS_13458");
		names.append(KEYCODE_BRL_DOTS_134578, "KEYCODE_BRL_DOTS_134578");
		names.append(KEYCODE_BRL_DOTS_12458, "KEYCODE_BRL_DOTS_12458");
		names.append(KEYCODE_BRL_DOTS_124578, "KEYCODE_BRL_DOTS_124578");
		names.append(KEYCODE_BRL_DOTS_123458, "KEYCODE_BRL_DOTS_123458");
		names.append(KEYCODE_BRL_DOTS_1234578, "KEYCODE_BRL_DOTS_1234578");
		names.append(KEYCODE_BRL_DOTS_68, "KEYCODE_BRL_DOTS_68");
		names.append(KEYCODE_BRL_DOTS_678, "KEYCODE_BRL_DOTS_678");
		names.append(KEYCODE_BRL_DOTS_368, "KEYCODE_BRL_DOTS_368");
		names.append(KEYCODE_BRL_DOTS_3678, "KEYCODE_BRL_DOTS_3678");
		names.append(KEYCODE_BRL_DOTS_268, "KEYCODE_BRL_DOTS_268");
		names.append(KEYCODE_BRL_DOTS_2678, "KEYCODE_BRL_DOTS_2678");
		names.append(KEYCODE_BRL_DOTS_2368, "KEYCODE_BRL_DOTS_2368");
		names.append(KEYCODE_BRL_DOTS_23678, "KEYCODE_BRL_DOTS_23678");
		names.append(KEYCODE_BRL_DOTS_168, "KEYCODE_BRL_DOTS_168");
		names.append(KEYCODE_BRL_DOTS_1678, "KEYCODE_BRL_DOTS_1678");
		names.append(KEYCODE_BRL_DOTS_1368, "KEYCODE_BRL_DOTS_1368");
		names.append(KEYCODE_BRL_DOTS_13678, "KEYCODE_BRL_DOTS_13678");
		names.append(KEYCODE_BRL_DOTS_1268, "KEYCODE_BRL_DOTS_1268");
		names.append(KEYCODE_BRL_DOTS_12678, "KEYCODE_BRL_DOTS_12678");
		names.append(KEYCODE_BRL_DOTS_12368, "KEYCODE_BRL_DOTS_12368");
		names.append(KEYCODE_BRL_DOTS_123678, "KEYCODE_BRL_DOTS_123678");
		names.append(KEYCODE_BRL_DOTS_468, "KEYCODE_BRL_DOTS_468");
		names.append(KEYCODE_BRL_DOTS_4678, "KEYCODE_BRL_DOTS_4678");
		names.append(KEYCODE_BRL_DOTS_3468, "KEYCODE_BRL_DOTS_3468");
		names.append(KEYCODE_BRL_DOTS_34678, "KEYCODE_BRL_DOTS_34678");
		names.append(KEYCODE_BRL_DOTS_2468, "KEYCODE_BRL_DOTS_2468");
		names.append(KEYCODE_BRL_DOTS_24678, "KEYCODE_BRL_DOTS_24678");
		names.append(KEYCODE_BRL_DOTS_23468, "KEYCODE_BRL_DOTS_23468");
		names.append(KEYCODE_BRL_DOTS_234678, "KEYCODE_BRL_DOTS_234678");
		names.append(KEYCODE_BRL_DOTS_1468, "KEYCODE_BRL_DOTS_1468");
		names.append(KEYCODE_BRL_DOTS_14678, "KEYCODE_BRL_DOTS_14678");
		names.append(KEYCODE_BRL_DOTS_13468, "KEYCODE_BRL_DOTS_13468");
		names.append(KEYCODE_BRL_DOTS_134678, "KEYCODE_BRL_DOTS_134678");
		names.append(KEYCODE_BRL_DOTS_12468, "KEYCODE_BRL_DOTS_12468");
		names.append(KEYCODE_BRL_DOTS_124678, "KEYCODE_BRL_DOTS_124678");
		names.append(KEYCODE_BRL_DOTS_123468, "KEYCODE_BRL_DOTS_123468");
		names.append(KEYCODE_BRL_DOTS_1234678, "KEYCODE_BRL_DOTS_1234678");
		names.append(KEYCODE_BRL_DOTS_568, "KEYCODE_BRL_DOTS_568");
		names.append(KEYCODE_BRL_DOTS_5678, "KEYCODE_BRL_DOTS_5678");
		names.append(KEYCODE_BRL_DOTS_3568, "KEYCODE_BRL_DOTS_3568");
		names.append(KEYCODE_BRL_DOTS_35678, "KEYCODE_BRL_DOTS_35678");
		names.append(KEYCODE_BRL_DOTS_2568, "KEYCODE_BRL_DOTS_2568");
		names.append(KEYCODE_BRL_DOTS_25678, "KEYCODE_BRL_DOTS_25678");
		names.append(KEYCODE_BRL_DOTS_23568, "KEYCODE_BRL_DOTS_23568");
		names.append(KEYCODE_BRL_DOTS_235678, "KEYCODE_BRL_DOTS_235678");
		names.append(KEYCODE_BRL_DOTS_1568, "KEYCODE_BRL_DOTS_1568");
		names.append(KEYCODE_BRL_DOTS_15678, "KEYCODE_BRL_DOTS_15678");
		names.append(KEYCODE_BRL_DOTS_13568, "KEYCODE_BRL_DOTS_13568");
		names.append(KEYCODE_BRL_DOTS_135678, "KEYCODE_BRL_DOTS_135678");
		names.append(KEYCODE_BRL_DOTS_12568, "KEYCODE_BRL_DOTS_12568");
		names.append(KEYCODE_BRL_DOTS_125678, "KEYCODE_BRL_DOTS_125678");
		names.append(KEYCODE_BRL_DOTS_123568, "KEYCODE_BRL_DOTS_123568");
		names.append(KEYCODE_BRL_DOTS_1235678, "KEYCODE_BRL_DOTS_1235678");
		names.append(KEYCODE_BRL_DOTS_4568, "KEYCODE_BRL_DOTS_4568");
		names.append(KEYCODE_BRL_DOTS_45678, "KEYCODE_BRL_DOTS_45678");
		names.append(KEYCODE_BRL_DOTS_34568, "KEYCODE_BRL_DOTS_34568");
		names.append(KEYCODE_BRL_DOTS_345678, "KEYCODE_BRL_DOTS_345678");
		names.append(KEYCODE_BRL_DOTS_24568, "KEYCODE_BRL_DOTS_24568");
		names.append(KEYCODE_BRL_DOTS_245678, "KEYCODE_BRL_DOTS_245678");
		names.append(KEYCODE_BRL_DOTS_234568, "KEYCODE_BRL_DOTS_234568");
		names.append(KEYCODE_BRL_DOTS_2345678, "KEYCODE_BRL_DOTS_2345678");
		names.append(KEYCODE_BRL_DOTS_14568, "KEYCODE_BRL_DOTS_14568");
		names.append(KEYCODE_BRL_DOTS_145678, "KEYCODE_BRL_DOTS_145678");
		names.append(KEYCODE_BRL_DOTS_134568, "KEYCODE_BRL_DOTS_134568");
		names.append(KEYCODE_BRL_DOTS_1345678, "KEYCODE_BRL_DOTS_1345678");
		names.append(KEYCODE_BRL_DOTS_124568, "KEYCODE_BRL_DOTS_124568");
		names.append(KEYCODE_BRL_DOTS_1245678, "KEYCODE_BRL_DOTS_1245678");
		names.append(KEYCODE_BRL_DOTS_1234568, "KEYCODE_BRL_DOTS_1234568");
		names.append(KEYCODE_BRL_DOTS_12345678, "KEYCODE_BRL_DOTS_12345678");

		names.append(KEYCODE_BRL_CHORD_7, "KEYCODE_BRL_CHORD_7");
		names.append(KEYCODE_BRL_CHORD_3, "KEYCODE_BRL_CHORD_3");
		names.append(KEYCODE_BRL_CHORD_37, "KEYCODE_BRL_CHORD_37");
		names.append(KEYCODE_BRL_CHORD_2, "KEYCODE_BRL_CHORD_2");
		names.append(KEYCODE_BRL_CHORD_27, "KEYCODE_BRL_CHORD_27");
		names.append(KEYCODE_BRL_CHORD_23, "KEYCODE_BRL_CHORD_23");
		names.append(KEYCODE_BRL_CHORD_237, "KEYCODE_BRL_CHORD_237");
		names.append(KEYCODE_BRL_CHORD_1, "KEYCODE_BRL_CHORD_1");
		names.append(KEYCODE_BRL_CHORD_17, "KEYCODE_BRL_CHORD_17");
		names.append(KEYCODE_BRL_CHORD_13, "KEYCODE_BRL_CHORD_13");
		names.append(KEYCODE_BRL_CHORD_137, "KEYCODE_BRL_CHORD_137");
		names.append(KEYCODE_BRL_CHORD_12, "KEYCODE_BRL_CHORD_12");
		names.append(KEYCODE_BRL_CHORD_127, "KEYCODE_BRL_CHORD_127");
		names.append(KEYCODE_BRL_CHORD_123, "KEYCODE_BRL_CHORD_123");
		names.append(KEYCODE_BRL_CHORD_1237, "KEYCODE_BRL_CHORD_1237");
		names.append(KEYCODE_BRL_CHORD_4, "KEYCODE_BRL_CHORD_4");
		names.append(KEYCODE_BRL_CHORD_47, "KEYCODE_BRL_CHORD_47");
		names.append(KEYCODE_BRL_CHORD_34, "KEYCODE_BRL_CHORD_34");
		names.append(KEYCODE_BRL_CHORD_347, "KEYCODE_BRL_CHORD_347");
		names.append(KEYCODE_BRL_CHORD_24, "KEYCODE_BRL_CHORD_24");
		names.append(KEYCODE_BRL_CHORD_247, "KEYCODE_BRL_CHORD_247");
		names.append(KEYCODE_BRL_CHORD_234, "KEYCODE_BRL_CHORD_234");
		names.append(KEYCODE_BRL_CHORD_2347, "KEYCODE_BRL_CHORD_2347");
		names.append(KEYCODE_BRL_CHORD_14, "KEYCODE_BRL_CHORD_14");
		names.append(KEYCODE_BRL_CHORD_147, "KEYCODE_BRL_CHORD_147");
		names.append(KEYCODE_BRL_CHORD_134, "KEYCODE_BRL_CHORD_134");
		names.append(KEYCODE_BRL_CHORD_1347, "KEYCODE_BRL_CHORD_1347");
		names.append(KEYCODE_BRL_CHORD_124, "KEYCODE_BRL_CHORD_124");
		names.append(KEYCODE_BRL_CHORD_1247, "KEYCODE_BRL_CHORD_1247");
		names.append(KEYCODE_BRL_CHORD_1234, "KEYCODE_BRL_CHORD_1234");
		names.append(KEYCODE_BRL_CHORD_12347, "KEYCODE_BRL_CHORD_12347");
		names.append(KEYCODE_BRL_CHORD_5, "KEYCODE_BRL_CHORD_5");
		names.append(KEYCODE_BRL_CHORD_57, "KEYCODE_BRL_CHORD_57");
		names.append(KEYCODE_BRL_CHORD_35, "KEYCODE_BRL_CHORD_35");
		names.append(KEYCODE_BRL_CHORD_357, "KEYCODE_BRL_CHORD_357");
		names.append(KEYCODE_BRL_CHORD_25, "KEYCODE_BRL_CHORD_25");
		names.append(KEYCODE_BRL_CHORD_257, "KEYCODE_BRL_CHORD_257");
		names.append(KEYCODE_BRL_CHORD_235, "KEYCODE_BRL_CHORD_235");
		names.append(KEYCODE_BRL_CHORD_2357, "KEYCODE_BRL_CHORD_2357");
		names.append(KEYCODE_BRL_CHORD_15, "KEYCODE_BRL_CHORD_15");
		names.append(KEYCODE_BRL_CHORD_157, "KEYCODE_BRL_CHORD_157");
		names.append(KEYCODE_BRL_CHORD_135, "KEYCODE_BRL_CHORD_135");
		names.append(KEYCODE_BRL_CHORD_1357, "KEYCODE_BRL_CHORD_1357");
		names.append(KEYCODE_BRL_CHORD_125, "KEYCODE_BRL_CHORD_125");
		names.append(KEYCODE_BRL_CHORD_1257, "KEYCODE_BRL_CHORD_1257");
		names.append(KEYCODE_BRL_CHORD_1235, "KEYCODE_BRL_CHORD_1235");
		names.append(KEYCODE_BRL_CHORD_12357, "KEYCODE_BRL_CHORD_12357");
		names.append(KEYCODE_BRL_CHORD_45, "KEYCODE_BRL_CHORD_45");
		names.append(KEYCODE_BRL_CHORD_457, "KEYCODE_BRL_CHORD_457");
		names.append(KEYCODE_BRL_CHORD_345, "KEYCODE_BRL_CHORD_345");
		names.append(KEYCODE_BRL_CHORD_3457, "KEYCODE_BRL_CHORD_3457");
		names.append(KEYCODE_BRL_CHORD_245, "KEYCODE_BRL_CHORD_245");
		names.append(KEYCODE_BRL_CHORD_2457, "KEYCODE_BRL_CHORD_2457");
		names.append(KEYCODE_BRL_CHORD_2345, "KEYCODE_BRL_CHORD_2345");
		names.append(KEYCODE_BRL_CHORD_23457, "KEYCODE_BRL_CHORD_23457");
		names.append(KEYCODE_BRL_CHORD_145, "KEYCODE_BRL_CHORD_145");
		names.append(KEYCODE_BRL_CHORD_1457, "KEYCODE_BRL_CHORD_1457");
		names.append(KEYCODE_BRL_CHORD_1345, "KEYCODE_BRL_CHORD_1345");
		names.append(KEYCODE_BRL_CHORD_13457, "KEYCODE_BRL_CHORD_13457");
		names.append(KEYCODE_BRL_CHORD_1245, "KEYCODE_BRL_CHORD_1245");
		names.append(KEYCODE_BRL_CHORD_12457, "KEYCODE_BRL_CHORD_12457");
		names.append(KEYCODE_BRL_CHORD_12345, "KEYCODE_BRL_CHORD_12345");
		names.append(KEYCODE_BRL_CHORD_123457, "KEYCODE_BRL_CHORD_123457");
		names.append(KEYCODE_BRL_CHORD_6, "KEYCODE_BRL_CHORD_6");
		names.append(KEYCODE_BRL_CHORD_67, "KEYCODE_BRL_CHORD_67");
		names.append(KEYCODE_BRL_CHORD_36, "KEYCODE_BRL_CHORD_36");
		names.append(KEYCODE_BRL_CHORD_367, "KEYCODE_BRL_CHORD_367");
		names.append(KEYCODE_BRL_CHORD_26, "KEYCODE_BRL_CHORD_26");
		names.append(KEYCODE_BRL_CHORD_267, "KEYCODE_BRL_CHORD_267");
		names.append(KEYCODE_BRL_CHORD_236, "KEYCODE_BRL_CHORD_236");
		names.append(KEYCODE_BRL_CHORD_2367, "KEYCODE_BRL_CHORD_2367");
		names.append(KEYCODE_BRL_CHORD_16, "KEYCODE_BRL_CHORD_16");
		names.append(KEYCODE_BRL_CHORD_167, "KEYCODE_BRL_CHORD_167");
		names.append(KEYCODE_BRL_CHORD_136, "KEYCODE_BRL_CHORD_136");
		names.append(KEYCODE_BRL_CHORD_1367, "KEYCODE_BRL_CHORD_1367");
		names.append(KEYCODE_BRL_CHORD_126, "KEYCODE_BRL_CHORD_126");
		names.append(KEYCODE_BRL_CHORD_1267, "KEYCODE_BRL_CHORD_1267");
		names.append(KEYCODE_BRL_CHORD_1236, "KEYCODE_BRL_CHORD_1236");
		names.append(KEYCODE_BRL_CHORD_12367, "KEYCODE_BRL_CHORD_12367");
		names.append(KEYCODE_BRL_CHORD_46, "KEYCODE_BRL_CHORD_46");
		names.append(KEYCODE_BRL_CHORD_467, "KEYCODE_BRL_CHORD_467");
		names.append(KEYCODE_BRL_CHORD_346, "KEYCODE_BRL_CHORD_346");
		names.append(KEYCODE_BRL_CHORD_3467, "KEYCODE_BRL_CHORD_3467");
		names.append(KEYCODE_BRL_CHORD_246, "KEYCODE_BRL_CHORD_246");
		names.append(KEYCODE_BRL_CHORD_2467, "KEYCODE_BRL_CHORD_2467");
		names.append(KEYCODE_BRL_CHORD_2346, "KEYCODE_BRL_CHORD_2346");
		names.append(KEYCODE_BRL_CHORD_23467, "KEYCODE_BRL_CHORD_23467");
		names.append(KEYCODE_BRL_CHORD_146, "KEYCODE_BRL_CHORD_146");
		names.append(KEYCODE_BRL_CHORD_1467, "KEYCODE_BRL_CHORD_1467");
		names.append(KEYCODE_BRL_CHORD_1346, "KEYCODE_BRL_CHORD_1346");
		names.append(KEYCODE_BRL_CHORD_13467, "KEYCODE_BRL_CHORD_13467");
		names.append(KEYCODE_BRL_CHORD_1246, "KEYCODE_BRL_CHORD_1246");
		names.append(KEYCODE_BRL_CHORD_12467, "KEYCODE_BRL_CHORD_12467");
		names.append(KEYCODE_BRL_CHORD_12346, "KEYCODE_BRL_CHORD_12346");
		names.append(KEYCODE_BRL_CHORD_123467, "KEYCODE_BRL_CHORD_123467");
		names.append(KEYCODE_BRL_CHORD_56, "KEYCODE_BRL_CHORD_56");
		names.append(KEYCODE_BRL_CHORD_567, "KEYCODE_BRL_CHORD_567");
		names.append(KEYCODE_BRL_CHORD_356, "KEYCODE_BRL_CHORD_356");
		names.append(KEYCODE_BRL_CHORD_3567, "KEYCODE_BRL_CHORD_3567");
		names.append(KEYCODE_BRL_CHORD_256, "KEYCODE_BRL_CHORD_256");
		names.append(KEYCODE_BRL_CHORD_2567, "KEYCODE_BRL_CHORD_2567");
		names.append(KEYCODE_BRL_CHORD_2356, "KEYCODE_BRL_CHORD_2356");
		names.append(KEYCODE_BRL_CHORD_23567, "KEYCODE_BRL_CHORD_23567");
		names.append(KEYCODE_BRL_CHORD_156, "KEYCODE_BRL_CHORD_156");
		names.append(KEYCODE_BRL_CHORD_1567, "KEYCODE_BRL_CHORD_1567");
		names.append(KEYCODE_BRL_CHORD_1356, "KEYCODE_BRL_CHORD_1356");
		names.append(KEYCODE_BRL_CHORD_13567, "KEYCODE_BRL_CHORD_13567");
		names.append(KEYCODE_BRL_CHORD_1256, "KEYCODE_BRL_CHORD_1256");
		names.append(KEYCODE_BRL_CHORD_12567, "KEYCODE_BRL_CHORD_12567");
		names.append(KEYCODE_BRL_CHORD_12356, "KEYCODE_BRL_CHORD_12356");
		names.append(KEYCODE_BRL_CHORD_123567, "KEYCODE_BRL_CHORD_123567");
		names.append(KEYCODE_BRL_CHORD_456, "KEYCODE_BRL_CHORD_456");
		names.append(KEYCODE_BRL_CHORD_4567, "KEYCODE_BRL_CHORD_4567");
		names.append(KEYCODE_BRL_CHORD_3456, "KEYCODE_BRL_CHORD_3456");
		names.append(KEYCODE_BRL_CHORD_34567, "KEYCODE_BRL_CHORD_34567");
		names.append(KEYCODE_BRL_CHORD_2456, "KEYCODE_BRL_CHORD_2456");
		names.append(KEYCODE_BRL_CHORD_24567, "KEYCODE_BRL_CHORD_24567");
		names.append(KEYCODE_BRL_CHORD_23456, "KEYCODE_BRL_CHORD_23456");
		names.append(KEYCODE_BRL_CHORD_234567, "KEYCODE_BRL_CHORD_234567");
		names.append(KEYCODE_BRL_CHORD_1456, "KEYCODE_BRL_CHORD_1456");
		names.append(KEYCODE_BRL_CHORD_14567, "KEYCODE_BRL_CHORD_14567");
		names.append(KEYCODE_BRL_CHORD_13456, "KEYCODE_BRL_CHORD_13456");
		names.append(KEYCODE_BRL_CHORD_134567, "KEYCODE_BRL_CHORD_134567");
		names.append(KEYCODE_BRL_CHORD_12456, "KEYCODE_BRL_CHORD_12456");
		names.append(KEYCODE_BRL_CHORD_124567, "KEYCODE_BRL_CHORD_124567");
		names.append(KEYCODE_BRL_CHORD_123456, "KEYCODE_BRL_CHORD_123456");
		names.append(KEYCODE_BRL_CHORD_1234567, "KEYCODE_BRL_CHORD_1234567");
		names.append(KEYCODE_BRL_CHORD_8, "KEYCODE_BRL_CHORD_8");
		names.append(KEYCODE_BRL_CHORD_78, "KEYCODE_BRL_CHORD_78");
		names.append(KEYCODE_BRL_CHORD_38, "KEYCODE_BRL_CHORD_38");
		names.append(KEYCODE_BRL_CHORD_378, "KEYCODE_BRL_CHORD_378");
		names.append(KEYCODE_BRL_CHORD_28, "KEYCODE_BRL_CHORD_28");
		names.append(KEYCODE_BRL_CHORD_278, "KEYCODE_BRL_CHORD_278");
		names.append(KEYCODE_BRL_CHORD_238, "KEYCODE_BRL_CHORD_238");
		names.append(KEYCODE_BRL_CHORD_2378, "KEYCODE_BRL_CHORD_2378");
		names.append(KEYCODE_BRL_CHORD_18, "KEYCODE_BRL_CHORD_18");
		names.append(KEYCODE_BRL_CHORD_178, "KEYCODE_BRL_CHORD_178");
		names.append(KEYCODE_BRL_CHORD_138, "KEYCODE_BRL_CHORD_138");
		names.append(KEYCODE_BRL_CHORD_1378, "KEYCODE_BRL_CHORD_1378");
		names.append(KEYCODE_BRL_CHORD_128, "KEYCODE_BRL_CHORD_128");
		names.append(KEYCODE_BRL_CHORD_1278, "KEYCODE_BRL_CHORD_1278");
		names.append(KEYCODE_BRL_CHORD_1238, "KEYCODE_BRL_CHORD_1238");
		names.append(KEYCODE_BRL_CHORD_12378, "KEYCODE_BRL_CHORD_12378");
		names.append(KEYCODE_BRL_CHORD_48, "KEYCODE_BRL_CHORD_48");
		names.append(KEYCODE_BRL_CHORD_478, "KEYCODE_BRL_CHORD_478");
		names.append(KEYCODE_BRL_CHORD_348, "KEYCODE_BRL_CHORD_348");
		names.append(KEYCODE_BRL_CHORD_3478, "KEYCODE_BRL_CHORD_3478");
		names.append(KEYCODE_BRL_CHORD_248, "KEYCODE_BRL_CHORD_248");
		names.append(KEYCODE_BRL_CHORD_2478, "KEYCODE_BRL_CHORD_2478");
		names.append(KEYCODE_BRL_CHORD_2348, "KEYCODE_BRL_CHORD_2348");
		names.append(KEYCODE_BRL_CHORD_23478, "KEYCODE_BRL_CHORD_23478");
		names.append(KEYCODE_BRL_CHORD_148, "KEYCODE_BRL_CHORD_148");
		names.append(KEYCODE_BRL_CHORD_1478, "KEYCODE_BRL_CHORD_1478");
		names.append(KEYCODE_BRL_CHORD_1348, "KEYCODE_BRL_CHORD_1348");
		names.append(KEYCODE_BRL_CHORD_13478, "KEYCODE_BRL_CHORD_13478");
		names.append(KEYCODE_BRL_CHORD_1248, "KEYCODE_BRL_CHORD_1248");
		names.append(KEYCODE_BRL_CHORD_12478, "KEYCODE_BRL_CHORD_12478");
		names.append(KEYCODE_BRL_CHORD_12348, "KEYCODE_BRL_CHORD_12348");
		names.append(KEYCODE_BRL_CHORD_123478, "KEYCODE_BRL_CHORD_123478");
		names.append(KEYCODE_BRL_CHORD_58, "KEYCODE_BRL_CHORD_58");
		names.append(KEYCODE_BRL_CHORD_578, "KEYCODE_BRL_CHORD_578");
		names.append(KEYCODE_BRL_CHORD_358, "KEYCODE_BRL_CHORD_358");
		names.append(KEYCODE_BRL_CHORD_3578, "KEYCODE_BRL_CHORD_3578");
		names.append(KEYCODE_BRL_CHORD_258, "KEYCODE_BRL_CHORD_258");
		names.append(KEYCODE_BRL_CHORD_2578, "KEYCODE_BRL_CHORD_2578");
		names.append(KEYCODE_BRL_CHORD_2358, "KEYCODE_BRL_CHORD_2358");
		names.append(KEYCODE_BRL_CHORD_23578, "KEYCODE_BRL_CHORD_23578");
		names.append(KEYCODE_BRL_CHORD_158, "KEYCODE_BRL_CHORD_158");
		names.append(KEYCODE_BRL_CHORD_1578, "KEYCODE_BRL_CHORD_1578");
		names.append(KEYCODE_BRL_CHORD_1358, "KEYCODE_BRL_CHORD_1358");
		names.append(KEYCODE_BRL_CHORD_13578, "KEYCODE_BRL_CHORD_13578");
		names.append(KEYCODE_BRL_CHORD_1258, "KEYCODE_BRL_CHORD_1258");
		names.append(KEYCODE_BRL_CHORD_12578, "KEYCODE_BRL_CHORD_12578");
		names.append(KEYCODE_BRL_CHORD_12358, "KEYCODE_BRL_CHORD_12358");
		names.append(KEYCODE_BRL_CHORD_123578, "KEYCODE_BRL_CHORD_123578");
		names.append(KEYCODE_BRL_CHORD_458, "KEYCODE_BRL_CHORD_458");
		names.append(KEYCODE_BRL_CHORD_4578, "KEYCODE_BRL_CHORD_4578");
		names.append(KEYCODE_BRL_CHORD_3458, "KEYCODE_BRL_CHORD_3458");
		names.append(KEYCODE_BRL_CHORD_34578, "KEYCODE_BRL_CHORD_34578");
		names.append(KEYCODE_BRL_CHORD_2458, "KEYCODE_BRL_CHORD_2458");
		names.append(KEYCODE_BRL_CHORD_24578, "KEYCODE_BRL_CHORD_24578");
		names.append(KEYCODE_BRL_CHORD_23458, "KEYCODE_BRL_CHORD_23458");
		names.append(KEYCODE_BRL_CHORD_234578, "KEYCODE_BRL_CHORD_234578");
		names.append(KEYCODE_BRL_CHORD_1458, "KEYCODE_BRL_CHORD_1458");
		names.append(KEYCODE_BRL_CHORD_14578, "KEYCODE_BRL_CHORD_14578");
		names.append(KEYCODE_BRL_CHORD_13458, "KEYCODE_BRL_CHORD_13458");
		names.append(KEYCODE_BRL_CHORD_134578, "KEYCODE_BRL_CHORD_134578");
		names.append(KEYCODE_BRL_CHORD_12458, "KEYCODE_BRL_CHORD_12458");
		names.append(KEYCODE_BRL_CHORD_124578, "KEYCODE_BRL_CHORD_124578");
		names.append(KEYCODE_BRL_CHORD_123458, "KEYCODE_BRL_CHORD_123458");
		names.append(KEYCODE_BRL_CHORD_1234578, "KEYCODE_BRL_CHORD_1234578");
		names.append(KEYCODE_BRL_CHORD_68, "KEYCODE_BRL_CHORD_68");
		names.append(KEYCODE_BRL_CHORD_678, "KEYCODE_BRL_CHORD_678");
		names.append(KEYCODE_BRL_CHORD_368, "KEYCODE_BRL_CHORD_368");
		names.append(KEYCODE_BRL_CHORD_3678, "KEYCODE_BRL_CHORD_3678");
		names.append(KEYCODE_BRL_CHORD_268, "KEYCODE_BRL_CHORD_268");
		names.append(KEYCODE_BRL_CHORD_2678, "KEYCODE_BRL_CHORD_2678");
		names.append(KEYCODE_BRL_CHORD_2368, "KEYCODE_BRL_CHORD_2368");
		names.append(KEYCODE_BRL_CHORD_23678, "KEYCODE_BRL_CHORD_23678");
		names.append(KEYCODE_BRL_CHORD_168, "KEYCODE_BRL_CHORD_168");
		names.append(KEYCODE_BRL_CHORD_1678, "KEYCODE_BRL_CHORD_1678");
		names.append(KEYCODE_BRL_CHORD_1368, "KEYCODE_BRL_CHORD_1368");
		names.append(KEYCODE_BRL_CHORD_13678, "KEYCODE_BRL_CHORD_13678");
		names.append(KEYCODE_BRL_CHORD_1268, "KEYCODE_BRL_CHORD_1268");
		names.append(KEYCODE_BRL_CHORD_12678, "KEYCODE_BRL_CHORD_12678");
		names.append(KEYCODE_BRL_CHORD_12368, "KEYCODE_BRL_CHORD_12368");
		names.append(KEYCODE_BRL_CHORD_123678, "KEYCODE_BRL_CHORD_123678");
		names.append(KEYCODE_BRL_CHORD_468, "KEYCODE_BRL_CHORD_468");
		names.append(KEYCODE_BRL_CHORD_4678, "KEYCODE_BRL_CHORD_4678");
		names.append(KEYCODE_BRL_CHORD_3468, "KEYCODE_BRL_CHORD_3468");
		names.append(KEYCODE_BRL_CHORD_34678, "KEYCODE_BRL_CHORD_34678");
		names.append(KEYCODE_BRL_CHORD_2468, "KEYCODE_BRL_CHORD_2468");
		names.append(KEYCODE_BRL_CHORD_24678, "KEYCODE_BRL_CHORD_24678");
		names.append(KEYCODE_BRL_CHORD_23468, "KEYCODE_BRL_CHORD_23468");
		names.append(KEYCODE_BRL_CHORD_234678, "KEYCODE_BRL_CHORD_234678");
		names.append(KEYCODE_BRL_CHORD_1468, "KEYCODE_BRL_CHORD_1468");
		names.append(KEYCODE_BRL_CHORD_14678, "KEYCODE_BRL_CHORD_14678");
		names.append(KEYCODE_BRL_CHORD_13468, "KEYCODE_BRL_CHORD_13468");
		names.append(KEYCODE_BRL_CHORD_134678, "KEYCODE_BRL_CHORD_134678");
		names.append(KEYCODE_BRL_CHORD_12468, "KEYCODE_BRL_CHORD_12468");
		names.append(KEYCODE_BRL_CHORD_124678, "KEYCODE_BRL_CHORD_124678");
		names.append(KEYCODE_BRL_CHORD_123468, "KEYCODE_BRL_CHORD_123468");
		names.append(KEYCODE_BRL_CHORD_1234678, "KEYCODE_BRL_CHORD_1234678");
		names.append(KEYCODE_BRL_CHORD_568, "KEYCODE_BRL_CHORD_568");
		names.append(KEYCODE_BRL_CHORD_5678, "KEYCODE_BRL_CHORD_5678");
		names.append(KEYCODE_BRL_CHORD_3568, "KEYCODE_BRL_CHORD_3568");
		names.append(KEYCODE_BRL_CHORD_35678, "KEYCODE_BRL_CHORD_35678");
		names.append(KEYCODE_BRL_CHORD_2568, "KEYCODE_BRL_CHORD_2568");
		names.append(KEYCODE_BRL_CHORD_25678, "KEYCODE_BRL_CHORD_25678");
		names.append(KEYCODE_BRL_CHORD_23568, "KEYCODE_BRL_CHORD_23568");
		names.append(KEYCODE_BRL_CHORD_235678, "KEYCODE_BRL_CHORD_235678");
		names.append(KEYCODE_BRL_CHORD_1568, "KEYCODE_BRL_CHORD_1568");
		names.append(KEYCODE_BRL_CHORD_15678, "KEYCODE_BRL_CHORD_15678");
		names.append(KEYCODE_BRL_CHORD_13568, "KEYCODE_BRL_CHORD_13568");
		names.append(KEYCODE_BRL_CHORD_135678, "KEYCODE_BRL_CHORD_135678");
		names.append(KEYCODE_BRL_CHORD_12568, "KEYCODE_BRL_CHORD_12568");
		names.append(KEYCODE_BRL_CHORD_125678, "KEYCODE_BRL_CHORD_125678");
		names.append(KEYCODE_BRL_CHORD_123568, "KEYCODE_BRL_CHORD_123568");
		names.append(KEYCODE_BRL_CHORD_1235678, "KEYCODE_BRL_CHORD_1235678");
		names.append(KEYCODE_BRL_CHORD_4568, "KEYCODE_BRL_CHORD_4568");
		names.append(KEYCODE_BRL_CHORD_45678, "KEYCODE_BRL_CHORD_45678");
		names.append(KEYCODE_BRL_CHORD_34568, "KEYCODE_BRL_CHORD_34568");
		names.append(KEYCODE_BRL_CHORD_345678, "KEYCODE_BRL_CHORD_345678");
		names.append(KEYCODE_BRL_CHORD_24568, "KEYCODE_BRL_CHORD_24568");
		names.append(KEYCODE_BRL_CHORD_245678, "KEYCODE_BRL_CHORD_245678");
		names.append(KEYCODE_BRL_CHORD_234568, "KEYCODE_BRL_CHORD_234568");
		names.append(KEYCODE_BRL_CHORD_2345678, "KEYCODE_BRL_CHORD_2345678");
		names.append(KEYCODE_BRL_CHORD_14568, "KEYCODE_BRL_CHORD_14568");
		names.append(KEYCODE_BRL_CHORD_145678, "KEYCODE_BRL_CHORD_145678");
		names.append(KEYCODE_BRL_CHORD_134568, "KEYCODE_BRL_CHORD_134568");
		names.append(KEYCODE_BRL_CHORD_1345678, "KEYCODE_BRL_CHORD_1345678");
		names.append(KEYCODE_BRL_CHORD_124568, "KEYCODE_BRL_CHORD_124568");
		names.append(KEYCODE_BRL_CHORD_1245678, "KEYCODE_BRL_CHORD_1245678");
		names.append(KEYCODE_BRL_CHORD_1234568, "KEYCODE_BRL_CHORD_1234568");
		names.append(KEYCODE_BRL_CHORD_12345678, "KEYCODE_BRL_CHORD_12345678");
		
		/*{KW} Individual Braille keys */
		names.append(KEYCODE_BRL_DOT_7, "KEYCODE_BRL_DOT_7");
		names.append(KEYCODE_BRL_DOT_3, "KEYCODE_BRL_DOT_3");
		names.append(KEYCODE_BRL_DOT_2, "KEYCODE_BRL_DOT_2");
		names.append(KEYCODE_BRL_DOT_1, "KEYCODE_BRL_DOT_1");
		names.append(KEYCODE_BRL_DOT_4, "KEYCODE_BRL_DOT_4");
		names.append(KEYCODE_BRL_DOT_5, "KEYCODE_BRL_DOT_5");
		names.append(KEYCODE_BRL_DOT_6, "KEYCODE_BRL_DOT_6");
		names.append(KEYCODE_BRL_DOT_8, "KEYCODE_BRL_DOT_8");
		names.append(KEYCODE_BRL_SPACE, "KEYCODE_BRL_SPACE");
		/*{KW} Cursor Routing keys block 1*/
		names.append(KEYCODE_BRL_CURSOR_0, "KEYCODE_BRL_CURSOR_0");
		names.append(KEYCODE_BRL_CURSOR_1, "KEYCODE_BRL_CURSOR_1");
		names.append(KEYCODE_BRL_CURSOR_2, "KEYCODE_BRL_CURSOR_2");
		names.append(KEYCODE_BRL_CURSOR_3, "KEYCODE_BRL_CURSOR_3");
		names.append(KEYCODE_BRL_CURSOR_4, "KEYCODE_BRL_CURSOR_4");
		names.append(KEYCODE_BRL_CURSOR_5, "KEYCODE_BRL_CURSOR_5");
		names.append(KEYCODE_BRL_CURSOR_6, "KEYCODE_BRL_CURSOR_6");
		names.append(KEYCODE_BRL_CURSOR_7, "KEYCODE_BRL_CURSOR_7");
		names.append(KEYCODE_BRL_CURSOR_8, "KEYCODE_BRL_CURSOR_8");
		names.append(KEYCODE_BRL_CURSOR_9, "KEYCODE_BRL_CURSOR_9");
		names.append(KEYCODE_BRL_CURSOR_10, "KEYCODE_BRL_CURSOR_10");
		names.append(KEYCODE_BRL_CURSOR_11, "KEYCODE_BRL_CURSOR_11");
		names.append(KEYCODE_BRL_CURSOR_12, "KEYCODE_BRL_CURSOR_12");
		names.append(KEYCODE_BRL_CURSOR_13, "KEYCODE_BRL_CURSOR_13");
		names.append(KEYCODE_BRL_CURSOR_14, "KEYCODE_BRL_CURSOR_14");
		names.append(KEYCODE_BRL_CURSOR_15, "KEYCODE_BRL_CURSOR_15");
		names.append(KEYCODE_BRL_CURSOR_16, "KEYCODE_BRL_CURSOR_16");
		names.append(KEYCODE_BRL_CURSOR_17, "KEYCODE_BRL_CURSOR_17");
		names.append(KEYCODE_BRL_CURSOR_18, "KEYCODE_BRL_CURSOR_18");
		names.append(KEYCODE_BRL_CURSOR_19, "KEYCODE_BRL_CURSOR_19");
		/*{KW} Cursor Routing keys block 2*/
		names.append(KEYCODE_BRL_CURSOR_20, "KEYCODE_BRL_CURSOR_20");
		names.append(KEYCODE_BRL_CURSOR_21, "KEYCODE_BRL_CURSOR_21");
		names.append(KEYCODE_BRL_CURSOR_22, "KEYCODE_BRL_CURSOR_22");
		names.append(KEYCODE_BRL_CURSOR_23, "KEYCODE_BRL_CURSOR_23");
		names.append(KEYCODE_BRL_CURSOR_24, "KEYCODE_BRL_CURSOR_24");
		names.append(KEYCODE_BRL_CURSOR_25, "KEYCODE_BRL_CURSOR_25");
		names.append(KEYCODE_BRL_CURSOR_26, "KEYCODE_BRL_CURSOR_26");
		names.append(KEYCODE_BRL_CURSOR_27, "KEYCODE_BRL_CURSOR_27");
		names.append(KEYCODE_BRL_CURSOR_28, "KEYCODE_BRL_CURSOR_28");
		names.append(KEYCODE_BRL_CURSOR_29, "KEYCODE_BRL_CURSOR_29");
		names.append(KEYCODE_BRL_CURSOR_30, "KEYCODE_BRL_CURSOR_30");
		names.append(KEYCODE_BRL_CURSOR_31, "KEYCODE_BRL_CURSOR_31");
		names.append(KEYCODE_BRL_CURSOR_32, "KEYCODE_BRL_CURSOR_32");
		names.append(KEYCODE_BRL_CURSOR_33, "KEYCODE_BRL_CURSOR_33");
		names.append(KEYCODE_BRL_CURSOR_34, "KEYCODE_BRL_CURSOR_34");
		names.append(KEYCODE_BRL_CURSOR_35, "KEYCODE_BRL_CURSOR_35");
		names.append(KEYCODE_BRL_CURSOR_36, "KEYCODE_BRL_CURSOR_36");
		names.append(KEYCODE_BRL_CURSOR_37, "KEYCODE_BRL_CURSOR_37");
		names.append(KEYCODE_BRL_CURSOR_38, "KEYCODE_BRL_CURSOR_38");
		names.append(KEYCODE_BRL_CURSOR_39, "KEYCODE_BRL_CURSOR_39");
		/*{KW} Backward, Forward keys*/
		names.append(KEYCODE_BRL_BACKWARD, "KEYCODE_BRL_BACKWARD");
		names.append(KEYCODE_BRL_FORWARD, "KEYCODE_BRL_FORWARD");
		/*{KW} end*/
		/*{KW} Unmapped key code for wakeup from suspend*/
		names.append(KEYCODE_DUMMY_WAKEUP, "KEYCODE_DUMMY_WAKEUP");
        /*{KW} end*/

    };

    // Symbolic names of all metakeys in bit order from least significant to most significant.
    // Accordingly there are exactly 32 values in this table.
    private static final String[] META_SYMBOLIC_NAMES = new String[] {
        "META_SHIFT_ON",
        "META_ALT_ON",
        "META_SYM_ON",
        "META_FUNCTION_ON",
        "META_ALT_LEFT_ON",
        "META_ALT_RIGHT_ON",
        "META_SHIFT_LEFT_ON",
        "META_SHIFT_RIGHT_ON",
        "META_CAP_LOCKED",
        "META_ALT_LOCKED",
        "META_SYM_LOCKED",
        "0x00000800",
        "META_CTRL_ON",
        "META_CTRL_LEFT_ON",
        "META_CTRL_RIGHT_ON",
        "0x00008000",
        "META_META_ON",
        "META_META_LEFT_ON",
        "META_META_RIGHT_ON",
        "0x00080000",
        "META_CAPS_LOCK_ON",
        "META_NUM_LOCK_ON",
        "META_SCROLL_LOCK_ON",
        "0x00800000",
        "0x01000000",
        "0x02000000",
        "0x04000000",
        "0x08000000",
        "0x10000000",
        "0x20000000",
        "0x40000000",
        "0x80000000",
    };

    /**
     * @deprecated There are now more than MAX_KEYCODE keycodes.
     * Use {@link #getMaxKeyCode()} instead.
     */
    @Deprecated
    public static final int MAX_KEYCODE             = 84;

    /**
     * {@link #getAction} value: the key has been pressed down.
     */
    public static final int ACTION_DOWN             = 0;
    /**
     * {@link #getAction} value: the key has been released.
     */
    public static final int ACTION_UP               = 1;
    /**
     * {@link #getAction} value: multiple duplicate key events have
     * occurred in a row, or a complex string is being delivered.  If the
     * key code is not {#link {@link #KEYCODE_UNKNOWN} then the
     * {#link {@link #getRepeatCount()} method returns the number of times
     * the given key code should be executed.
     * Otherwise, if the key code is {@link #KEYCODE_UNKNOWN}, then
     * this is a sequence of characters as returned by {@link #getCharacters}.
     */
    public static final int ACTION_MULTIPLE         = 2;

    /**
     * SHIFT key locked in CAPS mode.
     * Reserved for use by {@link MetaKeyKeyListener} for a published constant in its API.
     * @hide
     */
    public static final int META_CAP_LOCKED = 0x100;

    /**
     * ALT key locked.
     * Reserved for use by {@link MetaKeyKeyListener} for a published constant in its API.
     * @hide
     */
    public static final int META_ALT_LOCKED = 0x200;

    /**
     * SYM key locked.
     * Reserved for use by {@link MetaKeyKeyListener} for a published constant in its API.
     * @hide
     */
    public static final int META_SYM_LOCKED = 0x400;

    /**
     * Text is in selection mode.
     * Reserved for use by {@link MetaKeyKeyListener} for a private unpublished constant
     * in its API that is currently being retained for legacy reasons.
     * @hide
     */
    public static final int META_SELECTING = 0x800;

    /**
     * <p>This mask is used to check whether one of the ALT meta keys is pressed.</p>
     *
     * @see #isAltPressed()
     * @see #getMetaState()
     * @see #KEYCODE_ALT_LEFT
     * @see #KEYCODE_ALT_RIGHT
     */
    public static final int META_ALT_ON = 0x02;

    /**
     * <p>This mask is used to check whether the left ALT meta key is pressed.</p>
     *
     * @see #isAltPressed()
     * @see #getMetaState()
     * @see #KEYCODE_ALT_LEFT
     */
    public static final int META_ALT_LEFT_ON = 0x10;

    /**
     * <p>This mask is used to check whether the right the ALT meta key is pressed.</p>
     *
     * @see #isAltPressed()
     * @see #getMetaState()
     * @see #KEYCODE_ALT_RIGHT
     */
    public static final int META_ALT_RIGHT_ON = 0x20;

    /**
     * <p>This mask is used to check whether one of the SHIFT meta keys is pressed.</p>
     *
     * @see #isShiftPressed()
     * @see #getMetaState()
     * @see #KEYCODE_SHIFT_LEFT
     * @see #KEYCODE_SHIFT_RIGHT
     */
    public static final int META_SHIFT_ON = 0x1;

    /**
     * <p>This mask is used to check whether the left SHIFT meta key is pressed.</p>
     *
     * @see #isShiftPressed()
     * @see #getMetaState()
     * @see #KEYCODE_SHIFT_LEFT
     */
    public static final int META_SHIFT_LEFT_ON = 0x40;

    /**
     * <p>This mask is used to check whether the right SHIFT meta key is pressed.</p>
     *
     * @see #isShiftPressed()
     * @see #getMetaState()
     * @see #KEYCODE_SHIFT_RIGHT
     */
    public static final int META_SHIFT_RIGHT_ON = 0x80;

    /**
     * <p>This mask is used to check whether the SYM meta key is pressed.</p>
     *
     * @see #isSymPressed()
     * @see #getMetaState()
     */
    public static final int META_SYM_ON = 0x4;

    /**
     * <p>This mask is used to check whether the FUNCTION meta key is pressed.</p>
     *
     * @see #isFunctionPressed()
     * @see #getMetaState()
     */
    public static final int META_FUNCTION_ON = 0x8;

    /**
     * <p>This mask is used to check whether one of the CTRL meta keys is pressed.</p>
     *
     * @see #isCtrlPressed()
     * @see #getMetaState()
     * @see #KEYCODE_CTRL_LEFT
     * @see #KEYCODE_CTRL_RIGHT
     */
    public static final int META_CTRL_ON = 0x1000;

    /**
     * <p>This mask is used to check whether the left CTRL meta key is pressed.</p>
     *
     * @see #isCtrlPressed()
     * @see #getMetaState()
     * @see #KEYCODE_CTRL_LEFT
     */
    public static final int META_CTRL_LEFT_ON = 0x2000;

    /**
     * <p>This mask is used to check whether the right CTRL meta key is pressed.</p>
     *
     * @see #isCtrlPressed()
     * @see #getMetaState()
     * @see #KEYCODE_CTRL_RIGHT
     */
    public static final int META_CTRL_RIGHT_ON = 0x4000;

    /**
     * <p>This mask is used to check whether one of the META meta keys is pressed.</p>
     *
     * @see #isMetaPressed()
     * @see #getMetaState()
     * @see #KEYCODE_META_LEFT
     * @see #KEYCODE_META_RIGHT
     */
    public static final int META_META_ON = 0x10000;

    /**
     * <p>This mask is used to check whether the left META meta key is pressed.</p>
     *
     * @see #isMetaPressed()
     * @see #getMetaState()
     * @see #KEYCODE_META_LEFT
     */
    public static final int META_META_LEFT_ON = 0x20000;

    /**
     * <p>This mask is used to check whether the right META meta key is pressed.</p>
     *
     * @see #isMetaPressed()
     * @see #getMetaState()
     * @see #KEYCODE_META_RIGHT
     */
    public static final int META_META_RIGHT_ON = 0x40000;

    /**
     * <p>This mask is used to check whether the CAPS LOCK meta key is on.</p>
     *
     * @see #isCapsLockOn()
     * @see #getMetaState()
     * @see #KEYCODE_CAPS_LOCK
     */
    public static final int META_CAPS_LOCK_ON = 0x100000;

    /**
     * <p>This mask is used to check whether the NUM LOCK meta key is on.</p>
     *
     * @see #isNumLockOn()
     * @see #getMetaState()
     * @see #KEYCODE_NUM_LOCK
     */
    public static final int META_NUM_LOCK_ON = 0x200000;

    /**
     * <p>This mask is used to check whether the SCROLL LOCK meta key is on.</p>
     *
     * @see #isScrollLockOn()
     * @see #getMetaState()
     * @see #KEYCODE_SCROLL_LOCK
     */
    public static final int META_SCROLL_LOCK_ON = 0x400000;

    /**
     * This mask is a combination of {@link #META_SHIFT_ON}, {@link #META_SHIFT_LEFT_ON}
     * and {@link #META_SHIFT_RIGHT_ON}.
     */
    public static final int META_SHIFT_MASK = META_SHIFT_ON
            | META_SHIFT_LEFT_ON | META_SHIFT_RIGHT_ON;

    /**
     * This mask is a combination of {@link #META_ALT_ON}, {@link #META_ALT_LEFT_ON}
     * and {@link #META_ALT_RIGHT_ON}.
     */
    public static final int META_ALT_MASK = META_ALT_ON
            | META_ALT_LEFT_ON | META_ALT_RIGHT_ON;

    /**
     * This mask is a combination of {@link #META_CTRL_ON}, {@link #META_CTRL_LEFT_ON}
     * and {@link #META_CTRL_RIGHT_ON}.
     */
    public static final int META_CTRL_MASK = META_CTRL_ON
            | META_CTRL_LEFT_ON | META_CTRL_RIGHT_ON;

    /**
     * This mask is a combination of {@link #META_META_ON}, {@link #META_META_LEFT_ON}
     * and {@link #META_META_RIGHT_ON}.
     */
    public static final int META_META_MASK = META_META_ON
            | META_META_LEFT_ON | META_META_RIGHT_ON;

    /**
     * This mask is set if the device woke because of this key event.
     */
    public static final int FLAG_WOKE_HERE = 0x1;
    
    /**
     * This mask is set if the key event was generated by a software keyboard.
     */
    public static final int FLAG_SOFT_KEYBOARD = 0x2;
    
    /**
     * This mask is set if we don't want the key event to cause us to leave
     * touch mode.
     */
    public static final int FLAG_KEEP_TOUCH_MODE = 0x4;
    
    /**
     * This mask is set if an event was known to come from a trusted part
     * of the system.  That is, the event is known to come from the user,
     * and could not have been spoofed by a third party component.
     */
    public static final int FLAG_FROM_SYSTEM = 0x8;
    
    /**
     * This mask is used for compatibility, to identify enter keys that are
     * coming from an IME whose enter key has been auto-labelled "next" or
     * "done".  This allows TextView to dispatch these as normal enter keys
     * for old applications, but still do the appropriate action when
     * receiving them.
     */
    public static final int FLAG_EDITOR_ACTION = 0x10;
    
    /**
     * When associated with up key events, this indicates that the key press
     * has been canceled.  Typically this is used with virtual touch screen
     * keys, where the user can slide from the virtual key area on to the
     * display: in that case, the application will receive a canceled up
     * event and should not perform the action normally associated with the
     * key.  Note that for this to work, the application can not perform an
     * action for a key until it receives an up or the long press timeout has
     * expired. 
     */
    public static final int FLAG_CANCELED = 0x20;
    
    /**
     * This key event was generated by a virtual (on-screen) hard key area.
     * Typically this is an area of the touchscreen, outside of the regular
     * display, dedicated to "hardware" buttons.
     */
    public static final int FLAG_VIRTUAL_HARD_KEY = 0x40;
    
    /**
     * This flag is set for the first key repeat that occurs after the
     * long press timeout.
     */
    public static final int FLAG_LONG_PRESS = 0x80;
    
    /**
     * Set when a key event has {@link #FLAG_CANCELED} set because a long
     * press action was executed while it was down. 
     */
    public static final int FLAG_CANCELED_LONG_PRESS = 0x100;
    
    /**
     * Set for {@link #ACTION_UP} when this event's key code is still being
     * tracked from its initial down.  That is, somebody requested that tracking
     * started on the key down and a long press has not caused
     * the tracking to be canceled.
     */
    public static final int FLAG_TRACKING = 0x200;

    /**
     * Set when a key event has been synthesized to implement default behavior
     * for an event that the application did not handle.
     * Fallback key events are generated by unhandled trackball motions
     * (to emulate a directional keypad) and by certain unhandled key presses
     * that are declared in the key map (such as special function numeric keypad
     * keys when numlock is off).
     */
    public static final int FLAG_FALLBACK = 0x400;

    /**
     * Private control to determine when an app is tracking a key sequence.
     * @hide
     */
    public static final int FLAG_START_TRACKING = 0x40000000;

    /**
     * Private flag that indicates when the system has detected that this key event
     * may be inconsistent with respect to the sequence of previously delivered key events,
     * such as when a key up event is sent but the key was not down.
     *
     * @hide
     * @see #isTainted
     * @see #setTainted
     */
    public static final int FLAG_TAINTED = 0x80000000;

    /**
     * Returns the maximum keycode.
     */
    public static int getMaxKeyCode() {
        return LAST_KEYCODE;
    }

    /**
     * Get the character that is produced by putting accent on the character
     * c.
     * For example, getDeadChar('`', 'e') returns &egrave;.
     */
    public static int getDeadChar(int accent, int c) {
        return KeyCharacterMap.getDeadChar(accent, c);
    }
    
    static final boolean DEBUG = true;
    static final String TAG = "KeyEvent";

    private static final int MAX_RECYCLED = 10;
    private static final Object gRecyclerLock = new Object();
    private static int gRecyclerUsed;
    private static KeyEvent gRecyclerTop;

    private KeyEvent mNext;

    private int mDeviceId;
    private int mSource;
    private int mMetaState;
    private int mAction;
    private int mKeyCode;
    private int mScanCode;
    private int mRepeatCount;
    private int mFlags;
    private long mDownTime;
    private long mEventTime;
    private String mCharacters;

    public interface Callback {
        /**
         * Called when a key down event has occurred.  If you return true,
         * you can first call {@link KeyEvent#startTracking()
         * KeyEvent.startTracking()} to have the framework track the event
         * through its {@link #onKeyUp(int, KeyEvent)} and also call your
         * {@link #onKeyLongPress(int, KeyEvent)} if it occurs.
         * 
         * @param keyCode The value in event.getKeyCode().
         * @param event Description of the key event.
         * 
         * @return If you handled the event, return true.  If you want to allow
         *         the event to be handled by the next receiver, return false.
         */
        boolean onKeyDown(int keyCode, KeyEvent event);

        /**
         * Called when a long press has occurred.  If you return true,
         * the final key up will have {@link KeyEvent#FLAG_CANCELED} and
         * {@link KeyEvent#FLAG_CANCELED_LONG_PRESS} set.  Note that in
         * order to receive this callback, someone in the event change
         * <em>must</em> return true from {@link #onKeyDown} <em>and</em>
         * call {@link KeyEvent#startTracking()} on the event.
         * 
         * @param keyCode The value in event.getKeyCode().
         * @param event Description of the key event.
         * 
         * @return If you handled the event, return true.  If you want to allow
         *         the event to be handled by the next receiver, return false.
         */
        boolean onKeyLongPress(int keyCode, KeyEvent event);

        /**
         * Called when a key up event has occurred.
         * 
         * @param keyCode The value in event.getKeyCode().
         * @param event Description of the key event.
         * 
         * @return If you handled the event, return true.  If you want to allow
         *         the event to be handled by the next receiver, return false.
         */
        boolean onKeyUp(int keyCode, KeyEvent event);

        /**
         * Called when multiple down/up pairs of the same key have occurred
         * in a row.
         * 
         * @param keyCode The value in event.getKeyCode().
         * @param count Number of pairs as returned by event.getRepeatCount().
         * @param event Description of the key event.
         * 
         * @return If you handled the event, return true.  If you want to allow
         *         the event to be handled by the next receiver, return false.
         */
        boolean onKeyMultiple(int keyCode, int count, KeyEvent event);
    }

    static {
        populateKeycodeSymbolicNames();
    }

    private KeyEvent() {
    }

    /**
     * Create a new key event.
     * 
     * @param action Action code: either {@link #ACTION_DOWN},
     * {@link #ACTION_UP}, or {@link #ACTION_MULTIPLE}.
     * @param code The key code.
     */
    public KeyEvent(int action, int code) {
        mAction = action;
        mKeyCode = code;
        mRepeatCount = 0;
        mDeviceId = KeyCharacterMap.VIRTUAL_KEYBOARD;
    }

    /**
     * Create a new key event.
     * 
     * @param downTime The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this key code originally went down.
     * @param eventTime The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this event happened.
     * @param action Action code: either {@link #ACTION_DOWN},
     * {@link #ACTION_UP}, or {@link #ACTION_MULTIPLE}.
     * @param code The key code.
     * @param repeat A repeat count for down events (> 0 if this is after the
     * initial down) or event count for multiple events.
     */
    public KeyEvent(long downTime, long eventTime, int action,
                    int code, int repeat) {
        mDownTime = downTime;
        mEventTime = eventTime;
        mAction = action;
        mKeyCode = code;
        mRepeatCount = repeat;
        mDeviceId = KeyCharacterMap.VIRTUAL_KEYBOARD;
    }

    /**
     * Create a new key event.
     * 
     * @param downTime The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this key code originally went down.
     * @param eventTime The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this event happened.
     * @param action Action code: either {@link #ACTION_DOWN},
     * {@link #ACTION_UP}, or {@link #ACTION_MULTIPLE}.
     * @param code The key code.
     * @param repeat A repeat count for down events (> 0 if this is after the
     * initial down) or event count for multiple events.
     * @param metaState Flags indicating which meta keys are currently pressed.
     */
    public KeyEvent(long downTime, long eventTime, int action,
                    int code, int repeat, int metaState) {
        mDownTime = downTime;
        mEventTime = eventTime;
        mAction = action;
        mKeyCode = code;
        mRepeatCount = repeat;
        mMetaState = metaState;
        mDeviceId = KeyCharacterMap.VIRTUAL_KEYBOARD;
    }

    /**
     * Create a new key event.
     * 
     * @param downTime The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this key code originally went down.
     * @param eventTime The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this event happened.
     * @param action Action code: either {@link #ACTION_DOWN},
     * {@link #ACTION_UP}, or {@link #ACTION_MULTIPLE}.
     * @param code The key code.
     * @param repeat A repeat count for down events (> 0 if this is after the
     * initial down) or event count for multiple events.
     * @param metaState Flags indicating which meta keys are currently pressed.
     * @param deviceId The device ID that generated the key event.
     * @param scancode Raw device scan code of the event.
     */
    public KeyEvent(long downTime, long eventTime, int action,
                    int code, int repeat, int metaState,
                    int deviceId, int scancode) {
        mDownTime = downTime;
        mEventTime = eventTime;
        mAction = action;
        mKeyCode = code;
        mRepeatCount = repeat;
        mMetaState = metaState;
        mDeviceId = deviceId;
        mScanCode = scancode;
    }

    /**
     * Create a new key event.
     * 
     * @param downTime The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this key code originally went down.
     * @param eventTime The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this event happened.
     * @param action Action code: either {@link #ACTION_DOWN},
     * {@link #ACTION_UP}, or {@link #ACTION_MULTIPLE}.
     * @param code The key code.
     * @param repeat A repeat count for down events (> 0 if this is after the
     * initial down) or event count for multiple events.
     * @param metaState Flags indicating which meta keys are currently pressed.
     * @param deviceId The device ID that generated the key event.
     * @param scancode Raw device scan code of the event.
     * @param flags The flags for this key event
     */
    public KeyEvent(long downTime, long eventTime, int action,
                    int code, int repeat, int metaState,
                    int deviceId, int scancode, int flags) {
        mDownTime = downTime;
        mEventTime = eventTime;
        mAction = action;
        mKeyCode = code;
        mRepeatCount = repeat;
        mMetaState = metaState;
        mDeviceId = deviceId;
        mScanCode = scancode;
        mFlags = flags;
    }

    /**
     * Create a new key event.
     * 
     * @param downTime The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this key code originally went down.
     * @param eventTime The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this event happened.
     * @param action Action code: either {@link #ACTION_DOWN},
     * {@link #ACTION_UP}, or {@link #ACTION_MULTIPLE}.
     * @param code The key code.
     * @param repeat A repeat count for down events (> 0 if this is after the
     * initial down) or event count for multiple events.
     * @param metaState Flags indicating which meta keys are currently pressed.
     * @param deviceId The device ID that generated the key event.
     * @param scancode Raw device scan code of the event.
     * @param flags The flags for this key event
     * @param source The input source such as {@link InputDevice#SOURCE_KEYBOARD}.
     */
    public KeyEvent(long downTime, long eventTime, int action,
                    int code, int repeat, int metaState,
                    int deviceId, int scancode, int flags, int source) {
        mDownTime = downTime;
        mEventTime = eventTime;
        mAction = action;
        mKeyCode = code;
        mRepeatCount = repeat;
        mMetaState = metaState;
        mDeviceId = deviceId;
        mScanCode = scancode;
        mFlags = flags;
        mSource = source;
    }

    /**
     * Create a new key event for a string of characters.  The key code,
     * action, repeat count and source will automatically be set to
     * {@link #KEYCODE_UNKNOWN}, {@link #ACTION_MULTIPLE}, 0, and
     * {@link InputDevice#SOURCE_KEYBOARD} for you.
     * 
     * @param time The time (in {@link android.os.SystemClock#uptimeMillis})
     * at which this event occured.
     * @param characters The string of characters.
     * @param deviceId The device ID that generated the key event.
     * @param flags The flags for this key event
     */
    public KeyEvent(long time, String characters, int deviceId, int flags) {
        mDownTime = time;
        mEventTime = time;
        mCharacters = characters;
        mAction = ACTION_MULTIPLE;
        mKeyCode = KEYCODE_UNKNOWN;
        mRepeatCount = 0;
        mDeviceId = deviceId;
        mFlags = flags;
        mSource = InputDevice.SOURCE_KEYBOARD;
    }

    /**
     * Make an exact copy of an existing key event.
     */
    public KeyEvent(KeyEvent origEvent) {
        mDownTime = origEvent.mDownTime;
        mEventTime = origEvent.mEventTime;
        mAction = origEvent.mAction;
        mKeyCode = origEvent.mKeyCode;
        mRepeatCount = origEvent.mRepeatCount;
        mMetaState = origEvent.mMetaState;
        mDeviceId = origEvent.mDeviceId;
        mSource = origEvent.mSource;
        mScanCode = origEvent.mScanCode;
        mFlags = origEvent.mFlags;
        mCharacters = origEvent.mCharacters;
    }

    /**
     * Copy an existing key event, modifying its time and repeat count.
     * 
     * @deprecated Use {@link #changeTimeRepeat(KeyEvent, long, int)}
     * instead.
     * 
     * @param origEvent The existing event to be copied.
     * @param eventTime The new event time
     * (in {@link android.os.SystemClock#uptimeMillis}) of the event.
     * @param newRepeat The new repeat count of the event.
     */
    @Deprecated
    public KeyEvent(KeyEvent origEvent, long eventTime, int newRepeat) {
        mDownTime = origEvent.mDownTime;
        mEventTime = eventTime;
        mAction = origEvent.mAction;
        mKeyCode = origEvent.mKeyCode;
        mRepeatCount = newRepeat;
        mMetaState = origEvent.mMetaState;
        mDeviceId = origEvent.mDeviceId;
        mSource = origEvent.mSource;
        mScanCode = origEvent.mScanCode;
        mFlags = origEvent.mFlags;
        mCharacters = origEvent.mCharacters;
    }

    private static KeyEvent obtain() {
        final KeyEvent ev;
        synchronized (gRecyclerLock) {
            ev = gRecyclerTop;
            if (ev == null) {
                return new KeyEvent();
            }
            gRecyclerTop = ev.mNext;
            gRecyclerUsed -= 1;
        }
        ev.mNext = null;
        ev.prepareForReuse();
        return ev;
    }

    /**
     * Obtains a (potentially recycled) key event.
     *
     * @hide
     */
    public static KeyEvent obtain(long downTime, long eventTime, int action,
                    int code, int repeat, int metaState,
                    int deviceId, int scancode, int flags, int source, String characters) {
        KeyEvent ev = obtain();
        ev.mDownTime = downTime;
        ev.mEventTime = eventTime;
        ev.mAction = action;
        ev.mKeyCode = code;
        ev.mRepeatCount = repeat;
        ev.mMetaState = metaState;
        ev.mDeviceId = deviceId;
        ev.mScanCode = scancode;
        ev.mFlags = flags;
        ev.mSource = source;
        ev.mCharacters = characters;
        return ev;
    }

    /**
     * Obtains a (potentially recycled) copy of another key event.
     *
     * @hide
     */
    public static KeyEvent obtain(KeyEvent other) {
        KeyEvent ev = obtain();
        ev.mDownTime = other.mDownTime;
        ev.mEventTime = other.mEventTime;
        ev.mAction = other.mAction;
        ev.mKeyCode = other.mKeyCode;
        ev.mRepeatCount = other.mRepeatCount;
        ev.mMetaState = other.mMetaState;
        ev.mDeviceId = other.mDeviceId;
        ev.mScanCode = other.mScanCode;
        ev.mFlags = other.mFlags;
        ev.mSource = other.mSource;
        ev.mCharacters = other.mCharacters;
        return ev;
    }

    /** @hide */
    @Override
    public KeyEvent copy() {
        return obtain(this);
    }

    /**
     * Recycles a key event.
     * Key events should only be recycled if they are owned by the system since user
     * code expects them to be essentially immutable, "tracking" notwithstanding.
     *
     * @hide
     */
    @Override
    public final void recycle() {
        super.recycle();
        mCharacters = null;

        synchronized (gRecyclerLock) {
            if (gRecyclerUsed < MAX_RECYCLED) {
                gRecyclerUsed++;
                mNext = gRecyclerTop;
                gRecyclerTop = this;
            }
        }
    }

    /** @hide */
    @Override
    public final void recycleIfNeededAfterDispatch() {
        // Do nothing.
    }

    /**
     * Create a new key event that is the same as the given one, but whose
     * event time and repeat count are replaced with the given value.
     * 
     * @param event The existing event to be copied.  This is not modified.
     * @param eventTime The new event time
     * (in {@link android.os.SystemClock#uptimeMillis}) of the event.
     * @param newRepeat The new repeat count of the event.
     */
    public static KeyEvent changeTimeRepeat(KeyEvent event, long eventTime,
            int newRepeat) {
        return new KeyEvent(event, eventTime, newRepeat);
    }
    
    /**
     * Create a new key event that is the same as the given one, but whose
     * event time and repeat count are replaced with the given value.
     * 
     * @param event The existing event to be copied.  This is not modified.
     * @param eventTime The new event time
     * (in {@link android.os.SystemClock#uptimeMillis}) of the event.
     * @param newRepeat The new repeat count of the event.
     * @param newFlags New flags for the event, replacing the entire value
     * in the original event.
     */
    public static KeyEvent changeTimeRepeat(KeyEvent event, long eventTime,
            int newRepeat, int newFlags) {
        KeyEvent ret = new KeyEvent(event);
        ret.mEventTime = eventTime;
        ret.mRepeatCount = newRepeat;
        ret.mFlags = newFlags;
        return ret;
    }
    
    /**
     * Copy an existing key event, modifying its action.
     * 
     * @param origEvent The existing event to be copied.
     * @param action The new action code of the event.
     */
    private KeyEvent(KeyEvent origEvent, int action) {
        mDownTime = origEvent.mDownTime;
        mEventTime = origEvent.mEventTime;
        mAction = action;
        mKeyCode = origEvent.mKeyCode;
        mRepeatCount = origEvent.mRepeatCount;
        mMetaState = origEvent.mMetaState;
        mDeviceId = origEvent.mDeviceId;
        mSource = origEvent.mSource;
        mScanCode = origEvent.mScanCode;
        mFlags = origEvent.mFlags;
        // Don't copy mCharacters, since one way or the other we'll lose it
        // when changing the action.
    }

    /**
     * Create a new key event that is the same as the given one, but whose
     * action is replaced with the given value.
     * 
     * @param event The existing event to be copied.  This is not modified.
     * @param action The new action code of the event.
     */
    public static KeyEvent changeAction(KeyEvent event, int action) {
        return new KeyEvent(event, action);
    }
    
    /**
     * Create a new key event that is the same as the given one, but whose
     * flags are replaced with the given value.
     * 
     * @param event The existing event to be copied.  This is not modified.
     * @param flags The new flags constant.
     */
    public static KeyEvent changeFlags(KeyEvent event, int flags) {
        event = new KeyEvent(event);
        event.mFlags = flags;
        return event;
    }

    /** @hide */
    @Override
    public final boolean isTainted() {
        return (mFlags & FLAG_TAINTED) != 0;
    }

    /** @hide */
    @Override
    public final void setTainted(boolean tainted) {
        mFlags = tainted ? mFlags | FLAG_TAINTED : mFlags & ~FLAG_TAINTED;
    }

    /**
     * Don't use in new code, instead explicitly check
     * {@link #getAction()}.
     * 
     * @return If the action is ACTION_DOWN, returns true; else false.
     *
     * @deprecated
     * @hide
     */
    @Deprecated public final boolean isDown() {
        return mAction == ACTION_DOWN;
    }

    /**
     * Is this a system key?  System keys can not be used for menu shortcuts.
     * 
     * TODO: this information should come from a table somewhere.
     * TODO: should the dpad keys be here?  arguably, because they also shouldn't be menu shortcuts
     */
    public final boolean isSystem() {
        return native_isSystemKey(mKeyCode);
    }

    /** @hide */
    public final boolean hasDefaultAction() {
        return native_hasDefaultAction(mKeyCode);
    }

    /**
     * Returns true if the specified keycode is a gamepad button.
     * @return True if the keycode is a gamepad button, such as {@link #KEYCODE_BUTTON_A}.
     */
    public static final boolean isGamepadButton(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_BUTTON_B:
            case KeyEvent.KEYCODE_BUTTON_C:
            case KeyEvent.KEYCODE_BUTTON_X:
            case KeyEvent.KEYCODE_BUTTON_Y:
            case KeyEvent.KEYCODE_BUTTON_Z:
            case KeyEvent.KEYCODE_BUTTON_L1:
            case KeyEvent.KEYCODE_BUTTON_R1:
            case KeyEvent.KEYCODE_BUTTON_L2:
            case KeyEvent.KEYCODE_BUTTON_R2:
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_BUTTON_SELECT:
            case KeyEvent.KEYCODE_BUTTON_MODE:
            case KeyEvent.KEYCODE_BUTTON_1:
            case KeyEvent.KEYCODE_BUTTON_2:
            case KeyEvent.KEYCODE_BUTTON_3:
            case KeyEvent.KEYCODE_BUTTON_4:
            case KeyEvent.KEYCODE_BUTTON_5:
            case KeyEvent.KEYCODE_BUTTON_6:
            case KeyEvent.KEYCODE_BUTTON_7:
            case KeyEvent.KEYCODE_BUTTON_8:
            case KeyEvent.KEYCODE_BUTTON_9:
            case KeyEvent.KEYCODE_BUTTON_10:
            case KeyEvent.KEYCODE_BUTTON_11:
            case KeyEvent.KEYCODE_BUTTON_12:
            case KeyEvent.KEYCODE_BUTTON_13:
            case KeyEvent.KEYCODE_BUTTON_14:
            case KeyEvent.KEYCODE_BUTTON_15:
            case KeyEvent.KEYCODE_BUTTON_16:
                return true;
            default:
                return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final int getDeviceId() {
        return mDeviceId;
    }

    /** {@inheritDoc} */
    @Override
    public final int getSource() {
        return mSource;
    }

    /** {@inheritDoc} */
    @Override
    public final void setSource(int source) {
        mSource = source;
    }

    /**
     * <p>Returns the state of the meta keys.</p>
     *
     * @return an integer in which each bit set to 1 represents a pressed
     *         meta key
     *
     * @see #isAltPressed()
     * @see #isShiftPressed()
     * @see #isSymPressed()
     * @see #isCtrlPressed()
     * @see #isMetaPressed()
     * @see #isFunctionPressed()
     * @see #isCapsLockOn()
     * @see #isNumLockOn()
     * @see #isScrollLockOn()
     * @see #META_ALT_ON
     * @see #META_ALT_LEFT_ON
     * @see #META_ALT_RIGHT_ON
     * @see #META_SHIFT_ON
     * @see #META_SHIFT_LEFT_ON
     * @see #META_SHIFT_RIGHT_ON
     * @see #META_SYM_ON
     * @see #META_FUNCTION_ON
     * @see #META_CTRL_ON
     * @see #META_CTRL_LEFT_ON
     * @see #META_CTRL_RIGHT_ON
     * @see #META_META_ON
     * @see #META_META_LEFT_ON
     * @see #META_META_RIGHT_ON
     * @see #META_CAPS_LOCK_ON
     * @see #META_NUM_LOCK_ON
     * @see #META_SCROLL_LOCK_ON
     * @see #getModifiers
     */
    public final int getMetaState() {
        return mMetaState;
    }

    /**
     * Returns the state of the modifier keys.
     * <p>
     * For the purposes of this function, {@link #KEYCODE_CAPS_LOCK},
     * {@link #KEYCODE_SCROLL_LOCK}, and {@link #KEYCODE_NUM_LOCK} are
     * not considered modifier keys.  Consequently, this function specifically masks out
     * {@link #META_CAPS_LOCK_ON}, {@link #META_SCROLL_LOCK_ON} and {@link #META_NUM_LOCK_ON}.
     * </p><p>
     * The value returned consists of the meta state (from {@link #getMetaState})
     * normalized using {@link #normalizeMetaState(int)} and then masked with
     * {@link #getModifierMetaStateMask} so that only valid modifier bits are retained.
     * </p>
     *
     * @return An integer in which each bit set to 1 represents a pressed modifier key.
     * @see #getMetaState
     */
    public final int getModifiers() {
        return normalizeMetaState(mMetaState) & META_MODIFIER_MASK;
    }

    /**
     * Returns the flags for this key event.
     *
     * @see #FLAG_WOKE_HERE
     */
    public final int getFlags() {
        return mFlags;
    }

    // Mask of all modifier key meta states.  Specifically excludes locked keys like caps lock.
    private static final int META_MODIFIER_MASK =
            META_SHIFT_ON | META_SHIFT_LEFT_ON | META_SHIFT_RIGHT_ON
            | META_ALT_ON | META_ALT_LEFT_ON | META_ALT_RIGHT_ON
            | META_CTRL_ON | META_CTRL_LEFT_ON | META_CTRL_RIGHT_ON
            | META_META_ON | META_META_LEFT_ON | META_META_RIGHT_ON
            | META_SYM_ON | META_FUNCTION_ON;

    // Mask of all lock key meta states.
    private static final int META_LOCK_MASK =
            META_CAPS_LOCK_ON | META_NUM_LOCK_ON | META_SCROLL_LOCK_ON;

    // Mask of all valid meta states.
    private static final int META_ALL_MASK = META_MODIFIER_MASK | META_LOCK_MASK;

    // Mask of all synthetic meta states that are reserved for API compatibility with
    // historical uses in MetaKeyKeyListener.
    private static final int META_SYNTHETIC_MASK =
            META_CAP_LOCKED | META_ALT_LOCKED | META_SYM_LOCKED | META_SELECTING;

    // Mask of all meta states that are not valid use in specifying a modifier key.
    // These bits are known to be used for purposes other than specifying modifiers.
    private static final int META_INVALID_MODIFIER_MASK =
            META_LOCK_MASK | META_SYNTHETIC_MASK;

    /**
     * Gets a mask that includes all valid modifier key meta state bits.
     * <p>
     * For the purposes of this function, {@link #KEYCODE_CAPS_LOCK},
     * {@link #KEYCODE_SCROLL_LOCK}, and {@link #KEYCODE_NUM_LOCK} are
     * not considered modifier keys.  Consequently, the mask specifically excludes
     * {@link #META_CAPS_LOCK_ON}, {@link #META_SCROLL_LOCK_ON} and {@link #META_NUM_LOCK_ON}.
     * </p>
     *
     * @return The modifier meta state mask which is a combination of
     * {@link #META_SHIFT_ON}, {@link #META_SHIFT_LEFT_ON}, {@link #META_SHIFT_RIGHT_ON},
     * {@link #META_ALT_ON}, {@link #META_ALT_LEFT_ON}, {@link #META_ALT_RIGHT_ON},
     * {@link #META_CTRL_ON}, {@link #META_CTRL_LEFT_ON}, {@link #META_CTRL_RIGHT_ON},
     * {@link #META_META_ON}, {@link #META_META_LEFT_ON}, {@link #META_META_RIGHT_ON},
     * {@link #META_SYM_ON}, {@link #META_FUNCTION_ON}.
     */
    public static int getModifierMetaStateMask() {
        return META_MODIFIER_MASK;
    }

    /**
     * Returns true if this key code is a modifier key.
     * <p>
     * For the purposes of this function, {@link #KEYCODE_CAPS_LOCK},
     * {@link #KEYCODE_SCROLL_LOCK}, and {@link #KEYCODE_NUM_LOCK} are
     * not considered modifier keys.  Consequently, this function return false
     * for those keys.
     * </p>
     *
     * @return True if the key code is one of
     * {@link #KEYCODE_SHIFT_LEFT} {@link #KEYCODE_SHIFT_RIGHT},
     * {@link #KEYCODE_ALT_LEFT}, {@link #KEYCODE_ALT_RIGHT},
     * {@link #KEYCODE_CTRL_LEFT}, {@link #KEYCODE_CTRL_RIGHT},
     * {@link #KEYCODE_META_LEFT}, or {@link #KEYCODE_META_RIGHT},
     * {@link #KEYCODE_SYM}, {@link #KEYCODE_NUM}, {@link #KEYCODE_FUNCTION}.
     */
    public static boolean isModifierKey(int keyCode) {
        switch (keyCode) {
            case KEYCODE_SHIFT_LEFT:
            case KEYCODE_SHIFT_RIGHT:
            case KEYCODE_ALT_LEFT:
            case KEYCODE_ALT_RIGHT:
            case KEYCODE_CTRL_LEFT:
            case KEYCODE_CTRL_RIGHT:
            case KEYCODE_META_LEFT:
            case KEYCODE_META_RIGHT:
            case KEYCODE_SYM:
            case KEYCODE_NUM:
            case KEYCODE_FUNCTION:
                return true;
            default:
                return false;
        }
    }

    /**
     * Normalizes the specified meta state.
     * <p>
     * The meta state is normalized such that if either the left or right modifier meta state
     * bits are set then the result will also include the universal bit for that modifier.
     * </p><p>
     * If the specified meta state contains {@link #META_ALT_LEFT_ON} then
     * the result will also contain {@link #META_ALT_ON} in addition to {@link #META_ALT_LEFT_ON}
     * and the other bits that were specified in the input.  The same is process is
     * performed for shift, control and meta.
     * </p><p>
     * If the specified meta state contains synthetic meta states defined by
     * {@link MetaKeyKeyListener}, then those states are translated here and the original
     * synthetic meta states are removed from the result.
     * {@link MetaKeyKeyListener#META_CAP_LOCKED} is translated to {@link #META_CAPS_LOCK_ON}.
     * {@link MetaKeyKeyListener#META_ALT_LOCKED} is translated to {@link #META_ALT_ON}.
     * {@link MetaKeyKeyListener#META_SYM_LOCKED} is translated to {@link #META_SYM_ON}.
     * </p><p>
     * Undefined meta state bits are removed.
     * </p>
     *
     * @param metaState The meta state.
     * @return The normalized meta state.
     */
    public static int normalizeMetaState(int metaState) {
        if ((metaState & (META_SHIFT_LEFT_ON | META_SHIFT_RIGHT_ON)) != 0) {
            metaState |= META_SHIFT_ON;
        }
        if ((metaState & (META_ALT_LEFT_ON | META_ALT_RIGHT_ON)) != 0) {
            metaState |= META_ALT_ON;
        }
        if ((metaState & (META_CTRL_LEFT_ON | META_CTRL_RIGHT_ON)) != 0) {
            metaState |= META_CTRL_ON;
        }
        if ((metaState & (META_META_LEFT_ON | META_META_RIGHT_ON)) != 0) {
            metaState |= META_META_ON;
        }
        if ((metaState & MetaKeyKeyListener.META_CAP_LOCKED) != 0) {
            metaState |= META_CAPS_LOCK_ON;
        }
        if ((metaState & MetaKeyKeyListener.META_ALT_LOCKED) != 0) {
            metaState |= META_ALT_ON;
        }
        if ((metaState & MetaKeyKeyListener.META_SYM_LOCKED) != 0) {
            metaState |= META_SYM_ON;
        }
        return metaState & META_ALL_MASK;
    }

    /**
     * Returns true if no modifiers keys are pressed according to the specified meta state.
     * <p>
     * For the purposes of this function, {@link #KEYCODE_CAPS_LOCK},
     * {@link #KEYCODE_SCROLL_LOCK}, and {@link #KEYCODE_NUM_LOCK} are
     * not considered modifier keys.  Consequently, this function ignores
     * {@link #META_CAPS_LOCK_ON}, {@link #META_SCROLL_LOCK_ON} and {@link #META_NUM_LOCK_ON}.
     * </p><p>
     * The meta state is normalized prior to comparison using {@link #normalizeMetaState(int)}.
     * </p>
     *
     * @param metaState The meta state to consider.
     * @return True if no modifier keys are pressed.
     * @see #hasNoModifiers()
     */
    public static boolean metaStateHasNoModifiers(int metaState) {
        return (normalizeMetaState(metaState) & META_MODIFIER_MASK) == 0;
    }

    /**
     * Returns true if only the specified modifier keys are pressed according to
     * the specified meta state.  Returns false if a different combination of modifier
     * keys are pressed.
     * <p>
     * For the purposes of this function, {@link #KEYCODE_CAPS_LOCK},
     * {@link #KEYCODE_SCROLL_LOCK}, and {@link #KEYCODE_NUM_LOCK} are
     * not considered modifier keys.  Consequently, this function ignores
     * {@link #META_CAPS_LOCK_ON}, {@link #META_SCROLL_LOCK_ON} and {@link #META_NUM_LOCK_ON}.
     * </p><p>
     * If the specified modifier mask includes directional modifiers, such as
     * {@link #META_SHIFT_LEFT_ON}, then this method ensures that the
     * modifier is pressed on that side.
     * If the specified modifier mask includes non-directional modifiers, such as
     * {@link #META_SHIFT_ON}, then this method ensures that the modifier
     * is pressed on either side.
     * If the specified modifier mask includes both directional and non-directional modifiers
     * for the same type of key, such as {@link #META_SHIFT_ON} and {@link #META_SHIFT_LEFT_ON},
     * then this method throws an illegal argument exception.
     * </p>
     *
     * @param metaState The meta state to consider.
     * @param modifiers The meta state of the modifier keys to check.  May be a combination
     * of modifier meta states as defined by {@link #getModifierMetaStateMask()}.  May be 0 to
     * ensure that no modifier keys are pressed.
     * @return True if only the specified modifier keys are pressed.
     * @throws IllegalArgumentException if the modifiers parameter contains invalid modifiers
     * @see #hasModifiers
     */
    public static boolean metaStateHasModifiers(int metaState, int modifiers) {
        // Note: For forward compatibility, we allow the parameter to contain meta states
        //       that we do not recognize but we explicitly disallow meta states that
        //       are not valid modifiers.
        if ((modifiers & META_INVALID_MODIFIER_MASK) != 0) {
            throw new IllegalArgumentException("modifiers must not contain "
                    + "META_CAPS_LOCK_ON, META_NUM_LOCK_ON, META_SCROLL_LOCK_ON, "
                    + "META_CAP_LOCKED, META_ALT_LOCKED, META_SYM_LOCKED, "
                    + "or META_SELECTING");
        }

        metaState = normalizeMetaState(metaState) & META_MODIFIER_MASK;
        metaState = metaStateFilterDirectionalModifiers(metaState, modifiers,
                META_SHIFT_ON, META_SHIFT_LEFT_ON, META_SHIFT_RIGHT_ON);
        metaState = metaStateFilterDirectionalModifiers(metaState, modifiers,
                META_ALT_ON, META_ALT_LEFT_ON, META_ALT_RIGHT_ON);
        metaState = metaStateFilterDirectionalModifiers(metaState, modifiers,
                META_CTRL_ON, META_CTRL_LEFT_ON, META_CTRL_RIGHT_ON);
        metaState = metaStateFilterDirectionalModifiers(metaState, modifiers,
                META_META_ON, META_META_LEFT_ON, META_META_RIGHT_ON);
        return metaState == modifiers;
    }

    private static int metaStateFilterDirectionalModifiers(int metaState,
            int modifiers, int basic, int left, int right) {
        final boolean wantBasic = (modifiers & basic) != 0;
        final int directional = left | right;
        final boolean wantLeftOrRight = (modifiers & directional) != 0;

        if (wantBasic) {
            if (wantLeftOrRight) {
                throw new IllegalArgumentException("modifiers must not contain "
                        + metaStateToString(basic) + " combined with "
                        + metaStateToString(left) + " or " + metaStateToString(right));
            }
            return metaState & ~directional;
        } else if (wantLeftOrRight) {
            return metaState & ~basic;
        } else {
            return metaState;
        }
    }

    /**
     * Returns true if no modifier keys are pressed.
     * <p>
     * For the purposes of this function, {@link #KEYCODE_CAPS_LOCK},
     * {@link #KEYCODE_SCROLL_LOCK}, and {@link #KEYCODE_NUM_LOCK} are
     * not considered modifier keys.  Consequently, this function ignores
     * {@link #META_CAPS_LOCK_ON}, {@link #META_SCROLL_LOCK_ON} and {@link #META_NUM_LOCK_ON}.
     * </p><p>
     * The meta state is normalized prior to comparison using {@link #normalizeMetaState(int)}.
     * </p>
     *
     * @return True if no modifier keys are pressed.
     * @see #metaStateHasNoModifiers
     */
    public final boolean hasNoModifiers() {
        return metaStateHasNoModifiers(mMetaState);
    }

    /**
     * Returns true if only the specified modifiers keys are pressed.
     * Returns false if a different combination of modifier keys are pressed.
     * <p>
     * For the purposes of this function, {@link #KEYCODE_CAPS_LOCK},
     * {@link #KEYCODE_SCROLL_LOCK}, and {@link #KEYCODE_NUM_LOCK} are
     * not considered modifier keys.  Consequently, this function ignores
     * {@link #META_CAPS_LOCK_ON}, {@link #META_SCROLL_LOCK_ON} and {@link #META_NUM_LOCK_ON}.
     * </p><p>
     * If the specified modifier mask includes directional modifiers, such as
     * {@link #META_SHIFT_LEFT_ON}, then this method ensures that the
     * modifier is pressed on that side.
     * If the specified modifier mask includes non-directional modifiers, such as
     * {@link #META_SHIFT_ON}, then this method ensures that the modifier
     * is pressed on either side.
     * If the specified modifier mask includes both directional and non-directional modifiers
     * for the same type of key, such as {@link #META_SHIFT_ON} and {@link #META_SHIFT_LEFT_ON},
     * then this method throws an illegal argument exception.
     * </p>
     *
     * @param modifiers The meta state of the modifier keys to check.  May be a combination
     * of modifier meta states as defined by {@link #getModifierMetaStateMask()}.  May be 0 to
     * ensure that no modifier keys are pressed.
     * @return True if only the specified modifier keys are pressed.
     * @throws IllegalArgumentException if the modifiers parameter contains invalid modifiers
     * @see #metaStateHasModifiers
     */
    public final boolean hasModifiers(int modifiers) {
        return metaStateHasModifiers(mMetaState, modifiers);
    }

    /**
     * <p>Returns the pressed state of the ALT meta key.</p>
     *
     * @return true if the ALT key is pressed, false otherwise
     *
     * @see #KEYCODE_ALT_LEFT
     * @see #KEYCODE_ALT_RIGHT
     * @see #META_ALT_ON
     */
    public final boolean isAltPressed() {
        return (mMetaState & META_ALT_ON) != 0;
    }

    /**
     * <p>Returns the pressed state of the SHIFT meta key.</p>
     *
     * @return true if the SHIFT key is pressed, false otherwise
     *
     * @see #KEYCODE_SHIFT_LEFT
     * @see #KEYCODE_SHIFT_RIGHT
     * @see #META_SHIFT_ON
     */
    public final boolean isShiftPressed() {
        return (mMetaState & META_SHIFT_ON) != 0;
    }

    /**
     * <p>Returns the pressed state of the SYM meta key.</p>
     *
     * @return true if the SYM key is pressed, false otherwise
     *
     * @see #KEYCODE_SYM
     * @see #META_SYM_ON
     */
    public final boolean isSymPressed() {
        return (mMetaState & META_SYM_ON) != 0;
    }

    /**
     * <p>Returns the pressed state of the CTRL meta key.</p>
     *
     * @return true if the CTRL key is pressed, false otherwise
     *
     * @see #KEYCODE_CTRL_LEFT
     * @see #KEYCODE_CTRL_RIGHT
     * @see #META_CTRL_ON
     */
    public final boolean isCtrlPressed() {
        return (mMetaState & META_CTRL_ON) != 0;
    }

    /**
     * <p>Returns the pressed state of the META meta key.</p>
     *
     * @return true if the META key is pressed, false otherwise
     *
     * @see #KEYCODE_META_LEFT
     * @see #KEYCODE_META_RIGHT
     * @see #META_META_ON
     */
    public final boolean isMetaPressed() {
        return (mMetaState & META_META_ON) != 0;
    }

    /**
     * <p>Returns the pressed state of the FUNCTION meta key.</p>
     *
     * @return true if the FUNCTION key is pressed, false otherwise
     *
     * @see #KEYCODE_FUNCTION
     * @see #META_FUNCTION_ON
     */
    public final boolean isFunctionPressed() {
        return (mMetaState & META_FUNCTION_ON) != 0;
    }

    /**
     * <p>Returns the locked state of the CAPS LOCK meta key.</p>
     *
     * @return true if the CAPS LOCK key is on, false otherwise
     *
     * @see #KEYCODE_CAPS_LOCK
     * @see #META_CAPS_LOCK_ON
     */
    public final boolean isCapsLockOn() {
        return (mMetaState & META_CAPS_LOCK_ON) != 0;
    }

    /**
     * <p>Returns the locked state of the NUM LOCK meta key.</p>
     *
     * @return true if the NUM LOCK key is on, false otherwise
     *
     * @see #KEYCODE_NUM_LOCK
     * @see #META_NUM_LOCK_ON
     */
    public final boolean isNumLockOn() {
        return (mMetaState & META_NUM_LOCK_ON) != 0;
    }

    /**
     * <p>Returns the locked state of the SCROLL LOCK meta key.</p>
     *
     * @return true if the SCROLL LOCK key is on, false otherwise
     *
     * @see #KEYCODE_SCROLL_LOCK
     * @see #META_SCROLL_LOCK_ON
     */
    public final boolean isScrollLockOn() {
        return (mMetaState & META_SCROLL_LOCK_ON) != 0;
    }

    /**
     * Retrieve the action of this key event.  May be either
     * {@link #ACTION_DOWN}, {@link #ACTION_UP}, or {@link #ACTION_MULTIPLE}.
     * 
     * @return The event action: ACTION_DOWN, ACTION_UP, or ACTION_MULTIPLE.
     */
    public final int getAction() {
        return mAction;
    }

    /**
     * For {@link #ACTION_UP} events, indicates that the event has been
     * canceled as per {@link #FLAG_CANCELED}.
     */
    public final boolean isCanceled() {
        return (mFlags&FLAG_CANCELED) != 0;
    }
    
    /**
     * Call this during {@link Callback#onKeyDown} to have the system track
     * the key through its final up (possibly including a long press).  Note
     * that only one key can be tracked at a time -- if another key down
     * event is received while a previous one is being tracked, tracking is
     * stopped on the previous event.
     */
    public final void startTracking() {
        mFlags |= FLAG_START_TRACKING;
    }
    
    /**
     * For {@link #ACTION_UP} events, indicates that the event is still being
     * tracked from its initial down event as per
     * {@link #FLAG_TRACKING}.
     */
    public final boolean isTracking() {
        return (mFlags&FLAG_TRACKING) != 0;
    }
    
    /**
     * For {@link #ACTION_DOWN} events, indicates that the event has been
     * canceled as per {@link #FLAG_LONG_PRESS}.
     */
    public final boolean isLongPress() {
        return (mFlags&FLAG_LONG_PRESS) != 0;
    }
    
    /**
     * Retrieve the key code of the key event.  This is the physical key that
     * was pressed, <em>not</em> the Unicode character.
     * 
     * @return The key code of the event.
     */
    public final int getKeyCode() {
        return mKeyCode;
    }

    /**
     * For the special case of a {@link #ACTION_MULTIPLE} event with key
     * code of {@link #KEYCODE_UNKNOWN}, this is a raw string of characters
     * associated with the event.  In all other cases it is null.
     * 
     * @return Returns a String of 1 or more characters associated with
     * the event.
     */
    public final String getCharacters() {
        return mCharacters;
    }
    
    /**
     * Retrieve the hardware key id of this key event.  These values are not
     * reliable and vary from device to device.
     *
     * {@more}
     * Mostly this is here for debugging purposes.
     */
    public final int getScanCode() {
        return mScanCode;
    }

    /**
     * Retrieve the repeat count of the event.  For both key up and key down
     * events, this is the number of times the key has repeated with the first
     * down starting at 0 and counting up from there.  For multiple key
     * events, this is the number of down/up pairs that have occurred.
     * 
     * @return The number of times the key has repeated.
     */
    public final int getRepeatCount() {
        return mRepeatCount;
    }

    /**
     * Retrieve the time of the most recent key down event,
     * in the {@link android.os.SystemClock#uptimeMillis} time base.  If this
     * is a down event, this will be the same as {@link #getEventTime()}.
     * Note that when chording keys, this value is the down time of the
     * most recently pressed key, which may <em>not</em> be the same physical
     * key of this event.
     * 
     * @return Returns the most recent key down time, in the
     * {@link android.os.SystemClock#uptimeMillis} time base
     */
    public final long getDownTime() {
        return mDownTime;
    }

    /**
     * Retrieve the time this event occurred,
     * in the {@link android.os.SystemClock#uptimeMillis} time base.
     *
     * @return Returns the time this event occurred, 
     * in the {@link android.os.SystemClock#uptimeMillis} time base.
     */
    @Override
    public final long getEventTime() {
        return mEventTime;
    }

    /**
     * Retrieve the time this event occurred,
     * in the {@link android.os.SystemClock#uptimeMillis} time base but with
     * nanosecond (instead of millisecond) precision.
     * <p>
     * The value is in nanosecond precision but it may not have nanosecond accuracy.
     * </p>
     *
     * @return Returns the time this event occurred,
     * in the {@link android.os.SystemClock#uptimeMillis} time base but with
     * nanosecond (instead of millisecond) precision.
     *
     * @hide
     */
    @Override
    public final long getEventTimeNano() {
        return mEventTime * 1000000L;
    }

    /**
     * Renamed to {@link #getDeviceId}.
     * 
     * @hide
     * @deprecated use {@link #getDeviceId()} instead.
     */
    @Deprecated
    public final int getKeyboardDevice() {
        return mDeviceId;
    }

    /**
     * Gets the {@link KeyCharacterMap} associated with the keyboard device.
     *
     * @return The associated key character map.
     * @throws {@link KeyCharacterMap.UnavailableException} if the key character map
     * could not be loaded because it was malformed or the default key character map
     * is missing from the system.
     *
     * @see KeyCharacterMap#load
     */
    public final KeyCharacterMap getKeyCharacterMap() {
        return KeyCharacterMap.load(mDeviceId);
    }

    /**
     * Gets the primary character for this key.
     * In other words, the label that is physically printed on it.
     *
     * @return The display label character, or 0 if none (eg. for non-printing keys).
     */
    public char getDisplayLabel() {
        return getKeyCharacterMap().getDisplayLabel(mKeyCode);
    }
    
    /**
     * Gets the Unicode character generated by the specified key and meta
     * key state combination.
     * <p>
     * Returns the Unicode character that the specified key would produce
     * when the specified meta bits (see {@link MetaKeyKeyListener})
     * were active.
     * </p><p>
     * Returns 0 if the key is not one that is used to type Unicode
     * characters.
     * </p><p>
     * If the return value has bit {@link KeyCharacterMap#COMBINING_ACCENT} set, the
     * key is a "dead key" that should be combined with another to
     * actually produce a character -- see {@link KeyCharacterMap#getDeadChar} --
     * after masking with {@link KeyCharacterMap#COMBINING_ACCENT_MASK}.
     * </p>
     *
     * @return The associated character or combining accent, or 0 if none.
     */
    public int getUnicodeChar() {
        return getUnicodeChar(mMetaState);
    }
    
    /**
     * Gets the Unicode character generated by the specified key and meta
     * key state combination.
     * <p>
     * Returns the Unicode character that the specified key would produce
     * when the specified meta bits (see {@link MetaKeyKeyListener})
     * were active.
     * </p><p>
     * Returns 0 if the key is not one that is used to type Unicode
     * characters.
     * </p><p>
     * If the return value has bit {@link KeyCharacterMap#COMBINING_ACCENT} set, the
     * key is a "dead key" that should be combined with another to
     * actually produce a character -- see {@link KeyCharacterMap#getDeadChar} --
     * after masking with {@link KeyCharacterMap#COMBINING_ACCENT_MASK}.
     * </p>
     *
     * @param metaState The meta key modifier state.
     * @return The associated character or combining accent, or 0 if none.
     */
    public int getUnicodeChar(int metaState) {
        return getKeyCharacterMap().get(mKeyCode, metaState);
    }
    
    /**
     * Get the character conversion data for a given key code.
     *
     * @param results A {@link KeyCharacterMap.KeyData} instance that will be
     * filled with the results.
     * @return True if the key was mapped.  If the key was not mapped, results is not modified.
     *
     * @deprecated instead use {@link #getDisplayLabel()},
     * {@link #getNumber()} or {@link #getUnicodeChar(int)}.
     */
    @Deprecated
    public boolean getKeyData(KeyData results) {
        return getKeyCharacterMap().getKeyData(mKeyCode, results);
    }
    
    /**
     * Gets the first character in the character array that can be generated
     * by the specified key code.
     * <p>
     * This is a convenience function that returns the same value as
     * {@link #getMatch(char[],int) getMatch(chars, 0)}.
     * </p>
     *
     * @param chars The array of matching characters to consider.
     * @return The matching associated character, or 0 if none.
     */
    public char getMatch(char[] chars) {
        return getMatch(chars, 0);
    }
    
    /**
     * Gets the first character in the character array that can be generated
     * by the specified key code.  If there are multiple choices, prefers
     * the one that would be generated with the specified meta key modifier state.
     *
     * @param chars The array of matching characters to consider.
     * @param metaState The preferred meta key modifier state.
     * @return The matching associated character, or 0 if none.
     */
    public char getMatch(char[] chars, int metaState) {
        return getKeyCharacterMap().getMatch(mKeyCode, chars, metaState);
    }
    
    /**
     * Gets the number or symbol associated with the key.
     * <p>
     * The character value is returned, not the numeric value.
     * If the key is not a number, but is a symbol, the symbol is retuned.
     * </p><p>
     * This method is intended to to support dial pads and other numeric or
     * symbolic entry on keyboards where certain keys serve dual function
     * as alphabetic and symbolic keys.  This method returns the number
     * or symbol associated with the key independent of whether the user
     * has pressed the required modifier.
     * </p><p>
     * For example, on one particular keyboard the keys on the top QWERTY row generate
     * numbers when ALT is pressed such that ALT-Q maps to '1'.  So for that keyboard
     * when {@link #getNumber} is called with {@link KeyEvent#KEYCODE_Q} it returns '1'
     * so that the user can type numbers without pressing ALT when it makes sense.
     * </p>
     *
     * @return The associated numeric or symbolic character, or 0 if none.
     */
    public char getNumber() {
        return getKeyCharacterMap().getNumber(mKeyCode);
    }
    
    /**
     * Returns true if this key produces a glyph.
     *
     * @return True if the key is a printing key.
     */
    public boolean isPrintingKey() {
        return getKeyCharacterMap().isPrintingKey(mKeyCode);
    }
    
    /**
     * @deprecated Use {@link #dispatch(Callback, DispatcherState, Object)} instead.
     */
    @Deprecated
    public final boolean dispatch(Callback receiver) {
        return dispatch(receiver, null, null);
    }
    
    /**
     * Deliver this key event to a {@link Callback} interface.  If this is
     * an ACTION_MULTIPLE event and it is not handled, then an attempt will
     * be made to deliver a single normal event.
     * 
     * @param receiver The Callback that will be given the event.
     * @param state State information retained across events.
     * @param target The target of the dispatch, for use in tracking.
     * 
     * @return The return value from the Callback method that was called.
     */
    public final boolean dispatch(Callback receiver, DispatcherState state,
            Object target) {
        switch (mAction) {
            case ACTION_DOWN: {
                mFlags &= ~FLAG_START_TRACKING;
                if (DEBUG) Log.v(TAG, "Key down to " + target + " in " + state
                        + ": " + this);
                boolean res = receiver.onKeyDown(mKeyCode, this);
                if (state != null) {
                    if (res && mRepeatCount == 0 && (mFlags&FLAG_START_TRACKING) != 0) {
                        if (DEBUG) Log.v(TAG, "  Start tracking!");
                        state.startTracking(this, target);
                    } else if (isLongPress() && state.isTracking(this)) {
                        try {
                            if (receiver.onKeyLongPress(mKeyCode, this)) {
                                if (DEBUG) Log.v(TAG, "  Clear from long press!");
                                state.performedLongPress(this);
                                res = true;
                            }
                        } catch (AbstractMethodError e) {
                        }
                    }
                }
                return res;
            }
            case ACTION_UP:
                if (DEBUG) Log.v(TAG, "Key up to " + target + " in " + state
                        + ": " + this);
                if (state != null) {
                    state.handleUpEvent(this);
                }
                return receiver.onKeyUp(mKeyCode, this);
            case ACTION_MULTIPLE:
                final int count = mRepeatCount;
                final int code = mKeyCode;
                if (receiver.onKeyMultiple(code, count, this)) {
                    return true;
                }
                if (code != KeyEvent.KEYCODE_UNKNOWN) {
                    mAction = ACTION_DOWN;
                    mRepeatCount = 0;
                    boolean handled = receiver.onKeyDown(code, this);
                    if (handled) {
                        mAction = ACTION_UP;
                        receiver.onKeyUp(code, this);
                    }
                    mAction = ACTION_MULTIPLE;
                    mRepeatCount = count;
                    return handled;
                }
                return false;
        }
        return false;
    }

    /**
     * Use with {@link KeyEvent#dispatch(Callback, DispatcherState, Object)}
     * for more advanced key dispatching, such as long presses.
     */
    public static class DispatcherState {
        int mDownKeyCode;
        Object mDownTarget;
        SparseIntArray mActiveLongPresses = new SparseIntArray();
        
        /**
         * Reset back to initial state.
         */
        public void reset() {
            if (DEBUG) Log.v(TAG, "Reset: " + this);
            mDownKeyCode = 0;
            mDownTarget = null;
            mActiveLongPresses.clear();
        }
        
        /**
         * Stop any tracking associated with this target.
         */
        public void reset(Object target) {
            if (mDownTarget == target) {
                if (DEBUG) Log.v(TAG, "Reset in " + target + ": " + this);
                mDownKeyCode = 0;
                mDownTarget = null;
            }
        }
        
        /**
         * Start tracking the key code associated with the given event.  This
         * can only be called on a key down.  It will allow you to see any
         * long press associated with the key, and will result in
         * {@link KeyEvent#isTracking} return true on the long press and up
         * events.
         * 
         * <p>This is only needed if you are directly dispatching events, rather
         * than handling them in {@link Callback#onKeyDown}.
         */
        public void startTracking(KeyEvent event, Object target) {
            if (event.getAction() != ACTION_DOWN) {
                throw new IllegalArgumentException(
                        "Can only start tracking on a down event");
            }
            if (DEBUG) Log.v(TAG, "Start trackingt in " + target + ": " + this);
            mDownKeyCode = event.getKeyCode();
            mDownTarget = target;
        }
        
        /**
         * Return true if the key event is for a key code that is currently
         * being tracked by the dispatcher.
         */
        public boolean isTracking(KeyEvent event) {
            return mDownKeyCode == event.getKeyCode();
        }
        
        /**
         * Keep track of the given event's key code as having performed an
         * action with a long press, so no action should occur on the up.
         * <p>This is only needed if you are directly dispatching events, rather
         * than handling them in {@link Callback#onKeyLongPress}.
         */
        public void performedLongPress(KeyEvent event) {
            mActiveLongPresses.put(event.getKeyCode(), 1);
        }
        
        /**
         * Handle key up event to stop tracking.  This resets the dispatcher state,
         * and updates the key event state based on it.
         * <p>This is only needed if you are directly dispatching events, rather
         * than handling them in {@link Callback#onKeyUp}.
         */
        public void handleUpEvent(KeyEvent event) {
            final int keyCode = event.getKeyCode();
            if (DEBUG) Log.v(TAG, "Handle key up " + event + ": " + this);
            int index = mActiveLongPresses.indexOfKey(keyCode);
            if (index >= 0) {
                if (DEBUG) Log.v(TAG, "  Index: " + index);
                event.mFlags |= FLAG_CANCELED | FLAG_CANCELED_LONG_PRESS;
                mActiveLongPresses.removeAt(index);
            }
            if (mDownKeyCode == keyCode) {
                if (DEBUG) Log.v(TAG, "  Tracking!");
                event.mFlags |= FLAG_TRACKING;
                mDownKeyCode = 0;
                mDownTarget = null;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append("KeyEvent { action=").append(actionToString(mAction));
        msg.append(", keyCode=").append(keyCodeToString(mKeyCode));
        msg.append(", scanCode=").append(mScanCode);
        if (mCharacters != null) {
            msg.append(", characters=\"").append(mCharacters).append("\"");
        }
        msg.append(", metaState=").append(metaStateToString(mMetaState));
        msg.append(", flags=0x").append(Integer.toHexString(mFlags));
        msg.append(", repeatCount=").append(mRepeatCount);
        msg.append(", eventTime=").append(mEventTime);
        msg.append(", downTime=").append(mDownTime);
        msg.append(", deviceId=").append(mDeviceId);
        msg.append(", source=0x").append(Integer.toHexString(mSource));
        msg.append(" }");
        return msg.toString();
    }

    /**
     * Returns a string that represents the symbolic name of the specified action
     * such as "ACTION_DOWN", or an equivalent numeric constant such as "35" if unknown.
     *
     * @param action The action.
     * @return The symbolic name of the specified action.
     * @hide
     */
    public static String actionToString(int action) {
        switch (action) {
            case ACTION_DOWN:
                return "ACTION_DOWN";
            case ACTION_UP:
                return "ACTION_UP";
            case ACTION_MULTIPLE:
                return "ACTION_MULTIPLE";
            default:
                return Integer.toString(action);
        }
    }

    /**
     * Returns a string that represents the symbolic name of the specified keycode
     * such as "KEYCODE_A", "KEYCODE_DPAD_UP", or an equivalent numeric constant
     * such as "1001" if unknown.
     *
     * @param keyCode The key code.
     * @return The symbolic name of the specified keycode.
     *
     * @see KeyCharacterMap#getDisplayLabel
     */
    public static String keyCodeToString(int keyCode) {
        String symbolicName = KEYCODE_SYMBOLIC_NAMES.get(keyCode);
        return symbolicName != null ? symbolicName : Integer.toString(keyCode);
    }

    /**
     * Gets a keycode by its symbolic name such as "KEYCODE_A" or an equivalent
     * numeric constant such as "1001".
     *
     * @param symbolicName The symbolic name of the keycode.
     * @return The keycode or {@link #KEYCODE_UNKNOWN} if not found.
     * @see #keycodeToString
     */
    public static int keyCodeFromString(String symbolicName) {
        if (symbolicName == null) {
            throw new IllegalArgumentException("symbolicName must not be null");
        }

        final int count = KEYCODE_SYMBOLIC_NAMES.size();
        for (int i = 0; i < count; i++) {
            if (symbolicName.equals(KEYCODE_SYMBOLIC_NAMES.valueAt(i))) {
                return i;
            }
        }

        try {
            return Integer.parseInt(symbolicName, 10);
        } catch (NumberFormatException ex) {
            return KEYCODE_UNKNOWN;
        }
    }

    /**
     * Returns a string that represents the symbolic name of the specified combined meta
     * key modifier state flags such as "0", "META_SHIFT_ON",
     * "META_ALT_ON|META_SHIFT_ON" or an equivalent numeric constant such as "0x10000000"
     * if unknown.
     *
     * @param metaState The meta state.
     * @return The symbolic name of the specified combined meta state flags.
     * @hide
     */
    public static String metaStateToString(int metaState) {
        if (metaState == 0) {
            return "0";
        }
        StringBuilder result = null;
        int i = 0;
        while (metaState != 0) {
            final boolean isSet = (metaState & 1) != 0;
            metaState >>>= 1; // unsigned shift!
            if (isSet) {
                final String name = META_SYMBOLIC_NAMES[i];
                if (result == null) {
                    if (metaState == 0) {
                        return name;
                    }
                    result = new StringBuilder(name);
                } else {
                    result.append('|');
                    result.append(name);
                }
            }
            i += 1;
        }
        return result.toString();
    }

    public static final Parcelable.Creator<KeyEvent> CREATOR
            = new Parcelable.Creator<KeyEvent>() {
        public KeyEvent createFromParcel(Parcel in) {
            in.readInt(); // skip token, we already know this is a KeyEvent
            return KeyEvent.createFromParcelBody(in);
        }

        public KeyEvent[] newArray(int size) {
            return new KeyEvent[size];
        }
    };
    
    /** @hide */
    public static KeyEvent createFromParcelBody(Parcel in) {
        return new KeyEvent(in);
    }
    
    private KeyEvent(Parcel in) {
        mDeviceId = in.readInt();
        mSource = in.readInt();
        mAction = in.readInt();
        mKeyCode = in.readInt();
        mRepeatCount = in.readInt();
        mMetaState = in.readInt();
        mScanCode = in.readInt();
        mFlags = in.readInt();
        mDownTime = in.readLong();
        mEventTime = in.readLong();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(PARCEL_TOKEN_KEY_EVENT);

        out.writeInt(mDeviceId);
        out.writeInt(mSource);
        out.writeInt(mAction);
        out.writeInt(mKeyCode);
        out.writeInt(mRepeatCount);
        out.writeInt(mMetaState);
        out.writeInt(mScanCode);
        out.writeInt(mFlags);
        out.writeLong(mDownTime);
        out.writeLong(mEventTime);
    }

    private native boolean native_isSystemKey(int keyCode);
    private native boolean native_hasDefaultAction(int keyCode);
}
