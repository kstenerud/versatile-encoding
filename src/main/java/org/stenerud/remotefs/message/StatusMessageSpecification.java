package org.stenerud.remotefs.message;

import org.stenerud.remotefs.utility.Specification;

public class StatusMessageSpecification extends Specification {
    public static final String JOB_ID = "job_id";
    public static final String COMPLETION = "completion";

    public StatusMessageSpecification() {
        super("status",
                "Status notification",
                new ParameterSpecification(JOB_ID, Specification.Type.INTEGER, "The ID of the job to report status on"),
                new ParameterSpecification(COMPLETION, Specification.Type.FLOAT, "Proportion complete (0-1)")
        );
    }
}
