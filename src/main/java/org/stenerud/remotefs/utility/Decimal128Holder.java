package org.stenerud.remotefs.utility;

import java.util.Objects;

public class Decimal128Holder {
    public final long highWord;
    public final long lowWord;

    public Decimal128Holder(long highWord, long lowWord) {
        this.highWord = highWord;
        this.lowWord = lowWord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Decimal128Holder that = (Decimal128Holder) o;
        return highWord == that.highWord &&
                lowWord == that.lowWord;
    }

    @Override
    public int hashCode() {

        return Objects.hash(highWord, lowWord);
    }
}
