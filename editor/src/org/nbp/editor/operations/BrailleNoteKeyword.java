package org.nbp.editor.operations;
import org.nbp.editor.*;

public abstract class BrailleNoteKeyword {
  public final static int HEADER_SIZE = 0X26E;

  public final static byte END_OF_LINE = 0X0D;
  public final static byte END_OF_FILE = 0X1A;

  private BrailleNoteKeyword () {
  }
}
