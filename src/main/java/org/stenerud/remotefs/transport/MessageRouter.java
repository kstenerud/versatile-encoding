package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.session.Context;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

public class MessageRouter implements MessageProducer.Listener {
    private static final Logger LOG = Logger.getLogger(MessageRouter.class.getName());

    private final MessageHandlerRegistry handlerRegistry;
    private final Context context;

    public MessageRouter(MessageHandlerRegistry handlerRegistry, @Nonnull Context context) {
        this.handlerRegistry = handlerRegistry;
        this.context = context;
    }

    @Override
    public void onNewMessage(Message message) {
        handlerRegistry.get(message).handleMessage(message, context);
    }
}
