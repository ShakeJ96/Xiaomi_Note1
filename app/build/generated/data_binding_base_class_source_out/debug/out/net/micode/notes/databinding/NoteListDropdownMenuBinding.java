// Generated by view binder compiler. Do not edit!
package net.micode.notes.databinding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.viewbinding.ViewBinding;
import android.viewbinding.ViewBindings;
import android.widget.Button;
import android.widget.LinearLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import net.micode.notes.R;

public final class NoteListDropdownMenuBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final LinearLayout navigationBar;

  @NonNull
  public final Button selectionMenu;

  private NoteListDropdownMenuBinding(@NonNull LinearLayout rootView,
      @NonNull LinearLayout navigationBar, @NonNull Button selectionMenu) {
    this.rootView = rootView;
    this.navigationBar = navigationBar;
    this.selectionMenu = selectionMenu;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static NoteListDropdownMenuBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static NoteListDropdownMenuBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.note_list_dropdown_menu, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static NoteListDropdownMenuBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      LinearLayout navigationBar = (LinearLayout) rootView;

      id = R.id.selection_menu;
      Button selectionMenu = ViewBindings.findChildViewById(rootView, id);
      if (selectionMenu == null) {
        break missingId;
      }

      return new NoteListDropdownMenuBinding((LinearLayout) rootView, navigationBar, selectionMenu);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
