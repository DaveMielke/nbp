package org.nbp.editor.operations.aspose;
import org.nbp.editor.*;
import org.nbp.editor.spans.*;

import java.util.Date;

import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

import android.content.Context;

import android.text.Spanned;
import android.text.SpannedString;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import org.nbp.common.HighlightSpans;
import android.text.style.CharacterStyle;

import com.aspose.words.*;

public class WordsOperations extends ContentOperations {
  private final static String LOG_TAG = WordsOperations.class.getName();

  private final static WordsLicense wordsLicense = WordsLicense.singleton();

  private final int saveFormat;
  private final int loadFormat;

  public WordsOperations (int saveFormat, int loadFormat) throws IOException {
    super(true);

    this.saveFormat = saveFormat;
    this.loadFormat = loadFormat;
  }

  public WordsOperations (int saveFormat) throws IOException {
    this(saveFormat, LoadFormat.UNKNOWN);
  }

  private final static Map<String, String> listLabelMap =
               new HashMap<String, String>();

  static {
    listLabelMap.put("\uF0B7", "\u2022");
  }

  private class DocumentReader {
    private class RevisionMap extends HashMap<Node, Revision> {
      public RevisionMap () {
        super();
      }
    }

    private final RevisionMap insertionMap = new RevisionMap();
    private final RevisionMap deletionMap = new RevisionMap();

    private final void mapRevisions (Document document) {
      RevisionCollection revisions = document.getRevisions();

      if (revisions != null) {
        for (Revision revision : revisions) {
          Node node = revision.getParentNode();
          if (node == null) continue;

          switch (revision.getRevisionType()) {
            case RevisionType.INSERTION:
              insertionMap.put(node, revision);
              break;

            case RevisionType.DELETION:
              deletionMap.put(node, revision);
              break;
          }
        }
      }
    }

    private final void setReviewProperties (
      ReviewSpan review, RevisionMap revisionMap, Node node
    ) {
      if (review != null) {
        Revision revision = revisionMap.get(node);

        if (revision != null) {
          review.setReviewerName(revision.getAuthor());
          review.setReviewTime(revision.getDateTime());
        }
      }
    }

    private final void handleRevisionFlags (
      Editable content, int start, Node node,
      boolean isInsertion, boolean isDeletion
    ) {
      InsertionSpan insertion = isInsertion? new InsertionSpan(): null;
      DeletionSpan deletion = isDeletion? new DeletionSpan(insertion): null;

      RevisionSpan revision = deletion;
      if (revision == null) revision = insertion;

      if (revision != null) {
        if (addSpan(content, start, revision)) {
          setReviewProperties(insertion, insertionMap, node);
          setReviewProperties(deletion, deletionMap, node);
        }
      }
    }

    private class CommentDescriptor {
      public CommentDescriptor () {
      }

      private final void setSpan (Spannable content, Object span) {
        int position = content.length();
        content.setSpan(span, position, position, content.SPAN_MARK_MARK);
      }

      private Comment commentNode = null;
      private CommentRangeStart startPosition = null;
      private CommentRangeEnd endPosition = null;

      public final  Comment getComment () {
        return commentNode;
      }

      public final void setComment (Spannable content, Comment comment) {
        commentNode = comment;
        setSpan(content, comment);
      }

      public final CommentRangeStart getStart () {
        return startPosition;
      }

      public final void setStart (Spannable content, CommentRangeStart start) {
        startPosition = start;
        setSpan(content, start);
      }

      public final CommentRangeEnd getEnd () {
        return endPosition;
      }

      public final void setEnd (Spannable content, CommentRangeEnd end) {
        endPosition = end;
        setSpan(content, end);
      }
    }

    private final Map<Integer, CommentDescriptor> commentDescriptors =
          new HashMap<Integer, CommentDescriptor>();

    private final CommentDescriptor getCommentDescriptor (int identifier) {
      CommentDescriptor comment = commentDescriptors.get(identifier);
      if (comment != null) return comment;

      comment = new CommentDescriptor();
      commentDescriptors.put(identifier, comment);
      return comment;
    }

