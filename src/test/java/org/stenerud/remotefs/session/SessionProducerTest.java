package org.stenerud.remotefs.session;

import org.junit.Test;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.ObjectHolder;
import org.stenerud.remotefs.utility.SimpleTransportProducer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SessionProducerTest {
    @Test
    public void testSessionProduction() throws Exception {
        SimpleTransportProducer transportProducer = new SimpleTransportProducer();
        MessageHandlerRegistry registry = new MessageHandlerRegistry();
        Context context = new Context();
        SessionProducer sessionProducer = new SessionProducer(transportProducer, registry, context);
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
