package org.stenerud.remotefs.utility;

import java.util.Objects;

/**
 * Placeholder for 128-bit values until Java supports this better.
 */
public class Int128Holder {
    public final long highWord;
    public final long lowWord;

    public Int128Holder(long highWord, long lowWord) {
        this.highWord = highWord;
        this.lowWord = lowWord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Int128Holder)) return false;
        Int128Holder that = (Int128Holder) o;
        return highWord == that.highWord &&
                lowWord == that.lowWord;
    }

    @Override
    public int hashCode() {
        return Objects.hash(highWord, lowWord);
    }
}
