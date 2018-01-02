package org.stenerud.remotefs.utility;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoopingThreadTest {

    @Test
    public void testLoopingThread() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        LoopingThread thread = new LoopingThread() {
            @Override
            protected void performLoop() throws Exception {
                counter.incrementAndGet();
            }

            @Override
            protected void onUnexpectedException(Exception e) {
                throw new IllegalStateException();
            }
        };

        Thread.sleep(1);
        assertEquals(0, counter.get());
        thread.start();
        Thread.sleep(10);
        assertTrue(counter.get() > 0);
        thread.shutdown();
        Thread.sleep(10);
        int currentValue = counter.get();
        Thread.sleep(10);
        assertEquals(currentValue, counter.get());
    }
}
