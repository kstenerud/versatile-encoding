package org.stenerud.remotefs.session;

import org.stenerud.remotefs.codec.MessageCodec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class SocketTransportProducer implements TransportProducer {
    private final SocketProducer socketProducer;
    private final MessageCodec messageCodec;

    public SocketTransportProducer(@Nonnull MessageCodec messageCodec, int port) throws IOException {
        this.messageCodec = messageCodec;
        this.socketProducer = new SocketProducer(port);
    }

    public SocketTransportProducer(@Nonnull MessageCodec messageCodec, @Nullable String address, int port, int backlog) throws IOException {
        this.messageCodec = messageCodec;
        this.socketProducer = new SocketProducer(address, port, backlog);
    }

    @Override
    public void setListener(@Nonnull Listener listener) {
        socketProducer.setListener(socket -> {
            try {
                listener.onNewTransport(new SocketTransport(socket, messageCodec));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Override
    public void close() throws Exception {
        socketProducer.close();
    }
}
