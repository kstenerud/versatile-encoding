package org.stenerud.remotefs.codec;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.stenerud.remotefs.utility.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Converts to/from a compact binary format.
 */
public class BinaryCodec {
    private static final Logger LOG = Logger.getLogger(BinaryCodec.class.getName());
    private static final int SMALLINT_MIN = (byte)0x89;
    private static final int SMALLINT_MAX = (byte)0x74;

    public interface EncodedType {
        int STRING        = (byte)0x75;
        int BYTES         = (byte)0x76;
        int LIST          = (byte)0x77;
        int MAP           = (byte)0x78;
        int END_CONTAINER = (byte)0x79;
        int DATE_DAYS     = (byte)0x7a;
        int DATE_SECONDS  = (byte)0x7b;
        int DATE_MSECONDS = (byte)0x7c;
        int DATE_USECONDS = (byte)0x7d;
        int FALSE         = (byte)0x7e;
        int TRUE          = (byte)0x7f;

        int INT64         = (byte)0x80;
        int INT56         = (byte)0x81;
        int INT48         = (byte)0x82;
        int INT40         = (byte)0x83;
        int INT32         = (byte)0x84;
        int INT24         = (byte)0x85;
        int INT16         = (byte)0x86;
        int DECIMAL128    = (byte)0x87;
        int FLOAT64       = (byte)0x88;
        int FLOAT32       = (byte)0x89;
        int EMPTY         = (byte)0x8a;
    }

    public static class EndOfDataException extends IOException {
    }

    static class NoRoomException extends IOException {
        public NoRoomException() {
        }

        public NoRoomException(Throwable throwable) {
            super(throwable);
        }
    }

    public static class Encoder {
        interface Writer<T> {
            int write(@Nonnull T value) throws NoRoomException;
        }

        private final Map<Class, Writer> writers = StrictMap.withImplementation(ConcurrentHashMap::new);
        private final LittleEndianCodec endianCodec;
        final BinaryBuffer buffer;
        int currentOffset;

        Encoder(@Nonnull BinaryBuffer buffer) {
            this.buffer = buffer;
            this.currentOffset = buffer.startOffset;
            this.endianCodec = new LittleEndianCodec(buffer);
            buildWriters();
        }

        public @Nonnull BinaryBuffer newView() {
            return buffer.newView(buffer.startOffset, currentOffset);
        }

        private void buildWriters() {
            writers.put(Boolean.class, (Writer<Boolean>) this::writeBoolean);
            writers.put(Byte.class, (Writer<Byte>) this::writeInteger);
            writers.put(Short.class, (Writer<Short>) this::writeInteger);
            writers.put(Integer.class, (Writer<Integer>) this::writeInteger);
            writers.put(Long.class, (Writer<Long>) this::writeInteger);
            writers.put(Float.class, (Writer<Float>) this::writeFloat32);
            writers.put(Double.class, (Writer<Double>) this::writeFloat);
            writers.put(Int128Holder.class, (Writer<Int128Holder>) this::writeDecimal128);
            writers.put(byte[].class, (Writer<byte[]>) this::writeBytes);
            writers.put(BinaryBuffer.class, (Writer<BinaryBuffer>) this::writeBytes);
            writers.put(String.class, (Writer<String>) this::writeString);
            writers.put(Date.class, (Writer<Date>) this::writeDate);
            writers.put(Instant.class, (Writer<Instant>) this::writeDate);
            writers.put(List.class, (Writer<List>) this::writeList);
            writers.put(Map.class, (Writer<Map>) this::writeMap);
        }

        public int writeObject(@CheckForNull Object value) throws NoRoomException {
            if(value == null) {
                return writeEmpty();
            }
            Writer writer = writers.computeIfAbsent(value.getClass(), aClass -> {
                for(Map.Entry<Class, Writer> entry: writers.entrySet()) {
                    Class superclass = entry.getKey();
                    if(superclass.isAssignableFrom(aClass)) {
                        return entry.getValue();
                    }
                }
                throw new IllegalArgumentException("Don't know how to encode type " + aClass);
            });
            return writer.write(value);
        }

