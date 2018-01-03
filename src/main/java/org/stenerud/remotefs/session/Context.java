package org.stenerud.remotefs.session;

import org.stenerud.remotefs.utility.StrictMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Context {
    private static final Logger LOG = Logger.getLogger(Context.class.getName());
    private final Map<Class, Object> objects = StrictMap.withImplementation(ConcurrentHashMap::new).withErrorFormat("%s: No such object");

    public Context() {}

    public Context(Context parentContext) {
        objects.putAll(parentContext.objects);
    }

    public <T> T get(Class<T> key) {
        return (T)objects.get(key);
    }

    public void put(Class key, Object value) {
        objects.put(key, value);
    }
}