    private final void logUnhandledChildNode (Node parent, Object child) {
      if (ApplicationParameters.ASPOSE_LOG_UNHANDLED_CHILDREN) {
        Log.d(LOG_TAG,
          String.format(
            "unhandled child node: %s contains %s",
            parent.getClass().getSimpleName(),
            child.getClass().getSimpleName()
          )
        );
      }
    }

    private final void addRun (Editable content, Run run) throws Exception {
      final int start = content.length();
      content.append(run.getText());

      handleRevisionFlags(
        content, start, run,
        run.isInsertRevision(),
        run.isDeleteRevision()
      );

      new WordsFontSpan(run.getFont(), content, start).addAndroidSpans();
    }

    private final void addParagraph (Editable content, Paragraph paragraph) throws Exception {
      int start = content.length();

      if (paragraph.isListItem()) {
        ListLabel label = paragraph.getListLabel();
        String string = label.getLabelString();

        {
          String mapped = listLabelMap.get(string);
          if (mapped != null) string = mapped;
        }

        content.append(String.format("[%s] ", string));
        addSpan(content, start, new DecorationSpan());
      }

      for (Object child : paragraph.getChildNodes()) {
        if (child instanceof Run) {
          Run run = (Run)child;
          addRun(content, run);
          continue;
        }

        if (child instanceof Comment) {
          Comment comment = (Comment)child;
          getCommentDescriptor(comment.getId()).setComment(content, comment);
          continue;
        }

        if (child instanceof CommentRangeStart) {
          CommentRangeStart crs = (CommentRangeStart)child;
          getCommentDescriptor(crs.getId()).setStart(content, crs);
          continue;
        }

        if (child instanceof CommentRangeEnd) {
          CommentRangeEnd cre = (CommentRangeEnd)child;
          getCommentDescriptor(cre.getId()).setEnd(content, cre);
          continue;
        }

        logUnhandledChildNode(paragraph, child);
      }

      {
        int length = content.length();

        if (length > 0) {
          if (content.charAt(length-1) != '\n') {
            content.append('\n');
          }
        }
      }

      handleRevisionFlags(
        content, start, paragraph,
        paragraph.isInsertRevision(),
        paragraph.isDeleteRevision()
      );

      addSpan(content, start, new ParagraphSpan());
    }

    private final void addSection (Editable content, Section section) throws Exception {
      int start = content.length();

      for (Object child : section.getBody().getChildNodes()) {
        if (child instanceof Paragraph) {
          Paragraph paragraph = (Paragraph)child;
          addParagraph(content, paragraph);
          continue;
        }

        logUnhandledChildNode(section, child);
      }

      addSpan(content, start, new SectionSpan());
    }

    private final void addComment (Editable content, Comment comment) throws Exception {
      int start = content.length();

      for (Object child : comment.getChildNodes()) {
        if (child instanceof Paragraph) {
          Paragraph paragraph = (Paragraph)child;
          addParagraph(content, paragraph);
          continue;
        }

        logUnhandledChildNode(comment, child);
      }
    }

    private final int getPosition (Spanned content, Object span) {
      Object[] spans = content.getSpans(0, content.length(), span.getClass());
      return content.getSpanStart(spans[0]);
    }

    private final void addComment (Editable content, CommentDescriptor descriptor) throws Exception {
      Comment comment = descriptor.getComment();
      if (comment == null) return;

      Node startNode = descriptor.getStart();
      if (startNode == null) startNode = comment;

      Node endNode = descriptor.getEnd();
      if (endNode == null) endNode = comment;

      int startPosition = getPosition(content, startNode);
      int endPosition = (endNode == startNode)?
                        startPosition:
                        getPosition(content, endNode);

      content.removeSpan(comment);
      if (startNode != null) content.removeSpan(startNode);
      if (endNode != null) content.removeSpan(endNode);

      Editable text = new SpannableStringBuilder();
      addComment(text, comment);
      if (text.length() == 0) return;

      CommentSpan span = new CommentSpan(text);
      span.setReviewerName(comment.getAuthor());
      span.setReviewerInitials(comment.getInitial());
      span.setReviewTime(comment.getDateTime());

      content.setSpan(
        span, startPosition, endPosition, 
        (startPosition == endPosition)?
          Spanned.SPAN_POINT_POINT:
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      );
    }

    private final void addComments (Editable content) throws Exception {
      for (CommentDescriptor descriptor : commentDescriptors.values()) {
        addComment(content, descriptor);
      }
    }

