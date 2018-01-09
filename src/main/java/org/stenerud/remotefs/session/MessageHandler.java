package org.stenerud.remotefs.session;

import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.session.Context;

import javax.annotation.Nonnull;

public interface MessageHandler {
    void handleMessage(@Nonnull Message message, @Nonnull Context context);
}
