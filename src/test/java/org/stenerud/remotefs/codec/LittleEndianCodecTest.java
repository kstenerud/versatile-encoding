package org.stenerud.remotefs.codec;

import org.junit.Test;
import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.DeepEquality;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class LittleEndianCodecTest {
    private static final Map<Number, BinaryBuffer> TEST_DATA = new HashMap<>();
    static {
        TEST_DATA.put((short)0x0001, newBinaryBuffer((byte) 0x01, (byte) 0x00));
        TEST_DATA.put((short)0xa8f1, newBinaryBuffer((byte) 0xf1, (byte) 0xa8));
        TEST_DATA.put((int)0x12345678, newBinaryBuffer((byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12));
        TEST_DATA.put((int)0xff073465, newBinaryBuffer((byte) 0x65, (byte) 0x34, (byte) 0x07, (byte) 0xff));
        TEST_DATA.put(0x123456789abcdef0l, newBinaryBuffer((byte) 0xf0, (byte) 0xde, (byte) 0xbc, (byte) 0x9a, (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12));
        TEST_DATA.put(0xc640ff9a745cb319l, newBinaryBuffer((byte) 0x19, (byte) 0xb3, (byte) 0x5c, (byte) 0x74, (byte) 0x9a, (byte) 0xff, (byte) 0x40, (byte) 0xc6));
        TEST_DATA.put(1.1f, newBinaryBuffer((byte) 0xcd, (byte) 0xcc, (byte) 0x8c, (byte) 0x3f));
        TEST_DATA.put(8.9d, newBinaryBuffer((byte) 0xcd, (byte) 0xcc, (byte) 0xcc, (byte) 0xcc, (byte) 0xcc, (byte) 0xcc, (byte) 0x21, (byte) 0x40));
    }

    private static BinaryBuffer newBinaryBuffer(byte... values) {
        return new BinaryBuffer(values);
    }

    @Test
    public void testEncodeDecode() {
        for(Map.Entry<Number, BinaryBuffer> entry: TEST_DATA.entrySet()) {
            Number decoded = entry.getKey();
            BinaryBuffer encoded = entry.getValue();
            assertEncode(decoded, encoded);
            assertDecode(encoded, decoded);
        }
    }

    private void assertEncode(Number decoded, BinaryBuffer expected) {
        BinaryBuffer actual = new BinaryBuffer(10);
        LittleEndianCodec codec = new LittleEndianCodec(actual);
        if(decoded instanceof Short) {
            actual = actual.newView(0, codec.encodeInt16(0, (short) decoded));
        } else if(decoded instanceof Integer) {
            actual = actual.newView(0, codec.encodeInt32(0, (int)decoded));
        } else if(decoded instanceof Long) {
            actual = actual.newView(0, codec.encodeInt64(0, (long)decoded));
        } else if(decoded instanceof Float) {
            actual = actual.newView(0, codec.encodeFloat32(0, (float)decoded));
        } else if(decoded instanceof Double) {
            actual = actual.newView(0, codec.encodeFloat64(0, (double)decoded));
        } else {
            throw new IllegalArgumentException("Unhandled type: " + decoded.getClass());
        }

        assertEquals(expected, actual);
    }

    private void assertDecode(BinaryBuffer encoded, Number expected) {
        LittleEndianCodec codec = new LittleEndianCodec(encoded);
        Number actual;
        if(expected instanceof Short) {
            actual = codec.decodeInt16(0);
        } else if(expected instanceof Integer) {
            actual = codec.decodeInt32(0);
        } else if(expected instanceof Integer) {
            actual = codec.decodeInt32(0);
        } else if(expected instanceof Long) {
            actual = codec.decodeInt64(0);
        } else if(expected instanceof Float) {
            actual = codec.decodeFloat32(0);
        } else if(expected instanceof Double) {
            actual = codec.decodeFloat64(0);
        } else {
            throw new IllegalArgumentException("Unhandled type: " + expected.getClass());
        }
        DeepEquality.assertEquals(expected, actual);
    }
}
