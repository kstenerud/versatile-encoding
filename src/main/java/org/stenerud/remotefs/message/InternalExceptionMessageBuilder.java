package org.stenerud.remotefs.message;

import javax.annotation.Nonnull;

/**
 * "Message" for transmitting internal unhandled exceptions from a message producer's implementation.
 * This message is not meant to be sent over a transport, nor should it ever be included in a schema.
 * It's purely for local, internal signalling.
 *
 * The "exception" field should contain the actual throwable exception object.
 */
public class InternalExceptionMessageBuilder extends MessageBuilder {
    private static final String EXCEPTION = "exception";

    public InternalExceptionMessageBuilder() {
        super(new Specification("internal exception",
                "Internal exception notification. Not to be included in a schema or transmitted over a transport.",
                new Specification.ParameterSpecification(EXCEPTION, Specification.Type.ANY, "Exception that was thrown")
        ));
    }

    public class Message extends org.stenerud.remotefs.message.Message {
        Message(@Nonnull Throwable e) {
            super(InternalExceptionMessageBuilder.this.getSpecification());
            setUnchecked(EXCEPTION, Specification.Type.ANY, e);
        }

        @Nonnull
        public Throwable getException() {
            return get(EXCEPTION, Throwable.class);
        }
    }

    @Nonnull
    @Override
    public Message newMessage() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public Message newMessage(@Nonnull Throwable e) {
        return new Message(e);
    }
}
