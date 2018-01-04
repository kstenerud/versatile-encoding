package org.stenerud.remotefs.transport;

import org.junit.Test;
import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.session.Context;
import org.stenerud.remotefs.transport.MessageHandlerRegistry;
import org.stenerud.remotefs.transport.MessageRouter;
import org.stenerud.remotefs.utility.ObjectHolder;

import static org.junit.Assert.assertTrue;

public class MessageRouterTest {
    @Test
    public void testRouting() {
        MessageHandlerRegistry registry = new MessageHandlerRegistry();
        Specification specification = new Specification("test", "a description");
        ObjectHolder holder = new ObjectHolder();
        registry.registerHandler(specification, (message, context) -> holder.set(true));
        Context context = new Context();
        MessageRouter router = new MessageRouter(registry, context);
        router.onNewMessage(new Message(specification));
        assertTrue((boolean)holder.get());
    }
}
