package org.stenerud.remotefs.message;

import org.stenerud.remotefs.utility.Specification;

public class ExceptionMessageSpecification extends Specification {
    public static final String CONTEXT = "context";
    public static final String TYPE = "type";
    public static final String MESSAGE = "message";

    public ExceptionMessageSpecification() {
        super("exception",
                "Exception notification",
                new ParameterSpecification(CONTEXT, Specification.Type.INTEGER, "Context (job or resource ID) that generated the exception"),
                new ParameterSpecification(TYPE, Specification.Type.INTEGER, "Type of exception"),
                new ParameterSpecification(MESSAGE, Specification.Type.STRING, "Description of the exception", Attribute.OPTIONAL)
        );
    }
}
