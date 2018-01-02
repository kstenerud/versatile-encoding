package org.stenerud.remotefs.session;

import org.junit.Test;
import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.message.Parameters;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.DeepEquality;
import org.stenerud.remotefs.utility.StreamTransportPair;

public class StreamTransportTest {
    @Test
    public void testX() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        Specification specification = new Specification("test", "desc",
                new Specification.ParameterSpecification("one", Specification.Type.INTEGER, "first value"));
        messageCodec.registerSpecification(specification,1);
        Parameters message = new Parameters(specification).add(1);
        Parameters actual;
        try(StreamTransportPair transports = new StreamTransportPair(messageCodec)) {
            transports.clientTransport.sendMessage(message);
            actual = transports.serverTransport.getNextMessage();
        }
        DeepEquality.assertEquals(message, actual);
    }
}
