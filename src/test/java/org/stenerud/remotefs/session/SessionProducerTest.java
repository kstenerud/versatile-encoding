package org.stenerud.remotefs.session;

import org.junit.Test;
import org.stenerud.remotefs.transport.MessageHandlerRegistry;
import org.stenerud.remotefs.utility.ObjectHolder;
import org.stenerud.remotefs.transport.SimpleTransportProducer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SessionProducerTest {
    @Test
    public void testSessionProduction() throws Exception {
        SimpleTransportProducer transportProducer = new SimpleTransportProducer();
        Map<String, MessageHandler> messageHandlers = new HashMap<>();
        Context context = new Context();
        SessionProducer sessionProducer = new SessionProducer(transportProducer, messageHandlers, context);
        ObjectHolder holder = new ObjectHolder();
        sessionProducer.setListener(session -> holder.set(session));
        transportProducer.produceTransportPair();
        Session session = (Session)holder.get();
        holder.clear();
        assertNotNull(session);
        session.setListener(session1 -> holder.set(true));
        session.close();
        assertTrue((boolean)holder.get());
    }
}
