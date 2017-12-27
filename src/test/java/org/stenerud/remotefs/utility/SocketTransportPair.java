package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.SocketTransport;
import org.stenerud.remotefs.codec.MessageCodec;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketTransportPair implements AutoCloseable {

    public SocketTransport getClientTransport() {
        return clientTransport;
    }

    public SocketTransport getServerTransport() {
        return serverTransport;
    }

    public static class Builder {
        public static SocketTransportPair build(MessageCodec messageCodec) throws IOException {
            return new SocketTransportPair(messageCodec, PortCounter.next());
        }
    }

    private final SocketTransport clientTransport;
    private final SocketTransport serverTransport;

    private SocketTransportPair(MessageCodec messageCodec, int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket client = new Socket("127.0.0.1", port);
        Socket server = serverSocket.accept();

        clientTransport = new SocketTransport(client, messageCodec);
        serverTransport = new SocketTransport(server, messageCodec);
    }

    @Override
    public void close() throws Exception {
        clientTransport.close();
        serverTransport.close();
    }
}
