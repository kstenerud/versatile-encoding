package org.stenerud.remotefs.utility;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.logging.Logger;

public class Named {
    private static final Logger LOG = Logger.getLogger(Named.class.getName());
    public final String name;

    public Named(@Nonnull String name) {
        this.name = name;
    }

    @Override
    public boolean equals(@CheckForNull Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return name.equals(((Named)o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public @Nonnull String toString() {
        return name;
    }
}
