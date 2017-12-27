package org.stenerud.remotefs;

import org.junit.Assert;
import org.junit.Test;
import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.message.Parameters;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.DeepEquality;
import org.stenerud.remotefs.utility.SocketTransportPair;

import java.io.IOException;

public class SocketTransportTest {
    @Test
    public void testX() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        Specification specification = new Specification("test", "desc",
                new Specification.ParameterSpecification("one", Specification.Type.INTEGER, "first value"));
        messageCodec.registerSpecification(specification,1);
        Parameters message = new Parameters(specification).add(1);
        Parameters actual;
        try(SocketTransportPair transports = SocketTransportPair.Builder.build(messageCodec)) {
            sendMessage(message, transports.getClientTransport());
            actual = transports.getServerTransport().getNextMessage();
        }
        DeepEquality.assertEquals(message, actual);
    }

    private void sendMessage(Parameters message, SocketTransport transport) {
        new Thread(() -> {
            try {
                transport.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
