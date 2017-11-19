package org.stenerud.remotefs;

import org.junit.Test;

import static org.junit.Assert.*;

public class BinaryBufferTest {
    @Test
    public void testCreate() {
        int length = 10;
        BinaryBuffer buffer = new BinaryBuffer(length);
        assertEquals(length, buffer.length);
        assertEquals(0, buffer.startOffset);
        assertEquals(length, buffer.endOffset);
        for(int i = 0; i < length; i++) {
            assertEquals(0, buffer.data[i]);
        }
    }

    @Test
    public void testToString() throws Exception {
        String expected = "this is a test";
        BinaryBuffer buffer = new BinaryBuffer(expected.getBytes("UTF-8"));
        String actual = buffer.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testView() throws Exception {
        int length = 10;
        BinaryBuffer buffer = new BinaryBuffer(length);
        BinaryBuffer subview = buffer.view();
        assertEquals(10, subview.length);
        assertEquals(0, subview.startOffset);
        assertEquals(10, subview.endOffset);
        for(int i = subview.startOffset; i < subview.endOffset; i++) {
            assertEquals(0, subview.data[i]);
        }

        subview.data[subview.startOffset] = 10;
        assertEquals(subview.data[subview.startOffset], buffer.data[subview.startOffset]);
    }

    @Test
    public void testView2() throws Exception {
        int length = 10;
        BinaryBuffer buffer = new BinaryBuffer(length);
        BinaryBuffer subview = buffer.view(2, 8);
        assertEquals(6, subview.length);
        assertEquals(2, subview.startOffset);
        assertEquals(8, subview.endOffset);
        for(int i = subview.startOffset; i < subview.endOffset; i++) {
            assertEquals(0, subview.data[i]);
        }

        subview.data[subview.startOffset] = 10;
        assertEquals(subview.data[subview.startOffset], buffer.data[subview.startOffset]);
    }

    @Test
    public void testCopy() throws Exception {
        int length = 10;
        BinaryBuffer buffer = new BinaryBuffer(length);
        BinaryBuffer subcopy = buffer.copy();
        assertEquals(10, subcopy.length);
        assertEquals(0, subcopy.startOffset);
        assertEquals(10, subcopy.endOffset);
        for(int i = subcopy.startOffset; i < subcopy.endOffset; i++) {
            assertEquals(0, subcopy.data[i]);
        }

        subcopy.data[subcopy.startOffset] = 10;

        for(int i = buffer.startOffset; i < buffer.endOffset; i++) {
            assertEquals(0, buffer.data[i]);
        }
    }

    @Test
    public void testCopy2() throws Exception {
        int length = 10;
        BinaryBuffer buffer = new BinaryBuffer(length);
        BinaryBuffer subcopy = buffer.copy(2, 8);
        assertEquals(6, subcopy.length);
        assertEquals(0, subcopy.startOffset);
        assertEquals(6, subcopy.endOffset);
        for(int i = subcopy.startOffset; i < subcopy.endOffset; i++) {
            assertEquals(0, subcopy.data[i]);
        }

        subcopy.data[subcopy.startOffset] = 10;

        for(int i = buffer.startOffset; i < buffer.endOffset; i++) {
            assertEquals(0, buffer.data[i]);
        }
    }

    @Test
    public void testEquals() {
        BinaryBuffer buffer = new BinaryBuffer(1);
        BinaryBuffer bufferEq = new BinaryBuffer(new byte[1]);
        BinaryBuffer bufferEq2 = new BinaryBuffer(new byte[10], 2, 3);
        BinaryBuffer bufferEq3 = buffer.view();
        BinaryBuffer bufferEq4 = buffer.copy();
        BinaryBuffer bufferNeq = new BinaryBuffer(new byte[1]);
        bufferNeq.data[bufferNeq.startOffset] = 10;
        BinaryBuffer bufferNeq2 = new BinaryBuffer(new byte[10], 2, 3);
        bufferNeq2.data[bufferNeq2.startOffset] = 10;
        BinaryBuffer bufferNeq3 = new BinaryBuffer(new byte[2]);
        BinaryBuffer bufferNeq4 = new BinaryBuffer(new byte[10], 2, 5);
        assertFalse(buffer.equals(null));
        assertFalse(buffer.equals(1));
        assertTrue((buffer.equals(buffer)));
        assertTrue(buffer.equals(bufferEq));
        assertTrue(buffer.equals(bufferEq2));
        assertTrue(buffer.equals(bufferEq3));
        assertTrue(buffer.equals(bufferEq4));
        assertFalse(buffer.equals(bufferNeq));
        assertFalse(buffer.equals(bufferNeq2));
        assertFalse(buffer.equals(bufferNeq3));
        assertFalse(buffer.equals(bufferNeq4));
    }
}
