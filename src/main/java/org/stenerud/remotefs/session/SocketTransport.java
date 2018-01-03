package org.stenerud.remotefs.session;

import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.utility.Closer;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class SocketTransport extends StreamTransport {
    private static final Logger LOG = Logger.getLogger(SocketTransport.class.getName());
    private final Socket socket;

    public SocketTransport(Socket socket, MessageCodec messageCodec) throws IOException {
        super(socket.getInputStream(), socket.getOutputStream(), messageCodec);
        this.socket = socket;
    }

    @Override
    public void close() throws Exception {
        Closer.closeAll(
                (AutoCloseable) () -> SocketTransport.super.close(),
                socket
        );
    }
}
