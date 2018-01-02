package org.stenerud.remotefs.utility;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ObjectHolder {
    private static final Object NULL_OBJECT = new Object();
    private BlockingQueue queue = new ArrayBlockingQueue(1000);

    public void set(Object o) {
        queue.add(o == null ? NULL_OBJECT : o);
    }

    public Object get() {
        Object result = null;
        try {
            result = queue.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        if(result == null) {
            throw new IllegalStateException("Object still not available after 1 second");
        }
        if(result == NULL_OBJECT) {
            result = null;
        }
        return result;
    }
}
