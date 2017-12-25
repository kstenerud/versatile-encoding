package org.stenerud.remotefs.message;

public class StatusMessageSpecification extends Specification {
    public static final String JOB_ID = "job_id";
    public static final String COMPLETION = "percent_complete";
    public static final String RETURN = "return_value";

    public StatusMessageSpecification() {
        super("status",
                "Status notification",
                new ParameterSpecification(JOB_ID, Specification.Type.INTEGER, "The ID of the job to report status on"),
                new ParameterSpecification(COMPLETION, Type.INTEGER, "Percent complete"),
                new ParameterSpecification(RETURN, Type.ANY, "Return value", Attribute.OPTIONAL)
        );
    }
}
