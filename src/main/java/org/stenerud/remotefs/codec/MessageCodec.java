package org.stenerud.remotefs.codec;

import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.message.Parameters;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.StrictMap;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

// Length encoding is s3: vvvvvvvS (vvvvvvvv vvvvvvvv). When S = 1, two more bytes follow.
// Type encoding is s2: vvvvvvvS (vvvvvvvv). When S = 1, one more byte follows.
// Read as little endian, shift right 1. S bit determines what upper bits to mask.
// Message length is length including length field.
// Minimum message length is 2.
public class MessageCodec {
    public interface Types {
        // total chunks:
        // - positive: Most likely this number of chunks
        // - 0: no idea how many chunks
        // chunk number:
        // - positive: chunk number
        // - 0: this is the last chunk
        int RESOURCE  = 1; // stream id, total chunks, chunk number, chunk data (5 + chunk)
        int STATUS    = 2; // job id, percent complete, optional return value (5)
        int EXCEPTION = 3; // context id, type, message, context data (6 + message + data)

        // resource encoding/compression?
        // Compression using Google snappy

        // Functions:
        // get session id (void)
        // reconnect session id (id)
        // close session id (id)
        // abort function call (id)
        // get schema (search params, as summary?)
    }
    private final Map<Specification, Integer> specToType = new StrictMap<>(HashMap::new);
    private final Map<Integer, Specification> typeToSpec = new StrictMap<>(HashMap::new);

    public static final int MAX_MESSAGE_TYPE = IntegerCodec.OneTwo.MAX_VALUE;

    public void registerSpecification(@Nonnull Specification spec, int type) {
        if(type > MAX_MESSAGE_TYPE) {
            throw new IllegalArgumentException("Message type " + type + " is outside of allowed range");
        }
        specToType.put(spec, type);
        typeToSpec.put(type, spec);
    }

    public BinaryBuffer encode(@Nonnull Parameters parameters, @Nonnull BinaryBuffer buffer) throws BinaryCodec.NoRoomException {
        parameters.verifyCompleteness();
        int type = specToType.get(parameters.getSpecification());
        final int maxContentsOffset = IntegerCodec.OneTwo.MAX_LENGTH + IntegerCodec.OneThree.MAX_LENGTH;
        BinaryBuffer offsetView = buffer.newView(buffer.startOffset + maxContentsOffset);
        BinaryCodec.Encoder encoder = new BinaryCodec.Encoder(offsetView);
        LittleEndianCodec endianCodec = new LittleEndianCodec(offsetView);
        final IntegerCodec lengthCodec = new IntegerCodec.OneThree(endianCodec);
        final IntegerCodec typeCodec = new IntegerCodec.OneTwo(endianCodec);

        for(Object value: parameters) {
            encoder.writeObject(value);
        }

        BinaryBuffer encodedView = encoder.newView();
        int offset = encodedView.startOffset;
        int typeLength = typeCodec.getEncodedLength(type);
        offset -= typeLength;
        typeCodec.encode(offset, type);
        int length = encodedView.length + typeLength + 1;
        int lengthLength = lengthCodec.getEncodedLength(length);
        if(lengthLength > 1) {
            length += lengthLength - 1;
            lengthLength = lengthCodec.getEncodedLength(length);
        }
        offset -= lengthLength;
        lengthCodec.encode(offset, length);
        return buffer.newView(offset, encodedView.endOffset);
    }

    public @Nonnull Parameters decode(@Nonnull BinaryBuffer buffer) throws BinaryCodec.EndOfDataException {
        LittleEndianCodec endianCodec = new LittleEndianCodec(buffer);
        final IntegerCodec lengthCodec = new IntegerCodec.OneThree(endianCodec);
        final IntegerCodec typeCodec = new IntegerCodec.OneTwo(endianCodec);
        int offset = buffer.startOffset;
        int length = lengthCodec.decode(offset);
        offset += lengthCodec.getEncodedLength(length);
        int type = typeCodec.decode(offset);
        offset += typeCodec.getEncodedLength(type);
        Specification specification = typeToSpec.get(type);
        Parameters parameters = new Parameters(specification);
        BinaryCodec.Decoder decoder = new BinaryCodec.Decoder(parameters::add);
        decoder.feed(buffer.newView(offset));

        parameters.verifyCompleteness();
        return parameters;
    }
}
