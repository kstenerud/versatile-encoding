package org.stenerud.remotefs.session;

import org.stenerud.remotefs.codec.MessageCodec;
import org.stenerud.remotefs.utility.LoopingThread;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketTransportProducer implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(SocketTransportProducer.class.getName());

    public interface Listener {
        void onNewTransport(@Nonnull SocketTransport transport);
    }

    private final int port;
    private final MessageCodec messageCodec;
    private final ServerSocket listenerSocket;
    private Listener listener = transport -> {
        try {
            // Default behavior: hang up.
            transport.close();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    };
    private final LoopingThread thread = new LoopingThread() {
        @Override
        protected void performLoop() throws Exception {
            try {
                Socket socket = listenerSocket.accept();
                SocketTransport transport = new SocketTransport(socket, messageCodec);
                listener.onNewTransport(transport);
            } catch(SocketException e) {
                if(!e.getMessage().equals("Socket closed")) {
                    throw e;
                }
            }
        }

        @Override
        protected void onUnexpectedException(Exception e) {
            LOG.log(Level.WARNING, "Unexpected Exception", e);
            SocketTransportProducer.this.shutdown();
        }
    };

    public SocketTransportProducer(int port, MessageCodec messageCodec) throws IOException {
        this.port = port;
        this.messageCodec = messageCodec;
        // TODO: Allow binding to a specific interface
        listenerSocket = new ServerSocket(port);
        thread.start();
    }

    public void setListener(@Nonnull Listener listener) {
        this.listener = listener;
    }

    @Override
    public void close() throws Exception {
        thread.shutdown();
        listenerSocket.close();
    }

    private void shutdown() {
        try {
            close();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error shutting down transport producer using port " + port, e);
        }
    }
}
