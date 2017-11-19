package org.stenerud.remotefs;

public class LittleEndianCodec {
    public static int encodeInt16(int value, byte[] buffer, int offset) {
        buffer[offset++] = (byte) value;
        buffer[offset]   = (byte) (value >> 8);
        return 2;
    }

    public static int encodeInt32(int value, byte[] buffer, int offset) {
        buffer[offset++] = (byte) value;
        buffer[offset++] = (byte) (value >> 8);
        buffer[offset++] = (byte) (value >> 16);
        buffer[offset]   = (byte) (value >> 24);
        return 4;
    }

    public static int encodeInt64(long value, byte[] buffer, int offset) {
        buffer[offset++] = (byte) value;
        buffer[offset++] = (byte) (value >> 8);
        buffer[offset++] = (byte) (value >> 16);
        buffer[offset++] = (byte) (value >> 24);
        buffer[offset++] = (byte) (value >> 32);
        buffer[offset++] = (byte) (value >> 40);
        buffer[offset++] = (byte) (value >> 48);
        buffer[offset]   = (byte) (value >> 56);
        return 8;
    }

    public static int encodeFloat32(float value, byte[] buffer, int offset) {
        return encodeInt32(Float.floatToIntBits(value), buffer, offset);
    }

    public static int encodeFloat64(double value, byte[] buffer, int offset) {
        return encodeInt64(Double.doubleToLongBits(value), buffer, offset);
    }

    public static int decodeInt16(byte[] buffer, int offset) {
        return (short)(((((short)buffer[offset+1]) & 0xff) << 8) +
                        (((short)buffer[offset]) & 0xff));
    }

    public static int decodeInt32(byte[] buffer, int offset) {
        return ((((int)buffer[offset+3]) & 0xff) << 24) +
                ((((int)buffer[offset+2]) & 0xff) << 16) +
                ((((int)buffer[offset+1]) & 0xff) << 8) +
                (((int)buffer[offset]) & 0xff);
    }

    public static long decodeInt64(byte[] buffer, int offset) {
        return ((((long)buffer[offset+7]) & 0xff) << 56) +
                ((((long)buffer[offset+6]) & 0xff) << 48) +
                ((((long)buffer[offset+5]) & 0xff) << 40) +
                ((((long)buffer[offset+4]) & 0xff) << 32) +
                ((((long)buffer[offset+3]) & 0xff) << 24) +
                ((((long)buffer[offset+2]) & 0xff) << 16) +
                ((((long)buffer[offset+1]) & 0xff) << 8) +
                (((long)buffer[offset]) & 0xff);
    }

    public static float decodeFloat32(byte[] buffer, int offset) {
        return Float.intBitsToFloat(decodeInt32(buffer, offset));
    }

    public static double decodeFloat64(byte[] buffer, int offset) {
        return Double.longBitsToDouble(decodeInt64(buffer, offset));
    }
}
