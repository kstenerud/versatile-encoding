package org.stenerud.remotefs.message;

public class ExceptionMessageSpecification extends Specification {
    public static final String PROCESS_ID = "process_id";
    public static final String RESOURCE_ID = "resource_id";
    public static final String TYPE = "type";
    public static final String MESSAGE = "message";
    public static final String CONTEXT_INFO = "context_info";

    public ExceptionMessageSpecification() {
        super("exception",
                "Exception notification",
                new ParameterSpecification(PROCESS_ID, Specification.Type.INTEGER, "The process associated with this exception"),
                new ParameterSpecification(RESOURCE_ID, Specification.Type.INTEGER, "The resource (if any) associated with this exception", Attribute.OPTIONAL),
                new ParameterSpecification(TYPE, Specification.Type.INTEGER, "Type of exception"),
                new ParameterSpecification(MESSAGE, Specification.Type.STRING, "Description of the exception", Attribute.OPTIONAL),
                new ParameterSpecification(CONTEXT_INFO, Specification.Type.MAP, "Contextual information", Attribute.OPTIONAL)
        );
    }
}
