package org.stenerud.remotefs.utility;

public class PortCounter {
    private static UpCounter COUNTER = new UpCounter(9999);

    public static int next() {
        return COUNTER.next();
    }
}
