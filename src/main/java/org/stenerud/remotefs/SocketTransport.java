package org.stenerud.remotefs;

import org.stenerud.remotefs.codec.BinaryCodec;
import org.stenerud.remotefs.codec.IntegerCodec;
import org.stenerud.remotefs.codec.LittleEndianCodec;
import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.message.Parameters;
import org.stenerud.remotefs.utility.BinaryBuffer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Logger;

public class SocketTransport implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(SocketTransport.class.getName());

    private final Socket socket;
    private final InputStream inStream;
    private final OutputStream outStream;
    private final MessageCodec messageCodec;

    public SocketTransport(Socket socket, MessageCodec messageCodec) throws IOException {
        this.socket = socket;
        this.inStream = socket.getInputStream();
        this.outStream = socket.getOutputStream();
        this.messageCodec = messageCodec;
    }

    public void sendMessage(@Nonnull Parameters parameters) throws IOException {
        try {
            // TODO: Get this differently
            BinaryBuffer buffer = new BinaryBuffer(10000);
            BinaryBuffer encodedView = messageCodec.encode(parameters, buffer);
            writeBuffer(encodedView);
        } catch(ClosedChannelException e) {
            throw new DisconnectedException(e);
        } catch(IOException e) {
            if(e.getMessage().equals("Broken pipe")) {
                throw new DisconnectedException(e);
            }
            throw e;
        }
    }

    public @Nonnull
    Parameters getNextMessage() throws IOException {
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
        } catch(BinaryCodec.EndOfDataException e) {
            throw new DisconnectedException(e);
        } catch(ClosedChannelException e) {
            throw new DisconnectedException(e);
        }
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    private int readBytes(BinaryBuffer buffer, int startOffset, int length) throws IOException {
        if(length == 0) {
            return 0;
        }
        if(startOffset + length > buffer.endOffset) {
            throw new IllegalArgumentException("Cannot read " + length + " bytes. Only " + (buffer.endOffset - startOffset) + " bytes available");
        }
        try {
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
        } catch (SocketException e) {
            throw new DisconnectedException(e);
        }
    }

    private void writeBuffer(BinaryBuffer buffer) throws IOException {
        try {
            System.out.println("" + this + ": write " + buffer.length);
            outStream.write(buffer.data, buffer.startOffset, buffer.length);
        } catch(SocketException e) {
            throw new DisconnectedException(e);
        }
    }
}
