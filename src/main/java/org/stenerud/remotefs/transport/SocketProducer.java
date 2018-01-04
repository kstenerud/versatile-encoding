package org.stenerud.remotefs.transport;

import org.stenerud.remotefs.utility.Closer;
import org.stenerud.remotefs.utility.LoopingThread;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketProducer implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(SocketProducer.class.getName());

    public interface Listener {
        void onNewSocket(@Nonnull Socket socket);
    }

    public final int port;
    private final ServerSocket listenerSocket;
    private SocketProducer.Listener listener = socket -> {
        try {
            // Default behavior: hang up.
            socket.close();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    };
    private final LoopingThread thread = new LoopingThread() {
        @Override
        protected void performLoop() throws Exception {
            try {
                Socket socket = listenerSocket.accept();
                listener.onNewSocket(socket);
            } catch(SocketException e) {
                if(!e.getMessage().equals("Socket closed")) {
                    throw e;
                }
            }
        }

        @Override
        protected void onUnexpectedException(Exception e) {
            LOG.log(Level.WARNING, "Unexpected Exception", e);
            Closer.closeAllAndLogErrors(LOG, SocketProducer.this);
        }
    };

    /**
     * Create a socket producer that is bound to the specified port.
     * @param port The port to bind to. A port number of 0 means that the port number is automatically allocated, typically from an ephemeral port range.
     * @throws IOException
     */
    public SocketProducer(int port) throws IOException {
        this(null, port, 0);
    }

    /**
     * Create a socket producer that is bound to the specified port and address.
     *
     * @param address The address to bind to. null means allow connections on any interface.
     * @param port The port to bind to. 0 means choose a port automatically (typically from an ephemeral port range).
     * @param backlog maximum number of pending connections. <= 0 means use implementation defined value.
     * @throws IOException
     */
    public SocketProducer(@Nullable String address, int port, int backlog) throws IOException {
        listenerSocket = new ServerSocket(port, backlog, InetAddress.getByName(address));
        this.port = listenerSocket.getLocalPort();
        thread.start();
    }

    public void setListener(@Nonnull SocketProducer.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void close() throws Exception {
        Closer.closeAll(
                thread::shutdown,
                listenerSocket
        );
    }
}
