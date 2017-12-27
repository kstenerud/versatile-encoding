package org.stenerud.remotefs;

import javax.annotation.Nonnull;
import java.io.IOException;

public class DisconnectedException extends IOException {
    public DisconnectedException() {}
    public DisconnectedException(@Nonnull Throwable throwable) {
        super(throwable);
    }
}
