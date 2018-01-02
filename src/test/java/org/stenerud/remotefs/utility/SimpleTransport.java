package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.session.Transport;

import javax.annotation.Nonnull;
import java.io.IOException;

public class SimpleTransport implements Transport {
    private Listener listener = message -> {};

    @Override
    public void setListener(@Nonnull Listener listener) {
        this.listener = listener;
    }

    @Override
    public void sendMessage(@Nonnull Message message) throws IOException {
        listener.onNewMessage(message);
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void setAutoflush(boolean autoflush) {

    }

    @Override
    public void close() throws Exception {

    }
}
