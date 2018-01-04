package org.stenerud.remotefs.session;

import javax.annotation.Nonnull;

public interface Process {
    void execute(@Nonnull Context context);
}
