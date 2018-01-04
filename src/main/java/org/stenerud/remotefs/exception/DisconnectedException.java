package org.stenerud.remotefs.exception;

import javax.annotation.Nonnull;
import java.io.IOException;

public class DisconnectedException extends IOException {
    public DisconnectedException() {}
    public DisconnectedException(@Nonnull Throwable throwable) {
        super(throwable);
    }

    public DisconnectedException(String s) {
        super(s);
    }
}
