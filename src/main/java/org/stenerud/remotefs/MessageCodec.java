package org.stenerud.remotefs;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MessageCodec {
    public interface Types {
        public static final byte QUIT      = (byte)0xe0;
        public static final byte RESOURCE  = (byte)0xc0;
        public static final byte CALL      = (byte)0xa0;
        public static final byte ABORT     = (byte)0x80;
        public static final byte STATUS    = (byte)0x60;
        public static final byte EXCEPTION = (byte)0x40;
        public static final byte RESERVED  = (byte)0x20;
    }
    private static final int TYPE_MASK = (int)0xe0000000;
    private static final int MESSAGE_CONTENTS_OFFSET = 4;

    public static final int MAX_MESSAGE_SIZE = ~TYPE_MASK;

    public static byte getType(int field) {
        return (byte)(((field & TYPE_MASK) >> 24) & 0xff);
    }

    public static int getSize(int field) {
        return field & MAX_MESSAGE_SIZE;
    }

    private static int combineTypeAndSize(byte messageType, int size) {
        int field = size & MAX_MESSAGE_SIZE;
        if(field != size) {
            throw new IllegalStateException("Message is too long (" + size + "). Max length is " + MAX_MESSAGE_SIZE);
        }
        return size | (((int)messageType) << 24);
    }

    public static class Encoder {
        private final byte messageType;
        private final BinaryBuffer buffer;
        private final BinaryCodec.Encoder encoder;

        public Encoder(byte messageType, BinaryBuffer buffer) {
            this.messageType = messageType;
            this.buffer = buffer;
            this.encoder = new BinaryCodec.Encoder(buffer.view(MESSAGE_CONTENTS_OFFSET));
        }

        public int writeObject(@Nonnull Object value) throws BinaryCodec.NoRoomException {
            return encoder.writeObject(value);
        }

        public int writeInteger(long value) throws BinaryCodec.NoRoomException {
            return encoder.writeInteger(value);
        }

        public int writeFloat(double value) throws BinaryCodec.NoRoomException {
            return encoder.writeFloat(value);
        }

        public int writeString(@Nonnull String value) throws BinaryCodec.NoRoomException {
            return encoder.writeString(value);
        }

        public int writeBytes(@Nonnull BinaryBuffer value) throws BinaryCodec.NoRoomException {
            return encoder.writeBytes(value);
        }

        public int writeBytes(@Nonnull byte[] value) throws BinaryCodec.NoRoomException {
            return encoder.writeBytes(value);
        }

        public int writeList(@Nonnull List value) throws BinaryCodec.NoRoomException {
            return encoder.writeList(value);
        }

        public int writeMap(@Nonnull Map<?, ?> value) throws BinaryCodec.NoRoomException {
            return encoder.writeMap(value);
        }

        public void concludeMessage() {
            int typeAndSize = combineTypeAndSize(messageType, encoder.view().length);
            LittleEndianCodec.encodeInt32(typeAndSize, buffer.data, 0);
        }
    }

    public static class Decoder {

        /**
         * Decode an encoded message.
         *
         * @param buffer A buffer containing the complete message.
         * @return The contents of the message.
         */
        public List<Object> decode(BinaryBuffer buffer) {
            List<Object> list = new LinkedList<>();
            BinaryCodec.Decoder decoder = new BinaryCodec.Decoder(new BinaryCodec.Decoder.Visitor() {
                @Override
                public void onInteger(long value) {
                    list.add(value);
                }

                @Override
                public void onFloat(double value) {
                    list.add(value);
                }

                @Override
                public void onString(@Nonnull String value) {
                    list.add(value);
                }

                @Override
                public void onBytes(@Nonnull BinaryBuffer value) {
                    list.add(value);
                }

                @Override
                public void onList(@Nonnull List value) {
                    list.add(value);
                }

                @Override
                public void onMap(@Nonnull Map<?, ?> value) {
                    list.add(value);
                }
            });
            decoder.feed(buffer.view(MESSAGE_CONTENTS_OFFSET));
            return list;
        }
    }
}
