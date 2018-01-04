package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.message.Message;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface Transport extends MessageProducer, AutoCloseable {
    void sendMessage(@Nonnull Message message) throws IOException;

    void flush() throws IOException;

    public void setAutoflush(boolean autoflush);
}
