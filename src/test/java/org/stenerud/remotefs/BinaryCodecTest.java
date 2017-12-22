package org.stenerud.remotefs;

import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.DeepEquality;
import org.stenerud.remotefs.utility.ObjectHolder;

import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.*;

public class BinaryCodecTest {
    @Test
    public void testLongLength() throws Exception {
//        assertEncodeDecode(new BinaryBuffer(10));
//        assertEncodeDecode(new BinaryBuffer(100));
        assertEncodeDecode(new BinaryBuffer(1000));
        assertEncodeDecode(new BinaryBuffer(10000));
        assertEncodeDecode(new BinaryBuffer(100000));
    }

    static final int BYTES_PROTOCOL_OVERHEAD = 6;

    @Test
    public void testStringStreamCutoff() throws Exception {
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 0, "", "");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 100, "", "");

        // 2-byte
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 5, "straße", "stra");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 6, "straße", "straß");

        // 3-byte
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 14, "これはマルチバイトです", "これはマ");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 15, "これはマルチバイトです", "これはマル");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 16, "これはマルチバイトです", "これはマル");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 17, "これはマルチバイトです", "これはマル");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 18, "これはマルチバイトです", "これはマルチ");

        // 4-byte
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 4, "𩶘𩶘", "𩶘");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 5, "𩶘𩶘", "𩶘");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 6, "𩶘𩶘", "𩶘");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 7, "𩶘𩶘", "𩶘");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 8, "𩶘𩶘", "𩶘𩶘");

        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 0, "これはマルチバイトです", "");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 1, "これはマルチバイトです", "");
        assertStringStreamCutoff(BYTES_PROTOCOL_OVERHEAD + 2, "これはマルチバイトです", "");
    }

    private void assertStringStreamCutoff(int bufferLength, String input, String expected) throws Exception {
        BinaryBuffer encoded = new BinaryBuffer(bufferLength);
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(encoded);
        BinaryCodec.Encoder.ByteStream stream = encoder.newStringStream();
        stream.write(new BinaryBuffer(input.getBytes("UTF-8")));
        stream.close();
        String actual = decodeSingleObject(encoder.view(), String.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testStringStreamMultibyte() throws Exception {
        BinaryBuffer encoded = new BinaryBuffer(100);
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(encoded);
        BinaryCodec.Encoder.ByteStream stream = encoder.newStringStream();
        stream.write(new BinaryBuffer("これは".getBytes("UTF-8")));
        stream.write(new BinaryBuffer("マルチバイトです".getBytes("UTF-8")));
        stream.close();
        String result = decodeSingleObject(encoder.view(), String.class);
        String expected = "これはマルチバイトです";
        assertEquals(expected, result);
    }

    @Test
    public void testStringStream() throws Exception {
        BinaryBuffer encoded = new BinaryBuffer(100);
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(encoded);
        BinaryCodec.Encoder.ByteStream stream = encoder.newStringStream();
        stream.write(new BinaryBuffer("hello".getBytes("UTF-8")));
        stream.write(new BinaryBuffer(" world".getBytes("UTF-8")));
        stream.close();
        String result = decodeSingleObject(encoder.view(), String.class);
        String expected = "hello world";
        assertEquals(expected, result);
    }

    @Test
    public void testStreamBytes() throws Exception {
        assertStreamBufferToBuffer(5, 100, 12, 18);
        assertStreamBufferToBuffer(0, 100, 0, 6);
        assertStreamBufferToBuffer(0, 100, 0, 16);
        assertStreamBufferToBuffer(0, 100, 0, 7);
        assertStreamBufferToBuffer(5, 10, 12, 100);
        assertStreamBufferToBufferThrows(0, 100, 0, 5);
        assertStreamBufferToBufferThrows(0, 100, 10, 15);
    }

    private void assertStreamBufferToBufferThrows(int srcStartOffset, int srcEndOffset, int dstStartOffset, int dstEndOffset) throws BinaryCodec.NoRoomException {
        try {
            assertStreamBufferToBuffer(srcStartOffset, srcEndOffset, dstStartOffset, dstEndOffset);
            assertTrue("Should have thrown", false);
        } catch(BinaryCodec.NoRoomException e) {
            // Expected
        }
    }

    private void assertStreamBufferToBuffer(int srcStartOffset, int srcEndOffset, int dstStartOffset, int dstEndOffset) throws BinaryCodec.NoRoomException {
        BinaryBuffer src = new BinaryBuffer(srcEndOffset+100).newView(srcStartOffset, srcEndOffset);
        BinaryBuffer dst = new BinaryBuffer(dstEndOffset+100).newView(dstStartOffset, dstEndOffset);
        fillWithSequentialData(src);
        BinaryBuffer encoded = streamBufferToBuffer(src, dst);
        BinaryBuffer result = decodeSingleObject(encoded, BinaryBuffer.class);
        BinaryBuffer expected = createExpectedView(src, dst.length - BYTES_PROTOCOL_OVERHEAD);
        assertEquals(expected, result);
    }

    private BinaryBuffer createExpectedView(BinaryBuffer src, int length) {
        BinaryBuffer expected = new BinaryBuffer(length);
        int offset = 0;
        while(offset < expected.endOffset) {
            int copyLength = Math.min(Math.min(length, src.length), expected.lengthRemainingFromOffset(offset));
            expected.copyFrom(src, src.startOffset, offset, copyLength);
            offset += copyLength;
        }
        return expected;
    }

    private BinaryBuffer streamBufferToBuffer(BinaryBuffer src, BinaryBuffer dst) throws BinaryCodec.NoRoomException {
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(dst);
        BinaryCodec.Encoder.ByteStream stream = encoder.newByteStream();
        while(stream.write(src) > 0) {
        }
        stream.close();
        return encoder.view();
    }

    private <T> T decodeSingleObject(BinaryBuffer buffer, Class<T> cls) {
        final ObjectHolder holder = new ObjectHolder();
        BinaryCodec.Decoder decoder = new BinaryCodec.Decoder(new BinaryCodec.Decoder.Visitor() {
            @Override
            public void onValue(Object value) {
                holder.object = value;
            }
        });
        decoder.feed(buffer);
        return (T)holder.object;
    }

    private void fillWithSequentialData(BinaryBuffer buffer) {
        int offset = buffer.startOffset;
        try {
            for (; ; ) {
                for (int i = 0; i < 127; i++) {
                    buffer.data[offset++] = (byte)i;
                }
            }
        } catch(IndexOutOfBoundsException e) {
            // Ignored
        }
    }

    @Test
    public void testWriteTooMany() {
        assertWriteTooMany(1);
        assertWriteTooMany(1000);
        assertWriteTooMany(1000000);
        assertWriteTooMany(10000000000l);
        assertWriteTooMany(new byte[2]);
        assertWriteTooMany("test");
        assertWriteTooMany(null);
        assertWriteTooMany(1.0f);
        assertWriteTooMany(1.010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001);
        assertWriteTooMany(new LinkedList<>());
        assertWriteTooMany(new HashMap<>());
    }

    private void assertWriteTooMany(Object object) {
        BinaryBuffer buffer = new BinaryBuffer(10);
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(buffer);
        try {
            for (int i = 0; i < 1000; i++) {
                encoder.writeObject(object);
            }
            assertTrue("Did not fail writing too many objects", false);
        } catch(BinaryCodec.NoRoomException e) {
            // Expected
        }
    }

    @Test(expected = BinaryCodec.NoRoomException.class)
    public void testTooLong() throws Exception {
        BinaryBuffer buffer = new BinaryBuffer(10);
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(buffer);
        encoder.writeObject(new byte[100]);
    }

    @Test
    public void testEncodeDecode() throws Exception {
        assertEncodeDecode((byte)1);
        assertEncodeDecode((short)10000);
        assertEncodeDecode((int)100000000);
        assertEncodeDecode(100000000000000l);
        assertEncodeDecode(1.1f);
        assertEncodeDecode(1.8d);
        assertEncodeDecode(new byte[100]);
        assertEncodeDecode(new BinaryBuffer(100));
        assertEncodeDecode("test");
        assertEncodeDecode("");
        assertEncodeDecode(new byte[0]);
        assertEncodeDecode(null);

        List list = new LinkedList();
        list.add((byte)1);
        list.add((short)1000);
        list.add(1000000);
        list.add(10000000000000l);
        list.add(1.6f);
        list.add(1.9d);
        // Can't do bare byte array because the codec returns a BinaryBuffer
//        list.add(new byte[10]);
        list.add("this is a test");
        list.add(new BinaryBuffer(5));
        assertEncodeDecode(list);

        List superlist = new LinkedList();
        superlist.add(list);
        assertEncodeDecode(superlist);

        Map map = new HashMap();
        map.put("one", 1);
        map.put(2, "two");
        assertEncodeDecode(map);

        Map supermap = new HashMap();
        supermap.put("some map", map);
        assertEncodeDecode(supermap);
    }

    @Test
    public void testView() throws Exception {
        BinaryBuffer buffer = new BinaryBuffer(100);
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(buffer);
        encoder.writeObject(1);
        BinaryBuffer view = encoder.view();
        BinaryBuffer expected = buffer.newView(0, view.endOffset);
        assertEquals(expected, view);
    }

    @Test
    public void testTruncatedData() throws Exception {
        assertDecodeThrowsIllegalState(truncate(encode(new byte[10])));
        assertDecodeThrowsIllegalState(truncate(encode("this is a test")));
        assertDecodeThrowsIllegalState(truncate(encode(Arrays.asList(1, 2, 3))));
        Map map = new HashMap();
        map.put(1, 1);
        map.put(2, 2);
        assertDecodeThrowsIllegalState(truncate(encode(map)));
    }

    @Test
    public void testCoruptedData() throws Exception {
        BinaryBuffer buffer = encode(Arrays.asList(1, 2, 3));
        buffer.data[buffer.endOffset-2] = buffer.data[buffer.endOffset-1];
        assertDecodeThrowsIllegalState(buffer);
    }

    @Test
    public void testCoruptedData2() throws Exception {
        BinaryBuffer buffer = encode(Arrays.asList(1, 2, 3));
        buffer.data[0] = buffer.data[buffer.endOffset-1];
        assertDecodeThrowsIllegalState(buffer);
    }

    private BinaryBuffer encode(Object o) throws BinaryCodec.NoRoomException {
        BinaryBuffer buffer = new BinaryBuffer(1000000);
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(buffer);
        encoder.writeObject(o);
        return encoder.view();
    }

    private BinaryBuffer truncate(BinaryBuffer buffer) {
        return buffer.newView(buffer.startOffset, buffer.endOffset-1);
    }

    private void assertDecodeThrowsIllegalState(BinaryBuffer encodedBuffer) {
        try {
            decodeSingleObject(encodedBuffer, Object.class);
            throw new AssertionFailedError("Should have caused IllegalStateException");
        } catch(IllegalStateException e) {
            // Expected
            return;
        }
    }

    private void assertEncodeDecode(Object expected) throws BinaryCodec.NoRoomException {
        BinaryBuffer encodedBuffer = encode(expected);
        Object actual = decodeSingleObject(encodedBuffer, Object.class);
        if(expected instanceof byte[]) {
            expected = new BinaryBuffer((byte[])expected);
        }
        DeepEquality.assertEquals(expected, actual);
    }
}