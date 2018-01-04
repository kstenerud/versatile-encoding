package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.codec.MessageCodec;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Logger;

public class StreamTransportProducer implements TransportProducer {
    private static final Logger LOG = Logger.getLogger(StreamTransportProducer.class.getName());
    private final MessageCodec messageCodec;
    private Listener listener = transport -> {
        // Ignored
    };

    public StreamTransportProducer(MessageCodec messageCodec) {
        this.messageCodec = messageCodec;
    }

    @Override
    public void setListener(@Nonnull Listener listener) {
        this.listener = listener;
    }

    @Override
    public void close() throws Exception {

    }

    public Transport produceTransportPair() {

        try {
            PipedInputStream inToClient = new PipedInputStream();
            PipedInputStream inToServer = new PipedInputStream();
            PipedOutputStream outFromClient = new PipedOutputStream(inToClient);
            PipedOutputStream outFromServer = new PipedOutputStream(inToServer);
            Transport clientSideTransport = new StreamTransport(inToClient, outFromServer, messageCodec);
            clientSideTransport.setAutoflush(true);
            Transport serverSideTransport = new StreamTransport(inToServer, outFromClient, messageCodec);
            serverSideTransport.setAutoflush(true);

            listener.onNewTransport(serverSideTransport);
            return clientSideTransport;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
