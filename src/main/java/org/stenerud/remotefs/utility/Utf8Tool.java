package org.stenerud.remotefs.utility;

public class Utf8Tool {
    private static final byte MULTIBYTE_MASK = (byte)0x80;
    private static final byte MULTIBYTE_INITIATOR_MASK = (byte)0xc0;
    private static final byte TWO_BYTE_MASK = (byte)0xe0;
    private static final byte TWO_BYTE_MATCH = (byte)0xc0;
    private static final byte THREE_BYTE_MASK = (byte)0xf0;
    private static final byte THREE_BYTE_MATCH = (byte)0xe0;
    private static final byte FOUR_BYTE_MASK = (byte)0xf8;
    private static final byte FOUR_BYTE_MATCH = (byte)0xf0;

    public static int offsetToLastFullUTF8Character(byte[] data, int offset, int minimumOffset) {
        // Regular character
        if(offset == minimumOffset || (data[offset-1] & MULTIBYTE_MASK) == 0) {
            return offset;
        }

        for(int i = -1; i >= -4; i--) {
            byte currentByte = data[i + offset];
            if((currentByte & MULTIBYTE_INITIATOR_MASK) == MULTIBYTE_INITIATOR_MASK) {
                int multibyteCount = -i;
                int expectedMultibyteCount;
                if((currentByte & TWO_BYTE_MASK) == TWO_BYTE_MATCH) {
                    expectedMultibyteCount = 2;
                } else if((currentByte & THREE_BYTE_MASK) == THREE_BYTE_MATCH) {
                    expectedMultibyteCount = 3;
                } else if((currentByte & FOUR_BYTE_MASK) == FOUR_BYTE_MATCH) {
                    expectedMultibyteCount = 4;
                } else {
                    throw new IllegalStateException("Malformed UTF-8 character");
                }
                if(multibyteCount == expectedMultibyteCount) {
                    return offset;
                }
                if(multibyteCount < expectedMultibyteCount) {
                    return offset - multibyteCount;
                }
                throw new IllegalStateException("Malformed UTF-8 character");
            }
        }
        throw new IllegalStateException("Malformed UTF-8 character");
    }
}
