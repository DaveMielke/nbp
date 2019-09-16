package org.nbp.common.dictionary;

public class DefineCommand extends DefinitionsResponse {
  public DefineCommand (String word, String database) {
    super("define", database, word);
  }

  public DefineCommand (String word, boolean all) {
    this(word, (all? DictionaryConstants.DATABASE_ALL: DictionaryConstants.DATABASE_FIRST));
  }

  public DefineCommand (String word) {
    this(word, true);
  }
}