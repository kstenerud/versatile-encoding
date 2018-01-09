package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.session.MessageHandler;
import org.stenerud.remotefs.utility.StrictMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class MessageHandlerRegistry {
    private static final Logger LOG = Logger.getLogger(MessageHandlerRegistry.class.getName());
    private final Map<Specification, MessageHandler> messageHandlers = StrictMap.withImplementation(ConcurrentHashMap::new).withErrorFormat("%s: Unknown message type");

    public void registerHandler(Specification specification, MessageHandler handler) {
        messageHandlers.put(specification, handler);
    }

    public MessageHandler get(Message message) {
        return messageHandlers.get(message.getIdentifier());
    }
}
