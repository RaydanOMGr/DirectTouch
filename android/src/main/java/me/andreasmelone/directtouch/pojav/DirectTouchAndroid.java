package me.andreasmelone.directtouch.pojav;

import static net.kdt.pojavlaunch.Tools.dpToPx;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.RectF;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.MinecraftGLSurface;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import me.andreasmelone.directtouch.pojav.keycode.KeycodeExecutor;
import me.andreasmelone.directtouch.pojav.keycode.MojoKeycodeExecutor;
import me.andreasmelone.directtouch.pojav.keycode.StandardPojavKeycodeExecutor;
import top.fifthlight.touchcontroller.proxy.client.LauncherProxyClient;
import top.fifthlight.touchcontroller.proxy.client.MessageTransport;
import top.fifthlight.touchcontroller.proxy.client.PlatformCapability;
import top.fifthlight.touchcontroller.proxy.message.InitializeMessage;
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage;

@Keep
@SuppressWarnings("unused")
public class DirectTouchAndroid {
    public static KeycodeExecutor KEYCODE_EXECUTOR = new KeycodeExecutor.NoopFallback();

    static final BlockingQueue<RawMessage> MESSAGE_QUEUE = new LinkedBlockingQueue<>();

    @Keep
    public static void setupTouchController(String nativeLibPath) {
        try {
            System.load(nativeLibPath);
        } catch (Exception e) {
            Log.e("PojIntegr", "Couldn't load library", e);
        }

        try {
            MainActivity activity = (MainActivity) MainActivity.touchCharInput.getContext();

            Resources res = activity.getResources();
            String packageName = activity.getPackageName();

            KEYCODE_EXECUTOR = new MojoKeycodeExecutor();
            if (!KEYCODE_EXECUTOR.init()) {
                KEYCODE_EXECUTOR = new StandardPojavKeycodeExecutor();
                if (!KEYCODE_EXECUTOR.init()) {
                    KEYCODE_EXECUTOR = new KeycodeExecutor.NoopFallback();
                }
            }
            Log.v("DirectTouch", "Using " + KEYCODE_EXECUTOR.getName() + " keycode executor!");

            FrameLayout container = activity.findViewById(getId(activity, "content_frame"));
            ControlLayout layout = container.findViewById(getId(activity, "main_control_layout"));
            View glSurface = container.findViewById(getId(activity, "main_game_render_view"));

            TouchControllerInputView touchControllerInputView = new TouchControllerInputView(activity);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    (int)dpToPx(1),
                    (int)dpToPx(1)
            );
            params.gravity = Gravity.BOTTOM;
            touchControllerInputView.setLayoutParams(params);
            glSurface.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if(view != glSurface) return;
                touchControllerInputView.setSize(glSurface.getWidth(), glSurface.getHeight());
            });

            Tools.runOnUiThread(() -> {
                layout.addView(touchControllerInputView);
            });

            Window window = activity.getWindow();
            Window.Callback original = window.getCallback();

            window.setCallback(new DelegatedWindow(original) {
                @Override
                public boolean dispatchTouchEvent(MotionEvent motionEvent) {
                    TouchControllerUtils.processTouchEvent(motionEvent, glSurface);
                    return super.dispatchTouchEvent(motionEvent);
                }
            });
            TouchControllerUtils.initialize(activity, touchControllerInputView);
            tryDisableMojoPanning();
            Log.i("DirectTouch", "Successfully initialized direct touch backend!");
        } catch (Exception e) {
            Log.e("PojIntegr", "Something went wrong", e);
        }
    }

    @Keep
    public static void receiveMessage(byte[] bytes) {
        try {
            MESSAGE_QUEUE.put(new RawMessage.Data(bytes));
        } catch (Exception e) {
            Log.e("PojIntegr", "Something went wrong", e);
        }
    }

    @Keep
    public static void close() {
        try {
            TouchControllerUtils.proxyClient.close();
        } catch (Exception e) {
            Log.e("PojIntegr", "Something went wrong", e);
        }
    }

    @SuppressLint("DiscouragedApi")
    private static int getId(Activity activity, String name) {
        return activity.getResources().getIdentifier(name, "id", activity.getPackageName());
    }

    private static void tryDisableMojoPanning() {
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
            Log.i("DirectTouch", "Disabled mojo panning!");
        } catch (Exception e) {
            Log.e("DirectTouch", "Failed to disable mojo panning", e);
        }
    }
}