        private int write8(int value) throws NoRoomException {
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

        private int write128(@Nonnull Int128Holder value) throws NoRoomException {
            try {
                int length = endianCodec.encodeInt128(currentOffset, value);
                currentOffset += length;
                return length;
            } catch(IndexOutOfBoundsException e) {
                throw new NoRoomException();
            }
        }

        private int writeData(@Nonnull byte[] value, int startOffset, int endOffset) throws NoRoomException {
            int oldOffset = currentOffset;
            int length = endOffset - startOffset;
            writeLength(length);
            if(!buffer.hasSpace(currentOffset, currentOffset + length)) {
                currentOffset = oldOffset;
                throw new NoRoomException();
            }
            buffer.copyFrom(value,startOffset, currentOffset, length);
            currentOffset += length;
            return currentOffset - oldOffset;
        }

        private int writeData(@Nonnull byte[] value) throws NoRoomException {
            return writeData(value, 0, value.length);
        }

        private int writeData(@Nonnull BinaryBuffer value) throws NoRoomException {
            return writeData(value.data, value.startOffset, value.endOffset);
        }

        private int writeType(int type) throws NoRoomException {
            return write8(type);
        }

        private int writeLength(int length) throws NoRoomException {
            return writeInteger(length);
        }

        private int writeBoolean(boolean value) throws NoRoomException {
            return writeType(value ? EncodedType.TRUE : EncodedType.FALSE);
        }

        private int writeIntegerSmall(int value) throws NoRoomException {
            return write8(value);
        }

        private int writeInteger16(int value) throws NoRoomException {
            return writeType(EncodedType.INT16) + write16(value);
        }

        private int writeInteger24(int value) throws NoRoomException {
            return writeType(EncodedType.INT24) + write24(value);
        }

        private int writeInteger32(int value) throws NoRoomException {
            return writeType(EncodedType.INT32) + write32(value);
        }

        private int writeInteger40(long value) throws NoRoomException {
            return writeType(EncodedType.INT40) + write40(value);
        }

        private int writeInteger48(long value) throws NoRoomException {
            return writeType(EncodedType.INT48) + write48(value);
        }

        private int writeInteger56(long value) throws NoRoomException {
            return writeType(EncodedType.INT56) + write56(value);
        }

        private int writeInteger64(long value) throws NoRoomException {
            return writeType(EncodedType.INT64) + write64(value);
        }

        private int writeDecimal128(Int128Holder value) throws NoRoomException {
            return writeType(EncodedType.DECIMAL128) + write128(value);
        }

        private int writeInteger(long value) throws NoRoomException {
            if(value >= SMALLINT_MIN && value <= SMALLINT_MAX) {
                return writeIntegerSmall((int)value);
            }
            if(value >= -0x8000 && value <= 0x7fff) {
                return writeInteger16((int)value);
            }
            if(value >= -0x800000 && value <= 0x7fffff) {
                return writeInteger24((int)value);
            }
            if(value >= -0x80000000 && value <= 0x7fffffff) {
                return writeInteger32((int)value);
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

        private int writeFloat32(float value) throws NoRoomException {
            return writeType(EncodedType.FLOAT32) + write32(Float.floatToIntBits(value));
        }

        private int writeFloat64(double value) throws NoRoomException {
            return writeType(EncodedType.FLOAT64) + write64(Double.doubleToLongBits(value));
        }

        private int writeFloat(double value) throws NoRoomException {
            if((float)value == value) {
                return writeFloat32((float)value);
            } else {
                return writeFloat64(value);
            }
        }

        private int writeDateDay(int year, int month, int day) throws NoRoomException {
            if(day < 1 || day > 31) {
                throw new IllegalArgumentException("Day is invalid: " + day);
            }
            if(month < 1 || month > 12) {
                throw new IllegalArgumentException("Month is invalid: " + month);
            }
            int field = day | (month << 5) | (year << 9);
            return writeType(EncodedType.DATE_DAYS) + write32(field);
        }

        private int writeDateSeconds(long seconds) throws NoRoomException {
            return writeType(EncodedType.DATE_SECONDS) + write48(seconds);
        }

        private int writeDateMilliseconds(long seconds) throws NoRoomException {
            return writeType(EncodedType.DATE_MSECONDS) + write64(seconds);
        }

        private int writeDateMicroseconds(long seconds) throws NoRoomException {
            return writeType(EncodedType.DATE_USECONDS) + write64(seconds);
        }

        private int writeDate(@Nonnull Date date) throws NoRoomException {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.clear();
            calendar.setTime(date);
            long milliseconds = calendar.getTimeInMillis();
            if(calendar.get(Calendar.MILLISECOND) == 0) {
                if(calendar.get(Calendar.SECOND) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.HOUR) == 0) {
                    return writeDateDay(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH));
                }
                return writeDateSeconds(milliseconds / 1000);
            }
            return writeDateMilliseconds(milliseconds);
        }

        private int writeDate(@Nonnull Instant instant) throws NoRoomException {
            ZonedDateTime date = instant.atZone(ZoneOffset.UTC);
            if(date.getLong(ChronoField.SECOND_OF_DAY) == 0) {
                return writeDateDay((int)date.getLong(ChronoField.YEAR),
                        (int)date.getLong(ChronoField.MONTH_OF_YEAR),
                        (int)date.getLong(ChronoField.DAY_OF_MONTH));
            }
            long seconds = instant.getEpochSecond();
            long microseconds = instant.getNano() / 1000;
            if(microseconds == 0) {
                return writeDateSeconds(seconds);
            }
            if(microseconds % 1000 == 0) {
                return writeDateMilliseconds(seconds * 1000 + microseconds / 1000);
            }
            return writeDateMicroseconds(seconds * 1000000 + microseconds);
        }

        private int writeString(@Nonnull String value) throws NoRoomException {
            return writeType(EncodedType.STRING) + writeData(stringToBytes(value));
        }

        private int writeBytes(@Nonnull BinaryBuffer value) throws NoRoomException {
            return writeType(EncodedType.BYTES) + writeData(value);
        }

        private int writeBytes(@Nonnull byte[] value) throws NoRoomException {
            return writeType(EncodedType.BYTES) + writeData(value);
        }

        private int writeEndContainer() throws NoRoomException {
            return writeType(EncodedType.END_CONTAINER);
        }

        private int writeList(@Nonnull List value) throws NoRoomException {
            int length = writeType(EncodedType.LIST);
            for(Object o: value) {
                length += writeObject(o);
            }
            return length + writeEndContainer();
        }

        private int writeMap(@Nonnull Map<Object, Object> value) throws NoRoomException {
            int length = writeType(EncodedType.MAP);
            for(Map.Entry entry: value.entrySet()) {
                length += writeObject(entry.getKey());
                length += writeObject(entry.getValue());
            }
            return length + writeEndContainer();
        }

        private int writeEmpty() throws NoRoomException {
            return writeType(EncodedType.EMPTY);
        }

        private static @Nonnull byte[] stringToBytes(@Nonnull String string) {
            try {
                return string.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("UTF-8 encoding not supported", e);
            }
        }

        interface Stream<T> extends AutoCloseable {
            int write(T value) throws NoRoomException;
            @Override
            void close() throws Exception;
        }

        public Stream<BinaryBuffer> newByteStream() throws NoRoomException {
            return new ByteStream();
        }

        public Stream<BinaryBuffer> newStringStream() throws NoRoomException {
            return new StringStream();
        }

        public Stream<Object> newListStream() throws NoRoomException {
            return new ListStream();
        }

        public Stream<KeyValue> newMapStream() throws NoRoomException {
            return new MapStream();
        }

        public class ListStream implements Stream<Object> {
            private final Encoder encoder;

            private ListStream() throws NoRoomException {
                writeType(EncodedType.LIST);
                // Leave room for "end container" marker
                this.encoder = new Encoder(buffer.newView(currentOffset, buffer.endOffset-1));
            }

            public int write(@Nonnull Object value) throws NoRoomException {
                encoder.writeObject(value);
                currentOffset = encoder.currentOffset;
                return 1;
            }

            public void close() {
                try {
                    writeEndContainer();
                } catch(NoRoomException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        public class MapStream implements Stream<KeyValue> {
            private final Encoder encoder;

            private MapStream() throws NoRoomException {
                writeType(EncodedType.MAP);
                // Leave room for "end container" marker
                this.encoder = new Encoder(buffer.newView(currentOffset, buffer.endOffset-1));
            }

            public int write(@Nonnull KeyValue keyValue) throws NoRoomException {
                encoder.writeObject(keyValue.key);
                encoder.writeObject(keyValue.value);
                currentOffset = encoder.currentOffset;
                return 1;
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
            private StringStream() throws NoRoomException {
                super(EncodedType.STRING);
            }

            public int write(@Nonnull BinaryBuffer fromBuffer) throws NoRoomException {
                int beforeOffset = viewOffset;
                int bytesWritten = super.write(fromBuffer);
                if(bytesWritten < fromBuffer.length) {
                    viewOffset = Utf8Tool.offsetToLastFullUTF8Character(view.data, viewOffset, view.startOffset);
                }
                return viewOffset - beforeOffset;
            }
        }

        public class ByteStream implements Stream<BinaryBuffer> {
            // type BYTES, type INT32, 32-bit length
            final int typeAndLengthOffset = 6;
            final BinaryBuffer view;
            int viewOffset;

            private ByteStream() throws NoRoomException {
                this(EncodedType.BYTES);
            }

            private ByteStream(int type) throws NoRoomException {
                writeType(type);
                try {
                    // TODO: Optimization: Choose int size based on buffer length
                    this.view = buffer.newView(buffer.startOffset + typeAndLengthOffset);
                    this.viewOffset = this.view.startOffset;
                } catch(IndexOutOfBoundsException e) {
                    throw new NoRoomException(e);
                }
            }

            public int write(@Nonnull BinaryBuffer fromBuffer) throws NoRoomException {
                int bytesAdded;
                try {
                    view.copyFrom(fromBuffer, fromBuffer.startOffset, viewOffset, fromBuffer.length);
                    bytesAdded = fromBuffer.length;
                } catch(IndexOutOfBoundsException e) {
                    bytesAdded = view.lengthRemainingFromOffset(viewOffset);
                    if(bytesAdded <= 0) {
                        throw new NoRoomException();
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

        public Decoder(@Nonnull Visitor visitor) {
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

            private int readType() throws EndOfDataException {
                return readInt8();
            }

            private int readInt8() throws EndOfDataException {
                int bytesToRead = 1;
                checkCanReadBytes(bytesToRead);
                int result = endianCodec.decodeInt8(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private int readInt16() throws EndOfDataException {
                int bytesToRead = 2;
                checkCanReadBytes(bytesToRead);
                int result = endianCodec.decodeInt16(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private int readInt24() throws EndOfDataException {
                int bytesToRead = 3;
                checkCanReadBytes(bytesToRead);
                int result = endianCodec.decodeInt24(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private int readInt32() throws EndOfDataException {
                int bytesToRead = 4;
                checkCanReadBytes(bytesToRead);
                int result = endianCodec.decodeInt32(currentOffset);
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

            private @Nonnull
            Int128Holder readInt128() throws EndOfDataException {
                int bytesToRead = 16;
                checkCanReadBytes(bytesToRead);
                Int128Holder result = endianCodec.decodeInt128(currentOffset);
                currentOffset += bytesToRead;
                return result;
            }

            private @Nonnull
            Decimal128Holder readDecimal128() throws EndOfDataException {
                return new Decimal128Holder(readInt128());
            }

            private float readFloat32() throws EndOfDataException {
                return Float.intBitsToFloat(readInt32());
            }

            private double readFloat64() throws EndOfDataException {
                return Double.longBitsToDouble(readInt64());
            }

            private int readLength() throws EndOfDataException {
                int value = readType();
                switch(value) {
                    case EncodedType.INT16:
                        value = readInt16();
                        break;
                    case EncodedType.INT24:
                        value = readInt24();
                        break;
                    case EncodedType.INT32:
                        value = readInt32();
                        break;
                    default:
                        if(value < SMALLINT_MIN || value > SMALLINT_MAX) {
                            throw new IllegalStateException("Expected an integer type but got type " + value);
                        }
                }

                if(value < 0) {
                    throw new IllegalStateException("Invalid length: " + value);
                }
                return value;
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

            private @Nonnull Date readDateDays() throws EndOfDataException {
                int field = readInt32();
                int day = field & 0x1f;
                int month = (field>>5) & 0x0f;
                int year = field >> 9;
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.clear();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                return calendar.getTime();
            }

            private @Nonnull Date readDateSeconds() throws EndOfDataException {
                return new Date(readInt48() * 1000);
            }

            private @Nonnull Date readDateMilliseconds() throws EndOfDataException {
                return new Date(readInt64());
            }

            private @Nonnull Instant readDateMicroseconds() throws EndOfDataException {
                long microseconds = readInt64();
                return Instant.ofEpochSecond(microseconds / 1000000, (microseconds % 1000000) * 1000);
            }

            private boolean hasObject() {
                return currentOffset < buffer.endOffset;
            }

            private @Nullable Object readObject() throws EndOfDataException {
                int type = readType();
                switch (type) {
                    case EncodedType.INT16:
                        return (long)readInt16();
                    case EncodedType.INT24:
                        return (long)readInt24();
                    case EncodedType.INT32:
                        return (long)readInt32();
                    case EncodedType.INT40:
                        return readInt40();
                    case EncodedType.INT48:
                        return readInt48();
                    case EncodedType.INT56:
                        return readInt56();
                    case EncodedType.INT64:
                        return readInt64();
                    case EncodedType.FLOAT32:
                        return (double)readFloat32();
                    case EncodedType.FLOAT64:
                        return readFloat64();
                    case EncodedType.DECIMAL128:
                        return readDecimal128();
                    case EncodedType.DATE_DAYS:
                        return readDateDays();
                    case EncodedType.DATE_SECONDS:
                        return readDateSeconds();
                    case EncodedType.DATE_MSECONDS:
                        return readDateMilliseconds();
                    case EncodedType.DATE_USECONDS:
                        return readDateMicroseconds();
                    case EncodedType.BYTES:
                        return readBytes();
                    case EncodedType.STRING:
                        return readString();
                    case EncodedType.LIST:
                        return readList();
                    case EncodedType.MAP:
                        return readMap();
                    case EncodedType.EMPTY:
                        return null;
                    case EncodedType.FALSE:
                        return false;
                    case EncodedType.TRUE:
                        return true;
                    case EncodedType.END_CONTAINER:
                        return END_CONTAINER_MARKER;
                    default:
                        return (long)type;
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
