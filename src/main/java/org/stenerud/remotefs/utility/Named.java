package org.stenerud.remotefs.utility;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class Named {
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
