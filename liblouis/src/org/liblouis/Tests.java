package org.liblouis;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.io.File;

import android.util.Log;

public abstract class Tests {
  private final static String LOG_TAG = Tests.class.getName();

  private Tests () {
  }

  private final static void log (String text) {
    Log.d(LOG_TAG, text);
  }

  private final static void log (CharSequence text) {
    log(text.toString());
  }

  public final static void run (String name, Runnable test) {
    log(("begin " + name));

    try {
      test.run();
    } finally {
      log(("end " + name));
    }
  }

  public final static void log (String label, int... values) {
    StringBuilder sb = new StringBuilder();
    int count = values.length;

    sb.append(label);
    sb.append(": [");
    sb.append(count);
    sb.append(']');

    for (int index=0; index<count; index+=1) {
      sb.append(' ');
      sb.append(values[index]);
    }

    log(sb);
  }

  public final static void logCharacters (
    CharSequence label, CharSequence characters,
    boolean asText, boolean asHexadecimal
  ) {
    if (asText) log(String.format("%s: %s", label, characters));

    if (asHexadecimal) {
      StringBuilder sb = new StringBuilder();
      sb.append(label);
      sb.append(':');
      int count = characters.length();

      for (int index=0; index<count; index+=1) {
        sb.append(String.format(" %04X", (int)characters.charAt(index)));
      }

      log(sb);
    }
  }

  private final static char OUT_OF_BOUNDS_CHARACTER = (char)0x28FF;

  private static void logOffset (
    CharSequence fromTag, int fromOffset, char fromCharacter,
    CharSequence toTag, int toOffset, char toCharacter
  ) {
    log(
      String.format(
        "%s->%s: %d->%d %c->%c %04X->%04X",
        fromTag, toTag,
        fromOffset, toOffset,
        fromCharacter, toCharacter,
        (int)fromCharacter, (int)toCharacter
      )
    );
  }

  public final static void logOutputOffsets (Translation translation) {
    CharSequence inputTag = translation.getInputTag();
    CharSequence inputCharacters = translation.getConsumedInput();
    int inputLength = inputCharacters.length();

    CharSequence outputTag = translation.getOutputTag();
    char[] outputCharacters = translation.getOutputAsArray();
    int outputLength = outputCharacters.length;

    for (int inputOffset=0; inputOffset<inputLength; inputOffset+=1) {
      int outputOffset = translation.getOutputOffset(inputOffset);
      char outputCharacter =
        ((outputOffset >= 0) && (outputOffset < outputLength))?
        outputCharacters[outputOffset]: OUT_OF_BOUNDS_CHARACTER;

      logOffset(
        inputTag, inputOffset, inputCharacters.charAt(inputOffset),
        outputTag, outputOffset, outputCharacter
      );
    }
  }

  public final static void logInputOffsets (Translation translation) {
    CharSequence outputTag = translation.getOutputTag();
    char[] outputCharacters = translation.getOutputAsArray();
    int outputLength = outputCharacters.length;

    CharSequence inputTag = translation.getInputTag();
    CharSequence inputCharacters = translation.getConsumedInput();
    int inputLength = inputCharacters.length();

    for (int outputOffset=0; outputOffset<outputLength; outputOffset+=1) {
      int inputOffset = translation.getInputOffset(outputOffset);
      char inputCharacter =
        ((inputOffset >= 0) && (inputOffset < inputLength))?
        inputCharacters.charAt(inputOffset): OUT_OF_BOUNDS_CHARACTER;

      logOffset(
        outputTag, outputOffset, outputCharacters[outputOffset],
        inputTag, inputOffset, inputCharacter
      );
    }
  }

  public final static void logOffsets (Translation translation) {
    logOutputOffsets(translation);
    logInputOffsets(translation);
  }

