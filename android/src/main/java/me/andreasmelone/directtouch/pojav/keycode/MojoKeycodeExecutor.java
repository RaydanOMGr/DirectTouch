package me.andreasmelone.directtouch.pojav.keycode;

import android.view.KeyEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MojoKeycodeExecutor implements KeycodeExecutor {
    private static Method sendKeyEvent;
    private static Object platform;

    @Override
    public boolean init() {
        try {
            Class<?> platformClass =
                    Class.forName("net.kdt.pojavlaunch.game.platform.Platform");

            Field platformField = platformClass.getField("PLATFORM");
            platform = platformField.get(null);
            if(platform == null) return false;

            sendKeyEvent = platform.getClass().getMethod(
                    "sendKeyEvent",
                    int.class,
                    int.class,
                    int.class,
                    char.class
            );

            return true;
        } catch (Throwable ignored) {
            platform = null;
            sendKeyEvent = null;
            return false;
        }
    }

    @Override
    public void exec(KeyEvent event) {
        try {
            char key = (char) event.getUnicodeChar();

            sendKeyEvent.invoke(
                    platform,
                    event.getKeyCode(),
                    event.getAction() == KeyEvent.ACTION_DOWN ? 1 : 0,
                    getCurrentMods(event),
                    key
            );
        } catch (Throwable ignored) {
        }
    }

    @Override
    public String getName() {
        return "MojIntegr";
    }

    private static int getCurrentMods(KeyEvent event) {
        int value = 0;

        if (event.isAltPressed()) value |= 4;
        if (event.isCapsLockOn()) value |= 16;
        if (event.isCtrlPressed()) value |= 2;
        if (event.isNumLockOn()) value |= 32;
        if (event.isShiftPressed()) value |= 1;

        return value;
    }
}