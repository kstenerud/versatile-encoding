package org.stenerud.remotefs;

import javax.annotation.Nonnull;

public interface IntegerCodec {
    public int getMaxValue();

    public int getEncodedLength(int value);

    public int getMaxEncodedLength();

    public void encode(@Nonnull byte[] buffer, int offset, int value);

    public int decode(@Nonnull byte[] buffer, int offset);

    // One-Two encoding (little endian): vvvvvvvS vvvvvvvv
    // Where v = value, S = size bit.
    // When S = 1, second byte is present.
    // 1-byte form supports values to 127
    // 2-byte form supports values to 32k - 1
    public static class OneTwo implements IntegerCodec {
        private static final int MASKS[] = {0x0000007f, 0x00007fff};
        private final LittleEndianCodec endianCodec = new LittleEndianCodec();

        @Override
        public int getMaxValue() {
            return MASKS[1];
        }

        @Override
        public int getEncodedLength(int value) {
            return value > MASKS[0] ? 2 : 1;
        }

        @Override
        public int getMaxEncodedLength() {
            return getEncodedLength(getMaxValue());
        }

        @Override
        public void encode(@Nonnull byte[] buffer, int offset, int value) {
            if(value > getMaxValue()) {
                throw new IllegalArgumentException("Value " + value + " is larger than maximum of " + getMaxValue());
            }
            value <<= 1;
            buffer[offset] = (byte)value;
            if(value > 0xff) {
                buffer[offset] |= 1;
                buffer[offset + 1] = (byte)(value >> 8);
            }
        }

        @Override
        public int decode(@Nonnull byte[] buffer, int offset) {
            int value = endianCodec.decodeInt16(buffer, offset);
            int sizeFlag = value & 1;
            value >>= 1;
            return value & MASKS[sizeFlag];
        }
    }

    // One-Three encoding (little endian): vvvvvvvS vvvvvvvv vvvvvvvv
    // Where v = value, S = size bit.
    // When S = 1, second two bytes are present.
    // 1-byte form supports values to 127
    // 3-byte form supports values to 8M - 1
    public static class OneThree implements IntegerCodec{
        private static final int MASKS[] = {0x0000007f, 0x007fffff};
        private final LittleEndianCodec lowLevelCodec = new LittleEndianCodec();

        @Override
        public int getMaxValue() {
            return MASKS[1];
        }

        @Override
        public int getEncodedLength(int value) {
            return value > MASKS[0] ? 3 : 1;
        }

        @Override
        public int getMaxEncodedLength() {
            return getEncodedLength(getMaxValue());
        }

        @Override
        public void encode(@Nonnull byte[] buffer, int offset, int value) {
            if(value > getMaxValue()) {
                throw new IllegalArgumentException("Value " + value + " is larger than maximum of " + getMaxValue());
            }
            value <<= 1;
            buffer[offset] = (byte)value;
            if(value > 0xff) {
                buffer[offset] |= 1;
                buffer[offset + 1] = (byte)(value >> 8);
                buffer[offset + 2] = (byte)(value >> 16);
            }
        }

        @Override
        public int decode(@Nonnull byte[] buffer, int offset) {
            int value = lowLevelCodec.decodeInt32(buffer, offset);
            int sizeFlag = value & 1;
            value >>= 1;
            return value & MASKS[sizeFlag];
        }
    }
}
