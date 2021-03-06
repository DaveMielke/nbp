Typing Characters
-----------------

Typing Modes
~~~~~~~~~~~~

Space+Dots78+t (dots 2345) displays a message (see `Messages`_) that confirms
which typing mode is currently being used.

Regular text is the default, and is used for `Directly Typing Regular Text`_.

Actual cells is used for `Directly Typing Actual Braille Cells`_.

Either booting the |product name| or waking it up (see `The Power Switch`_)
automatically resets the typing mode to regular text.

Directly Typing Regular Text
````````````````````````````

Space+Dot7+t (dots 2345) restores the typing mode to regular text (the default).
This mode honours the currently selected braille mode
(see `Braille Modes and Codes`_).

Typing in Literary Braille
''''''''''''''''''''''''''

Any character (or character sequence) that has a defined `Literary Braille`_
representation within the currently selected braille code can be typed.

In general, a cursor routing key corresponds to the first character of the
literary braille symbol that it's behind. The only exception to this rule is
when setting the end of a `text selection`_. In that case
it corresponds to the last character of the symbol.

A cursor routing key may, on occasion, seem to be corresponding to the wrong
character. This is because the rules used to define the currently selected
braille code sometimes need to group more than one symbol together. For
example, in UEB (Unified English Braille), at the time of this writing:

* There are two ways to contract "ear": either by using the "ea" contraction or
  by using the "ar" contraction. To ensure that the "ar" contraction is always
  used, the whole sequence is covered by a single rule. The cursor routing key
  behind the "ar" contraction, therefore, unexpectedly corresponds to the "e"
  rather than to the "a".

* The honourific title "Saint" is often written as "St.". In order to prevent
  the "st" contraction from being used, there's a rule that also covers the
  trailing period. When setting the end of a text selection, therefore, the
  cursor routing keys behind the "s" and the "t" unexpectedly set the end of
  the selection to the period.

Deleting characters (see `Input Areas`_) is done one text character at a time.

A special literary braille prefix, e.g. a capitalization sign, may not appear
until the first character of the symbol it applies to has been typed.

The ability to type any character by entering its Unicode value
(see `Indirectly Typing Any Character`_)
doesn't work when literary braille is being used.
If you need to do it then you'll need to (temporarily) switch to
`Computer Braille`_ mode.

Typing in Computer Braille
''''''''''''''''''''''''''

Any character that has a defined `Computer Braille`_ representation
(see `Computer Braille Characters`_) can be directly typed by pressing the
corresponding keys as a single combination. There are two exceptions to this
simple (and obvious) rule.

If the defined representation of a character is either just dot 7 or just dot 8
then it can't be typed by pressing the corresponding key because those keys
are, respectively, Backspace and Enter. Press Space+Dot7 for just dot 7, and
press Space+Dot8 for just dot 8.

Directly Typing Actual Braille Cells
````````````````````````````````````

Space+Dot8+t (dots 2345) sets the typing mode to actual cells.

In this typing mode, the characters that render in text as actual braille cells
(see `Unicode Braille Patterns`_) can be directly typed by pressing the
corresponding keys as a single combination.

Two of these characters can't be typed by pressing the corresponding keys -
just dot 7 (because it's the Backspace key), and just dot 8 (because it's the
Enter key). Press Space+Dot7 for just dot 7, and press Space+Dot8 for just dot
8.

These characters have several uses, including (but not limited to):

* Actual braille cells can be written into text documents.

* Contracted braille can be accurately saved.

* Braille music can be accurately saved, and also shared with others who use
  different localized braille character mappings.

Typing Highlighted Characters
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The following highlighting styles are supported for typing:

Bold

  * Press Space+Dot8+b (dots 12) to turn it on.
  * Press Space+Dot7+b (dots 12) to turn it off.

Italics

  * Press Space+Dot8+i (dots 24) to turn it on.
  * Press Space+Dot7+i (dots 24) to turn it off.

Strike-through

  * Press Space+Dot8+s (dots 234) to turn it on.
  * Press Space+Dot7+s (dots 234) to turn it off.

Underlining

  * Press Space+Dot8+u (dots 136) to turn it on.
  * Press Space+Dot7+u (dots 136) to turn it off.

Pressing Space+Dots78+h (dots 125) displays a pop-up (see `Pop-ups`_)
that lists the highlighting styles that are currently active.

Press Space+Dot7+h (dots 125) to turn all of these highlighting styles off.
Either booting the |product name| or waking it up (see `The Power Switch`_)
also turns them off.

Each highlighted character that you type will be flagged by
`The Selection Indicator`_. You can verify that a character has been
highlighted correctly by holding Dot3 while pressing the cursor routing key
associated with it.

Typing a Control Character
~~~~~~~~~~~~~~~~~~~~~~~~~~

