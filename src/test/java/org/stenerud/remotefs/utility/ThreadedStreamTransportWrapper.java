package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.message.Parameters;
import org.stenerud.remotefs.session.StreamTransport;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedStreamTransportWrapper implements AutoCloseable {
    private final StreamTransport transport;
    private final ExecutorService executor;

    public ThreadedStreamTransportWrapper(StreamTransport transport) {
        this.transport = transport;
        executor = Executors.newSingleThreadExecutor();
    }

    public void sendMessage(@Nonnull Parameters parameters) throws IOException {
        executor.execute(() -> {
            try {
                transport.sendMessage(parameters);
                transport.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Nonnull
    public Parameters getNextMessage() throws IOException {
        return transport.getNextMessage();
    }

    public void close() throws Exception {
        executor.shutdown();
        transport.close();
    }

}
