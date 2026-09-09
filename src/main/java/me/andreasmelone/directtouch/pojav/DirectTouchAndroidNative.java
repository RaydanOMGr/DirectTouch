package me.andreasmelone.directtouch.pojav;

import com.mojang.logging.LogUtils;

import org.intellij.lang.annotations.MagicConstant;

import java.nio.ByteBuffer;

import me.andreasmelone.directtouch.DirectTouchClient;
import top.fifthlight.touchcontroller.proxy.message.CapabilityMessage;
import top.fifthlight.touchcontroller.proxy.message.InitializeMessage;
import top.fifthlight.touchcontroller.proxy.message.InputStatusMessage;
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage;

@SuppressWarnings("UnsafeDynamicallyLoadedCode")
public class DirectTouchAndroidNative {
    public static final int INIT_SUCCESS = 0x00;
    public static final int INIT_GENERIC_ERROR = 0xFFFFFFFF;
    public static final int INIT_DEX_NOT_INITIALIZED = 0xFFFFFFFE;
    public static final int INIT_DVM_NOT_FOUND = 0xFFFFFFFD;
    public static final int INIT_METHOD_NOT_INITIALIZED = 0xFFFFFFFC;

    public static final String NATIVE_PATH;

    @MagicConstant(intValues = {
            INIT_SUCCESS, INIT_GENERIC_ERROR, INIT_DEX_NOT_INITIALIZED, INIT_DVM_NOT_FOUND, INIT_METHOD_NOT_INITIALIZED
    })
    public static native int init(String ownPath);
    public static native void setDexData(byte[] data);
    public static native void sendMessage(int size, ByteBuffer messageBytes);
    public static native void close();

    private static void receiveMessage(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        ProxyMessage decode = ProxyMessage.Companion.decode(buffer.getInt(), buffer);
        DirectTouchClient.receiveMessage(decode);
    }

    static {
        NATIVE_PATH = AndroidLibLoader.INSTANCE.get("directtouch");
        LogUtils.getLogger().info("Loading natives from {}", NATIVE_PATH);
        System.load(NATIVE_PATH);
    }
}