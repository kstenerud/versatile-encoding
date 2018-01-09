package org.stenerud.remotefs.message;

import javax.annotation.Nonnull;

public class ExampleMessageBuilder extends MessageBuilder {
    private static final String A = "a";
    private static final String B = "b";

    public ExampleMessageBuilder() {
        super(new Specification("example",
                "Example message",
                new Specification.ParameterSpecification(A, Specification.Type.INTEGER, "A"),
                new Specification.ParameterSpecification(B, Specification.Type.STRING, "B", Specification.Attribute.OPTIONAL)
        ));
    }

    public class Message extends org.stenerud.remotefs.message.Message {
        public Message() {
            super(ExampleMessageBuilder.this.getSpecification());
        }

        public Message(long a, String b) {
            this();
            set(A, a);
            set(B, b);
        }

        public long getProcessId() {
            return get(A, Long.class);
        }

        public String getCompletion() {
            return get(B, String.class);
        }
    }

    @Nonnull
    @Override
    public Message newMessage() {
        return new Message();
    }

    @Nonnull public Message newMessage(long a, String b) {
        return new Message(a, b);
    }
}
