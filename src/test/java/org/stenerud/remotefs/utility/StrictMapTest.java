package org.stenerud.remotefs.utility;

import org.junit.Test;
import org.stenerud.remotefs.NotFoundException;
import org.stenerud.remotefs.utility.StrictMap;

import java.util.HashMap;

public class StrictMapTest {
    @Test(expected = NotFoundException.class)
    public void testNotFound() {
        StrictMap<String, Object> map = new StrictMap<>(HashMap::new);
        map.put("test", "blah");
        map.get("aaaa");
    }

    @Test(expected = NotFoundException.class)
    public void testNullKey() {
        StrictMap<String, Object> map = new StrictMap<>(HashMap::new);
        map.put("test", "blah");
        map.get(null);
    }
}
