package org.stenerud.remotefs.session;

import org.stenerud.remotefs.message.Message;

import javax.annotation.Nonnull;

public interface MessageProducer {
    interface Listener {
        public void onNewMessage(Message message);
    }

    void setListener(@Nonnull Listener listener);
}
