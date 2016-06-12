package org.nbp.common.editor;
import org.nbp.common.R;

public class ParagraphSpan extends StructureSpan {
  @Override
  public final int getSpanName () {
    return R.string.editor_span_name_paragraph;
  }

  @Override
  public final String getSpanIdentifier () {
    return "par";
  }

  public ParagraphSpan () {
    super();
  }
}
