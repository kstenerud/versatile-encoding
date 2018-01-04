package org.stenerud.remotefs.utility;

import org.junit.Test;
import org.stenerud.remotefs.exception.NotFoundException;

import java.util.HashMap;

public class StrictMapTest {
    @Test(expected = NotFoundException.class)
    public void testNotFound() {
        StrictMap<String, Object> map = StrictMap.withImplementation(HashMap::new);
        map.put("test", "blah");
        map.get("aaaa");
    }

    @Test(expected = NotFoundException.class)
    public void testNullKey() {
        StrictMap<String, Object> map = StrictMap.withImplementation(HashMap::new);
        map.put("test", "blah");
        map.get(null);
    }
}
