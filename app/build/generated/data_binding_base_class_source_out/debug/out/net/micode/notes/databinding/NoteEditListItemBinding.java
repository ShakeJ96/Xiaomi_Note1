// Generated by view binder compiler. Do not edit!
package net.micode.notes.databinding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.viewbinding.ViewBinding;
import android.viewbinding.ViewBindings;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import net.micode.notes.R;
import net.micode.notes.ui.NoteEditText;

public final class NoteEditListItemBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final CheckBox cbEditItem;

  @NonNull
  public final NoteEditText etEditText;

  private NoteEditListItemBinding(@NonNull LinearLayout rootView, @NonNull CheckBox cbEditItem,
      @NonNull NoteEditText etEditText) {
    this.rootView = rootView;
    this.cbEditItem = cbEditItem;
    this.etEditText = etEditText;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static NoteEditListItemBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static NoteEditListItemBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.note_edit_list_item, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static NoteEditListItemBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.cb_edit_item;
      CheckBox cbEditItem = ViewBindings.findChildViewById(rootView, id);
      if (cbEditItem == null) {
        break missingId;
      }

      id = R.id.et_edit_text;
      NoteEditText etEditText = ViewBindings.findChildViewById(rootView, id);
      if (etEditText == null) {
        break missingId;
      }

      return new NoteEditListItemBinding((LinearLayout) rootView, cbEditItem, etEditText);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
