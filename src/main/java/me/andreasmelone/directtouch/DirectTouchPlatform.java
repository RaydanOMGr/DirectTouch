package me.andreasmelone.directtouch;

import java.nio.ByteBuffer;

import me.andreasmelone.directtouch.pojav.DirectTouchAndroidNative;
import top.fifthlight.touchcontroller.common.platform.Platform;
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage;

//? if touchcontroller: >=0.3
import top.fifthlight.combine.core.data.Text;
public class DirectTouchPlatform implements Platform {
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(65536);

    //? if touchcontroller: >=0.3 {
    @Override
    public Text getName() {
        return Text.Companion.literal("DirectTouch (running on PojIntegr)");
    }

    @Override
    public boolean getUseDefaultInputHandler() {
        return true;
    }
    //? } else {
    /*@Override
    public void resize(int width, int height) {
    }
    *///? }

    @Override
    public ProxyMessage pollEvent() {
        return DirectTouchClient.PROXY_MESSAGE_QUEUE.poll();
    }

    @Override
    public void sendEvent(ProxyMessage proxyMessage) {
        buffer.clear();
        proxyMessage.encode(buffer);

        int length = buffer.position();
        buffer.flip();

        DirectTouchAndroidNative.sendMessage(length, buffer);
    }
}
