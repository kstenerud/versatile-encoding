package org.stenerud.remotefs.session;

import org.stenerud.remotefs.message.Message;

import javax.annotation.Nonnull;

public class IncomingResource implements MessageHandler {
    boolean hasMore() {
        return false;
    }

    Object fetch() {
        return null;
    }

    @Override
    public void handleMessage(@Nonnull Message message, @Nonnull Context context) {

    }
}
