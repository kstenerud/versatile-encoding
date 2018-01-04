package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.message.Message;

import javax.annotation.Nonnull;

public interface MessageProducer {
    interface Listener {
        public void onNewMessage(Message message);
    }

    void setListener(@Nonnull Listener listener);
}
