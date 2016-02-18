package org.nbp.common;

import java.util.Set;
import java.util.TreeSet;
import java.util.LinkedHashSet;

import java.util.Map;
import java.util.LinkedHashMap;

import java.io.IOException;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;

import android.util.Log;
import android.content.Context;

import android.app.Activity;
import android.os.AsyncTask;

import android.content.DialogInterface;
import android.app.AlertDialog;

import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

public abstract class FileFinder {
  private final static String LOG_TAG = FileFinder.class.getName();

  public interface FileHandler {
    public void handleFile (File file);
  }

  public interface FilesHandler {
    public void handleFiles (File[] files);
  }

  public final static class Builder {
    private final Activity ownerActivity;
    private String userTitle = null;
    private Map<String, File> rootLocations = new LinkedHashMap<String, File>();
    private boolean forWriting = false;

    private final String getString (int resource) {
      return ownerActivity.getString(resource);
    }

    public final Activity getOwnerActivity () {
      return ownerActivity;
    }

    public final String getUserTitle () {
      return userTitle;
    }

    public final Builder setUserTitle (String title) {
      userTitle = title;
      return this;
    }

    public final Builder setUserTitle (int title) {
      return setUserTitle(getString(title));
    }

    public final Set<String> getRootLabels () {
      return rootLocations.keySet();
    }

    public final File getRootLocation (String label) {
      return rootLocations.get(label);
    }

    public final Builder addRootLocation (String label, File location) {
      rootLocations.put(label, location);
      return this;
    }

    public final Builder addRootLocation (File location) {
      return addRootLocation(location.getAbsolutePath(), location);
    }

    public final Builder addRootLocation (String label) {
      return addRootLocation(label, FileSystems.getMountpoint(label));
    }

    public final boolean getForWriting () {
      return forWriting;
    }

    public final Builder setForWriting (boolean yes) {
      forWriting = yes;
      return this;
    }

    public final void find (FileHandler handler) {
      new SingleFileFinder(this, handler);
    }

    public final void find (FilesHandler handler) {
      new MultipleFileFinder(this, handler);
    }

    public Builder (Activity owner) {
      ownerActivity = owner;
    }
  }

  private final Activity ownerActivity;
  private final String userTitle;
  private final boolean forWriting;

  private Map<String, File> rootLocations = new LinkedHashMap<String, File>();

  private File currentReference = null;

  private final String getString (int resource) {
    return ownerActivity.getString(resource);
  }

  private final View inflateLayout (int resource) {
    return ownerActivity.getLayoutInflater().inflate(resource, null);
  }

  private final View findView (int id) {
    return ownerActivity.findViewById(id);
  }

  private final View findView (DialogInterface dialog, int id) {
    return ((AlertDialog)dialog).findViewById(id);
  }

  protected abstract void setItems (AlertDialog.Builder builder, String[] items, File reference);
  protected abstract void handleFiles (File[] files);

  private final void handleFile (File file) {
    handleFiles(new File[] {file});
  }

  private final void requestCancelled () {
    handleFiles(null);
  }

  private final AlertDialog.Builder newAlertDialogBuilder () {
    return new AlertDialog.Builder(ownerActivity)
                          .setCancelable(false)
                          ;
  }

  private final void setTitle (AlertDialog.Builder builder, String... details) {
    StringBuilder sb = new StringBuilder(getString(R.string.FileFinder_title_main));

    if (userTitle != null) {
      sb.append(" - ");
      sb.append(userTitle);
    }

    for (String detail : details) {
      sb.append('\n');
      sb.append(detail);
    }

    builder.setTitle(sb.toString());
  }

  private final void showMessage (
    CharSequence message,
    DialogInterface.OnClickListener dismissListener
  ) {
    newAlertDialogBuilder()
      .setMessage(message)
      .setNeutralButton(R.string.FileFinder_action_dismiss, dismissListener)
      .show();
  }

  private final void setButtonEnabled (DialogInterface dialog, int button, boolean enabled) {
    ((AlertDialog)dialog).getButton(button).setEnabled(enabled);
  }

  private final void setDoneButton (
    AlertDialog.Builder builder,
    DialogInterface.OnClickListener listener
  ) {
    builder.setPositiveButton(R.string.FileFinder_action_done, listener);
  }

