package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.session.StreamTransport;
import org.stenerud.remotefs.session.Transport;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class StreamTransportPair implements AutoCloseable {
    public final Transport clientSideTransport;
    public final Transport serverSideTransport;

    public StreamTransportPair(MessageCodec messageCodec) {
        try {
            PipedInputStream inToClient = new PipedInputStream();
            PipedInputStream inToServer = new PipedInputStream();
            PipedOutputStream outFromClient = new PipedOutputStream(inToClient);
            PipedOutputStream outFromServer = new PipedOutputStream(inToServer);
            clientSideTransport = new StreamTransport(inToClient, outFromServer, messageCodec);
            clientSideTransport.setAutoflush(true);
            serverSideTransport = new StreamTransport(inToServer, outFromClient, messageCodec);
            serverSideTransport.setAutoflush(true);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws Exception {
        clientSideTransport.close();
        serverSideTransport.close();
    }
}
