package org.stenerud.remotefs;

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
    private static final Object END_CONTAINER_MARKER = new Object();

    private static final int SMALLINT_MIN = -123;
    private static final int SMALLINT_MAX =  122;
    private static final int INT16_MIN    = -32768;
    private static final int INT16_MAX    =  32767;
    private static final int INT32_MIN    = -2147483648;
    private static final int INT32_MAX    =  2147483647;

    // TODO: May have to add partial array/list/map types...
    // no way to know if an array is terminated or not
    public interface Types {
        byte EMPTY         = -123;
        byte INT16         =  123;
        byte INT32         = -124;
        byte INT64         =  124;
        byte FLOAT32       = -125;
        byte FLOAT64       =  125;
        byte STRING        = -126;
        byte BYTES         =  126;
        byte LIST          = -127;
        byte MAP           =  127;
        byte END_CONTAINER = -128;
    }

    static class EndOfDataException extends IOException {
    }

    static class NoRoomException extends IOException {
    }

    public static class Encoder {
        private final BinaryBuffer buffer;
        private int currentOffset;
        private byte currentPortionType = Types.END_CONTAINER;

        Encoder(@Nonnull BinaryBuffer buffer) {
            this.buffer = buffer;
            this.currentOffset = buffer.startOffset;
        }

        public @Nonnull BinaryBuffer view() {
            return buffer.view(buffer.startOffset, currentOffset);
        }

        private int write8(byte value) throws NoRoomException {
            try {
                buffer.data[currentOffset++] = value;
                return 1;
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int write16(int value) throws NoRoomException {
            try {
                int length = LittleEndianCodec.encodeInt16(value, buffer.data, currentOffset);
                currentOffset += length;
                return length;
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int write32(int value) throws NoRoomException {
            try {
                int length = LittleEndianCodec.encodeInt32(value, buffer.data, currentOffset);
                currentOffset += length;
                return length;
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int write64(long value) throws NoRoomException {
            try {
                int length = LittleEndianCodec.encodeInt64(value, buffer.data, currentOffset);
                currentOffset += length;
                return length;
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int writeType(byte type) throws NoRoomException {
            return write8(type);
        }

        private int writeLength(int length) throws NoRoomException {
            return writeInteger(length);
        }

        private int writeBufferContents(@Nonnull byte[] value, int startOffset, int endOffset) throws NoRoomException {
            try {
                int oldOffset = currentOffset;
                int length = endOffset - startOffset;
                writeLength(length);
                byte[] bufferData = buffer.data;
                for (int srcI = startOffset; srcI < endOffset; srcI++) {
                    bufferData[currentOffset++] = value[srcI];
                }
                return currentOffset - oldOffset;
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int writeBufferContents(@Nonnull byte[] value) throws NoRoomException {
            return writeBufferContents(value, 0, value.length);
        }

        private int writeBufferContents(@Nonnull BinaryBuffer value) throws NoRoomException {
            return writeBufferContents(value.data, value.startOffset, value.endOffset);
        }

//        private int startNewPortion(final byte type) throws NoRoomException {
//            if(currentPortionType != type) {
//                if(currentPortionType != Types.END_CONTAINER) {
//                    throw new IllegalArgumentException("Cannot start new object portion until previous portion has been completed");
//                }
//                return write8(type);
//            }
//            return 0;
//        }

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
                return writeFloat((double)(float)value);
            } else {
                throw new IllegalArgumentException("Don't know how to encode type " + value.getClass());
            }
        }

        private int writeInteger(long value) throws NoRoomException {
            if(value >= SMALLINT_MIN && value <= SMALLINT_MAX) {
                return write8((byte)value);
            } else if(value >= INT16_MIN && value <= INT16_MAX) {
                return writeType(Types.INT16) + write16((short)value);
            } else if(value >= INT32_MIN && value <= INT32_MAX) {
                return writeType(Types.INT32) + write32((int)value);
            } else {
                return writeType(Types.INT64) + write64(value);
            }
        }

        private int writeFloat(double value) throws NoRoomException {
            if((float)value == value) {
                return writeType(Types.FLOAT32) + write32(Float.floatToIntBits((float)value));
            } else {
                return writeType(Types.FLOAT64) + write64(Double.doubleToLongBits(value));
            }
        }

        private int writeString(@Nonnull String value) throws NoRoomException {
            return writeType(Types.STRING) + writeBufferContents(stringToBytes(value));
        }

        private int writeBytes(@Nonnull BinaryBuffer value) throws NoRoomException {
            return writeType(Types.BYTES) + writeBufferContents(value);
        }

        private int writeBytes(@Nonnull byte[] value) throws NoRoomException {
            return writeType(Types.BYTES) + writeBufferContents(value);
        }

        private int writeList(@Nonnull List value) throws NoRoomException {
            int length = writeType(Types.LIST);
            for(Object o: value) {
                length += writeObject(o);
            }
            return length + writeType(Types.END_CONTAINER);
        }

        private int writeMap(@Nonnull Map<Object, Object> value) throws NoRoomException {
            int length = writeType(Types.MAP);
            for(Map.Entry entry: value.entrySet()) {
                length += writeObject(entry.getKey());
                length += writeObject(entry.getValue());
            }
            return length + writeType(Types.END_CONTAINER);
        }

        private int writeEmpty() throws NoRoomException {
            return writeType(Types.EMPTY);
        }

//        public int writeStringPortion(@Nonnull String value) throws NoRoomException {
//            return startNewPortion(Types.STRING) + writeBytes(stringToBytes(value));
//        }
//
//        public int writeBytesPortion(@Nonnull BinaryBuffer value) throws NoRoomException {
//            return startNewPortion(Types.BYTES) + writeBytes(value);
//        }
//
//        public int writeBytesPortion(@Nonnull byte[] value) throws NoRoomException {
//            return startNewPortion(Types.BYTES) + writeBytes(value);
//        }
//
//        public int writeListPortion(@Nonnull Object value) throws NoRoomException {
//            return startNewPortion(Types.LIST) + writeObject(value);
//        }
//
//        public int writeMapPortion(@Nonnull Object key, @Nonnull Object value) throws NoRoomException {
//            return startNewPortion(Types.MAP) + writeObject(key) + writeObject(value);
//        }
//
//        public int concludeObject() throws NoRoomException {
//            currentPortionType = Types.END_CONTAINER;
//            return write8(Types.END_CONTAINER);
//        }

        private static @Nonnull byte[] stringToBytes(@Nonnull String string) {
            try {
                return string.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("UTF-8 encoding not supported", e);
            }
        }

    }

    public static class Decoder {
        public interface Visitor {
            void onValue(Object value);
        }

        private final Visitor visitor;

        Decoder(@Nonnull Visitor visitor) {
            this.visitor = visitor;
        }

        public void feed(@Nonnull BinaryBuffer buffer) {
            Reader reader = new Reader(buffer);
            try {
                for (; ; ) {
                    byte type = reader.readType();
                    Object value;
                    switch (type) {
                        case Types.INT16:
                            value = (long) (short) reader.readInteger(2);
                            break;
                        case Types.INT32:
                            value = (long) (int) reader.readInteger(4);
                            break;
                        case Types.INT64:
                            value = reader.readInteger(8);
                            break;
                        case Types.FLOAT32:
                            value = reader.readFloat32();
                            break;
                        case Types.FLOAT64:
                            value = reader.readFloat64();
                            break;
                        case Types.BYTES:
                            value = reader.readBytes();
                            break;
                        case Types.STRING:
                            value = reader.readString();
                            break;
                        case Types.LIST:
                            value = reader.readList();
                            break;
                        case Types.MAP:
                            value = reader.readMap();
                            break;
                        case Types.EMPTY:
                            value = null;
                            break;
                        case Types.END_CONTAINER:
                            throw new IllegalStateException("Unexpected end of container");
                        default:
                            value = (long) (byte) type;
                    }
                    visitor.onValue(value);
                }
            } catch(EndOfDataException e) {
                // No more data, so return
            }
        }

        private static class Reader {
            private final BinaryBuffer buffer;
            private int currentOffset;

            Reader(@Nonnull BinaryBuffer buffer) {
                this.buffer = buffer;
                this.currentOffset = buffer.startOffset;
            }

            private void checkCanReadBytes(int byteCount) throws EndOfDataException {
                if(currentOffset + byteCount > buffer.endOffset) {
                    throw new EndOfDataException();
                }
            }

            byte readType() throws EndOfDataException {
                checkCanReadBytes(1);
                return buffer.data[currentOffset++];
            }

            long readInteger(int byteCount) throws EndOfDataException {
                checkCanReadBytes(byteCount);
                int lowOffset = currentOffset;
                int currentOffset = this.currentOffset + byteCount;
                byte[] src = buffer.data;
                long accumulator = src[currentOffset--]; // preserve sign
                for(; currentOffset >= lowOffset; currentOffset--) {
                    accumulator <<= 8;
                    accumulator += 0xff & src[currentOffset];
                }
                this.currentOffset += byteCount;
                return accumulator;
            }

            long readInteger() throws EndOfDataException {
                byte type = readType();
                switch(type) {
                    case Types.INT16:
                        return readInteger(2);
                    case Types.INT32:
                        return readInteger(4);
                    case Types.INT64:
                        return readInteger(8);
                    case Types.FLOAT32:
                    case Types.FLOAT64:
                    case Types.EMPTY:
                    case Types.BYTES:
                    case Types.STRING:
                    case Types.LIST:
                    case Types.MAP:
                    case Types.END_CONTAINER:
                        throw new IllegalStateException("Expected an integer type but got type " + type);
                    default:
                        return type;
                }
            }

            double readFloat32() throws EndOfDataException {
                int intBits = (int)readInteger(4);
                return Float.intBitsToFloat(intBits);
            }

            double readFloat64() throws EndOfDataException {
                long longBits = readInteger(8);
                return Double.longBitsToDouble(longBits);
            }

            int readLength() throws EndOfDataException {
                int length = (int)readInteger();
                if(length < 0) {
                    throw new IllegalStateException("Invalid length: " + length);
                }
                return length;
            }

            int readAndVerifyByteArrayLength() throws EndOfDataException {
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
                return buffer.view(startOffst, currentOffset);
            }

            @Nonnull String readString() throws EndOfDataException {
                return readBytes().utf8String();
            }

            private @Nullable Object readObject() throws EndOfDataException {
                byte type = readType();
                switch (type) {
                    case Types.INT16:
                        return (long) (short) readInteger(2);
                    case Types.INT32:
                        return (long) (int) readInteger(4);
                    case Types.INT64:
                        return readInteger(8);
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
                    case Types.END_CONTAINER:
                        return END_CONTAINER_MARKER;
                    default:
                        return (long) (byte) type;
                }
            }

            @Nonnull List<Object> readList() {
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

            @Nonnull Map<Object, Object> readMap() {
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
