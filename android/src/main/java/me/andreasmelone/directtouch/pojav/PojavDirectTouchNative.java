package me.andreasmelone.directtouch.pojav;

import java.nio.ByteBuffer;

public class PojavDirectTouchNative {
    public static native void sendMessage(int size, int offset, ByteBuffer buffer);
}
