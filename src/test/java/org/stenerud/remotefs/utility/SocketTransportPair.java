package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.session.SocketTransport;
import org.stenerud.remotefs.codec.MessageCodec;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketTransportPair implements AutoCloseable {
    public final ThreadedStreamTransportWrapper clientTransport;
    public final ThreadedStreamTransportWrapper serverTransport;

    public SocketTransportPair(MessageCodec messageCodec) throws IOException {
        this(messageCodec, PortCounter.next());
    }

    private SocketTransportPair(MessageCodec messageCodec, int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket client = new Socket("127.0.0.1", port);
        Socket server = serverSocket.accept();

        clientTransport = new ThreadedStreamTransportWrapper(new SocketTransport(client, messageCodec));
        serverTransport = new ThreadedStreamTransportWrapper(new SocketTransport(server, messageCodec));
    }

    @Override
    public void close() throws Exception {
        clientTransport.close();
        serverTransport.close();
    }
}
