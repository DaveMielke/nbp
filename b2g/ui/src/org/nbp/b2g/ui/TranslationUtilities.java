package org.nbp.b2g.ui;

import android.util.Log;

import org.liblouis.TranslationBuilder;
import org.liblouis.BrailleTranslation;
import org.liblouis.TextTranslation;

public abstract class TranslationUtilities {
  private final static String LOG_TAG = TranslationUtilities.class.getName();

  private final static int LENGTH_MULTIPLIER = 3;
  private final static int LENGTH_EXTRA = 0X100;

  public static TranslationBuilder newTranslationBuilder (CharSequence input) {
    return new TranslationBuilder()
              .setInputCharacters(input)
              .setOutputLength((input.length() * LENGTH_MULTIPLIER) + LENGTH_EXTRA)
              .setAllowLongerOutput(true)
              .setTranslator(ApplicationSettings.BRAILLE_CODE.getTranslator())
              ;
  }

  public static TranslationBuilder newLiteraryTranslationBuilder (CharSequence input) {
    if (!ApplicationSettings.LITERARY_BRAILLE) return null;
    return newTranslationBuilder(input);
  }

  public static BrailleTranslation newBrailleTranslation (
    CharSequence text, boolean includeHighlighting
  ) {
    TranslationBuilder builder = newLiteraryTranslationBuilder(text);
    if (builder == null) return null;
    builder.setIncludeHighlighting(includeHighlighting);
    return builder.newBrailleTranslation();
  }

  public static BrailleTranslation newBrailleTranslation (
    char text, boolean includeHighlighting
  ) {
    return newBrailleTranslation(Character.toString(text), includeHighlighting);
  }

  public static TextTranslation newTextTranslation (CharSequence braille) {
    TranslationBuilder builder = newLiteraryTranslationBuilder(braille);
    if (builder == null) return null;
    return builder.newTextTranslation();
  }

  public static TextTranslation newTextTranslation (char braille) {
    return newTextTranslation(Character.toString(braille));
  }

  public static void cacheBraille (char character) {
    String braille = Character.toString(character);
    TextTranslation translation = newTextTranslation(braille);
    CharSequence text = translation.getTextWithSpans();
    TranslationCache.put(text, translation);
  }

  public final static void refresh () {
    TranslationCache.clear();

    if (ApplicationSettings.LITERARY_BRAILLE) {
      Endpoints.getCurrentEndpoint().refresh();
    }
  }

  private TranslationUtilities () {
  }
}
