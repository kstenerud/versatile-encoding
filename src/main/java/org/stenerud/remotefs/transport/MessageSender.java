package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.message.Message;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface MessageSender {
    void sendMessage(@Nonnull Message message) throws IOException;
}
