package org.nbp.b2g.ui;

import org.liblouis.Translator;
import org.liblouis.TranslatorIdentifier;

public enum BrailleCode {
  EN_UEB_G1(TranslatorIdentifier.EN_UEB_G1),
  EN_UEB_G2(TranslatorIdentifier.EN_UEB_G2),
  EN_EBAE_G1(TranslatorIdentifier.EN_US_G1),
  EN_EBAE_G2(TranslatorIdentifier.EN_US_G2, true),
  EN_GB_G1(TranslatorIdentifier.EN_GB_G1),
  EN_GB_G2(TranslatorIdentifier.EN_GB_G2, true),
  FR_G1(TranslatorIdentifier.FR_G1),
  FR_G2(TranslatorIdentifier.FR_G2),
  DE_G0(TranslatorIdentifier.DE_G0),
  DE_G1(TranslatorIdentifier.DE_G1),
  DE_G2(TranslatorIdentifier.DE_G2),
  EL(TranslatorIdentifier.EL),
  GRC_INTL_EN(TranslatorIdentifier.GRC_INTL_EN),
  HE(TranslatorIdentifier.HE_COMP8),
  IT(TranslatorIdentifier.IT),
  PT_G1(TranslatorIdentifier.PT_G1),
  PT_G2(TranslatorIdentifier.PT_G2),
  RU(TranslatorIdentifier.RU),
  ES_G1(TranslatorIdentifier.ES_G1),
  ES_G2(TranslatorIdentifier.ES_G2),
  PINYIN(TranslatorIdentifier.PINYIN),
  ; // end of enumeration

  private final TranslatorIdentifier translatorIdentifier;
  private final boolean joinableWords;

  BrailleCode (TranslatorIdentifier identifier, boolean joinable) {
    translatorIdentifier = identifier;
    joinableWords = joinable;
  }

  BrailleCode (TranslatorIdentifier identifier) {
    this(identifier, false);
  }

  public final TranslatorIdentifier getTranslatorIdentifier () {
    return translatorIdentifier;
  }

  public final Translator getTranslator () {
    return translatorIdentifier.getTranslator();
  }

  public final String getDescription () {
    return getTranslatorIdentifier().getDescription();
  }

  public final boolean hasJoinableWords () {
    return joinableWords;
  }
}
