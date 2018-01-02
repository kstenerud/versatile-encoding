package org.stenerud.remotefs.session;

import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.StrictMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageRouter implements MessageProducer.Listener {

    public interface MessageHandler {
        void handleMessage(@Nonnull Message message);
    }

    private final Map<Specification, MessageHandler> messageHandlers = StrictMap.with(ConcurrentHashMap::new).withErrorFormat("%s: Unknown message type");

    public void registerHandler(Specification specification, MessageHandler handler) {
        messageHandlers.put(specification, handler);
    }

    @Override
    public void onNewMessage(Message message) {
        messageHandlers.get(message.getSpecification()).handleMessage(message);
    }
}
