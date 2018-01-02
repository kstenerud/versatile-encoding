package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.session.Transport;
import org.stenerud.remotefs.session.TransportProducer;

import javax.annotation.Nonnull;
import java.io.IOException;

public class StreamTransportProducer implements TransportProducer {
    private final MessageCodec messageCodec;
    private Listener listener = transport -> {
        // Ignored
    };

    public StreamTransportProducer(MessageCodec messageCodec) {
        this.messageCodec = messageCodec;
    }

    @Override
    public void setListener(@Nonnull Listener listener) {
        this.listener = listener;
    }

    @Override
    public void close() throws Exception {

    }

    public Transport newTransport() throws IOException {
        StreamTransportPair transportPair = new StreamTransportPair(messageCodec);
        listener.onNewTransport(transportPair.serverSideTransport);
        return transportPair.clientSideTransport;
    }
}
