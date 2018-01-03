package org.stenerud.remotefs.session;

import org.junit.Test;
import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.DeepEquality;
import org.stenerud.remotefs.utility.ObjectHolder;
import org.stenerud.remotefs.utility.StreamTransportPair;

public class StreamTransportTest {
    @Test
    public void testTransport() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        Specification specification = new Specification("test", "desc",
                new Specification.ParameterSpecification("one", Specification.Type.INTEGER, "first value"));
        messageCodec.registerSpecification(specification,1);
        Message message = new Message(specification).add(1);
        ObjectHolder holder = new ObjectHolder();
        try(StreamTransportPair transports = new StreamTransportPair(messageCodec)) {
            transports.serverSideTransport.setListener(message1 -> holder.set(message1));
            transports.clientSideTransport.sendMessage(message);
            DeepEquality.assertEquals(message, holder.get());
        }
    }
}
