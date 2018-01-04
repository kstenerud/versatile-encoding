package org.stenerud.remotefs.transport;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

public class SimpleTransportProducer implements TransportProducer {
    private static final Logger LOG = Logger.getLogger(SimpleTransportProducer.class.getName());
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

    public Transport produceTransportPair() {
        SimpleTransport transport1 = new SimpleTransport();
        SimpleTransport transport2 = new SimpleTransport(transport1);
        listener.onNewTransport(transport1);
        return transport2;
    }
}
