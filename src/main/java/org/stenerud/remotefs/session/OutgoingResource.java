package org.stenerud.remotefs.session;


import org.stenerud.remotefs.transport.MessageSender;

public class OutgoingResource<T> {
    private final MessageSender messageSender;

    public OutgoingResource(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void add(T data) {

    }
}
