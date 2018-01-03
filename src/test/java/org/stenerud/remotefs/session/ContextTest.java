package org.stenerud.remotefs.session;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContextTest {
    @Test
    public void testContext() {
        Context context = new Context();
        String expected = "expected";
        String somethingElse = "something else";
        context.put(String.class, expected);
        Context subContext = new Context(context);
        context.put(String.class, somethingElse);

        assertEquals(expected, subContext.get(String.class));
        assertEquals(somethingElse, context.get(String.class));
    }

}
