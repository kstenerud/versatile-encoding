package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.exception.DisconnectedException;
import org.stenerud.remotefs.codec.IntegerCodec;
import org.stenerud.remotefs.codec.LittleEndianCodec;
import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.message.InternalExceptionMessageSpecification;
import org.stenerud.remotefs.message.Message;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.Closer;
import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.LoopingThread;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class StreamTransport implements AutoCloseable, Transport {
    private static final Logger LOG = Logger.getLogger(StreamTransport.class.getName());

    private final InputStream inStream;
    private boolean autoflush;
    private boolean isOpen;

    private final OutputStream outStream;
    private final MessageCodec messageCodec;
    private MessageProducer.Listener listener = message -> {};
    private LoopingThread thread = new LoopingThread() {
        @Override
        protected void performLoop() throws Exception {
            Message message = getNextMessage();
            listener.onNewMessage(message);
        }

        @Override
        protected void onUnexpectedException(Exception e) {
//            LOG.info(this + ": unexpected " + e);
//            e.printStackTrace();
            listener.onNewMessage(new Message(new InternalExceptionMessageSpecification())
                    .addUnchecked(Specification.Type.ANY, e));
            Closer.closeAllAndLogErrors(this);
        }
    };

    public StreamTransport(InputStream inStream, OutputStream outStream, MessageCodec messageCodec) {
        this.inStream = inStream;
        this.outStream = outStream;
        this.messageCodec = messageCodec;
        isOpen = true;
        thread.start();
    }

    @Override
    public void setListener(@Nonnull MessageProducer.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void sendMessage(@Nonnull Message message) throws IOException {
        try {
            // TODO: Get this differently
            BinaryBuffer buffer = new BinaryBuffer(10000);
            BinaryBuffer encodedView = messageCodec.encode(message, buffer);
            writeBuffer(encodedView);
            if(autoflush) {
                flush();
            }
        } catch(IOException e) {
            throw new DisconnectedException(e);
        }
    }


    private @Nonnull
    Message getNextMessage() throws IOException {
        if (!isOpen) {
            throw new DisconnectedException("Transport is closed");
        }
        try {
            // TODO: Get this somewhere else
            BinaryBuffer buffer = new BinaryBuffer(10000);
            IntegerCodec.OneThree lengthCodec = new IntegerCodec.OneThree(new LittleEndianCodec(buffer));
            int currentOffset = buffer.startOffset;
            currentOffset += readBytes(buffer, currentOffset, 1);
            int firstByte = buffer.data[buffer.startOffset];
            currentOffset += readBytes(buffer, currentOffset, lengthCodec.getRequiredAdditionalBytesCount(firstByte));
            int length = lengthCodec.decode(buffer.startOffset);
            currentOffset += readBytes(buffer, currentOffset, length);
            return messageCodec.decode(buffer.newView(buffer.startOffset, currentOffset));
        } catch (DisconnectedException e) {
            throw e;
        } catch(IOException e) {
            throw new DisconnectedException(e);
        }
    }

    @Override
    public void flush() throws IOException {
        outStream.flush();
    }

    @Override
    public void close() throws Exception {
        isOpen = false;
        Closer.closeAll(thread, inStream, outStream);
    }

    private int readBytes(BinaryBuffer buffer, int startOffset, int length) throws IOException {
        if(length == 0) {
            return 0;
        }
        if(startOffset + length > buffer.endOffset) {
            throw new IllegalArgumentException("Cannot read " + length + " bytes. Only " + (buffer.endOffset - startOffset) + " bytes available");
        }
        int currentOffset = startOffset;
        int endOffset = startOffset + length;
        while(currentOffset < endOffset) {
            int bytesRead = inStream.read(buffer.data, currentOffset, endOffset - currentOffset);
            if(bytesRead < 0) {
                throw new DisconnectedException();
            }
            currentOffset += bytesRead;
        }
        return currentOffset - startOffset;
    }

    private void writeBuffer(BinaryBuffer buffer) throws IOException {
        outStream.write(buffer.data, buffer.startOffset, buffer.length);
    }

    @Override
    public void setAutoflush(boolean autoflush) {
        this.autoflush = autoflush;
    }
}
