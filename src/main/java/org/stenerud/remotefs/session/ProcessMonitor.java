package org.stenerud.remotefs.session;

public interface ProcessMonitor {
    void onReturnValues(Object ... returnValues);
    void onException(Exception exception);
    void onPercentComplete(int percent);
}
