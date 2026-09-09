package me.andreasmelone.directtouch.pojav;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

import top.fifthlight.touchcontroller.proxy.client.MessageTransport;

public class DirectTouchTransport implements MessageTransport {
    private volatile boolean closed = false;
    private final ByteBuffer goingOnLarge = ByteBuffer.allocateDirect(65536);
    private boolean isEnd;

    @Override
    public void send(@NonNull ByteBuffer buffer) {
        if(closed) return;

        int length = buffer.remaining();

        // large
        boolean isLarge = false;
        if(buffer.hasRemaining() && buffer.getInt(buffer.position()) == 6) {
            isLarge = true;
            buffer.position(buffer.position() + 4);
            int messageLength = buffer.get();
            isEnd = buffer.get() != 0;

            if(buffer.hasArray() && !buffer.isReadOnly()) {
                byte[] array = buffer.array();
                goingOnLarge.put(array, buffer.position() + buffer.arrayOffset(), messageLength);
                buffer.position(buffer.position() + messageLength);
            } else {
                byte[] array = new byte[messageLength];
                buffer.get(array);
                goingOnLarge.put(array);
            }

            if(isEnd) {
                buffer = goingOnLarge;
                goingOnLarge.flip();
            } else {
                return;
            }
        }

        if(isLarge || buffer.isDirect()) {
            PojavDirectTouchNative.sendMessage(length, buffer.position(), buffer);
            if(isEnd) goingOnLarge.clear();
            return;
        }

        ByteBuffer direct = ByteBuffer.allocateDirect(length);
        if(buffer.hasArray() && !buffer.isReadOnly()) {
            byte[] array = buffer.array();
            direct.put(array, buffer.position() + buffer.arrayOffset(), length);
            buffer.position(buffer.position() + length);
        } else {
            byte[] array = new byte[length];
            buffer.get(array);
            direct.put(array);
        }

        PojavDirectTouchNative.sendMessage(length, 0, direct);
    }

    @Override
    public boolean receive(@NonNull ByteBuffer byteBuffer) {
        if(closed) return false;
        try {
            RawMessage polled = DirectTouchAndroid.MESSAGE_QUEUE.take();
            if(polled instanceof RawMessage.Close) return false;
            byteBuffer.put(((RawMessage.Data)polled).getBytes());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        this.closed = true;
        DirectTouchAndroid.MESSAGE_QUEUE.clear();
        DirectTouchAndroid.MESSAGE_QUEUE.put(new RawMessage.Close());
    }
}
