package org.stenerud.remotefs.utility;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.logging.Logger;

public class BinaryBuffer {
    private static final Logger LOG = Logger.getLogger(BinaryBuffer.class.getName());
    public final byte[] data;
    public final int startOffset;
    public final int endOffset;
    public final int length;

    public BinaryBuffer(@Nonnull byte[] data, int startOffset, int endOffset) {
        if(startOffset < 0) {
            throw new IndexOutOfBoundsException("Start offset " + startOffset + " is less than 0");
        }
        if(startOffset > endOffset) {
            throw new IndexOutOfBoundsException("Start offset " + startOffset + " is greater than end offset " + endOffset);
        }
        if(endOffset > data.length) {
            throw new IndexOutOfBoundsException("End offset " + endOffset + " is greater than data length " + data.length);
        }
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

    public @Nonnull BinaryBuffer newCopy() {
        return newCopy(startOffset, endOffset);
    }

    public @Nonnull BinaryBuffer newCopy(int startOffset, int endOffset) {
        int length = endOffset - startOffset;
        BinaryBuffer newBuffer = new BinaryBuffer(length);
        newBuffer.copyFrom(this, startOffset, newBuffer.startOffset, length);
        return newBuffer;
    }

    public @Nonnull BinaryBuffer newView(int startOffset, int endOffset) {
        if(startOffset < this.startOffset) {
            throw new IndexOutOfBoundsException("New start offset " + startOffset + " is less than existing start offset " + this.startOffset);
        }
        if(endOffset > this.endOffset) {
            throw new IndexOutOfBoundsException("New end offset " + endOffset + " is greater than existing end offset " + this.endOffset);
        }
        return new BinaryBuffer(data, startOffset, endOffset);
    }

    public @Nonnull BinaryBuffer newView(int startOffset) {
        return newView(startOffset, endOffset);
    }

    public @Nonnull BinaryBuffer newView() {
        return newView(startOffset, endOffset);
    }

    private static final char[] HEX_VALUES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Override
    public @Nonnull String toString() {
        final int maxLengthForToString = 100;
        int workingEndOffset = endOffset;
        String suffix = "";
        if(length > maxLengthForToString) {
            workingEndOffset = startOffset + maxLengthForToString;
            suffix = ", ...";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(int i = startOffset; i < workingEndOffset; i++) {
            byte value = data[i];
            builder.append(HEX_VALUES[(value >> 4) & 0xf]);
            builder.append(HEX_VALUES[value & 0xf]);
            if(i < endOffset - 1) {
                builder.append(",");
            }
        }
        builder.append(suffix);
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

    public boolean hasSpace(int startOffset, int endOffset) {
        return startOffset >= this.startOffset &&
                endOffset <= this.endOffset &&
                startOffset <= endOffset;
    }

    public int lengthToOffset(int offset) {
        return offset - startOffset;
    }

    public int lengthRemainingFromOffset(int offset) {
        return endOffset - offset;
    }

    public void copyFrom(BinaryBuffer them, int themStartOffset, int thisStartOffset, int length) {
        if(themStartOffset + length > them.endOffset) {
            throw new IndexOutOfBoundsException("Source start offset " + themStartOffset + " + length " + length + " is beyond end offset " + them.endOffset);
        }
        copyFrom(them.data, themStartOffset, thisStartOffset, length);
    }

    public void copyFrom(byte[] srcData, int srcDataStartOffset, int thisStartOffset, int length) {
        if(srcDataStartOffset + length > srcData.length) {
            throw new IndexOutOfBoundsException("Source start offset " + srcDataStartOffset + " + length " + length + " is beyond buffer length " + srcData.length);
        }
        if(lengthRemainingFromOffset(thisStartOffset) < length) {
            throw new IndexOutOfBoundsException("Dst start offset " + thisStartOffset + " + length " + length + " is beyond end offset " + endOffset);
        }
        int di = thisStartOffset;
        int dEnd = di + length;
        int si = srcDataStartOffset;
        while(di < dEnd) {
            data[di++] = srcData[si++];
        }
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
        int result = Objects.hash(startOffset, length);
        for(int offset = startOffset; offset < endOffset; offset++) {
            result = 31 * data[offset];
        }
        return result;
    }
}
