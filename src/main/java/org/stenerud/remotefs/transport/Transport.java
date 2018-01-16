package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.session.OutgoingResource;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface Transport extends MessageProducer, MessageSender, AutoCloseable {

    @Nonnull
    <T> OutgoingResource<T> sendStreamMessage(@Nonnull Message message, Class<T> streamType) throws IOException;

    void flush() throws IOException;

    public void setAutoflush(boolean autoflush);
}
