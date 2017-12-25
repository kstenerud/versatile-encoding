package org.stenerud.remotefs.utility;

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
    public void testLengthRemainingFromOffset() {
        BinaryBuffer buffer = new BinaryBuffer(4);
        assertEquals(4, buffer.lengthRemainingFromOffset(0));
        assertEquals(3, buffer.lengthRemainingFromOffset(1));
        assertEquals(2, buffer.lengthRemainingFromOffset(2));
        assertEquals(1, buffer.lengthRemainingFromOffset(3));
        assertEquals(0, buffer.lengthRemainingFromOffset(4));
        assertEquals(-1, buffer.lengthRemainingFromOffset(5));

        BinaryBuffer view = buffer.newView(1, 3);
        assertEquals(2, view.lengthRemainingFromOffset(1));
        assertEquals(1, view.lengthRemainingFromOffset(2));
        assertEquals(0, view.lengthRemainingFromOffset(3));
        assertEquals(-1, view.lengthRemainingFromOffset(4));
    }

    @Test
    public void testLengthToOffset() {
        BinaryBuffer buffer = new BinaryBuffer(4);
        assertEquals(0, buffer.lengthToOffset(0));
        assertEquals(1, buffer.lengthToOffset(1));
        assertEquals(2, buffer.lengthToOffset(2));
        assertEquals(3, buffer.lengthToOffset(3));
        assertEquals(4, buffer.lengthToOffset(4));

        BinaryBuffer view = buffer.newView(1, 3);
        assertEquals(0, view.lengthToOffset(1));
        assertEquals(1, view.lengthToOffset(2));
        assertEquals(2, view.lengthToOffset(3));
    }

    @Test
    public void testCopyFrom() {
        BinaryBuffer buffer1 = new BinaryBuffer(10);
        buffer1.data[0] = 1;
        buffer1.data[1] = 2;
        buffer1.data[2] = 3;
        BinaryBuffer buffer2 = new BinaryBuffer(5);
        buffer2.copyFrom(buffer1, 0, 1, 3);
        assertEquals(0, buffer2.data[0]);
        assertEquals(1, buffer2.data[1]);
        assertEquals(2, buffer2.data[2]);
        assertEquals(3, buffer2.data[3]);
        assertEquals(0, buffer2.data[4]);

        assertIsBadCopyFromParameters(buffer1, 0, buffer2, 0, 100);
        assertIsBadCopyFromParameters(buffer1, 0, buffer2, 0, 6);
        assertIsBadCopyFromParameters(buffer1, -1, buffer2, 0, 1);
        assertIsBadCopyFromParameters(buffer1, 10, buffer2, 0, 1);
        assertIsBadCopyFromParameters(buffer1, 3, buffer2, 3, 4);

        assertIsBadCopyFromParameters(buffer1.data, 0, buffer2, 0, 100);
        assertIsBadCopyFromParameters(buffer1.data, 0, buffer2, 0, 6);
        assertIsBadCopyFromParameters(buffer1.data, -1, buffer2, 0, 1);
        assertIsBadCopyFromParameters(buffer1.data, 10, buffer2, 0, 1);
        assertIsBadCopyFromParameters(buffer1.data, 3, buffer2, 3, 4);
    }

    @Test
    public void testBadConstructors() {
        byte[] data = new byte[10];
        assertIsBadConstructorParams(data, -1, 10);
        assertIsBadConstructorParams(data, 0, -1);
        assertIsBadConstructorParams(data, 1, 0);
        assertIsBadConstructorParams(data, 0, 11);
        assertIsBadConstructorParams(data, 11, 10);
    }

    private void assertIsBadConstructorParams(byte[] data, int startOffset, int endOffset) {
        try {
            new BinaryBuffer(data, startOffset, endOffset);
            assertTrue("Should have thrown", false);
        } catch(IndexOutOfBoundsException e) {
            // Do nothing
        }
    }

    private void assertIsBadCopyFromParameters(BinaryBuffer fromBuffer, int fromOffset, BinaryBuffer toBuffer, int toOffset, int length) {
        try {
            toBuffer.copyFrom(fromBuffer, fromOffset, toOffset, length);
            assertTrue("Should have thrown", false);
        } catch(IndexOutOfBoundsException e) {
            // Do nothing
        }
    }

    private void assertIsBadCopyFromParameters(byte[] fromData, int fromOffset, BinaryBuffer toBuffer, int toOffset, int length) {
        try {
            toBuffer.copyFrom(fromData, fromOffset, toOffset, length);
            assertTrue("Should have thrown", false);
        } catch(IndexOutOfBoundsException e) {
            // Do nothing
        }
    }

    private void assertIsBadViewParameters(BinaryBuffer buffer, int startOffset, int endOffset) {
        try {
            buffer.newView(startOffset, endOffset);
            assertTrue("Should have thrown", false);
        } catch(IndexOutOfBoundsException e) {
            // Do nothing
        }
    }

    @Test
    public void testUTF8String() throws Exception {
        String expected = "this is a test";
        BinaryBuffer buffer = new BinaryBuffer(expected.getBytes("UTF-8"));
        String actual = buffer.utf8String();
        assertEquals(expected, actual);
    }

    @Test
    public void testToString() throws Exception {
        String expected = "[74,65,73,74]";
        BinaryBuffer buffer = new BinaryBuffer("test".getBytes("UTF-8"));
        String actual = buffer.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testView() throws Exception {
        int length = 10;
        BinaryBuffer buffer = new BinaryBuffer(length);
        BinaryBuffer subview = buffer.newView();
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
        BinaryBuffer subview = buffer.newView(2, 8);
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
    public void testView3() throws Exception {
        int length = 10;
        BinaryBuffer buffer = new BinaryBuffer(length);
        BinaryBuffer subview = buffer.newView(2);
        assertEquals(8, subview.length);
        assertEquals(2, subview.startOffset);
        assertEquals(10, subview.endOffset);
        for(int i = subview.startOffset; i < subview.endOffset; i++) {
            assertEquals(0, subview.data[i]);
        }

        subview.data[subview.startOffset] = 10;
        assertEquals(subview.data[subview.startOffset], buffer.data[subview.startOffset]);
    }

    @Test
    public void testView4() throws Exception {
        BinaryBuffer buffer = new BinaryBuffer(100);
        BinaryBuffer view = buffer.newView(15, 30);
        assertIsBadViewParameters(buffer, 0, 101);
        assertIsBadViewParameters(buffer, -1, 100);
        assertIsBadViewParameters(view, 0, 100);
        assertIsBadViewParameters(view, 14, 17);
        assertIsBadViewParameters(view, 15, 31);
        view.newView(15, 30);
        BinaryBuffer subview = view.newView(20, 20);
        assertIsBadViewParameters(subview, 20, 21);
        assertIsBadViewParameters(subview, 19, 20);
    }

    @Test
    public void testCopy() throws Exception {
        int length = 10;
        BinaryBuffer buffer = new BinaryBuffer(length);
        BinaryBuffer subcopy = buffer.newCopy();
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
        BinaryBuffer subcopy = buffer.newCopy(2, 8);
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
        BinaryBuffer bufferEq3 = buffer.newView();
        BinaryBuffer bufferEq4 = buffer.newCopy();
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
