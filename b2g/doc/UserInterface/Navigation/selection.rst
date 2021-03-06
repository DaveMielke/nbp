Text Selection
--------------

Each character within the current text selection is highlighted via
|the selection indicator|. |if selection| the cursor isn't shown.

Selecting text is useful in at least the following ways:

* Typing a character deletes the selected text, puts the cursor where
  the selected text was, and then inserts the typed character at that point.
  This, in other words, is an efficient way to replace old text with new text.
  Just select the old text, and then start typing the new text.

* Pressing any of the delete keys (see `Deleting Characters`_) removes all of
  the selected text. This, in other words, is an efficient way to delete a
  block of text. Just select it, and then delete it.

A quick way to select all of the characters within the input area is to press
Space+Dot8+a (dot 1).

Any subset of the characters within the input area can be selected by following
these steps:

1) If necessary, use the Forward and Backward keys to pan to a place where the
   first character to be selected can be seen.

2) Press the cursor routing key behind the character
   that's to start the selection while holding Backward.

3) If necessary, use the Forward and Backward keys to pan to a place where the
   last character to be selected can be seen.

4) Press the cursor routing key behind the character
   that's to end the selection while holding Forward.

Steps 1 and 2 may be reperformed at any time in order to change the start of
the selection. Likewise, steps 3 and 4 may be reperformed at any time in order
to change the end of the selection. In fact, the end of the selection can be
set before the start of the selection has been set, i.e. steps 3 and 4 may be
performed before steps 1 and 2 have been performed.

If the start of the selection is set first, and if the cursor is after that
character, then the selection is implicitly extended forward to (but not
including) the character where the cursor is. This, for example, provides an
easy way to delete or replace several characters that have just been typed.

If the end of the selection is set first, and if the cursor is before that
character, then the selection is implicitly extended back to (and including)
the character where the cursor is. This, for example, provides an easy way to
replace a word, line, paragraph, etc.

Alternate combinations that are usable in `One Hand Mode`_ have been defined.
They use:

* Dot1 instead of Backward to set the start of the selection
* Dot4 instead of Forward to set the end of the selection