  private final void setBackButton (AlertDialog.Builder builder) {
    builder.setNeutralButton(
      R.string.FileFinder_action_back,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick (DialogInterface dialog, int button) {
          showListing(currentReference);
        }
      }
    );
  }

  private final void setCancelButton (AlertDialog.Builder builder) {
    builder.setNegativeButton(
      R.string.FileFinder_action_cancel,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick (DialogInterface dialog, int button) {
          requestCancelled();
        }
      }
    );
  }

  private final void setEditedPath (AlertDialog dialog, File reference) {
    String directory;
    String file;

    if (!reference.isAbsolute()) {
      directory = currentReference.getAbsolutePath();
      file = reference.getPath();
    } else if (reference.isDirectory()) {
      directory = reference.getAbsolutePath();
      file = "";
    } else {
      directory = reference.getParentFile().getAbsolutePath();
      file = reference.getName();
    }

    TextView directoryView = (TextView)dialog.findViewById(R.id.PathEditor_directory);
    directoryView.setText(directory);

    EditText fileView = (EditText)dialog.findViewById(R.id.PathEditor_file);
    final Button doneButton = dialog.getButton(dialog.BUTTON_POSITIVE);

    new OnTextEditedListener(fileView) {
      @Override
      protected void onTextEdited (boolean isDifferent) {
        doneButton.setEnabled(isDifferent);
      }
    };

    fileView.setText(file);
    fileView.setSelection(fileView.length());
  }

  private final File getEditedPath (DialogInterface dialog) {
    TextView directoryView = (TextView)findView(dialog, R.id.PathEditor_directory);
    String directory = directoryView.getText().toString();
    if (directory.isEmpty()) return null;

    EditText fileView = (EditText)findView(dialog, R.id.PathEditor_file);
    String file = fileView.getText().toString().trim();
    if (file.isEmpty()) return null;

    return new File(new File(directory), file);
  }

  private final void showPathProblem (final File file, int problem) {
    showMessage(
      String.format(
        "%s: %s",
        getString(problem),
        file.getAbsolutePath()
      ),

      new DialogInterface.OnClickListener() {
        @Override
        public void onClick (DialogInterface dialog, int button) {
          showPathEditor(file);
        }
      }
    );
  }

  private final void showPathEditor (File reference) {
    AlertDialog.Builder builder = newAlertDialogBuilder()
      .setTitle(R.string.FileFinder_action_path)
      .setView(inflateLayout(R.layout.path_editor));

    setDoneButton(builder,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick (DialogInterface dialog, int button) {
          File file = getEditedPath(dialog);

          if (file == null) {
            requestCancelled();
          } else {
            if (!file.isAbsolute()) {
              file = new File(currentReference, file.getPath());
            }

            if (!file.exists()) {
              File directory = file.getParentFile();

              if (directory.isDirectory() || directory.mkdirs()) {
                handleFile(file);
              } else {
                showPathProblem(directory, R.string.FileFinder_message_uncreatable_directory);
              }
            } else if (file.isFile()) {
              handleFile(file);
            } else if (file.isDirectory()) {
              showListing(file);
            } else {
              showPathProblem(file, R.string.FileFinder_message_uneditable_file);
            }
          }
        }
      }
    );

    setBackButton(builder);
    setCancelButton(builder);

    AlertDialog dialog = builder.create();
    dialog.show();
    setEditedPath(dialog, reference);
  }

  private interface ListingCreator {
    public Set<String> createListing ();
  }

  private final void showListing (final File reference, final ListingCreator listingCreator) {
    currentReference = reference;
    final boolean haveReference = reference != null;

    new AsyncTask<Void, Void, AlertDialog.Builder>() {
      final AlertDialog message = newAlertDialogBuilder()
              .setMessage(R.string.FileFinder_message_creating_listing)
              .create();

      @Override
      protected void onPreExecute () {
        message.show();
      }

      @Override
      protected AlertDialog.Builder doInBackground (Void... arguments) {
        AlertDialog.Builder builder = newAlertDialogBuilder();

        if (haveReference) {
          setTitle(builder, reference.getAbsolutePath());
        } else {
          setTitle(builder);
        }

        Set<String> listing = listingCreator.createListing();
        int count = listing.size();

        if (count == 0) {
          builder.setMessage(R.string.FileFinder_message_empty_directory);
        } else {
          final String[] items = new String[count];
          count = 0;

          for (String item : listing) {
            items[count++] = item;
          }

          setItems(builder, items, reference);
        }

        if (forWriting) {
          builder.setPositiveButton(
            R.string.FileFinder_action_path,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick (DialogInterface dialog, int button) {
                showPathEditor(reference);
              }
            }
          );
        }

        builder.setNeutralButton(
          R.string.FileFinder_action_up,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick (DialogInterface dialog, int button) {
              showListing(reference.getParentFile());
            }
          }
        );

        setCancelButton(builder);
        return builder;
      }

      @Override
      protected void onPostExecute (AlertDialog.Builder builder) {
        message.dismiss();

        AlertDialog dialog = builder.create();
        dialog.show();

        if (forWriting) {
          boolean canCreate =  haveReference
                            && (reference.isDirectory())
                            && (reference.canWrite())
                            ;

          if (!canCreate) {
            setButtonEnabled(dialog, dialog.BUTTON_POSITIVE, false);
          }
        }

        if (!haveReference) {
          setButtonEnabled(dialog, dialog.BUTTON_NEUTRAL, false);
        }
      }
    }.execute();
  }

  private final static boolean canAccessDirectory (File directory) {
    if (!directory.canRead()) return false;
    if (!directory.canExecute()) return false;
    return true;
  }

  private final Set<String> createRootListing () {
    Set<String> listing = new LinkedHashSet<String>();

    for (String label : rootLocations.keySet()) {
      if (canAccessDirectory(rootLocations.get(label))) {
        listing.add(label);
      }
    }

    return listing;
  }

  private final Set<String> createDirectoryListing (File directory) {
    Set<String> listing = new TreeSet();

    for (File file : directory.listFiles()) {
      if (file.isHidden()) continue;

      String name = file.getName();
      char indicator = 0;

      if (file.isDirectory()) {
        if (!canAccessDirectory(file)) continue;
        indicator = File.separatorChar;
      } else {
        if (!file.isFile()) continue;

        if (forWriting) {
          if (!file.canWrite()) continue;
        } else {
          if (!file.canRead()) continue;
        }
      }

      if (indicator != 0) name += indicator;
      listing.add(name);
    }

    return listing;
  }

  protected final void showListing (final File reference) {
    if (reference == null) {
      showListing(null,
        new ListingCreator() {
          @Override
          public Set<String> createListing () {
            return createRootListing();
          }
        }
      );
    } else if (reference.isDirectory()) {
      showListing(reference,
        new ListingCreator() {
          @Override
          public Set<String> createListing () {
            return createDirectoryListing(reference);
          }
        }
      );
    } else {
      handleFile(reference);
    }
  }

  protected final void showRootListing (String label) {
    showListing(rootLocations.get(label));
  }

  private FileFinder (Builder builder) {
    ownerActivity = builder.getOwnerActivity();
    userTitle = builder.getUserTitle();
    forWriting = builder.getForWriting();

    {
      Set<String> labels = builder.getRootLabels();

      if (labels.isEmpty()) {
        for (String label : FileSystems.getAllLabels()) {
          rootLocations.put(label, FileSystems.getMountpoint(label));
        }
      } else {
        for (String label : labels) {
          rootLocations.put(label, builder.getRootLocation(label));
        }
      }
    }

    if (rootLocations.size() == 1) {
      Set<String> labels = rootLocations.keySet();
      showListing(rootLocations.get(labels.toArray(new String[1])[0]));
    } else {
      showListing(null);
    }
  }

  private static class SingleFileFinder extends FileFinder {
    private final FileHandler fileHandler;

    protected final void handleFiles (File[] files) {
      File file = null;
      if (files != null) file = files[0];
      fileHandler.handleFile(file);
    }

    protected final void setItems (
      AlertDialog.Builder builder,
      final String[] items,
      final File reference
    ) {
      builder.setItems(items,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick (DialogInterface dialog, int index) {
            String item = items[index];

            if (reference == null) {
              showRootListing(item);
            } else if (item.charAt(0) == File.separatorChar) {
              showListing(new File(item));
            } else {
              showListing(new File(reference, item));
            }
          }
        }
      );
    }

    private SingleFileFinder (Builder builder, FileHandler handler) {
      super(builder);
      fileHandler = handler;
    }
  }

  private static class MultipleFileFinder extends FileFinder {
    private final FilesHandler filesHandler;

    protected final void handleFiles (File[] files) {
      filesHandler.handleFiles(files);
    }

    protected final void setItems (AlertDialog.Builder builder, String[] items, File reference) {
    }

    private MultipleFileFinder (Builder builder, FilesHandler handler) {
      super(builder);
      filesHandler = handler;
    }
  }
}
