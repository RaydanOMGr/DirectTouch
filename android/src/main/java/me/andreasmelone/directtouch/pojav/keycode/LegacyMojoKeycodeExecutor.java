package me.andreasmelone.directtouch.pojav.keycode;

import android.util.Log;
import android.view.KeyEvent;

import net.kdt.pojavlaunch.EfficientAndroidLWJGLKeycode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class LegacyMojoKeycodeExecutor implements KeycodeExecutor {
    private Class<?> glfwClass;
    private Method sendKeyMethod;

    private Class<?> callbackBridgeClass;
    private Field holdingAltField;
    private Field holdingCapslockField;
    private Field holdingCtrlField;
    private Field holdingNumlockField;
    private Field holdingShiftField;
    private Method getCurrentModsMethod;

    private int[] androidKeycodes;

    @Override
    public boolean init() {
        try {
            glfwClass = Class.forName("git.artdeell.dnbootstrap.glfw.GLFW");
            sendKeyMethod = glfwClass.getDeclaredMethod("sendKeyEvent", int.class, boolean.class, int.class);

            callbackBridgeClass = Class.forName("net.kdt.pojavlaunch.CallbackBridge");
            holdingAltField = callbackBridgeClass.getDeclaredField("holdingAlt");
            holdingCapslockField = callbackBridgeClass.getDeclaredField("holdingCapslock");
            holdingCtrlField = callbackBridgeClass.getDeclaredField("holdingCtrl");
            holdingNumlockField = callbackBridgeClass.getDeclaredField("holdingNumlock");
            holdingShiftField = callbackBridgeClass.getDeclaredField("holdingShift");
            getCurrentModsMethod = callbackBridgeClass.getDeclaredMethod("getCurrentMods");

            Field androidKeycodesField = EfficientAndroidLWJGLKeycode.class.getDeclaredField("sAndroidKeycodes");
            androidKeycodesField.setAccessible(true);
            androidKeycodes = (int[]) androidKeycodesField.get(null);
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException |
                 IllegalAccessException e) {
            return false;
        }
        return true;
    }

    @Override
    public void exec(KeyEvent event) {
        try {
            int index = getIndexByKey(event.getKeyCode());

            if (index >= 0) {
                holdingAltField.setBoolean(null, event.isAltPressed());
                holdingCapslockField.setBoolean(null, event.isCapsLockOn());
                holdingCtrlField.setBoolean(null, event.isCtrlPressed());
                holdingNumlockField.setBoolean(null, event.isNumLockOn());
                holdingShiftField.setBoolean(null, event.isShiftPressed());

                sendKeyEvent(
                        EfficientAndroidLWJGLKeycode.getValueByIndex(index),
                        event.getAction() == KeyEvent.ACTION_DOWN,
                        getCurrentMods()
                );
            }
        } catch (IllegalAccessException e) {
            Log.e(getName(), "Failed to exec key event", e);
        }
    }

    @Override
    public String getName() {
        return "MojIntegr Legacy";
    }

    private int getIndexByKey(int key) {
        return Arrays.binarySearch(androidKeycodes, key);
    }

    private void sendKeyEvent(int glfwCode, boolean state, int mods) {
        try {
            sendKeyMethod.invoke(null, glfwCode, state, mods);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Log.e(getName(), "Failed to invoke sendKeyEvent", e);
        }
    }

    private int getCurrentMods() {
        try {
            return (int) getCurrentModsMethod.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NullPointerException e) {
            Log.e(getName(), "Failed to invoke getCurrentMods", e);
            return 0;
        }
    }
}
