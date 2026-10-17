package me.andreasmelone.directtouch.pojav.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;

public interface IPojavLauncher {
    Activity getMainActivity();
    float dpToPx(float dp);
    void runOnUiThread(Runnable runnable);

    void tryDisableMojoPanning(); // mojo only

    String getName();

    @SuppressLint("DiscouragedApi")
    static int getId(Activity activity, String name) {
        return activity.getResources().getIdentifier(name, "id", activity.getPackageName());
    }
}
