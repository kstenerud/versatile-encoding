package org.stenerud.remotefs.codec;

import org.junit.Test;
import org.stenerud.remotefs.utility.BinaryBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntegerCodecTest {
    @Test
    public void testOneTwoMaxLength() {
        LittleEndianCodec endianCodec = new LittleEndianCodec(new BinaryBuffer(10));
        int maxLength = new IntegerCodec.OneTwo(endianCodec).getMaxEncodedLength();
        assertEquals(2, maxLength);
    }

    @Test
    public void testOneThreeMaxLength() {
        LittleEndianCodec endianCodec = new LittleEndianCodec(new BinaryBuffer(10));
        int maxLength = new IntegerCodec.OneThree(endianCodec).getMaxEncodedLength();
        assertEquals(3, maxLength);
    }

    @Test
    public void testOneTwo() {
        assertEncodeDecode2(0);
        assertEncodeDecode2(1);
        assertEncodeDecode2(2);
        assertEncodeDecode2(3);
        assertEncodeDecode2(126);
        assertEncodeDecode2(127);
        assertEncodeDecode2(128);
        assertEncodeDecode2(0x7fff);
        assertEncodeFails2(0x8000);
    }

    @Test
    public void testOneTwoEncodedLength() {
        LittleEndianCodec endianCodec = new LittleEndianCodec(new BinaryBuffer(10));
        assertEquals(1, new IntegerCodec.OneTwo(endianCodec).getEncodedLength(0));
        assertEquals(1, new IntegerCodec.OneTwo(endianCodec).getEncodedLength(1));
        assertEquals(1, new IntegerCodec.OneTwo(endianCodec).getEncodedLength(127));
        assertEquals(2, new IntegerCodec.OneTwo(endianCodec).getEncodedLength(128));
        assertEquals(2, new IntegerCodec.OneTwo(endianCodec).getEncodedLength(0x7fff));
    }

    @Test
    public void testOneThree() {
        assertEncodeDecode3(0);
        assertEncodeDecode3(1);
        assertEncodeDecode3(2);
        assertEncodeDecode3(3);
        assertEncodeDecode3(126);
        assertEncodeDecode3(127);
        assertEncodeDecode3(128);
        assertEncodeDecode3(0x7fffff);
        assertEncodeFails3(0x800000);
    }

    @Test
    public void testOneThreeEncodedLength() {
        LittleEndianCodec endianCodec = new LittleEndianCodec(new BinaryBuffer(10));
        assertEquals(1, new IntegerCodec.OneThree(endianCodec).getEncodedLength(0));
        assertEquals(1, new IntegerCodec.OneThree(endianCodec).getEncodedLength(1));
        assertEquals(1, new IntegerCodec.OneThree(endianCodec).getEncodedLength(127));
        assertEquals(3, new IntegerCodec.OneThree(endianCodec).getEncodedLength(128));
        assertEquals(3, new IntegerCodec.OneThree(endianCodec).getEncodedLength(0x7fffff));
    }

    private void assertEncodeFails2(int value) {
        LittleEndianCodec endianCodec = new LittleEndianCodec(new BinaryBuffer(10));
        IntegerCodec codec = new IntegerCodec.OneTwo(endianCodec);
        try {
            codec.encode(0, value);
            assertTrue("Should have thrown", false);
        } catch(IllegalArgumentException e) {
            // Expected
        }
    }
    private void assertEncodeDecode2(int value) {
        LittleEndianCodec endianCodec = new LittleEndianCodec(new BinaryBuffer(10));
        IntegerCodec codec = new IntegerCodec.OneTwo(endianCodec);
        codec.encode(0, value);
        int result = codec.decode(0);
    }

    private void assertEncodeFails3(int value) {
        LittleEndianCodec endianCodec = new LittleEndianCodec(new BinaryBuffer(10));
        IntegerCodec codec = new IntegerCodec.OneThree(endianCodec);
        try {
            codec.encode(0, value);
            assertTrue("Should have thrown", false);
        } catch(IllegalArgumentException e) {
            // Expected
        }
    }
    private void assertEncodeDecode3(int value) {
        LittleEndianCodec endianCodec = new LittleEndianCodec(new BinaryBuffer(10));
        IntegerCodec codec = new IntegerCodec.OneThree(endianCodec);
        codec.encode(0, value);
        int result = codec.decode(0);
    }
}
