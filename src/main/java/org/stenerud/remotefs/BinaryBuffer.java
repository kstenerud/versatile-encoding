package org.stenerud.remotefs;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class BinaryBuffer {
    public final byte[] data;
    public final int startOffset;
    public final int endOffset;
    public final int length;

    public BinaryBuffer(@Nonnull byte[] data, int startOffset, int endOffset) {
        this.data = data;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.length = endOffset - startOffset;
    }

    public BinaryBuffer(@Nonnull byte[] data) {
        this(data, 0, data.length);
    }

    public BinaryBuffer(int length) {
        this(new byte[length], 0, length);
    }

    public @Nonnull BinaryBuffer view(int startOffset, int endOffset) {
        return new BinaryBuffer(data, startOffset, endOffset);
    }

    public @Nonnull BinaryBuffer view(int startOffset) {
        return new BinaryBuffer(data, startOffset, endOffset);
    }

    public @Nonnull BinaryBuffer view() {
        return view(startOffset, endOffset);
    }

    private static final char[] HEX_VALUES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Override
    public @Nonnull String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(int i = startOffset; i < endOffset; i++) {
            byte value = data[i];
            builder.append(HEX_VALUES[(value >> 4) & 0xf]);
            builder.append(HEX_VALUES[value & 0xf]);
            if(i < endOffset - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public @Nonnull String utf8String() {
        try {
            return new String(data, startOffset, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported", e);
        }
    }

    public @Nonnull BinaryBuffer copy() {
        return copy(startOffset, endOffset);
    }

    public @Nonnull BinaryBuffer copy(int startOffset, int endOffset) {
        int length = endOffset - startOffset;
        BinaryBuffer newBuffer = new BinaryBuffer(length);
        int si = startOffset;
        int di = 0;
        while(si < endOffset) {
            newBuffer.data[di++] = data[si++];
        }
        return newBuffer;
    }

    @Override
    public boolean equals(@CheckForNull Object otherObject) {
        if(otherObject == null) {
            return false;
        }
        if(otherObject == this) {
            return true;
        }
        if(this.getClass() != otherObject.getClass()) {
            return false;
        }
        BinaryBuffer other = (BinaryBuffer)otherObject;
        if(this.length != other.length) {
            return false;
        }
        if(this.startOffset == other.startOffset && this.data == other.data) {
            return true;
        }
        for(int i = 0; i < this.length; i++) {
            if(this.data[this.startOffset + i] != other.data[other.startOffset + i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for(int i = startOffset; i < endOffset; i++) {
            result = 31 * result + data[i];
        }
        result = 31 * result + startOffset;
        result = 31 * result + endOffset;
        return result;
    }
}
