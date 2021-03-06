package org.stenerud.remotefs.codec;

import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.message.MessageBuilder;
import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.StrictMap;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

// Length encoding is s3: vvvvvvvS (vvvvvvvv vvvvvvvv). When S = 1, two more bytes follow.
// Type encoding is s2: vvvvvvvS (vvvvvvvv). When S = 1, one more byte follows.
// Read as little endian, shift right 1. S bit determines what upper bits to mask.
// Message length is length including length field.
// Minimum message length is 2.
public class MessageCodec {
    private static final Logger LOG = Logger.getLogger(MessageCodec.class.getName());
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
        // closeAll session id (id)
        // abort function call (id)
        // get schema (search params, as summary?)
    }
    private final Map<String, Integer> identifierToType = StrictMap.withImplementation(HashMap::new).withErrorFormat("No message type registered for specification %s");
    private final Map<Integer, MessageBuilder> typeToBuilder = StrictMap.withImplementation(HashMap::new).withErrorFormat("No specification registered for message type %s");

    public static final int MAX_MESSAGE_TYPE = IntegerCodec.OneTwo.MAX_VALUE;

    public void registerBuilder(@Nonnull MessageBuilder builder, int type) {
        if(type > MAX_MESSAGE_TYPE) {
            throw new IllegalArgumentException("Message type " + type + " is outside of allowed range");
        }
        identifierToType.put(builder.getIdentifier(), type);
        typeToBuilder.put(type, builder);
    }

    public class Encoder {
        private final Message message;
        private final BinaryCodec.Encoder binaryEncoder;
        private final BinaryBuffer buffer;
        private final BinaryBuffer offsetView;
        public Encoder(@Nonnull Message message, @Nonnull BinaryBuffer buffer) throws BinaryCodec.NoRoomException {
            this.message = message;
            this.buffer = buffer;
            message.verifyCompleteness();
            final int maxContentsOffset = IntegerCodec.OneTwo.MAX_LENGTH + IntegerCodec.OneThree.MAX_LENGTH;
            this.offsetView = buffer.newView(buffer.startOffset + maxContentsOffset);
            binaryEncoder = new BinaryCodec.Encoder(offsetView);
        }

        public void writeMessageParameters(int parameterCount) throws BinaryCodec.NoRoomException {
            Iterator<Object> iterator = message.iterator();
            for(int i = 0; i < parameterCount; i++) {
                binaryEncoder.writeObject(iterator.next());

            }
        }

        public BinaryBuffer completeEncoding() {
            int type = identifierToType.get(message.getIdentifier());
            LittleEndianCodec endianCodec = new LittleEndianCodec(offsetView);
            final IntegerCodec lengthCodec = new IntegerCodec.OneThree(endianCodec);
            final IntegerCodec typeCodec = new IntegerCodec.OneTwo(endianCodec);

            BinaryBuffer encodedView = binaryEncoder.newView();
            int offset = encodedView.startOffset;
            int typeLength = typeCodec.getRequiredEncodingLength(type);
            offset -= typeLength;
            typeCodec.encode(offset, type);
            int length = encodedView.length + typeLength;
            int lengthLength = lengthCodec.getRequiredEncodingLength(length);
            offset -= lengthLength;
            lengthCodec.encode(offset, length);
            return buffer.newView(offset, encodedView.endOffset);
        }
    }

    public BinaryBuffer encode(@Nonnull Message message, @Nonnull BinaryBuffer buffer) throws BinaryCodec.NoRoomException {
        Encoder encoder = new Encoder(message, buffer);
        encoder.writeMessageParameters(message.getParameterCount());
        return encoder.completeEncoding();
    }

    public @Nonnull
    Message decode(@Nonnull BinaryBuffer buffer) throws BinaryCodec.EndOfDataException {
        LittleEndianCodec endianCodec = new LittleEndianCodec(buffer);
        final IntegerCodec lengthCodec = new IntegerCodec.OneThree(endianCodec);
        final IntegerCodec typeCodec = new IntegerCodec.OneTwo(endianCodec);
        int offset = buffer.startOffset + lengthCodec.getEncodedLength(buffer.startOffset);
        int type = typeCodec.decode(offset);
        offset += typeCodec.getRequiredEncodingLength(type);
        MessageBuilder builder = typeToBuilder.get(type);
        Message message = builder.newMessage();
        BinaryCodec.Decoder decoder = new BinaryCodec.Decoder(message::add);
        decoder.feed(buffer.newView(offset));

        message.verifyCompleteness();
        return message;
    }
}
