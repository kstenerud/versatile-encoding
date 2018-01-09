package org.stenerud.remotefs.message;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public abstract class FunctionCallMessageBuilder extends MessageBuilder {
    protected static final String PROCESS_ID = "process id";

    private static List<Specification.ParameterSpecification> addToFront(Specification.ParameterSpecification[] parameters,
                                                                         Specification.ParameterSpecification firstParam) {
        List<Specification.ParameterSpecification> params = Arrays.asList(parameters);
        params.add(0, firstParam);
        return params;
    }

    public FunctionCallMessageBuilder(@Nonnull String name, @Nonnull String description, Specification.ParameterSpecification ... parameters) {
        super(new Specification(name,
                description,
                addToFront(parameters, new Specification.ParameterSpecification(PROCESS_ID, Specification.Type.INTEGER, "The process identifier to use"))
        ));
    }

    public abstract class Message extends org.stenerud.remotefs.message.Message {
        public Message() {
            super(FunctionCallMessageBuilder.this.getSpecification());
        }

        public Message(long processId) {
            this();
            set(PROCESS_ID, processId);
        }

        public long getProcessId() {
            return get(PROCESS_ID, Long.class);
        }
    }
}
