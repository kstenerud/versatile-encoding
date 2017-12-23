package org.stenerud.remotefs;

import org.junit.Test;
import org.stenerud.remotefs.message.CallMessageSpecification;
import org.stenerud.remotefs.message.ExceptionMessageSpecification;
import org.stenerud.remotefs.message.ResourceMessageSpecification;
import org.stenerud.remotefs.message.StatusMessageSpecification;
import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.DeepEquality;
import org.stenerud.remotefs.utility.Parameters;
import org.stenerud.remotefs.utility.Specification;

import java.util.LinkedList;

public class MessageCodecTest {

    @Test
    public void testException() throws Exception {
        assertEncodeDecode(new Parameters(new ExceptionMessageSpecification())
                .set(ExceptionMessageSpecification.CONTEXT_ID, 1)
                .set(ExceptionMessageSpecification.TYPE, 10)
                .set(ExceptionMessageSpecification.MESSAGE, "It's broken!"), 100);
    }

    @Test
    public void testException2() throws Exception {
        assertEncodeDecode(new Parameters(new ExceptionMessageSpecification())
                .set(ExceptionMessageSpecification.CONTEXT_ID, 1)
                .set(ExceptionMessageSpecification.TYPE, 10), 100);
    }

    @Test
    public void testStatus() throws Exception {
        assertEncodeDecode(new Parameters(new StatusMessageSpecification())
                .set(StatusMessageSpecification.JOB_ID, 1)
                .set(StatusMessageSpecification.COMPLETION, 0.5), 100);
    }

    @Test
    public void testCall() throws Exception {
        assertEncodeDecode(new Parameters(new CallMessageSpecification())
                .set(CallMessageSpecification.JOB_ID, 1)
                .set(CallMessageSpecification.FUNCTION, 10)
                .set(CallMessageSpecification.RETURN_ID, 3)
                .set(CallMessageSpecification.PARAMETERS, new LinkedList<>()), 100);
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

    private void assertResourceMessage(int chunkSize) throws Exception {
        assertEncodeDecode(new Parameters(new ResourceMessageSpecification())
                .set(ResourceMessageSpecification.STREAM_ID, 1)
                .set(ResourceMessageSpecification.CHUNK_COUNT, 1)
                .set(ResourceMessageSpecification.CHUNK_INDEX, 0)
                .set(ResourceMessageSpecification.CHUNK, new byte[chunkSize]), chunkSize + 100);
    }

    private static MessageCodec messageCodec = new MessageCodec();
    static {
        messageCodec.registerSpecification(new ExceptionMessageSpecification(), MessageCodec.Types.EXCEPTION);
        messageCodec.registerSpecification(new ResourceMessageSpecification(), MessageCodec.Types.RESOURCE);
        messageCodec.registerSpecification(new StatusMessageSpecification(), MessageCodec.Types.STATUS);
        messageCodec.registerSpecification(new CallMessageSpecification(), MessageCodec.Types.CALL);
    }

    private void assertEncodeDecode(Parameters parameters, int bufferSize) throws Exception {
        BinaryBuffer buffer = new BinaryBuffer(bufferSize);
        BinaryBuffer encodedView = messageCodec.encode(parameters, buffer);
        Parameters result = messageCodec.decode(encodedView);
        for(Specification.ParameterSpecification paramSpec: parameters.getSpecification()) {
            if(parameters.isPresent(paramSpec.name)) {
                Object expected = parameters.getObject(paramSpec.name);
                Object actual = parameters.getObject(paramSpec.name);
                DeepEquality.assertEquals(expected, actual);
            }
        }
    }
}
