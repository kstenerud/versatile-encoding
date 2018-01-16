package org.stenerud.remotefs.message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class ExceptionMessageBuilder extends MessageBuilder {
    public static final String ID = "exception";

    private static final String PROCESS_ID = "process_id";
    private static final String RESOURCE_ID = "resource_id";
    private static final String TYPE = "type";
    private static final String MESSAGE = "message";
    private static final String CONTEXT_INFO = "context_info";

    public ExceptionMessageBuilder() {
        super(new Specification(ID,
                "Exception notification",
                new Specification.ParameterSpecification(PROCESS_ID, Specification.Type.INTEGER, "The process (if any) associated with this exception", Specification.Attribute.OPTIONAL),
                new Specification.ParameterSpecification(RESOURCE_ID, Specification.Type.INTEGER, "The resource (if any) associated with this exception", Specification.Attribute.OPTIONAL),
                new Specification.ParameterSpecification(TYPE, Specification.Type.INTEGER, "Type of exception"),
                new Specification.ParameterSpecification(MESSAGE, Specification.Type.STRING, "Description of the exception", Specification.Attribute.OPTIONAL),
                new Specification.ParameterSpecification(CONTEXT_INFO, Specification.Type.MAP, "Contextual information", Specification.Attribute.OPTIONAL)
        ));
    }

    public class Message extends org.stenerud.remotefs.message.Message {
        public Message() {
            super(ExceptionMessageBuilder.this.getSpecification());
        }

        public Message(@Nullable Long processId, @Nullable Long resourceId, long type, @Nullable String message, @Nullable Map<Object, Object> contextInfo) {
            this();
            set(PROCESS_ID, processId);
            set(RESOURCE_ID, resourceId);
            set(TYPE, type);
            set(MESSAGE, message);
            set(CONTEXT_INFO, contextInfo);
        }

        public @Nullable Long getProcessId() {
            return get(PROCESS_ID, Long.class);
        }

        public @Nullable Long getResourceId() {
            return get(RESOURCE_ID, Long.class);
        }

        public long getType() {
            return get(TYPE, Long.class);
        }

        public @Nullable String getMessage() {
            return get(MESSAGE, String.class);
        }

        public @Nullable Map<Object, Object> getContextInfo() {
            return get(CONTEXT_INFO, Map.class);
        }
    }

    @Override
    public @Nonnull Message newMessage() {
        return new Message();
    }

    public @Nonnull Message newMessage(@Nullable Long processId, @Nullable Long resourceId, long type, @Nullable String message, @Nullable Map<Object, Object> contextInfo) {
        return new Message(processId, resourceId, type, message, contextInfo);
    }
}
