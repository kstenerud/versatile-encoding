package org.stenerud.remotefs.message;

import javax.annotation.Nonnull;

public class InternalExceptionMessageSpecification extends Specification {
    public static final String EXCEPTION = "exception";

    public InternalExceptionMessageSpecification() {
        super("internal exception",
                "Internal exception notification",
                new ParameterSpecification(EXCEPTION, Specification.Type.ANY, "Exception that was thrown")
        );
    }
}
