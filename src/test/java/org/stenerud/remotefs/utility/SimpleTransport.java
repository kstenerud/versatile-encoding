package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.session.Transport;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.Logger;

public class SimpleTransport implements Transport {
    private static final Logger LOG = Logger.getLogger(SimpleTransport.class.getName());
    private Listener listener = message -> {};
    private SimpleTransport peer;

    public SimpleTransport() {}

    public SimpleTransport(SimpleTransport peer) {
        this.peer = peer;
        peer.peer = this;
    }

    @Override
    public void setListener(@Nonnull Listener listener) {
        this.listener = listener;
    }

    @Override
    public void sendMessage(@Nonnull Message message) throws IOException {
        peer.listener.onNewMessage(message);
    }

    @Override public void flush() throws IOException {}
    @Override public void setAutoflush(boolean autoflush) {}
    @Override public void close() throws Exception {}
}
