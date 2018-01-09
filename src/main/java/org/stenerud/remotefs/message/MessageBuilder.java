package org.stenerud.remotefs.message;

import javax.annotation.Nonnull;

public abstract class MessageBuilder {
    private final Specification specification;

    protected MessageBuilder(@Nonnull Specification specification) {
        this.specification = specification;
    }

    public @Nonnull String getIdentifier() {
        return specification.name;
    }

    public @Nonnull Specification getSpecification() {
        return specification;
    }

    /**
     * Default builder function. For internal use only.
     *
     * This is used by the message codec to progressively build the correct type of
     * message from an incoming stream.
     *
     * @return A new message with no parameters yet.
     */
    abstract public @Nonnull Message newMessage();
}
