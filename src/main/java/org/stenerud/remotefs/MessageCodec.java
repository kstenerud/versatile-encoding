package org.stenerud.remotefs;

import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.Parameters;
import org.stenerud.remotefs.utility.Specification;
import org.stenerud.remotefs.utility.StrictMap;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

// LL: SSSSTTXX
// LH: SSSSSSSS
// HL: SSSSSSSS
// HH: SSSSSSSS
// Read as int32. Low 2 bits are type, mid 2 bits are size. Discard what's not needed, shift right 4.
// XX: how many additional bytes are used (0-3)
// Message sizes 15 + 4 (4 bit), 4k + 4 (12 bit), 1M + 4 (20 bit), 256M + 4 (28 bit)
// NOTE: Minimum message size (size field + message contents) is 4
public class MessageCodec {
    public interface Types {
        // total chunks:
        // - positive: Most likely this number of chunks
        // - 0: no idea how many chunks
        // chunk number:
        // - positive: chunk number
        // - 0: this is the last chunk
        int RESOURCE  = 0x00; // stream id, total chunks, chunk number, chunk data (3-12 bytes + chunk) (single = 3-6 + chunk)
        int CALL      = 0x04; // job id, function, result stream id, parameters (3-12 bytes + params)
        int STATUS    = 0x08; // job id, proportion complete (5-8 bytes)
        int EXCEPTION = 0x0c; // context id, type, message (2-8 bytes + message)

        // resource encoding/compression?

        // Functions:
        // get session id (void)
        // reconnect session id (id)
        // close session id (id)
        // abort function call (id)
        // get schema (search params, as summary?)
    }
    private static final int TYPE_MASK = 0x0c;
    private static final int SIZE_FIELD_SIZE_MASK = 0x03;
    private static final int SIZE_SHIFT_AMOUNT = 4;
    private static final int SIZE_MASKS[] = {0x0f, 0x0fff, 0x0fffff, 0x0fffffff};
    private static final int MAX_MESSAGE_CONTENTS_OFFSET = 4;
    private static final int MIN_MESSAGE_SIZE = MAX_MESSAGE_CONTENTS_OFFSET;
    public static final int MAX_MESSAGE_SIZE = SIZE_MASKS[SIZE_MASKS.length - 1] + MIN_MESSAGE_SIZE;

    private final Map<Specification, Integer> specToType = new StrictMap<>(HashMap::new);
    private final Map<Integer, Specification> typeToSpec = new StrictMap<>(HashMap::new);

    private static int getTypeFieldContents(int field) {
        return field & TYPE_MASK;
    }

    private static int getSizeFieldContents(int field) {
        return field & SIZE_FIELD_SIZE_MASK;
    }

    public static int getMessageSize(int field) {
        return ((field>>SIZE_SHIFT_AMOUNT) & SIZE_MASKS[getSizeFieldContents(field)]) + MIN_MESSAGE_SIZE;
    }

    private static int getMessageSizeSize(int field) {
        return getSizeFieldContents(field) + 1;
    }

    private static int getAppropriateSizeFieldSize(int size) {
        for(int i = 0; i < SIZE_MASKS.length; i++) {
            if(size < SIZE_MASKS[i]) {
                return i;
            }
        }
        throw new IllegalStateException("Message is too long (" + (size + MIN_MESSAGE_SIZE) + "). Max length is " + MAX_MESSAGE_SIZE);
    }

    private static int combineTypeAndSize(int messageType, int size) {
        size -= MIN_MESSAGE_SIZE;
        if(size < 0) {
            throw new IllegalStateException("Message is too short (" + (size + MIN_MESSAGE_SIZE) + "). Min length is " + MIN_MESSAGE_SIZE);
        }
        return (size << SIZE_SHIFT_AMOUNT) | messageType | getAppropriateSizeFieldSize(size);
    }

    public void registerSpecification(@Nonnull Specification spec, int type) {
        if((type & TYPE_MASK) != type) {
            throw new IllegalArgumentException("Message type " + type + " is outside of allowed range");
        }
        specToType.put(spec, type);
        typeToSpec.put(type, spec);
    }

    public BinaryBuffer encode(@Nonnull Parameters parameters, @Nonnull BinaryBuffer buffer) throws BinaryCodec.NoRoomException {
        parameters.verifyCompleteness();
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(buffer.newView(MAX_MESSAGE_CONTENTS_OFFSET));

        for(Object value: parameters) {
            encoder.writeObject(value);
        }

        int typeAndSize = combineTypeAndSize(specToType.get(parameters.getSpecification()), encoder.view().length);
        LittleEndianCodec.encodeInt32(typeAndSize, buffer.data, buffer.startOffset);
        int messageSizeSize = getMessageSizeSize(typeAndSize);
        int messageOffset = buffer.startOffset + MAX_MESSAGE_CONTENTS_OFFSET;
        int sizeFieldOffset = messageOffset - messageSizeSize;
        if(sizeFieldOffset < messageOffset) {
            moveMemory(buffer, buffer.startOffset, sizeFieldOffset, messageSizeSize);
        }
        return buffer.newView(sizeFieldOffset, encoder.view().endOffset);
    }

    private void moveMemory(BinaryBuffer buffer, int srcOffset, int dstOffset, int size) {
        int si = srcOffset + size;
        int di = dstOffset + size;
        byte[] data = buffer.data;
        while(si > srcOffset) {
            data[--di] = data[--si];
        }
    }

    public @Nonnull Parameters decode(@Nonnull BinaryBuffer buffer) {
        int typeAndSize = LittleEndianCodec.decodeInt32(buffer.data, buffer.startOffset);
        Specification specification = typeToSpec.get(getTypeFieldContents(typeAndSize));
        Parameters parameters = new Parameters(specification);
        BinaryCodec.Decoder decoder = new BinaryCodec.Decoder(new BinaryCodec.Decoder.Visitor() {
            @Override
            public void onValue(Object value) {
                parameters.add(value);
            }
        });
        decoder.feed(buffer.newView(buffer.startOffset + getMessageSizeSize(typeAndSize)));

        parameters.verifyCompleteness();
        return parameters;
    }
}
