package org.stenerud.remotefs.session;

import org.junit.Test;
import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.DeepEquality;
import org.stenerud.remotefs.utility.ObjectHolder;
import org.stenerud.remotefs.utility.SocketTransportPair;

public class SocketTransportTest {
    @Test
    public void testTransport() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        Specification specification = new Specification("test", "desc",
                new Specification.ParameterSpecification("one", Specification.Type.INTEGER, "first value"));
        messageCodec.registerSpecification(specification,1);
        Message message = new Message(specification).add(1);
        ObjectHolder holder = new ObjectHolder();
        try(SocketTransportPair transports = new SocketTransportPair(messageCodec)) {
            transports.serverSideTransport.setListener(new MessageProducer.Listener() {
                @Override
                public void onNewMessage(Message message) {
                    holder.set(message);
                }
            });
            transports.clientSideTransport.sendMessage(message);
        }
        Thread.sleep(100);
        DeepEquality.assertEquals(message, holder.get());
    }
}
