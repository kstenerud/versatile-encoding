package org.stenerud.remotefs.session;

import org.stenerud.remotefs.utility.Closer;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

public class SessionProducer {
    private static final Logger LOG = Logger.getLogger(SessionProducer.class.getName());
    interface Listener {
        void onNewSession(Session session);
    }

    private final TransportProducer transportProducer;
    private final MessageHandlerRegistry messageHandlerRegistry;
    private final Context context;
    private Listener listener = new Listener() {
        @Override
        public void onNewSession(Session session) {
            Closer.closeAllAndLogErrors(session);
        }
    };

    public SessionProducer(TransportProducer transportProducer, MessageHandlerRegistry messageHandlerRegistry, Context context) {
        this.transportProducer = transportProducer;
        this.messageHandlerRegistry = messageHandlerRegistry;
        this.context = new Context(context);
        transportProducer.setListener(new TransportProducer.Listener() {
            @Override
            public void onNewTransport(@Nonnull Transport transport) {
                listener.onNewSession(new Session(transport, messageHandlerRegistry, context));
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
