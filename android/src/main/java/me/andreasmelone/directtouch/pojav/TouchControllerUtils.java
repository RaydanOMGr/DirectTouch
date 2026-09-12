/*
 * This file originates from the Amethyst-Android project and is licensed under the GNU Lesser General Public License,
 * version 3 or later.
 * See the LICENSE file in the Amethyst-Android repository for the full license text.
 *
 * Repository: https://github.com/AngelAuraMC/Amethyst-Android
 * Path: app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/utils/TouchControllerUtils.java
 */
package me.andreasmelone.directtouch.pojav;

import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import top.fifthlight.touchcontroller.proxy.client.LauncherProxyClient;
import top.fifthlight.touchcontroller.proxy.client.MessageTransport;
import top.fifthlight.touchcontroller.proxy.client.PlatformCapability;
import top.fifthlight.touchcontroller.proxy.message.VibrateMessage;

import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class TouchControllerUtils {
    private TouchControllerUtils() {
    }

    public static LauncherProxyClient proxyClient;

    private static class VibrationHandler implements LauncherProxyClient.VibrationHandler {
        private final Vibrator vibrator;

        public VibrationHandler(Vibrator vibrator) {
            this.vibrator = vibrator;
        }

        @Override
        @SuppressWarnings("DEPRECATION")
        public void vibrate(@NonNull VibrateMessage.Kind kind) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(
                            VibrationEffect.createPredefined(
                                    VibrationEffect.EFFECT_TICK
                            )
                    );
                } else {
                    vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                    100,
                                    50
                            )
                    );
                }
            } else vibrator.vibrate(100);
        }
    }

    private static final SparseIntArray pointerIdMap = new SparseIntArray();
    private static int nextPointerId = 1;

    public static void processTouchEvent(MotionEvent motionEvent, View view) {
        if (proxyClient == null) {
            return;
        }
        int pointerId;
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pointerId = nextPointerId++;
                pointerIdMap.put(motionEvent.getPointerId(0), pointerId);
                proxyClient.addPointer(pointerId, motionEvent.getX(0) / view.getWidth(), motionEvent.getY(0) / view.getHeight());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                pointerId = nextPointerId++;
                int actionIndex = motionEvent.getActionIndex();
                pointerIdMap.put(motionEvent.getPointerId(actionIndex), pointerId);
                proxyClient.addPointer(pointerId, motionEvent.getX(actionIndex) / view.getWidth(), motionEvent.getY(actionIndex) / view.getHeight());
                break;
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < motionEvent.getPointerCount(); i++) {
                    pointerId = pointerIdMap.get(motionEvent.getPointerId(i));
                    if (pointerId == 0) {
                        Log.d("TouchController", "Move pointerId is 0");
                        continue;
                    }
                    proxyClient.addPointer(pointerId, motionEvent.getX(i) / view.getWidth(), motionEvent.getY(i) / view.getHeight());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (proxyClient != null) {
                    proxyClient.clearPointer();
                    pointerIdMap.clear();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (proxyClient != null) {
                    int i = motionEvent.getActionIndex();
                    pointerId = pointerIdMap.get(motionEvent.getPointerId(i));
                    if (pointerId == 0) {
                        Log.d("TouchController", "Pointer up pointerId is 0");
                        break;
                    }
                    pointerIdMap.delete(pointerId);
                    proxyClient.removePointer(pointerId);
                }
                break;
        }
    }

    public static void initialize(Context context, TouchControllerInputView touchControllerInputView) {
        if (proxyClient != null) {
            return;
        }

        MessageTransport transport = new DirectTouchTransport();
        proxyClient = new LauncherProxyClient(transport, Set.of(PlatformCapability.TEXT_STATUS, PlatformCapability.KEYBOARD_SHOW));
        proxyClient.run();
        touchControllerInputView.setClient(proxyClient);
        Vibrator vibrator = ContextCompat.getSystemService(context, Vibrator.class);
        if (vibrator != null && checkSelfPermission(context, android.Manifest.permission.VIBRATE)
                == PackageManager.PERMISSION_GRANTED) {
            LauncherProxyClient.VibrationHandler vibrationHandler = new VibrationHandler(vibrator);
            proxyClient.setVibrationHandler(vibrationHandler);
        }
    }
}