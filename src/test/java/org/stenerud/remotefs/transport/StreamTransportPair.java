package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.codec.MessageCodec;

import java.util.LinkedList;
import java.util.List;

public class StreamTransportPair implements AutoCloseable {
    public final Transport clientSideTransport;
    public final Transport serverSideTransport;

    public StreamTransportPair(MessageCodec messageCodec) {
        StreamTransportProducer producer = new StreamTransportProducer(messageCodec);
        List<Transport> list = new LinkedList<>();
        producer.setListener(transport -> list.add(transport));
        clientSideTransport = producer.produceTransportPair();
        clientSideTransport.setAutoflush(true);
        serverSideTransport = list.get(0);
        serverSideTransport.setAutoflush(true);
    }

    @Override
    public void close() throws Exception {
        clientSideTransport.close();
        serverSideTransport.close();
    }
}
