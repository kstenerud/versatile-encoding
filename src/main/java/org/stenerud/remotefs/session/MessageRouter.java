package org.stenerud.remotefs.session;

import org.stenerud.remotefs.message.Parameters;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.StrictMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageRouter {
    public interface MessageHandler {
        void handleMessage(@Nonnull Parameters messageParameters);
    }

    private final Map<Specification, MessageHandler> messageHandlers = new StrictMap<>(ConcurrentHashMap::new);

    public void registerHandler(Specification specification, MessageHandler handler) {
        messageHandlers.put(specification, handler);
    }

    public void routeMessage(Parameters messageParameters) {
        messageHandlers.get(messageParameters.getSpecification()).handleMessage(messageParameters);
    }
}
