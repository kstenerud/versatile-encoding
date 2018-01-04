package org.stenerud.remotefs.transport;

import org.junit.Test;
import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.transport.SocketTransportProducer;
import org.stenerud.remotefs.utility.ObjectHolder;
import org.stenerud.remotefs.utility.PortCounter;

import java.net.InetSocketAddress;
import java.net.Socket;

import static org.junit.Assert.assertNotNull;

public class SocketTransportProducerTest {
    @Test
    public void testTransportProducer() throws Exception {
        int port = PortCounter.next();
        MessageCodec messageCodec = new MessageCodec();
        try (SocketTransportProducer producer = new SocketTransportProducer(messageCodec, port); Socket client = new Socket()) {
            ObjectHolder holder = new ObjectHolder();
            producer.setListener(transport -> holder.set(transport));
            client.connect(new InetSocketAddress("127.0.0.1", port));
            assertNotNull(holder.get());
        }

    }

    @Test
    public void testTransportProducer2() throws Exception {
        int port = PortCounter.next();
        MessageCodec messageCodec = new MessageCodec();
        try (SocketTransportProducer producer = new SocketTransportProducer(messageCodec, "127.0.0.1", port, 1);
             Socket client = new Socket()) {
            ObjectHolder holder = new ObjectHolder();
            producer.setListener(transport -> holder.set(transport));
            client.connect(new InetSocketAddress("127.0.0.1", port));
            assertNotNull(holder.get());
        }

    }
}
