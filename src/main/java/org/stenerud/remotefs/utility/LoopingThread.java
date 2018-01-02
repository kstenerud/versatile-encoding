package org.stenerud.remotefs.utility;

/**
 * Thread adapter that repeatedly calls the performLoop() method until shut down.
 */
public abstract class LoopingThread extends Thread implements AutoCloseable {
    private boolean isRunning = true;

    @Override
    public void run() {
        while(isRunning) {
            try {
                performLoop();
            } catch(Exception e) {
                onUnexpectedException(e);
            }
        }
    }

    protected abstract void performLoop() throws Exception;

    protected abstract void onUnexpectedException(Exception e);

    public void shutdown() {
        isRunning = false;
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }
}
