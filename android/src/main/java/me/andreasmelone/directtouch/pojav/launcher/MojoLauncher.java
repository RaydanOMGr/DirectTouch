package me.andreasmelone.directtouch.pojav.launcher;

import android.app.Activity;
import git.artdeell.mojo.Tools;
import git.artdeell.mojo.game.GameActivity;
import git.artdeell.mojo.prefs.LauncherPreferences;
import me.andreasmelone.directtouch.pojav.logger.DirectTouchLog;

public class MojoLauncher implements IPojavLauncher {
    @Override
    public Activity getMainActivity() {
        return (Activity) GameActivity.touchCharInput.getContext();
    }

    @Override
    public float dpToPx(float dp) {
        return Tools.dpToPx(dp);
    }

    @Override
    public void runOnUiThread(Runnable runnable) {
        Tools.runOnUiThread(runnable);
    }

    @Override
    public void tryDisableMojoPanning() {
        LauncherPreferences.PREF_KEYBOARD_AUTOPANNING = false;
        DirectTouchLog.info("DirectTouch", "Disabled mojo panning!");
    }

    @Override
    public String getName() {
        return "MojIntegr";
    }
}
