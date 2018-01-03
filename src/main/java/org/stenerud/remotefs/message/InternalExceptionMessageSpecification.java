package org.stenerud.remotefs.message;

/**
 * "Message" for transmitting internal unhandled exceptions from a message producer's implementation.
 * This message is not meant to be sent over a transport, nor should it ever be included in a schema.
 * It's purely for local, internal signalling.
 *
 * The "exception" field should contain the actual throwable exception object.
 */
public class InternalExceptionMessageSpecification extends Specification {
    public static final String EXCEPTION = "exception";

    public InternalExceptionMessageSpecification() {
        super("internal exception",
                "Internal exception notification. Not to be included in a schema or transmitted over a transport.",
                new ParameterSpecification(EXCEPTION, Specification.Type.ANY, "Exception that was thrown")
        );
    }
}
