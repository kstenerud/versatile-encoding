package org.stenerud.remotefs.transport;

import javax.annotation.Nonnull;

public interface TransportProducer extends AutoCloseable {
    interface Listener {
        void onNewTransport(@Nonnull Transport transport);
    }

    void setListener(@Nonnull Listener listener);
}
