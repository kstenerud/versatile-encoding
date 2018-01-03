package org.stenerud.remotefs.codec;

import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.Int128Holder;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

public class LittleEndianCodec {
    private static final Logger LOG = Logger.getLogger(LittleEndianCodec.class.getName());
    private final BinaryBuffer buffer;
    private final ByteBuffer byteBuffer;

    public LittleEndianCodec(BinaryBuffer buffer) {
        this.buffer = buffer;
        this.byteBuffer = ByteBuffer.wrap(buffer.data, buffer.startOffset, buffer.length);
        this.byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public int encodeInt8(int offset, int value) {
        byteBuffer.put(offset, (byte)value);
        return 1;
    }

    public int encodeInt16(int offset, int value) {
        byteBuffer.putShort(offset, (short)value);
        return 2;
    }

    public int encodeInt24(int offset, int value) {
        byteBuffer.putShort(offset, (short)value);
        byteBuffer.put(offset+2, (byte)(value>>16));
        return 3;
    }

    public int encodeInt32(int offset, int value) {
        byteBuffer.putInt(offset, value);
        return 4;
    }

    public int encodeInt40(int offset, long value) {
        byteBuffer.putInt(offset, (int)value);
        byteBuffer.put(offset+4, (byte)(value>>32));
        return 5;
    }

    public int encodeInt48(int offset, long value) {
        byteBuffer.putInt(offset, (int)value);
        byteBuffer.putShort(offset+4, (short) (value>>32));
        return 6;
    }

    public int encodeInt56(int offset, long value) {
        byteBuffer.putInt(offset, (int)value);
        byteBuffer.putShort(offset+4, (short) (value>>32));
        byteBuffer.put(offset+6, (byte)(value>>48));
        return 7;
    }

    public int encodeInt64(int offset, long value) {
        byteBuffer.putLong(offset, value);
        return 8;
    }

    public int encodeInt128(int offset, @Nonnull Int128Holder value) {
        byteBuffer.putLong(offset, value.lowWord);
        byteBuffer.putLong(offset+8, value.highWord);
        return 16;
    }

    public int encodeFloat32(int offset, float value) {
        return encodeInt32(offset, Float.floatToIntBits(value));
    }

    public int encodeFloat64(int offset, double value) {
        return encodeInt64(offset, Double.doubleToLongBits(value));
    }

    public int decodeInt8(int offset) {
        return buffer.data[offset];
    }

    private long decodeInt8Long(int offset) {
        return decodeInt8(offset);
    }

    public int decodeInt16(int offset) {
        return byteBuffer.getShort(offset);
    }

    private long decodeInt16Long(int offset) {
        return decodeInt16(offset);
    }

    public int decodeInt24(int offset) {
        return (decodeInt16(offset) & 0xffff) | (decodeInt8(offset + 2) << 16);
    }

    public int decodeInt32(int offset) {
        return byteBuffer.getInt(offset);
    }

    private long decodeInt32Long(int offset) {
        return (long)byteBuffer.getInt(offset);
    }

    public long decodeInt40(int offset) {
        return (decodeInt32Long(offset) & 0xffffffffL) | (decodeInt8Long(offset + 4) << 32);
    }

    public long decodeInt48(int offset) {
        return (decodeInt32Long(offset) & 0xffffffffL) | (decodeInt16Long(offset + 4) << 32);
    }

    public long decodeInt56(int offset) {
        return (decodeInt48(offset) & 0xffffffffffffL) | (decodeInt8Long(offset + 6) << 48);
    }

    public long decodeInt64(int offset) {
        return byteBuffer.getLong(offset);
    }

    public @Nonnull
    Int128Holder decodeInt128(int offset) {
        return new Int128Holder(byteBuffer.getLong(offset+8), byteBuffer.getLong(offset));
    }

    public float decodeFloat32(int offset) {
        return Float.intBitsToFloat(decodeInt32(offset));
    }

    public double decodeFloat64(int offset) {
        return Double.longBitsToDouble(decodeInt64(offset));
    }
}
