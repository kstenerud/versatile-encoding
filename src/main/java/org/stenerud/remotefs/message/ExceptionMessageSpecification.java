package org.stenerud.remotefs.message;

public class ExceptionMessageSpecification extends Specification {
    public static final String CONTEXT_ID = "context_id";
    public static final String TYPE = "type";
    public static final String MESSAGE = "message";
    public static final String CONTEXT_INFO = "context_info";

    public ExceptionMessageSpecification() {
        super("exception",
                "Exception notification",
                new ParameterSpecification(CONTEXT_ID, Specification.Type.INTEGER, "Context (job or resource ID) that generated the exception"),
                new ParameterSpecification(TYPE, Specification.Type.INTEGER, "Type of exception"),
                new ParameterSpecification(MESSAGE, Specification.Type.STRING, "Description of the exception", Attribute.OPTIONAL),
                new ParameterSpecification(CONTEXT_INFO, Specification.Type.MAP, "Contextual information", Attribute.OPTIONAL)
        );
    }
}
