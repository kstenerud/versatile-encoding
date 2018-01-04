package org.stenerud.remotefs.codec;

import org.junit.Test;
import org.stenerud.remotefs.message.*;
import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.DeepEquality;
import org.stenerud.remotefs.message.Message;

import static org.junit.Assert.assertTrue;

public class MessageCodecTest {

    @Test
    public void testException() throws Exception {
        assertEncodeDecode(new Message(new ExceptionMessageSpecification())
                .set(ExceptionMessageSpecification.PROCESS_ID, 1)
                .set(ExceptionMessageSpecification.TYPE, 10)
                .set(ExceptionMessageSpecification.MESSAGE, "It's broken!"), 100);
    }

    @Test
    public void testException2() throws Exception {
        assertEncodeDecode(new Message(new ExceptionMessageSpecification())
                .set(ExceptionMessageSpecification.PROCESS_ID, 1)
                .set(ExceptionMessageSpecification.TYPE, 10), 100);
    }

    @Test
    public void testStatus() throws Exception {
        assertEncodeDecode(new Message(new StatusMessageSpecification())
                .set(StatusMessageSpecification.PROCESS_ID, 1)
                .set(StatusMessageSpecification.COMPLETION, 50), 100);
    }

    @Test
    public void testResource() throws Exception {
        assertResourceMessage(0);
        assertResourceMessage(1);
        assertResourceMessage(2);
        assertResourceMessage(3);
        assertResourceMessage(4);
        assertResourceMessage(5);
        assertResourceMessage(13);
        assertResourceMessage(14);
        assertResourceMessage(15);
        assertResourceMessage(4091);
        assertResourceMessage(4092);
        assertResourceMessage(4093);
        assertResourceMessage(1024*1024 - 7);
        assertResourceMessage(1024*1024 - 6);
        assertResourceMessage(1024*1024 - 5);
    }

    @Test
    public void testMessageSizeAndType() throws Exception {
        assertMessageWithTypeAndSize(0, 0);
        assertMessageWithTypeAndSize(0, 1);
        assertMessageWithTypeAndSize(0, 2);

        // 1-3 byte length crossover point
        assertMessageWithTypeAndSize(0, 0x7a);
        assertMessageWithTypeAndSize(0, 0x7b);

        // 1-2 byte type crossover point
        assertMessageWithTypeAndSize(0x7f, 0);
        assertMessageWithTypeAndSize(0x80, 1);

        // Both crossover point
        assertMessageWithTypeAndSize(0x7f, 0x7a);
        assertMessageWithTypeAndSize(0x80, 0x7a);
        assertMessageWithTypeAndSize(0x7f, 0x7b);
        assertMessageWithTypeAndSize(0x80, 0x7b);
    }

    @Test
    public void testBadMessageTypeAndSize() throws Exception {
        assertBadMessageType(0x8000);
        assertBadMessageLength(0x800000);
    }

    private void assertResourceMessage(int chunkSize) throws Exception {
        assertEncodeDecode(new Message(new ResourceMessageSpecification())
                .set(ResourceMessageSpecification.RESOURCE_ID, 1)
                .set(ResourceMessageSpecification.CHUNK_COUNT, 1)
                .set(ResourceMessageSpecification.CHUNK_INDEX, 0)
                .set(ResourceMessageSpecification.CHUNK, new byte[chunkSize]), chunkSize + 100);
    }

    private MessageCodec getStandardMessageCodec() {
        MessageCodec messageCodec = new MessageCodec();
        messageCodec.registerSpecification(new ExceptionMessageSpecification(), MessageCodec.Types.EXCEPTION);
        messageCodec.registerSpecification(new ResourceMessageSpecification(), MessageCodec.Types.RESOURCE);
        messageCodec.registerSpecification(new StatusMessageSpecification(), MessageCodec.Types.STATUS);
        return messageCodec;
    }

    private void assertBadMessageType(int type) throws Exception {
        try {
            assertMessageWithTypeAndSize(type, 10);
            assertTrue("Should have thrown", false);
        } catch(IllegalArgumentException e) {
            // Expected
        }
    }

    private void assertBadMessageLength(int length) throws Exception {
        try {
            assertMessageWithTypeAndSize(1, length);
            assertTrue("Should have thrown", false);
        } catch(IllegalArgumentException e) {
            // Expected
        }
    }

    private void assertMessageWithTypeAndSize(int type, int size) throws Exception {
        MessageCodec codec = new MessageCodec();
        Specification specification = new Specification("test", "description",
                new Specification.ParameterSpecification("chunk", Specification.Type.ANY, "The chunk data")
                );
        codec.registerSpecification(specification, type);
        Message message = new Message(specification).add(new byte[size]);
        BinaryBuffer buffer = new BinaryBuffer(size + 11);
        BinaryBuffer encodedView = codec.encode(message, buffer);
        Message result = codec.decode(encodedView);
        for(Specification.ParameterSpecification paramSpec: message.getSpecification()) {
            if(result.isPresent(paramSpec.name)) {
                Object expected = result.getObject(paramSpec.name);
                Object actual = result.getObject(paramSpec.name);
                DeepEquality.assertEquals(expected, actual);
            }
        }
    }

    private void assertEncodeDecode(Message message, int bufferSize) throws Exception {
        MessageCodec messageCodec = getStandardMessageCodec();
        BinaryBuffer buffer = new BinaryBuffer(bufferSize);
        BinaryBuffer encodedView = messageCodec.encode(message, buffer);
        Message result = messageCodec.decode(encodedView);
        for(Specification.ParameterSpecification paramSpec: message.getSpecification()) {
            if(result.isPresent(paramSpec.name)) {
                Object expected = result.getObject(paramSpec.name);
                Object actual = result.getObject(paramSpec.name);
                DeepEquality.assertEquals(expected, actual);
            }
        }
    }
}
