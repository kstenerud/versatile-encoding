package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TypeMappings {
    private static final Map<Class<?>, Specification.Type> CLASS_TO_TYPE = new StrictMap<>(ConcurrentHashMap::new);
    private static final Map<Specification.Type, Class<?>> TYPE_TO_CLASS = new StrictMap<>(HashMap::new);
    static {
        registerType(Specification.Type.BOOLEAN, Boolean.class);
        registerType(Specification.Type.INTEGER, Long.class);
        registerType(Specification.Type.FLOAT, Double.class);
        registerType(Specification.Type.STRING, String.class);
        registerType(Specification.Type.BYTES, byte[].class);
        registerType(Specification.Type.LIST, List.class);
        registerType(Specification.Type.MAP, Map.class);
    }

    private static void registerType(Specification.Type type, Class javaClass) {
        CLASS_TO_TYPE.put(javaClass, type);
        TYPE_TO_CLASS.put(type, javaClass);
    }

    public static Specification.Type getType(Class javaClass) {
        try {
            return CLASS_TO_TYPE.get(javaClass);
        } catch(NotFoundException e) {
            for(Map.Entry<Class<?>, Specification.Type> entry: CLASS_TO_TYPE.entrySet()) {
                if(entry.getKey().isAssignableFrom(javaClass)) {
                    Specification.Type type = entry.getValue();
                    CLASS_TO_TYPE.put(javaClass, type);
                    return type;
                }
            }
            throw e;
        }
    }

    public static Class getClass(Specification.Type type) {
        return TYPE_TO_CLASS.get(type);
    }
}
