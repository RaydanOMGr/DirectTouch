package me.andreasmelone.directtouch.pojav.launcher;

import android.app.Activity;
import me.andreasmelone.directtouch.pojav.logger.DirectTouchLog;
import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.lang.reflect.Field;

public class StandardPojavLauncher implements IPojavLauncher {
    @Override
    public Activity getMainActivity() {
        return (Activity) MainActivity.touchCharInput.getContext();
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
        try {
            Class<?> launcherPrefs = LauncherPreferences.class;
            Field autoPanning;
            try {
                autoPanning = launcherPrefs.getDeclaredField("PREF_KEYBOARD_AUTOPANNING");
            } catch (NoSuchFieldException ignored) {
                // not mojo then
                return;
            }
            autoPanning.setBoolean(null, false);
            DirectTouchLog.info("DirectTouch", "Disabled mojo panning!");
        } catch (Exception e) {
            DirectTouchLog.error("DirectTouch", "Failed to disable mojo panning", e);
        }
    }

    @Override
    public String getName() {
        return "PojIntegr";
    }
}
