package org.stenerud.remotefs.codec;

import javax.annotation.Nonnull;

public interface IntegerCodec {
    public int getMaxValue();

    public int getRequiredEncodingLength(int value);

    public int getRequiredAdditionalBytesCount(int firstByte);

    public int getEncodedLength(int offset);

    public int getMaxEncodedLength();

    public int encode(int offset, int value);

    public int decode(int offset);

    // One-Two encoding (little endian): vvvvvvvS vvvvvvvv
    // Where v = value, S = size bit.
    // When S = 1, second byte is present.
    // 1-byte form supports values to 127
    // 2-byte form supports values to 32k - 1
    public static class OneTwo implements IntegerCodec {
        private static final int MASKS[] = {0x0000007f, 0x00007fff};
        public static final int MAX_VALUE = MASKS[1];
        public static final int MAX_LENGTH = 2;
        private final LittleEndianCodec endianCodec;

        public OneTwo(@Nonnull LittleEndianCodec endianCodec) {
            this.endianCodec = endianCodec;
        }

        @Override
        public int getMaxValue() {
            return MASKS[1];
        }

        @Override
        public int getRequiredEncodingLength(int value) {
            return (value<<1) > 0xff ? 2 : 1;
        }

        @Override
        public int getRequiredAdditionalBytesCount(int firstByte) {
            return (firstByte & 1) == 1 ? 1 : 0;
        }

        @Override
        public int getEncodedLength(int offset) {
            return (endianCodec.decodeInt8(offset) & 1) == 1 ? 3 : 1;
        }

        @Override
        public int getMaxEncodedLength() {
            return getRequiredEncodingLength(getMaxValue());
        }

        @Override
        public int encode(int offset, int value) {
            if(value > getMaxValue()) {
                throw new IllegalArgumentException("Value " + value + " is larger than maximum of " + getMaxValue());
            }
            value <<= 1;
            if(value <= 0xff) {
                return endianCodec.encodeInt8(offset, value);
            }
            return endianCodec.encodeInt16(offset, value | 1);
        }

        @Override
        public int decode(int offset) {
            int value = endianCodec.decodeInt16(offset);
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
        public static final int MAX_VALUE = MASKS[1];
        public static final int MAX_LENGTH = 3;
        private final LittleEndianCodec endianCodec;

        public OneThree(@Nonnull LittleEndianCodec endianCodec) {
            this.endianCodec = endianCodec;
        }

        @Override
        public int getMaxValue() {
            return MASKS[1];
        }

        @Override
        public int getRequiredEncodingLength(int value) {
            return (value<<1) > 0xff ? 3 : 1;
        }

        @Override
        public int getRequiredAdditionalBytesCount(int firstByte) {
            return (firstByte & 1) == 1 ? 2 : 0;
        }

        @Override
        public int getEncodedLength(int offset) {
            return (endianCodec.decodeInt8(offset) & 1) == 1 ? 3 : 1;
        }

        @Override
        public int getMaxEncodedLength() {
            return getRequiredEncodingLength(getMaxValue());
        }

        @Override
        public int encode(int offset, int value) {
            if(value > getMaxValue()) {
                throw new IllegalArgumentException("Value " + value + " is larger than maximum of " + getMaxValue());
            }
            value <<= 1;
            if(value <= 0xff) {
                return endianCodec.encodeInt8(offset, value);
            }
            return endianCodec.encodeInt24(offset, value | 1);
        }

        @Override
        public int decode(int offset) {
            int value = endianCodec.decodeInt8(offset);
            int sizeFlag = value & 1;
            if(sizeFlag != 0) {
                value = (value & 0xff) | endianCodec.decodeInt16(offset+1);
            }
            value >>>= 1;
            return value & MASKS[sizeFlag];
        }
    }
}