    public DocumentReader (InputStream stream, Editable content) throws Exception {
      LoadOptions options = new LoadOptions();
      options.setLoadFormat(loadFormat);

      Document document = new Document(stream, options);
      document.updateListLabels();
      document.joinRunsWithSameFormatting();

      mapRevisions(document);
      addSection(content, document.getFirstSection());
      addComments(content);
    }
  }

  @Override
  public final void read (InputStream stream, Editable content) throws IOException {
    wordsLicense.check();
    if (loadFormat == LoadFormat.UNKNOWN) readingNotSupported();

    try {
      new DocumentReader(stream, content);
    } catch (Exception exception) {
      Log.w(LOG_TAG, ("input error: " + exception.getMessage()));
      throw new IOException("Aspose Words input error", exception);
    }
  }

  private class DocumentWriter {
    private final Document document = new Document();
    private final DocumentBuilder builder = new DocumentBuilder(document);
    private int bookmarkNumber = 0;

    private final String newBookmarkName () {
      return "bookmark" + ++bookmarkNumber;
    }

    private final void beginRevisionTracking (RevisionSpan span) {
      document.startTrackRevisions(
        span.getReviewerName(), span.getReviewTime()
      );
    }

    private final void endRevisionTracking () {
      document.stopTrackRevisions();
    }

    private final void appendSibling (Node node) {
      builder.getCurrentParagraph().appendChild(node);
    }

    private final void appendSibling (Node node, Node reference) {
      reference.getParentNode().insertAfter(node, reference);
    }

    private class TextWriter {
      private int documentPosition = 0;

      private final void writeText (CharSequence text) throws Exception {
        builder.write(text.toString());
        documentPosition += text.length();
      }

      private abstract class SpanFinisher {
        public abstract void finishSpan () throws Exception;
      }

      private final Map<Integer, List<SpanFinisher>> spanFinishers =
            new HashMap<Integer, List<SpanFinisher>>();

      private final List<SpanFinisher> getSpanFinishers (int position) {
        return spanFinishers.get(position);
      }

      private final void addSpanFinisher (int position, SpanFinisher finisher) {
        if (finisher == null) return;
        List<SpanFinisher> finishers = getSpanFinishers(position);

        if (finishers == null) {
          finishers = new ArrayList();
          spanFinishers.put(position, finishers);
        }

        finishers.add(finisher);
      }

      private final void finishSpans (int position) throws Exception {
        List<SpanFinisher> finishers = getSpanFinishers(position);

        if (finishers != null) {
          for (SpanFinisher finisher : finishers) {
            finisher.finishSpan();
          }

          spanFinishers.remove(position);
        }
      }

      private final SpanFinisher beginInsertion (final InsertionSpan insertion) {
        beginRevisionTracking(insertion);

        return new SpanFinisher() {
          @Override
          public void finishSpan () {
            endRevisionTracking();
          }
        };
      }

      private final SpanFinisher beginDeletion (final DeletionSpan deletion) throws Exception {
        {
          InsertionSpan insertion = deletion.getInsertion();
          if (insertion != null) beginRevisionTracking(insertion);
        }

        final String bookmarkName = newBookmarkName();
        final BookmarkStart bookmarkStart = builder.startBookmark(bookmarkName);

        return new SpanFinisher() {
          @Override
          public void finishSpan () throws Exception {
            builder.endBookmark(bookmarkName);
            Bookmark bookmark = bookmarkStart.getBookmark();

            beginRevisionTracking(deletion);
            bookmark.setText("");
            endRevisionTracking();

            bookmark.remove();
          }
        };
      }

