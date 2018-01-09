package org.stenerud.remotefs.session;

import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.transport.Transport;
import org.stenerud.remotefs.utility.Closer;
import org.stenerud.remotefs.utility.StrictMap;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Session implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(Session.class.getName());

    interface Listener {
        public void onClose(Session session);
    }

    private final Transport transport;
    private final Context context;
    private final Map<String, MessageHandler> messageHandlers = StrictMap.withImplementation(HashMap::new).withErrorFormat("No message handler registered for specification %s");
    private final Map<Integer, LocalProcess> activeProcesses = StrictMap.withImplementation(ConcurrentHashMap::new).withErrorFormat("%s: No such process");
    private final Map<Integer, OutgoingResource> outgoingResources = StrictMap.withImplementation(ConcurrentHashMap::new).withErrorFormat("%s: No such outgoing resource");
    private final Map<Integer, IncomingResource> incomingResources = StrictMap.withImplementation(ConcurrentHashMap::new).withErrorFormat("%s: No such incoming resource");

    private Listener listener = session -> {};

    public Session(Transport transport, Map<String, MessageHandler> messageHandlers, Context context) {
        this.context = new Context(context);
        this.context.put(this.getClass(), this);
        this.transport = transport;
        this.messageHandlers.putAll(messageHandlers);
        this.transport.setListener(message -> handleMessage(message));
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void handleMessage(@Nonnull Message message) {
        messageHandlers.get(message.getIdentifier()).handleMessage(message, context);
    }

    @Override
    public void close() throws Exception {
        try {
            Closer.closeAll(transport);
        } finally {
            listener.onClose(this);
        }
    }

    public <T> OutgoingResource<T> newOutgoingResource(Class<T> type) {
        return null;
    }

    public RemoteProcess callFunction(String name, ProcessMonitor processMonitor, Object ... arguments) {
        return null;
    }
}
