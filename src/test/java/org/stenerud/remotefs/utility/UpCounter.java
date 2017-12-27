package org.stenerud.remotefs.utility;

import java.util.concurrent.atomic.AtomicInteger;

public class UpCounter {
    private AtomicInteger counter = new AtomicInteger();

    public UpCounter(int startValue) {
        counter.set(startValue);
    }

    public int next() {
        return counter.getAndIncrement();
    }
}
