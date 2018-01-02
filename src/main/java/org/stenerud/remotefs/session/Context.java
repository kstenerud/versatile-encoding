package org.stenerud.remotefs.session;

public class Context {
    private final Session session;

    public Context(Session session) {
        this.session = session;
    }

    public void closeSession() {
        // TODO: This needs to be done higher so that the session registration can bve removed
        session.getClass();
    }
}
