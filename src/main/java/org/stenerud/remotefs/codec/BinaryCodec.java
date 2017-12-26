package org.stenerud.remotefs.codec;

import org.stenerud.remotefs.utility.BinaryBuffer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Converts to/from a compact binary format.
 */
public class BinaryCodec {
    private static final int SMALLINT_MIN = (byte)0x89;
    private static final int SMALLINT_MAX = (byte)0x74;

    public interface Types {
        byte EMPTY         = (byte)0x8a;
        byte FLOAT32       = (byte)0x89;
        byte FLOAT64       = (byte)0x88;
//        byte DECIMAL128    = (byte)0x87;
        byte INT16         = (byte)0x86;
        byte INT24         = (byte)0x85;
        byte INT32         = (byte)0x84;
        byte INT40         = (byte)0x83;
        byte INT48         = (byte)0x82;
        byte INT56         = (byte)0x81;
        byte INT64         = (byte)0x80;

        byte STRING        = (byte)0x75;
        byte BYTES         = (byte)0x76;
        byte LIST          = (byte)0x77;
        byte MAP           = (byte)0x78;
        byte END_CONTAINER = (byte)0x79;
//        byte DATE_DAYS     = (byte)0x7a;
//        byte DATE_SECONDS  = (byte)0x7b;
//        byte DATE_MSECONDS = (byte)0x7c;
//        byte DATE_USECONDS = (byte)0x7d;
        byte FALSE         = (byte)0x7e;
        byte TRUE          = (byte)0x7f;
    }

    static class EndOfDataException extends IOException {
    }

    static class NoRoomException extends IOException {
        public NoRoomException() {
        }

        public NoRoomException(Throwable throwable) {
            super(throwable);
        }
    }

    public static class Encoder {
        private final LittleEndianCodec endianCodec;
        protected final BinaryBuffer buffer;
        protected int currentOffset;

        Encoder(@Nonnull BinaryBuffer buffer) {
            this.buffer = buffer;
            this.currentOffset = buffer.startOffset;
            this.endianCodec = new LittleEndianCodec(buffer);
        }

        public @Nonnull BinaryBuffer newView() {
            return buffer.newView(buffer.startOffset, currentOffset);
        }

