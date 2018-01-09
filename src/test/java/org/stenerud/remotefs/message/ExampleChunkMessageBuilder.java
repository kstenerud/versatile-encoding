package org.stenerud.remotefs.message;

import javax.annotation.Nonnull;

public class ExampleChunkMessageBuilder extends MessageBuilder {
    private static final String CHUNK = "chunk";

    public ExampleChunkMessageBuilder() {
        super(new Specification("example chunk",
                "Example chunk message",
                new Specification.ParameterSpecification(CHUNK, Specification.Type.ANY, "Chunk")
        ));
    }

    public class Message extends org.stenerud.remotefs.message.Message {
        public Message() {
            super(ExampleChunkMessageBuilder.this.getSpecification());
        }

        public Message(Object chunk) {
            this();
            set(CHUNK, chunk);
        }

        public Object getChunk() {
            return get(CHUNK, Object.class);
        }
    }

    @Nonnull
    @Override
    public Message newMessage() {
        return new Message();
    }

    @Nonnull public Message newMessage(Object chunk) {
        return new Message(chunk);
    }
}
