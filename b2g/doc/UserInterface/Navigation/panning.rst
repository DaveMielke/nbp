Navigating with the Forward and Backward Keys
---------------------------------------------

The easiest way to navigate the screen is to use the Forward and Backward 
keys because they move sequentially through all of the screen elements,
including those that merely present helpful text, without missing any of
them. The Forward key stops at the end of the screen, and the Backward key
stops at the start of the screen.

If the text of a screen element is longer than the braille display 
and/or has more than one line, then:

* The Forward key pans to the right, wrapping to the start of the next line as
  needed, such that all of the text is presented. When it reaches the end of
  the text, it moves to the start of the next screen element.

* The Backward key pans to the left, wrapping to the end of the previous line
  as needed, such that all of the text is presented. When it reaches the start
  of the text, it moves to the start of the previous screen element.

Repeatedly pressing Forward, therefore, reads through all of the text
on the screen because it reads through all of the text associated with the
current screen element before moving to the next one. Repeatedly pressing
Backward, however, pans to the left through the text associated with the
current screen element, but, from then on, moves directly to the start of each
successive preceding screen element.

Leaving the Current Screen Element
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The following methods may be used to force an immediate, direct move to the
start of the next (or previous) screen element:

* Press Space together with Forward (or Backward).

* Long press Forward (or Backward). See `Long Press Mode`_.

One of these methods must also be used in order to leave an input area (see
`Input Areas`_).

Reverse Panning Mode
~~~~~~~~~~~~~~~~~~~~

Reverse Panning Mode is designed for those who prefer to read with their right
hands while navigating with their left hands. When enabled, the Forward and
Backward keys are reversed insofar as navigation is concerned:

* Backward pans to the right, wraps to the start of the next line, moves to the
  next screen element, etc.

* Forward pans to the left, wraps to the end of the previous line, moves to the
  previous screen element, etc.
  
This mode is disabled by default. Pressing Forward+Dot5 enables it, and
pressing Backward+Dot2 disables it.

Explicit Panning
~~~~~~~~~~~~~~~~

The Forward and Backward keys pan right and left by the full length of the
braille display. This can make it difficult to read an indented block of
related text (a column, a long word, a timestamp, etc).

The braille display can be explicitly panned by holding Dot8 while pressing a
cursor routing key. This positions the braille display such that the rendered
portion of the current line starts with that character.
