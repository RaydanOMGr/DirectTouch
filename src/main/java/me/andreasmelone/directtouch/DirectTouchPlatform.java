package me.andreasmelone.directtouch;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

import me.andreasmelone.directtouch.pojav.DirectTouchAndroidNative;
import top.fifthlight.combine.core.data.Text;
import top.fifthlight.touchcontroller.common.platform.Platform;
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage;

public class DirectTouchPlatform implements Platform {
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(65536);

    @Override
    public @NonNull Text getName() {
        return Text.Companion.literal("DirectTouch (running on PojIntegr)");
    }

    @Override
    public boolean getUseDefaultInputHandler() {
        return true;
    }

    @Override
    public @Nullable ProxyMessage pollEvent() {
        return DirectTouchClient.PROXY_MESSAGE_QUEUE.poll();
    }

    @Override
    public void sendEvent(@NonNull ProxyMessage proxyMessage) {
        buffer.clear();
        proxyMessage.encode(buffer);

        int length = buffer.position();
        buffer.flip();

        DirectTouchAndroidNative.sendMessage(length, buffer);
    }
}
