package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.session.StreamTransport;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class StreamTransportPair implements AutoCloseable {
    public final ThreadedStreamTransportWrapper clientTransport;
    public final ThreadedStreamTransportWrapper serverTransport;

    public StreamTransportPair(MessageCodec messageCodec) {
        try {
            PipedInputStream inToClient = new PipedInputStream();
            PipedInputStream inToServer = new PipedInputStream();
            PipedOutputStream outFromClient = new PipedOutputStream(inToClient);
            PipedOutputStream outFromServer = new PipedOutputStream(inToServer);
            this.clientTransport = new ThreadedStreamTransportWrapper(new StreamTransport(inToClient, outFromServer, messageCodec));
            this.serverTransport = new ThreadedStreamTransportWrapper(new StreamTransport(inToServer, outFromClient, messageCodec));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws Exception {
        clientTransport.close();
        serverTransport.close();
    }
}
