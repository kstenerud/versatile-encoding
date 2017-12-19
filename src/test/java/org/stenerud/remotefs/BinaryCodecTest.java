package org.stenerud.remotefs;

import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.DeepEquality;
import org.stenerud.remotefs.utility.ObjectHolder;

import javax.annotation.Nonnull;
import java.util.*;

import static org.junit.Assert.*;

public class BinaryCodecTest {
    @Test
    public void testLongLength() throws Exception {
        assertEncodeDecode(new BinaryBuffer(10));
        assertEncodeDecode(new BinaryBuffer(100));
        assertEncodeDecode(new BinaryBuffer(1000));
        assertEncodeDecode(new BinaryBuffer(10000));
        assertEncodeDecode(new BinaryBuffer(100000));
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
        BinaryBuffer expected = buffer.view(0, view.endOffset);
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
        return buffer.view(buffer.startOffset, buffer.endOffset-1);
    }

    private void assertDecodeThrowsIllegalState(BinaryBuffer encoded) {
        BinaryCodec.Decoder decoder = new BinaryCodec.Decoder(new BinaryCodec.Decoder.Visitor() {
            @Override public void onValue(Object value) {}
        });

        try {
            decoder.feed(encoded);
            throw new AssertionFailedError("Should have caused IllegalStateException");
        } catch(IllegalStateException e) {
            // Expected
            return;
        }
    }

    private void assertEncodeDecode(Object expected) throws BinaryCodec.NoRoomException {
        BinaryBuffer encodedBuffer = encode(expected);
        ObjectHolder holder = new ObjectHolder();
        BinaryCodec.Decoder decoder = new BinaryCodec.Decoder(new BinaryCodec.Decoder.Visitor() {
            @Override
            public void onValue(Object value) {
                holder.object = value;
            }
        });
        decoder.feed(encodedBuffer);

        if(expected instanceof byte[]) {
            expected = new BinaryBuffer((byte[])expected);
        }
        DeepEquality.assertEquals(expected, holder.object);
    }
}