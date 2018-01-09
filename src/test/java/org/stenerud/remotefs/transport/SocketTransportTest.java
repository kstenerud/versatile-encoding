package org.stenerud.remotefs.transport;

import org.junit.Test;
import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.message.ExampleMessageBuilder;
import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.DeepEquality;
import org.stenerud.remotefs.utility.ObjectHolder;

public class SocketTransportTest {
    @Test
    public void testTransport() throws Exception {
        ExampleMessageBuilder builder = new ExampleMessageBuilder();
        MessageCodec messageCodec = new MessageCodec();
        messageCodec.registerBuilder(builder,1);
        Message message = builder.newMessage(1, null);
        ObjectHolder holder = new ObjectHolder();
        try(SocketTransportPair transports = new SocketTransportPair(messageCodec)) {
            transports.serverSideTransport.setListener(message1 -> holder.set(message1));
            transports.clientSideTransport.sendMessage(message);
            DeepEquality.assertEquals(message, holder.get());
        }
    }
}
