package org.stenerud.remotefs;

import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.Parameters;
import org.stenerud.remotefs.utility.Specification;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class MessageCodec {
    public interface Types {
        // request message specs msg
        int END_SESSION = 0xe0000000;
        int RESOURCE  = 0xc0000000;
        int CALL      = 0xa0000000;
        int ABORT     = 0x80000000; // abort job or resource...
        int STATUS    = 0x60000000;
        int EXCEPTION = 0x40000000;
        int RESERVED1 = 0x20000000;
        int RESERVED2 = 0x00000000;
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
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(buffer.view(MESSAGE_CONTENTS_OFFSET));

        for(Object value: parameters) {
            encoder.writeObject(value);
        }

        int typeAndSize = combineTypeAndSize(specToType.get(parameters.getSpecification()), encoder.view().length);
        LittleEndianCodec.encodeInt32(typeAndSize, buffer.data, buffer.startOffset);
        return buffer.view(buffer.startOffset, MESSAGE_CONTENTS_OFFSET + encoder.view().length);
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
        decoder.feed(buffer.view(MESSAGE_CONTENTS_OFFSET));

        parameters.verifyCompleteness();
        return parameters;
    }
}
