package org.stenerud.remotefs.session;

import org.stenerud.remotefs.transport.MessageHandlerRegistry;
import org.stenerud.remotefs.transport.MessageRouter;
import org.stenerud.remotefs.transport.Transport;
import org.stenerud.remotefs.utility.Closer;

import java.util.logging.Logger;

public class Session implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(Session.class.getName());

    interface Listener {
        public void onClose(Session session);
    }

    private final Transport transport;
    private final MessageHandlerRegistry messageHandlerRegistry;
    private final Context context;
    private final MessageRouter messageRouter;

    private Listener listener = session -> {};

    public Session(Transport transport, MessageHandlerRegistry messageHandlerRegistry, Context context) {
        this.transport = transport;
        this.messageHandlerRegistry = messageHandlerRegistry;
        this.context = new Context(context);
        this.messageRouter = new MessageRouter(messageHandlerRegistry, context);
        this.transport.setListener(this.messageRouter);
        this.context.put(this.getClass(), this);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void close() throws Exception {
        try {
            Closer.closeAll(transport);
        } finally {
            listener.onClose(this);
        }
    }
}
