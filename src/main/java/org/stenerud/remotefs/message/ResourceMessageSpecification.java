package org.stenerud.remotefs.message;

public class ResourceMessageSpecification extends Specification {
    public static final String PROCESS_ID = "process_id";
    public static final String RESOURCE_ID = "resource_id";
    public static final String CHUNK_COUNT = "chunk_count";
    public static final String CHUNK_INDEX = "chunk_index";
    public static final String CHUNK = "chunk";

    public ResourceMessageSpecification() {
        super("resource",
                "Resource message",
                new ParameterSpecification(PROCESS_ID, Specification.Type.INTEGER, "The process this resource is for"),
                new ParameterSpecification(RESOURCE_ID, Specification.Type.INTEGER, "Resource ID"),
                new ParameterSpecification(CHUNK_COUNT, Specification.Type.INTEGER, "The total number of chunks"),
                new ParameterSpecification(CHUNK_INDEX, Specification.Type.INTEGER, "The current index in the chunks being sent"),
                new ParameterSpecification(CHUNK, Specification.Type.ANY, "The chunk data")
        );
    }
}
