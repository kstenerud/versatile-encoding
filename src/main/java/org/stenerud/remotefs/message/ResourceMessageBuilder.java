package org.stenerud.remotefs.message;

import javax.annotation.Nonnull;

public class ResourceMessageBuilder extends MessageBuilder {
    public static final String ID = "resource";

    private static final String RESOURCE_ID = "resource_id";
    private static final String CHUNK_COUNT = "chunk_count";
    private static final String CHUNK_INDEX = "chunk_index";
    private static final String CHUNK = "chunk";

    public ResourceMessageBuilder() {
        super(new Specification(ID,
                "Resource message",
                new Specification.ParameterSpecification(RESOURCE_ID, Specification.Type.INTEGER, "Resource ID"),
                new Specification.ParameterSpecification(CHUNK_COUNT, Specification.Type.INTEGER, "The total number of chunks"),
                new Specification.ParameterSpecification(CHUNK_INDEX, Specification.Type.INTEGER, "The current index in the chunks being sent"),
                new Specification.ParameterSpecification(CHUNK, Specification.Type.ANY, "The chunk data")
        ));
    }

    public class Message extends org.stenerud.remotefs.message.Message {
        public Message() {
            super(ResourceMessageBuilder.this.getSpecification());
        }

        public Message(long resourceId, long chunkCount, long chunkIndex, Object chunkData) {
            this();
            set(RESOURCE_ID, resourceId);
            set(CHUNK_COUNT, chunkCount);
            set(CHUNK_INDEX, chunkIndex);
            set(CHUNK, chunkData);
        }

        public long getResourceId() {
            return get(RESOURCE_ID, Long.class);
        }

        public long getChunkCount() {
            return get(CHUNK_COUNT, Long.class);
        }

        public long getChunkIndex() {
            return get(CHUNK_INDEX, Long.class);
        }

        @Nonnull
        public Object getChunkData() {
            return get(CHUNK, Object.class);
        }
    }

    @Nonnull
    @Override
    public Message newMessage() {
        return new Message();
    }

    @Nonnull
    public Message newMessage(long resourceId, long totalChunks, long chunkIndex, Object chunkData) {
        return new Message(resourceId, totalChunks, chunkIndex, chunkData);
    }
}