        private int write8(byte value) throws NoRoomException {
            try {
                int length = endianCodec.encodeInt8(currentOffset, value);
                currentOffset += length;
                return length;
            } catch(IndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int write16(int value) throws NoRoomException {
            try {
                int length = endianCodec.encodeInt16(currentOffset, value);
                currentOffset += length;
                return length;
            } catch(IndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int write24(int value) throws NoRoomException {
            try {
                int length = endianCodec.encodeInt24(currentOffset, value);
                currentOffset += length;
                return length;
            } catch(IndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int write32(int value) throws NoRoomException {
            try {
                int length = endianCodec.encodeInt32(currentOffset, value);
                currentOffset += length;
                return length;
            } catch(IndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int write40(long value) throws NoRoomException {
            try {
                int length = endianCodec.encodeInt40(currentOffset, value);
                currentOffset += length;
                return length;
            } catch(IndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int write48(long value) throws NoRoomException {
            try {
                int length = endianCodec.encodeInt48(currentOffset, value);
                currentOffset += length;
                return length;
            } catch(IndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int write56(long value) throws NoRoomException {
            try {
                int length = endianCodec.encodeInt56(currentOffset, value);
                currentOffset += length;
                return length;
            } catch(IndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int write64(long value) throws NoRoomException {
            try {
                int length = endianCodec.encodeInt64(currentOffset, value);
                currentOffset += length;
                return length;
            } catch(IndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int writeType(byte type) throws NoRoomException {
            return write8(type);
        }

        private int writeLength(int length) throws NoRoomException {
            return writeInteger(length);
        }

        private int writeData(@Nonnull byte[] value, int startOffset, int endOffset) throws NoRoomException {
            int oldOffset = currentOffset;
            int length = endOffset - startOffset;
            writeLength(length);
            if(!buffer.hasSpace(currentOffset, currentOffset + length)) {
                currentOffset = oldOffset;
                throw new NoRoomException();
            }
            byte[] bufferData = buffer.data;
            for (int srcI = startOffset; srcI < endOffset; srcI++) {
                bufferData[currentOffset++] = value[srcI];
            }
            return currentOffset - oldOffset;
        }

        private int writeData(@Nonnull byte[] value) throws NoRoomException {
            return writeData(value, 0, value.length);
        }

        private int writeData(@Nonnull BinaryBuffer value) throws NoRoomException {
            return writeData(value.data, value.startOffset, value.endOffset);
        }

        public int writeObject(@CheckForNull Object value) throws NoRoomException {
            if(value == null) {
                return writeEmpty();
            } else if(value instanceof Long) {
                return writeInteger((long)value);
            } else if(value instanceof Double) {
                return writeFloat((double)value);
            } else if(value instanceof String) {
                return writeString((String)value);
            } else if(value instanceof BinaryBuffer) {
                return writeBytes((BinaryBuffer) value);
            } else if(value instanceof List) {
                return writeList((List)value);
            } else if(value instanceof Map) {
                return writeMap((Map<Object, Object>)value);
            } else if(value instanceof byte[]) {
                return writeBytes((byte[])value);
            } else if(value instanceof Byte) {
                return writeInteger((long)(byte)value);
            } else if(value instanceof Short) {
                return writeInteger((long)(short)value);
            } else if(value instanceof Integer) {
                return writeInteger((long)(int)value);
            } else if(value instanceof Float) {
                return writeFloat32((float)value);
            } else if(value instanceof Boolean) {
                return writeBoolean((boolean)value);
            } else {
                throw new IllegalArgumentException("Don't know how to encode type " + value.getClass());
            }
        }

        private int writeBoolean(boolean value) throws NoRoomException {
            return writeType(value ? Types.TRUE : Types.FALSE);
        }

        private int writeIntegerSmall(long value) throws NoRoomException {
            return write8((byte)value);
        }

        private int writeInteger16(long value) throws NoRoomException {
            return writeType(Types.INT16) + write16((short)value);
        }

        private int writeInteger24(long value) throws NoRoomException {
            return writeType(Types.INT24) + write24((int)value);
        }

        private int writeInteger32(long value) throws NoRoomException {
            return writeType(Types.INT32) + write32((int)value);
        }

        private int writeInteger40(long value) throws NoRoomException {
            return writeType(Types.INT40) + write40(value);
        }

        private int writeInteger48(long value) throws NoRoomException {
            return writeType(Types.INT48) + write48(value);
        }

        private int writeInteger56(long value) throws NoRoomException {
            return writeType(Types.INT56) + write56(value);
        }

        private int writeInteger64(long value) throws NoRoomException {
            return writeType(Types.INT64) + write64(value);
        }

        private int writeInteger(long value) throws NoRoomException {
            if(value >= SMALLINT_MIN && value <= SMALLINT_MAX) {
                return writeIntegerSmall(value);
            }
            if(value >= -0x8000 && value <= 0x7fff) {
                return writeInteger16(value);
            }
            if(value >= -0x800000 && value <= 0x7fffff) {
                return writeInteger24(value);
            }
            if(value >= -0x80000000 && value <= 0x7fffffff) {
                return writeInteger32(value);
            }
            if(value >= -0x8000000000L && value <= 0x7fffffffffL) {
                return writeInteger40(value);
            }
            if(value >= -0x800000000000L && value <= 0x7fffffffffffL) {
                return writeInteger48(value);
            }
            if(value >= -0x80000000000000L && value <= 0x7fffffffffffffL) {
                return writeInteger56(value);
            }
            return writeInteger64(value);
        }

        private int writeFloat32(double value) throws NoRoomException {
            return writeType(Types.FLOAT32) + write32(Float.floatToIntBits((float)value));
        }

        private int writeFloat64(double value) throws NoRoomException {
            return writeType(Types.FLOAT64) + write64(Double.doubleToLongBits(value));
        }

        private int writeFloat(double value) throws NoRoomException {
            if((float)value == value) {
                return writeFloat32(value);
            } else {
                return writeFloat64(value);
            }
        }

        private int writeString(@Nonnull String value) throws NoRoomException {
            return writeType(Types.STRING) + writeData(stringToBytes(value));
        }

        private int writeBytes(@Nonnull BinaryBuffer value) throws NoRoomException {
            return writeType(Types.BYTES) + writeData(value);
        }

        private int writeBytes(@Nonnull byte[] value) throws NoRoomException {
            return writeType(Types.BYTES) + writeData(value);
        }

        private int writeEndContainer() throws NoRoomException {
            return writeType(Types.END_CONTAINER);
        }

        private int writeList(@Nonnull List value) throws NoRoomException {
            int length = writeType(Types.LIST);
            for(Object o: value) {
                length += writeObject(o);
            }
            return length + writeEndContainer();
        }

        private int writeMap(@Nonnull Map<Object, Object> value) throws NoRoomException {
            int length = writeType(Types.MAP);
            for(Map.Entry entry: value.entrySet()) {
                length += writeObject(entry.getKey());
                length += writeObject(entry.getValue());
            }
            return length + writeEndContainer();
        }

        private int writeEmpty() throws NoRoomException {
            return writeType(Types.EMPTY);
        }

        private static @Nonnull byte[] stringToBytes(@Nonnull String string) {
            try {
                return string.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("UTF-8 encoding not supported", e);
            }
        }

        public ByteStream newByteStream() throws NoRoomException {
            return new ByteStream();
        }

        public ByteStream newStringStream() throws NoRoomException {
            return new StringStream();
        }

        public ListStream newListStream() throws NoRoomException {
            return new ListStream();
        }

        public MapStream newMapStream() throws NoRoomException {
            return new MapStream();
        }

        public class ListStream {
            private final Encoder encoder;

            private ListStream() throws NoRoomException {
                writeType(Types.LIST);
                // Leave room for "end container" marker
                this.encoder = new Encoder(buffer.newView(currentOffset, buffer.endOffset-1));
            }

            public void write(@Nonnull Object value) throws NoRoomException {
                encoder.writeObject(value);
                currentOffset = encoder.currentOffset;
            }

            public void close() {
                try {
                    writeEndContainer();
                } catch(NoRoomException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        public class MapStream {
            private final Encoder encoder;

            private MapStream() throws NoRoomException {
                writeType(Types.MAP);
                // Leave room for "end container" marker
                this.encoder = new Encoder(buffer.newView(currentOffset, buffer.endOffset-1));
            }

            public void write(@Nonnull Object key, @Nonnull Object value) throws NoRoomException {
                encoder.writeObject(key);
                encoder.writeObject(value);
                currentOffset = encoder.currentOffset;
            }

            public void close() {
                try {
                    writeEndContainer();
                } catch(NoRoomException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        public class StringStream extends ByteStream {
            private final byte MULTIBYTE_MASK = (byte)0x80;
            private final byte MULTIBYTE_INITIATOR_MASK = (byte)0xc0;
            private final byte TWO_BYTE_MASK = (byte)0xe0;
            private final byte TWO_BYTE_MATCH = (byte)0xc0;
            private final byte THREE_BYTE_MASK = (byte)0xf0;
            private final byte THREE_BYTE_MATCH = (byte)0xe0;
            private final byte FOUR_BYTE_MASK = (byte)0xf8;
            private final byte FOUR_BYTE_MATCH = (byte)0xf0;

            private StringStream() throws NoRoomException {
                super(Types.STRING);
            }

            private int offsetToLastFullUTF8Character() {
                // Regular character
                if(viewOffset == view.startOffset || (view.data[viewOffset-1] & MULTIBYTE_MASK) == 0) {
                    return viewOffset;
                }

                for(int i = -1; i >= -4; i--) {
                    byte currentByte = view.data[i + viewOffset];
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
                            return viewOffset;
                        }
                        if(multibyteCount < expectedMultibyteCount) {
                            return viewOffset - multibyteCount;
                        }
                        throw new IllegalStateException("Malformed UTF-8 character");
                    }
                }
                throw new IllegalStateException("Malformed UTF-8 character");
            }

            public int write(@Nonnull BinaryBuffer fromBuffer) {
                int beforeOffset = viewOffset;
                int bytesWritten = super.write(fromBuffer);
                if(bytesWritten < fromBuffer.length) {
                    viewOffset = offsetToLastFullUTF8Character();
                }
                return viewOffset - beforeOffset;
            }
        }

        public class ByteStream {
            // type BYTES, type INT32, 32-bit length
            protected final int typeAndLengthOffset = 6;
            protected final BinaryBuffer view;
            protected int viewOffset;

            private ByteStream() throws NoRoomException {
                this(Types.BYTES);
            }

            private ByteStream(byte type) throws NoRoomException {
                writeType(type);
                try {
                    // TODO: Optimization: Choose int size based on buffer length
                    this.view = buffer.newView(buffer.startOffset + typeAndLengthOffset);
                    this.viewOffset = this.view.startOffset;
                } catch(IndexOutOfBoundsException e) {
                    throw new NoRoomException(e);
                }
            }

            public int write(@Nonnull BinaryBuffer fromBuffer) {
                int bytesAdded;
                try {
                    view.copyFrom(fromBuffer, fromBuffer.startOffset, viewOffset, fromBuffer.length);
                    bytesAdded = fromBuffer.length;
                } catch(IndexOutOfBoundsException e) {
                    bytesAdded = view.lengthRemainingFromOffset(viewOffset);
                    if(bytesAdded <= 0) {
                        return 0;
                    }
                    view.copyFrom(fromBuffer, fromBuffer.startOffset, viewOffset, bytesAdded);
                }
                viewOffset += bytesAdded;
                return bytesAdded;
            }

            public void close() {
                try {
                    writeInteger32(view.lengthToOffset(viewOffset));
                    currentOffset = viewOffset;
                } catch(NoRoomException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    public static class Decoder {
        private static final Object END_CONTAINER_MARKER = new Object();

        public interface Visitor {
            void onValue(Object value);
        }

        private final Visitor visitor;

        Decoder(@Nonnull Visitor visitor) {
            this.visitor = visitor;
        }

        public void feed(@Nonnull BinaryBuffer buffer) throws EndOfDataException {
            Reader reader = new Reader(buffer);
            while (reader.hasObject()) {
                Object value = reader.readObject();
                if(value == END_CONTAINER_MARKER) {
                    throw new IllegalStateException("Unexpected end of container");
                }
                visitor.onValue(value);
            }
        }

        private static class Reader {
            private final LittleEndianCodec endianCodec;
            private final BinaryBuffer buffer;
            private int currentOffset;

            Reader(@Nonnull BinaryBuffer buffer) {
                this.buffer = buffer;
                this.currentOffset = buffer.startOffset;
                this.endianCodec = new LittleEndianCodec(buffer);
            }

            private void checkCanReadBytes(int byteCount) throws EndOfDataException {
                if(currentOffset + byteCount > buffer.endOffset) {
                    throw new EndOfDataException();
                }
            }

            private byte readType() throws EndOfDataException {
                return readInt8();
            }

            private byte readInt8() throws EndOfDataException {
                int bytesToRead = 1;
                checkCanReadBytes(bytesToRead);
                long result = endianCodec.decodeInt8(currentOffset);
                currentOffset += bytesToRead;
                return (byte)result;
            }

            private long readInt16() throws EndOfDataException {
                int bytesToRead = 2;
                checkCanReadBytes(bytesToRead);
                long result = endianCodec.decodeInt16(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private long readInt24() throws EndOfDataException {
                int bytesToRead = 3;
                checkCanReadBytes(bytesToRead);
                long result = endianCodec.decodeInt24(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private long readInt32() throws EndOfDataException {
                int bytesToRead = 4;
                checkCanReadBytes(bytesToRead);
                long result = endianCodec.decodeInt32(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private long readInt40() throws EndOfDataException {
                int bytesToRead = 5;
                checkCanReadBytes(bytesToRead);
                long result = endianCodec.decodeInt40(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private long readInt48() throws EndOfDataException {
                int bytesToRead = 6;
                checkCanReadBytes(bytesToRead);
                long result = endianCodec.decodeInt48(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private long readInt56() throws EndOfDataException {
                int bytesToRead = 7;
                checkCanReadBytes(bytesToRead);
                long result = endianCodec.decodeInt56(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private long readInt64() throws EndOfDataException {
                int bytesToRead = 8;
                checkCanReadBytes(bytesToRead);
                long result = endianCodec.decodeInt64(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private long readInteger() throws EndOfDataException {
                byte type = readType();
                switch(type) {
                    case Types.INT16:
                        return readInt16();
                    case Types.INT24:
                        return readInt24();
                    case Types.INT32:
                        return readInt32();
                    case Types.INT40:
                        return readInt40();
                    case Types.INT48:
                        return readInt48();
                    case Types.INT56:
                        return readInt56();
                    case Types.INT64:
                        return readInt64();
                }
                if(type >= SMALLINT_MIN && type <= SMALLINT_MAX) {
                    return type;
                }
                throw new IllegalStateException("Expected an integer type but got type " + type);
            }

            private double readFloat32() throws EndOfDataException {
                return Float.intBitsToFloat((int)readInt32());
            }

            private double readFloat64() throws EndOfDataException {
                return Double.longBitsToDouble(readInt64());
            }

            private int readLength() throws EndOfDataException {
                int length = (int)readInteger();
                if(length < 0) {
                    throw new IllegalStateException("Invalid length: " + length);
                }
                return length;
            }

            private int readAndVerifyByteArrayLength() throws EndOfDataException {
                int length = readLength();
                try {
                    checkCanReadBytes(length);
                    return length;
                } catch(EndOfDataException e) {
                    int remainingLength = buffer.endOffset - currentOffset;
                    throw new IllegalStateException("Expected to be able to read byte array of length " + length + " but only " + remainingLength + " bytes available", e);
                }
            }

            private @Nonnull BinaryBuffer readBytes() throws EndOfDataException {
                int length = readAndVerifyByteArrayLength();
                int startOffst = currentOffset;
                currentOffset += length;
                return buffer.newView(startOffst, currentOffset);
            }

            private @Nonnull String readString() throws EndOfDataException {
                return readBytes().utf8String();
            }

            private boolean hasObject() {
                return currentOffset < buffer.endOffset;
            }

            private @Nullable Object readObject() throws EndOfDataException {
                byte type = readType();
                switch (type) {
                    case Types.INT16:
                        return readInt16();
                    case Types.INT24:
                        return readInt24();
                    case Types.INT32:
                        return readInt32();
                    case Types.INT40:
                        return readInt40();
                    case Types.INT48:
                        return readInt48();
                    case Types.INT56:
                        return readInt56();
                    case Types.INT64:
                        return readInt64();
                    case Types.FLOAT32:
                        return readFloat32();
                    case Types.FLOAT64:
                        return readFloat64();
                    case Types.BYTES:
                        return readBytes();
                    case Types.STRING:
                        return readString();
                    case Types.LIST:
                        return readList();
                    case Types.MAP:
                        return readMap();
                    case Types.EMPTY:
                        return null;
                    case Types.FALSE:
                        return false;
                    case Types.TRUE:
                        return true;
                    case Types.END_CONTAINER:
                        return END_CONTAINER_MARKER;
                    default:
                        return (long) (byte) type;
                }
            }

            private @Nonnull List<Object> readList() {
                List<Object> list = new LinkedList<>();
                Object value;
                try {
                    while ((value = readObject()) != END_CONTAINER_MARKER) {
                        if(value != null) {
                            list.add(value);
                        }
                    }
                } catch(EndOfDataException e) {
                    throw new IllegalStateException("Premature end of list object");
                }
                return list;
            }

            private @Nonnull Map<Object, Object> readMap() {
                Map<Object, Object> map = new HashMap<>();
                Object key;
                try {
                    while((key = readObject()) != END_CONTAINER_MARKER) {
                        if(key == null) {
                            throw new IllegalArgumentException("Key cannot be null");
                        }
                        Object value = readObject();
                        if(value != null) {
                            if (value == END_CONTAINER_MARKER) {
                                throw new IllegalStateException("Unexpected end of container");
                            }
                            map.put(key, value);
                        }
                    }
                    return map;
                } catch(EndOfDataException e) {
                    throw new IllegalStateException("Premature end of map object");
                }
            }
        }
    }
}
