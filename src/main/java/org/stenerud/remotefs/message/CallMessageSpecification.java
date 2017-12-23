package org.stenerud.remotefs.message;

import org.stenerud.remotefs.utility.Specification;

public class CallMessageSpecification extends Specification {
    public static final String JOB_ID = "job_id";
    public static final String FUNCTION = "function";
    public static final String RETURN_ID = "return_id";
    public static final String PARAMETERS = "parameters";

    public CallMessageSpecification() {
        super("call",
                "Calls a function",
                new ParameterSpecification(JOB_ID, Specification.Type.INTEGER, "The ID of the job to report status on"),
                new ParameterSpecification(FUNCTION, Type.INTEGER, "Function to call"),
                new ParameterSpecification(RETURN_ID, Type.INTEGER, "Resource ID to use for the returned value", Attribute.OPTIONAL),
                new ParameterSpecification(PARAMETERS, Type.LIST, "Parameters to the function")
        );
    }
}
