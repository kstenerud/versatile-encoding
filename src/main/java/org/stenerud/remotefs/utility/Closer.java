package org.stenerud.remotefs.utility;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ensures that closeAll methods of ALL objects get called, regardless of which one(s) threw exceptions.
 */
public class Closer {
    private static final Logger LOG = Logger.getLogger(Closer.class.getName());

    public static class ClosingFailedException extends RuntimeException {
        private final List<Exception> exceptions;

        public ClosingFailedException(@Nonnull List<Exception> exceptions) {
            super(exceptions.get(0));
            this.exceptions = exceptions;
        }

        public @Nonnull List<Exception> getExceptions() {
            return exceptions;
        }
    }

    /**
     * Closer all autocloseables in the order specified.
     * All exceptions thrown during closing will be collected and thrown as a single ClosingFailedException.
     *
     * @param closeables Objects to closeAll in the order they should be closed.
     */
    public static void closeAll(AutoCloseable ... closeables) {
        List<Exception> exceptions = new LinkedList<>();
        for(AutoCloseable closeable: closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        if(!exceptions.isEmpty()) {
            throw new ClosingFailedException(exceptions);
        }
    }

    public static void closeAllAndLogErrors(AutoCloseable ... closeables) {
        closeAllAndLogErrors(LOG, closeables);
    }

    public static void closeAllAndLogErrors(Logger logger, AutoCloseable ... closeables) {
        try {
            closeAll(closeables);
        } catch(ClosingFailedException e) {
            for(Exception e2: e.getExceptions())
                logger.log(Level.WARNING, "Exception while closing", e2);
        } catch(Exception e) {
            logger.log(Level.WARNING, "Exception while closing", e);
        }
    }
}
