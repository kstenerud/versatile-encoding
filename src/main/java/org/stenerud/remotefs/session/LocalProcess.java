package org.stenerud.remotefs.session;

import javax.annotation.Nonnull;

public interface LocalProcess {
    void execute(@Nonnull Context context);
    void abort();
}
