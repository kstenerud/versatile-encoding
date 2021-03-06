package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.transport.SocketTransport;
import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.transport.Transport;
import org.stenerud.remotefs.utility.PortCounter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketTransportPair implements AutoCloseable {
    public final Transport clientSideTransport;
    public final Transport serverSideTransport;

    public SocketTransportPair(MessageCodec messageCodec) throws IOException {
        this(messageCodec, PortCounter.next());
    }

    private SocketTransportPair(MessageCodec messageCodec, int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket client = new Socket("127.0.0.1", port);
        Socket server = serverSocket.accept();

        clientSideTransport = new SocketTransport(client, messageCodec);
        clientSideTransport.setAutoflush(true);
        serverSideTransport = new SocketTransport(server, messageCodec);
        serverSideTransport.setAutoflush(true);
    }

    @Override
    public void close() throws Exception {
        clientSideTransport.close();
        serverSideTransport.close();
    }
}
