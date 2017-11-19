package org.stenerud.remotefs;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;

public class BinaryBuffer {
    public final byte[] data;
    public final int startOffset;
    public final int endOffset;
    public final int length;

    public BinaryBuffer(byte[] data, int startOffset, int endOffset) {
        this.data = data;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.length = endOffset - startOffset;
    }

    public BinaryBuffer(byte[] data) {
        this(data, 0, data.length);
    }

    public BinaryBuffer(int length) {
        this(new byte[length], 0, length);
    }

    public BinaryBuffer view(int startOffset, int endOffsetValue) {
        return new BinaryBuffer(data, startOffset, endOffsetValue);
    }

    public BinaryBuffer view() {
        return view(startOffset, endOffset);
    }

    @Override
    public @Nonnull String toString() {
        try {
            return new String(data, startOffset, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported", e);
        }
    }

    public BinaryBuffer copy() {
        return copy(startOffset, endOffset);
    }

    public BinaryBuffer copy(int startOffset, int endOffset) {
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
    public boolean equals(Object otherObject) {
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
}
//copy to array
//copy create buffer
