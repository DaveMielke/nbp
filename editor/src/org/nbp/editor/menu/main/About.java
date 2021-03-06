package org.nbp.editor.menu.main;
import org.nbp.editor.*;

import org.nbp.common.DialogFinisher;
import org.nbp.common.DialogHelper;

import org.liblouis.Louis;

public class About extends EditorAction implements DialogFinisher {
  public About (EditorActivity editor) {
    super(editor);
  }

  @Override
  public void finishDialog (DialogHelper helper) {
    helper.setText(R.id.about_package_version, R.string.NBP_Editor_version_name);
    helper.setText(R.id.about_build_time, R.string.NBP_Editor_build_time);
    helper.setText(R.id.about_source_revision, R.string.NBP_Editor_source_revision);
    helper.setText(R.id.about_liblouis_version, Louis.getVersion());
    helper.setTextFromAsset(R.id.about_copyright, "copyright");
  }

  @Override
  public void performAction () {
    getEditor().showDialog(R.string.menu_main_About, R.layout.about, this);
  }
}
