package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.session.Transport;
import org.stenerud.remotefs.session.TransportProducer;

import javax.annotation.Nonnull;
import java.io.IOException;

public class SimpleTransportProducer implements TransportProducer {
    private Listener listener = transport -> {
        // Ignored
    };

    @Override
    public void setListener(@Nonnull Listener listener) {
        this.listener = listener;
    }

    @Override
    public void close() throws Exception {

    }

    public Transport newTransport() throws IOException {
        SimpleTransport transport = new SimpleTransport();
        listener.onNewTransport(transport);
        return transport;
    }
}
