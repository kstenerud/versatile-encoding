package org.stenerud.remotefs;

import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.Parameters;
import org.stenerud.remotefs.utility.Specification;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

// Length + type encoding:
// first 2 bits: message type
// remaining bits: length
// when top bit set, read next byte for next lower part of length
// repeat
// T = type encoding, X = extension bit, S = size data bit
// TTXSSSSS  5 = 32
// XSSSSSSS 12 = 4K
// XSSSSSSS 19 = 512K
// XSSSSSSS 26 = 64M

// TTXXSSSS  4 = 16
// SSSSSSSS 12 = 4k
// SSSSSSSS 20 = 1M
// SSSSSSSS 28 = 512M

// For LE:
// LL: SSSSXXTT
// LH: SSSSSSSS
// HL: SSSSSSSS
// HH: SSSSSSSS
// Read as int32. Low 2 bits are type, mid 2 bits are size. Discard what's not needed, shift right 4.
// XX: how many additional bytes are used (0-3)

// alternative:
// TTSSSSSS SSSSSSSS 14 = 16k
// Upper values have special meanings for ^2
// 3fff = 16k - 8
// 3ffe = 16k
// 3ffd = 32k
// 3ffc = 64k
// 3ffb = 128k
// 3ffa = 256k
// 3ff9 = 512k
// 3ff8 = 1M
public class MessageCodec {
    public interface Types {
        // total chunks:
        // - positive: Most likely this number of chunks
        // - 0: no idea how many chunks
        // chunk number:
        // - positive: chunk number
        // - 0: this is the last chunk
        int RESOURCE  = 0x00000000; // stream id, total chunks, chunk number, chunk data (3-12 bytes + chunk) (single = 3-6 + chunk)
        int CALL      = 0x40000000; // job id, function, result stream id, parameters (3-12 bytes + params)
        int STATUS    = 0x80000000; // job id, proportion complete (5-8 bytes)
        int EXCEPTION = 0xc0000000; // context id, type, message (2-8 bytes + message)

        // resource encoding/compression?

        // Functions:
        // get session id (void)
        // reconnect session id (id)
        // close session id (id)
        // abort function call (id)
        // get schema (search params, as summary?)
    }
    private static final int TYPE_MASK = 0xe0000000;
    private static final int MESSAGE_CONTENTS_OFFSET = 4;

    private final Map<Specification, Integer> specToType = new HashMap<>();
    private final Map<Integer, Specification> typeToSpec = new HashMap<>();

    public static final int MAX_MESSAGE_SIZE = ~TYPE_MASK;

    private static int getType(int field) {
        return field & TYPE_MASK;
    }

    private static int getSize(int field) {
        return field & MAX_MESSAGE_SIZE;
    }

    private static int combineTypeAndSize(int messageType, int size) {
        if(getSize(size) != size) {
            throw new IllegalStateException("Message is too long (" + size + "). Max length is " + MAX_MESSAGE_SIZE);
        }
        return size | messageType;
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
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(buffer.newView(MESSAGE_CONTENTS_OFFSET));

        for(Object value: parameters) {
            encoder.writeObject(value);
        }

        int typeAndSize = combineTypeAndSize(specToType.get(parameters.getSpecification()), encoder.view().length);
        LittleEndianCodec.encodeInt32(typeAndSize, buffer.data, buffer.startOffset);
        return buffer.newView(buffer.startOffset, MESSAGE_CONTENTS_OFFSET + encoder.view().length);
    }

    public @Nonnull Parameters decode(@Nonnull BinaryBuffer buffer) {
        int typeAndSize = LittleEndianCodec.decodeInt32(buffer.data, buffer.startOffset);
        Specification specification = typeToSpec.get(getType(typeAndSize));
        Parameters parameters = new Parameters(specification);
        BinaryCodec.Decoder decoder = new BinaryCodec.Decoder(new BinaryCodec.Decoder.Visitor() {
            @Override
            public void onValue(Object value) {
                parameters.add(value);
            }
        });
        decoder.feed(buffer.newView(MESSAGE_CONTENTS_OFFSET));

        parameters.verifyCompleteness();
        return parameters;
    }
}
