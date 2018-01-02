package org.stenerud.remotefs.session;

import org.stenerud.remotefs.utility.Closer;

public class Session implements AutoCloseable {
    private final Transport transport;
    private final MessageRouter messageRouter;

    public Session(Transport transport, MessageRouter messageRouter) {
        this.transport = transport;
        this.messageRouter = messageRouter;
    }

    @Override
    public void close() throws Exception {
        Closer.closeAll(transport);
    }
}
