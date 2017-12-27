package org.stenerud.remotefs.utility;

/**
 * Holder for raw IEEE 754 decimal128 values.
 */
public class Decimal128Holder extends Int128Holder {
    public Decimal128Holder(long highWord, long lowWord) {
        super(highWord, lowWord);
    }

    public Decimal128Holder(Int128Holder holder) {
        super(holder.highWord, holder.lowWord);
    }
}
