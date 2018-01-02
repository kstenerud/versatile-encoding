package org.stenerud.remotefs.session;

import org.stenerud.remotefs.codec.MessageCodec;

import java.io.IOException;
import java.net.Socket;

public class SocketTransport extends StreamTransport {
    private final Socket socket;

    public SocketTransport(Socket socket, MessageCodec messageCodec) throws IOException {
        super(socket.getInputStream(), socket.getOutputStream(), messageCodec);
        this.socket = socket;
    }
    @Override
    public void close() throws Exception {
        super.close();
        socket.close();
    }
}
