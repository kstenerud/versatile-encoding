package org.stenerud.remotefs.session;

import org.junit.Test;
import org.stenerud.remotefs.utility.ObjectHolder;
import org.stenerud.remotefs.utility.PortCounter;

import java.net.InetSocketAddress;
import java.net.Socket;

import static org.junit.Assert.assertNotNull;

public class SocketProducerTest {
    @Test
    public void testSocketProducer() throws Exception {
        int port = PortCounter.next();
        try (SocketProducer producer = new SocketProducer(port); Socket client = new Socket()) {
            ObjectHolder holder = new ObjectHolder();
            producer.setListener(socket -> holder.set(socket));
            client.connect(new InetSocketAddress("127.0.0.1", port));
            assertNotNull(holder.get());
        }
    }

    @Test
    public void testSocketProducer2() throws Exception {
        int port = PortCounter.next();
        try (SocketProducer producer = new SocketProducer("127.0.0.1", port, 1); Socket client = new Socket()) {
            ObjectHolder holder = new ObjectHolder();
            producer.setListener(socket -> holder.set(socket));
            client.connect(new InetSocketAddress("127.0.0.1", port));
            assertNotNull(holder.get());
        }
    }

    @Test
    public void testSocketProducerNoListener() throws Exception {
        int port = PortCounter.next();
        try (SocketProducer producer = new SocketProducer(port); Socket client = new Socket()) {
            client.connect(new InetSocketAddress("127.0.0.1", port));
        }
    }
}
