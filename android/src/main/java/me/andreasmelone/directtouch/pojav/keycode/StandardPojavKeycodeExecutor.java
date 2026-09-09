package me.andreasmelone.directtouch.pojav.keycode;

import android.view.KeyEvent;

import net.kdt.pojavlaunch.EfficientAndroidLWJGLKeycode;

public class StandardPojavKeycodeExecutor implements KeycodeExecutor {
    @Override
    public boolean init() {
        try {
            EfficientAndroidLWJGLKeycode.class.getDeclaredMethod("getIndexByKey", int.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    @Override
    public void exec(KeyEvent event) {
        int index = EfficientAndroidLWJGLKeycode.getIndexByKey(event.getKeyCode());
        if (EfficientAndroidLWJGLKeycode.containsIndex(index)) {
            EfficientAndroidLWJGLKeycode.execKey(event, index);
        }
    }

    @Override
    public String getName() {
        return "PojIntegr";
    }
}
