package org.nbp.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

import org.nbp.common.CommonContext;
import android.content.Context;

import android.text.SpannableStringBuilder;
import org.nbp.common.HighlightSpans;

import com.aspose.words.*;

public class AsposeWordsOperations extends AsposeWordsApplication implements ContentOperations {
  private final static String LOG_TAG = AsposeWordsOperations.class.getName();

  private final static License license = new License();
  private static Throwable licenseProblem = null;

  public AsposeWordsOperations () throws IOException {
    super();

    synchronized (LOG_TAG) {
      if (licenseProblem == null) {
        Context context = CommonContext.getContext();
        loadLibs(context);

        try {
          license.setLicense(context.getAssets().open("Aspose.Words.lic"));
          Log.d(LOG_TAG, "Aspose Words ready");
          return;
        } catch (Throwable problem) {
          licenseProblem = problem;
        }

        Log.w(LOG_TAG, ("Aspose Words license problem: " + licenseProblem.getMessage()));
        throw new IOException("Aspose Words license problem", licenseProblem);
      }
    }
  }

  @Override
  public final void read (InputStream stream, SpannableStringBuilder content) throws IOException {
    try {
      Document document = new Document(stream);

      for (Object documentChild : document.getFirstSection().getBody().getChildNodes()) {
        if (documentChild instanceof Paragraph) {
          Paragraph paragraph = (Paragraph)documentChild;

          for (Object paragraphChild : paragraph.getChildNodes()) {
            if (paragraphChild instanceof Run) {
              Run run = (Run)paragraphChild;

              CharSequence text = run.getText();
              Font font = run.getFont();

              int start = content.length();
              content.append(text);
              int end = content.length();
              HighlightSpans.Entry spanEntry = null;

              if (font.getBold() && font.getItalic()) {
                spanEntry = HighlightSpans.BOLD_ITALIC;
              } else if (font.getBold()) {
                spanEntry = HighlightSpans.BOLD;
              } else if (font.getItalic()) {
                spanEntry = HighlightSpans.ITALIC;
              } else if (font.getUnderline() != Underline.NONE) {
                spanEntry = HighlightSpans.UNDERLINE;
              }

              if (spanEntry != null) {
                content.setSpan(spanEntry.newInstance(), start, end, content.SPAN_EXCLUSIVE_EXCLUSIVE);
              }
            }
          }

          content.append('\n');
        }
      }
    } catch (Exception exception) {
    }
  }

  @Override
  public final void write (OutputStream stream, CharSequence content) {
  }
}
