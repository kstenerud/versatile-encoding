package org.stenerud.remotefs.message;

import javax.annotation.Nonnull;

public class ProcessStatusMessageBuilder extends MessageBuilder {
    private static final String PROCESS_ID = "process id";
    private static final String COMPLETION = "percent_complete";

    public ProcessStatusMessageBuilder() {
        super(new Specification("status",
                "Status notification",
                new Specification.ParameterSpecification(PROCESS_ID, Specification.Type.INTEGER, "The ID of the process to report status on"),
                new Specification.ParameterSpecification(COMPLETION, Specification.Type.INTEGER, "Percent complete")
        ));
    }

    public class Message extends org.stenerud.remotefs.message.Message {
        public Message() {
            super(ProcessStatusMessageBuilder.this.getSpecification());
        }

        public Message(long processId, long completion) {
            this();
            set(PROCESS_ID, processId);
            set(COMPLETION, completion);
        }

        public long getProcessId() {
            return get(PROCESS_ID, Long.class);
        }

        public long getCompletion() {
            return get(COMPLETION, Long.class);
        }
    }

    @Nonnull
    @Override
    public Message newMessage() {
        return new Message();
    }

    @Nonnull public Message newMessage(long processId, long completion) {
        return new Message(processId, completion);
    }
}
