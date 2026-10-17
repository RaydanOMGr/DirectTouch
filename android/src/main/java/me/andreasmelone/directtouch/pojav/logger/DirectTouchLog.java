package me.andreasmelone.directtouch.pojav.logger;

import android.util.Log;

import me.andreasmelone.directtouch.pojav.PojavDirectTouchNative;

public class DirectTouchLog {
    public static void verbose(String tag, String message) {
        Log.v(tag, message);
        PojavDirectTouchNative.logMessage("V | [" + tag + "] " + message);
    }

    public static void info(String tag, String message) {
        Log.v(tag, message);
        PojavDirectTouchNative.logMessage("I | [" + tag + "] " + message);
    }

    public static void error(String tag, String message) {
        Log.v(tag, message);
        PojavDirectTouchNative.logMessage("E | [" + tag + "] " + message);
    }

    public static void error(String tag, String message, Throwable thr) {
        Log.v(tag, message);
        StringBuilder sb = new StringBuilder();
        sb.append("E | [").append(tag).append("] ").append(message).append("\n")
                .append(thr.getMessage()).append("\n");
        for (StackTraceElement stackTraceElement : thr.getStackTrace()) {
            sb.append("\tat ").append(stackTraceElement.toString());
        }

        PojavDirectTouchNative.logMessage(sb.toString());
    }

    public static void warn(String tag, String message) {
        Log.v(tag, message);
        PojavDirectTouchNative.logMessage("W | [" + tag + "] " + message);
    }
}
