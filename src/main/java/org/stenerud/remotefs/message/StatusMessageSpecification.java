package org.stenerud.remotefs.message;

public class StatusMessageSpecification extends Specification {
    public static final String PROCESS_ID = "job_id";
    public static final String COMPLETION = "percent_complete";
    public static final String RETURN = "return_value";

    public StatusMessageSpecification() {
        super("status",
                "Status notification",
                new ParameterSpecification(PROCESS_ID, Specification.Type.INTEGER, "The ID of the process to report status on"),
                new ParameterSpecification(COMPLETION, Type.INTEGER, "Percent complete"),
                new ParameterSpecification(RETURN, Type.ANY, "Return value", Attribute.OPTIONAL)
        );
    }
}
