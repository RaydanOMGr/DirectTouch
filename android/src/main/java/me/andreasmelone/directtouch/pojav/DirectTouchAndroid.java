package me.andreasmelone.directtouch.pojav;

import static me.andreasmelone.directtouch.pojav.launcher.IPojavLauncher.getId;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.Keep;

import me.andreasmelone.directtouch.pojav.launcher.DummyPojavLauncher;
import me.andreasmelone.directtouch.pojav.launcher.IPojavLauncher;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import me.andreasmelone.directtouch.pojav.keycode.MojoKeycodeExecutor;
import me.andreasmelone.directtouch.pojav.keycode.KeycodeExecutor;
import me.andreasmelone.directtouch.pojav.keycode.LegacyMojoKeycodeExecutor;
import me.andreasmelone.directtouch.pojav.keycode.StandardPojavKeycodeExecutor;
import me.andreasmelone.directtouch.pojav.launcher.MojoLauncher;
import me.andreasmelone.directtouch.pojav.launcher.StandardPojavLauncher;
import me.andreasmelone.directtouch.pojav.logger.DirectTouchLog;
import me.andreasmelone.directtouch.pojav.touchcontroller.TouchControllerInputView;
import me.andreasmelone.directtouch.pojav.touchcontroller.TouchControllerUtils;

@Keep
@SuppressWarnings("unused")
public class DirectTouchAndroid {
    public static KeycodeExecutor KEYCODE_EXECUTOR = new KeycodeExecutor.NoopFallback();
    public static IPojavLauncher POJAV = new DummyPojavLauncher();

    static final BlockingQueue<RawMessage> MESSAGE_QUEUE = new LinkedBlockingQueue<>();

    @Keep
    public static void setupTouchController(String nativeLibPath) {
        try {
            Log.i("PojIntegr", "Loading lib from " + nativeLibPath);
            System.load(nativeLibPath);
        } catch (Throwable e) {
            Log.e("PojIntegr", "Couldn't load library", e);
        }

        try {
            selectPojavBackend();

            Activity activity = POJAV.getMainActivity();
            if(activity == null) {
                DirectTouchLog.error("DirectTouch", "Failed to get main activity, exiting!");
                return;
            }

            Resources res = activity.getResources();
            String packageName = activity.getPackageName();

            selectKeycodeExecutor();
            DirectTouchLog.verbose("DirectTouch", "Using " + KEYCODE_EXECUTOR.getName() + " keycode executor!");

            FrameLayout container = activity.findViewById(getId(activity, "content_frame"));
            FrameLayout layout = container.findViewById(getId(activity, "main_control_layout"));
            View glSurface = container.findViewById(getId(activity, "main_game_render_view"));

            TouchControllerInputView touchControllerInputView = new TouchControllerInputView(activity);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    (int)POJAV.dpToPx(1),
                    (int)POJAV.dpToPx(1)
            );
            params.gravity = Gravity.BOTTOM;
            touchControllerInputView.setLayoutParams(params);
            glSurface.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if(view != glSurface) return;
                touchControllerInputView.setSize(glSurface.getWidth(), glSurface.getHeight());
            });

            POJAV.runOnUiThread(() -> {
                layout.addView(touchControllerInputView);
            });

            Window window = activity.getWindow();
            Window.Callback original = window.getCallback();

            window.setCallback(new DelegatedWindow(original) {
                @Override
                public boolean dispatchTouchEvent(MotionEvent motionEvent) {
                    int[] location = new int[2];
                    glSurface.getLocationInWindow(location);

                    MotionEvent adjustedEvent = MotionEvent.obtain(motionEvent);
                    // accounts for the position of the view in the hierarchy, as
                    // it does not occupy the entire activity screen and may be offset to the right or bottom,
                    // for example due to the notch taking part of the screen
                    // I can actually not recreate the bug
                    adjustedEvent.offsetLocation(-location[0], -location[1]);

                    try {
                        TouchControllerUtils.processTouchEvent(adjustedEvent, glSurface);
                    } finally {
                        adjustedEvent.recycle();
                    }

                    return super.dispatchTouchEvent(motionEvent);
                }
            });
            TouchControllerUtils.initialize(activity, touchControllerInputView);
            POJAV.tryDisableMojoPanning();
            DirectTouchLog.info("DirectTouch", "Successfully initialized direct touch backend!");
        } catch (Throwable e) {
            DirectTouchLog.error("PojIntegr", "Something went wrong", e);
        }
    }

    private static void selectKeycodeExecutor() {
        KEYCODE_EXECUTOR = new MojoKeycodeExecutor();
        if(KEYCODE_EXECUTOR.init()) return;

        KEYCODE_EXECUTOR = new LegacyMojoKeycodeExecutor();
        if(KEYCODE_EXECUTOR.init()) return;

        KEYCODE_EXECUTOR = new StandardPojavKeycodeExecutor();
        if(KEYCODE_EXECUTOR.init()) return;

        KEYCODE_EXECUTOR = new KeycodeExecutor.NoopFallback();
        KEYCODE_EXECUTOR.init();
    }

    private static void selectPojavBackend() {
        try {
            if (classExists("git.artdeell.mojo.Tools")) {
                POJAV = new MojoLauncher();
            } else {
                POJAV = new StandardPojavLauncher();
            }
        } catch (Exception e) {
            DirectTouchLog.error("PojIntegr", "Failed to initialize pojav backend", e);
            POJAV = new DummyPojavLauncher();
        }
        DirectTouchLog.info("PojIntegr", "Selected pojav backend " + POJAV.getName());
    }

    @Keep
    public static void receiveMessage(byte[] bytes) {
        try {
            MESSAGE_QUEUE.put(new RawMessage.Data(bytes));
        } catch (Throwable e) {
            DirectTouchLog.error("PojIntegr", "Something went wrong", e);
        }
    }

    @Keep
    public static void close() {
        try {
            TouchControllerUtils.proxyClient.close();
        } catch (Throwable e) {
            DirectTouchLog.error("PojIntegr", "Something went wrong", e);
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}