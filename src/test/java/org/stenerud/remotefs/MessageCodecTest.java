package org.stenerud.remotefs;

import org.junit.Test;
import org.stenerud.remotefs.message.ExceptionMessageSpecification;
import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.DeepEquality;
import org.stenerud.remotefs.utility.Parameters;
import org.stenerud.remotefs.utility.Specification;

public class MessageCodecTest {

    @Test
    public void testException() throws Exception {
        assertEncodeDecode(new Parameters(new ExceptionMessageSpecification())
                .set(ExceptionMessageSpecification.CONTEXT, 1)
                .set(ExceptionMessageSpecification.TYPE, 10)
                .set(ExceptionMessageSpecification.MESSAGE, "It's broken!"));
    }

    private void assertEncodeDecode(Parameters parameters) throws Exception {
        BinaryBuffer buffer = new BinaryBuffer(10000);
        MessageCodec messageCodec = new MessageCodec();
        messageCodec.registerSpecification(parameters.getSpecification(), MessageCodec.Types.EXCEPTION);
        BinaryBuffer encodedView = messageCodec.encode(parameters, buffer);
        Parameters result = messageCodec.decode(encodedView);
        for(Specification.ParameterSpecification paramSpec: parameters.getSpecification()) {
            Object expected = parameters.getObject(paramSpec.name);
            Object actual = parameters.getObject(paramSpec.name);
            DeepEquality.assertEquals(expected, actual);
        }
    }
}