  public final static void testTextTranslation (
    final Translator translator, final CharSequence text,
    final boolean showText, final boolean showHexadecimal,
    final boolean showOffsets
  ) {
    run(
      "text translation test",
      new Runnable() {
        @Override
        public void run () {
          logCharacters("txt", text, showText, showHexadecimal);

          CharSequence braille;
          {
            BrailleTranslation translation = Louis.getBrailleTranslation(translator, text);
            braille = translation.getBrailleWithSpans();
            logCharacters("brl", braille, showText, showHexadecimal);
            if (showOffsets) logOffsets(translation);
          }

          {
            TextTranslation translation = Louis.getTextTranslation(translator, braille);
            logCharacters("bck", translation.getTextWithSpans(), showText, showHexadecimal);
            if (showOffsets) logOffsets(translation);
          }
        }
      }
    );
  }

  public final static void testBrailleTranslation (
    final Translator translator, final CharSequence braille,
    final boolean showText, final boolean showHexadecimal,
    final boolean showOffsets
  ) {
    run(
      "braille translation test",
      new Runnable() {
        @Override
        public void run () {
          logCharacters("brl", braille, showText, showHexadecimal);

          CharSequence text;
          {
            TextTranslation translation = Louis.getTextTranslation(translator, braille);
            text = translation.getTextWithSpans();
            logCharacters("txt", text, showText, showHexadecimal);
            if (showOffsets) logOffsets(translation);
          }

          {
            BrailleTranslation translation = Louis.getBrailleTranslation(translator, text);
            logCharacters("bck", translation.getBrailleWithSpans(), showText, showHexadecimal);
            if (showOffsets) logOffsets(translation);
          }
        }
      }
    );
  }

  public final static void testTextTranslation (
    TranslatorIdentifier identifier, CharSequence text,
    boolean showText, boolean showHexadecimal, boolean showOffsets
  ) {
    testTextTranslation(
      identifier.getTranslator(), text,
      showText, showHexadecimal, showOffsets
    );
  }

  public final static void testBrailleTranslation (
    TranslatorIdentifier identifier, CharSequence braille,
    boolean showText, boolean showHexadecimal, boolean showOffsets
  ) {
    testBrailleTranslation(
      identifier.getTranslator(), braille,
      showText, showHexadecimal, showOffsets
    );
  }

  public final static void testMakeInputOffsets (
    final int outputLength, final int... outputOffsets
  ) {
    run(
      "make input offsets test",
      new Runnable() {
        @Override
        public void run () {
          log("in->out", outputOffsets);

          int[] inputOffsets = new int[outputLength + 1];
          int inputLength = Translator.makeInputOffsets(inputOffsets, outputOffsets);
          int outputLength = outputOffsets[inputLength];

          log(String.format("Lengths: In:%d Out:%d", inputLength, outputLength));
          log("out->in", inputOffsets);
        }
      }
    );
  }

  private final static void auditFiles (Set<File> notFound, InternalTable table) {
    for (String name : table.getNames()) {
      File file = table.getFile(name);

      if (file.exists()) {
        notFound.remove(file);
      } else {
        log(("file not found: " + file.getAbsolutePath()));
      }
    }
  }

  public static void auditTranslatorIdentifiers () {
    run(
      "translator identifier audit",
      new Runnable() {
        @Override
        public void run () {
          Set<File> notFound = new HashSet<File>();
          Collections.addAll(notFound, InternalTable.getAllFiles());

          for (TranslatorIdentifier identifier : TranslatorIdentifier.values()) {
            {
              String name = identifier.name();

              if (!name.equals(name.toUpperCase())) {
                log(("translator identifier not all uppercase: " + name));
              }
            }

            {
              Translator translator = identifier.getTranslator();

              if (translator instanceof InternalTranslator) {
                InternalTranslator internal = (InternalTranslator)translator;

                InternalTable forward = internal.getForwardTable();
                auditFiles(notFound, forward);

                InternalTable backward = internal.getBackwardTable();
                if (backward != forward) auditFiles(notFound, backward);
              }
            }
          }

          for (File file : notFound) {
            log(("unknown table file: " + file.getAbsolutePath()));
          }
        }
      }
    );
  }
}