      private final SpanFinisher beginComment (final CommentSpan span) throws Exception {
        final int position = documentPosition;

        final Comment comment = new Comment(document);
        appendSibling(comment);

        final Paragraph paragraph = new Paragraph(document);
        comment.getParagraphs().add(paragraph);

        {
          String name = span.getReviewerName();
          if (name != null) comment.setAuthor(name);
        }

        {
          String initials = span.getReviewerInitials();
          if (initials != null) comment.setInitial(initials);
        }

        {
          Date time = span.getReviewTime();
          if (time != null) comment.setDateTime(time);
        }

        return new SpanFinisher() {
          @Override
          public void finishSpan () throws Exception {
            if (documentPosition != position) {
              int identifier = comment.getId();

              CommentRangeStart start = new CommentRangeStart(document, identifier);
              appendSibling(start, comment);

              CommentRangeEnd end = new CommentRangeEnd(document, identifier);
              appendSibling(end);
            }

            {
              String name = "comment";
              BookmarkStart start = builder.startBookmark(name);
              BookmarkEnd end = builder.endBookmark(name);

              builder.moveTo(paragraph);
              new TextWriter(span.getCommentText());

              builder.moveToBookmark(name);
              start.getBookmark().remove();
            }
          }
        };
      }

      public TextWriter (Spanned text) throws Exception {
        int length = text.length();
        int start = 0;

        while (start < length) {
          int end = text.nextSpanTransition(start, length, Object.class);
          finishSpans(end);

          Font font = builder.getFont();
          font.clearFormatting();

          boolean isDecoration = false;
          boolean isBold = false;
          boolean isItalic = false;
          boolean isUnderline = false;
          boolean isStrikeThrough = false;
          boolean isSuperscript = false;
          boolean isSubscript = false;

          Font explicit = null;
          Object[] spans = text.getSpans(start, end, Object.class);

          if (spans != null) {
            for (Object span : spans) {
              boolean isStart = text.getSpanStart(span) == start;
              boolean isEnd = text.getSpanEnd(span) == end;

              if (span instanceof WordsFontSpan) {
                WordsFontSpan wfs = (WordsFontSpan)span;
                explicit = wfs.getFont();
              } else if (span instanceof CharacterStyle) {
                CharacterStyle cs = (CharacterStyle)span;

                if (HighlightSpans.BOLD_ITALIC.isFor(cs)) {
                  isBold = isItalic = true;
                } else if (HighlightSpans.BOLD.isFor(cs)) {
                  isBold = true;
                } else if (HighlightSpans.ITALIC.isFor(cs)) {
                  isItalic = true;
                } else if (HighlightSpans.UNDERLINE.isFor(cs)) {
                  isUnderline = true;
                } else if (HighlightSpans.STRIKE.isFor(cs)) {
                  isStrikeThrough = true;
                } else if (HighlightSpans.SUPERSCRIPT.isFor(cs)) {
                  isSuperscript = true;
                } else if (HighlightSpans.SUBSCRIPT.isFor(cs)) {
                  isSubscript = true;
                }
              } else if (span instanceof DecorationSpan) {
                isDecoration = true;
              } else if (span instanceof InsertionSpan) {
                if (isStart) {
                  addSpanFinisher(end, beginInsertion((InsertionSpan)span));
                }
              } else if (span instanceof DeletionSpan) {
                if (isStart) {
                  addSpanFinisher(end, beginDeletion((DeletionSpan)span));
                }
              } else if (span instanceof CommentSpan) {
                if (isStart) {
                  addSpanFinisher(end, beginComment((CommentSpan)span));
                }
              }
            }
          }

          if (explicit != null) {
            WordsFontCopier.copyFont(explicit, font);
          }

          font.setBold(isBold);
          font.setItalic(isItalic);
          font.setUnderline(isUnderline? Underline.DASH: Underline.NONE);
          font.setStrikeThrough(isStrikeThrough);
          font.setSuperscript(isSuperscript);
          font.setSubscript(isSubscript);

          if (!isDecoration) {
            writeText(text.subSequence(start, end));
          }

          finishSpans(start);
          start = end;
        }
      }
    }

    public DocumentWriter (OutputStream stream, CharSequence content) throws Exception {
      Spanned text = (content instanceof Spanned)? (Spanned)content: new SpannedString(content);
      new TextWriter(text);

      document.joinRunsWithSameFormatting();
      document.save(stream, saveFormat);
    }
  }

  @Override
  public final void write (OutputStream stream, CharSequence content) throws IOException {
    wordsLicense.check();
    if (saveFormat == SaveFormat.UNKNOWN) writingNotSupported();

    try {
      new DocumentWriter(stream, content);
    } catch (Exception exception) {
      Log.w(LOG_TAG, ("output error: " + exception.getMessage()));
      throw new IOException("Aspose Words output error", exception);
    }
  }
}