In order to type a control character, press Space+x (dots 1346) immediately
before the letter or special symbol that represents it (see `ASCII Control
Characters`_). For example, in order to type a tab (which happens to be control
I), press Space+x and then immediately type the letter ``i``.

The letter or special symbol must be typed within |partial entry timeout|.

Indirectly Typing Any Character
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Any character can always be indirectly typed, even if directly typing it isn't 
possible:

* Its braille representation hasn't been defined.

* You don't know its braille representation.

* It's represented in braille by either just dot 7 (the Backspace key) or just
  dot 8 (the Enter key).

In order to indirectly type it, press Space+u (dots 136). This brings up a
prompt (see `Prompts`_) with the following header::

  Unicode> U+

``U+`` is the conventional prefix for the hexadecimal value of a Unicode 
character. The prompt allows you to enter up to four hexadecimal digits -
``0`` through ``9`` and ``A`` through ``F`` (either upper or lower case).
Leading zeroes may be omitted. The Unicode values of characters are, of course,
beyond the scope of this document.

The digits you enter may be freely edited, e.g. the cursor can be moved, any
digit may be deleted, a new digit may be inserted, etc. In other words, making
corrections doesn't require backspacing and retyping. When you're done, press
Enter.

If no digits have been entered yet (or if they've all been deleted) then you'll
only see the header. If, however, at least one digit has been entered then the
character represented (so far) by the digit(s) is displayed just to the right,
and that character's formal name is displayed on the next line.

To illustrate, let's see how a lowercase ``s`` would be indirectly typed. It's
Unicode value is U+0073. In order to keep this example simple, let's skip the
two leading zeroes.

.. topic:: Indirectly Typing the Letter ``s`` (U+0073)

  1) Press Space+u (dots 136)::

       Unicode> U+

  2) Type the digit ``7`` (dots 2356)::

       Unicode> U+7 ⣛
       bell

  3) Type the digit ``3`` (dots 25)::

       Unicode> U+73 ⠎
       latin small letter s

  4) Press Enter. The prompt goes away, and the ``s`` is typed.

Adding/Removing Diacritical Marks
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

One way to add or remove a diacritical mark (an accent) from a letter is
to retype that letter using the corrected braille representation.
This is the most natural way to do it for those who
are using an appropriate braille code and are familiar with it.
For other braille users, however,
this seemingly simple and common literary endeavour
can be rather daunting.
It may even be impossible if the braille code being used doesn't support it.

The |user interface| provides a menu-based way to add and remove diacritical marks
that can be easily used by anyone.
It modifies the character that's immediately to the left of the cursor indicator
so that the most common case,
typing a letter and then adding diacritical marks to it,
can be performed in a natural and intuitive way.
See `Supported Diacritical Marks`_ for a list of
the diacritical marks that the |user interface| supports.

To add a diacritical mark to the character, press Space+Dot8+m (dots 134).
This presents a pop-up (see `Pop-ups`_) that lists
all of the diacritical marks that may be added to the character.
Navigate to the one that you'd like to add, and press Center.
Pressing Enter will cancel the operation without modifying the character.

To remove a diacritical mark to the character, press Space+Dot7+m (dots 134).
This presents a pop-up (see `Pop-ups`_) that lists
all of the diacritical marks that are currently on the character.
Navigate to the one that you'd like to remove, and press Center.
Pressing Enter will cancel the operation without modifying the character.

There are a few ways to find out which diacritical marks are currently on a character:

* Place the cursor indicator immediately to the right of the character
  and then press Space+Dot7+m (dots 134) -
  the process (documented here) to remove a diacritical mark.
  Read the list of diacritical marks that are currently on the character,
  and then press Enter to cancel the operation.
  This method won't work if there are no diacritical marks on the character.

* Hold Dot7 while pressing the cursor routing key behind the character.
  See `Identifying an Unrecognized Character`_ for details.

* Place the cursor indicator on the character
  and then press Dots36 to have it spoken.
  See `Editing with Speech`_ for details.

Typing Emoticons
~~~~~~~~~~~~~~~~

Emoticons are sequences of basic characters
(commonly used within emails, text messages, etc)
that depict emotions.
For example:

* ``:-)`` for a smile (happy face).
* ``:-(`` for a frown (sad face).

The |user interface| offers a menu-based way to type an emoticon.
Pressing Space+e (dots 15) presents a list of emoticon descriptions
within a pop-up (see `Pop-ups`_).
Pressing Center on the desired description types its corresponding emoticon.
Spaces are added to either side of the emoticon, as needed,
to ensure that it's separated from the surrounding text.
Pressing Enter will cancel the operation without typing an emoticon.

See `Supported Emoticons`_ for the list of emoticons
that the |user interface| supports.
Some `literary braille`_ codes might not work well with some emoticons -
for example, those that don't allow for a colon at the beginning of a word.
Likewise, some speech voices may behave unexpectedly -
for example, they may:

* speak a different description
* spell out the individual characters
* say nothing at all

