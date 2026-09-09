package me.andreasmelone.directtouch.pojav;

public interface RawMessage {
    final class Data implements RawMessage {
        private final byte[] bytes;

        public Data(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return this.bytes;
        }
    }

    final class Close implements RawMessage {
    }
}
