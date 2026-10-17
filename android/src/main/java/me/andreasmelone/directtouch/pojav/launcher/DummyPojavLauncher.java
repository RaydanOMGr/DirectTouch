package me.andreasmelone.directtouch.pojav.launcher;

import android.app.Activity;

public class DummyPojavLauncher implements IPojavLauncher {
    @Override
    public Activity getMainActivity() {
        return null;
    }

    @Override
    public float dpToPx(float dp) {
        return dp;
    }

    @Override
    public void runOnUiThread(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void tryDisableMojoPanning() {
    }

    @Override
    public String getName() {
        return "Dummy";
    }
}
