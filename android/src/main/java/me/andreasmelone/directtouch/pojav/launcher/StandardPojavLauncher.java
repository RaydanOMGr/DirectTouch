package me.andreasmelone.directtouch.pojav.launcher;

import android.app.Activity;
import android.view.View;
import me.andreasmelone.directtouch.pojav.logger.DirectTouchLog;
import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.lang.reflect.Field;
import java.util.Objects;

public class StandardPojavLauncher implements IPojavLauncher {
    @Override
    public Activity getMainActivity() {
        try {
            return (Activity) MainActivity.touchCharInput.getContext();
        } catch (Throwable e) {
            DirectTouchLog.error(getName(), "Failed to get activity", e);
            DirectTouchLog.warn(getName(), "Falling back to reflection");
            try {
                Class<?> mainActivityClass = Class.forName("net.kdt.pojavlaunch.game.GameActivity");
                Field touchCharInputField = mainActivityClass.getDeclaredField("touchCharInput");
                View touchCharInput = (View) touchCharInputField.get(null);
                Activity context = (Activity) Objects.requireNonNull(touchCharInput).getContext();
                DirectTouchLog.info(getName(), "Successfully retrieved context reflectively, previous messages may be ignored");
                return context;
            } catch (Throwable ex) {
                DirectTouchLog.error(getName(), "Failed to get activity reflectively", e);
                return null;
            }
        }
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
