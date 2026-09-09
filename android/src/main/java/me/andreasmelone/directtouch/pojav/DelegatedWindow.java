package me.andreasmelone.directtouch.pojav;

import android.os.Build;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class DelegatedWindow implements Window.Callback {
    protected final Window.Callback original;

    public DelegatedWindow(Window.Callback original) {
        this.original = original;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return original.dispatchTouchEvent(motionEvent);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
        return original.dispatchGenericMotionEvent(motionEvent);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return original.dispatchKeyEvent(keyEvent);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent keyEvent) {
        return original.dispatchKeyShortcutEvent(keyEvent);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent
                                                              accessibilityEvent) {
        return original.dispatchPopulateAccessibilityEvent(accessibilityEvent);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent motionEvent) {
        return original.dispatchTrackballEvent(motionEvent);
    }

    @Override
    public void onActionModeFinished(ActionMode actionMode) {
        original.onActionModeFinished(actionMode);
    }

    @Override
    public void onActionModeStarted(ActionMode actionMode) {
        original.onActionModeStarted(actionMode);
    }

    @Override
    public void onAttachedToWindow() {
        original.onAttachedToWindow();
    }

    @Override
    public void onContentChanged() {
        original.onContentChanged();
    }

    @Override
    public boolean onCreatePanelMenu(int i, @NonNull Menu menu) {
        return original.onCreatePanelMenu(i, menu);
    }

    @Nullable
    @Override
    public View onCreatePanelView(int i) {
        return original.onCreatePanelView(i);
    }

    @Override
    public void onDetachedFromWindow() {
        original.onDetachedFromWindow();
    }

    @Override
    public boolean onMenuItemSelected(int i, @NonNull MenuItem menuItem) {
        return original.onMenuItemSelected(i, menuItem);
    }

    @Override
    public boolean onMenuOpened(int i, @NonNull Menu menu) {
        return original.onMenuOpened(i, menu);
    }

    @Override
    public void onPanelClosed(int i, @NonNull Menu menu) {
        original.onPanelClosed(i, menu);
    }

    @Override
    public boolean onPreparePanel(int i, @Nullable View view, @NonNull Menu menu) {
        return original.onPreparePanel(i, view, menu);
    }

    @Override
    public boolean onSearchRequested() {
        return original.onSearchRequested();
    }

    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return original.onSearchRequested(searchEvent);
        }
        return true;
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams layoutParams) {
        original.onWindowAttributesChanged(layoutParams);
    }

    @Override
    public void onWindowFocusChanged(boolean b) {
        original.onWindowFocusChanged(b);
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return original.onWindowStartingActionMode(callback);
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int i) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return original.onWindowStartingActionMode(callback, i);
        }
        return null;
    }
}
