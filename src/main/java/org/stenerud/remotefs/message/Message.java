package org.stenerud.remotefs.message;

import org.stenerud.remotefs.NotFoundException;
import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.StrictMap;
import org.stenerud.remotefs.utility.TypeConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message.
 */
public class Message implements Iterable<Object> {
    private static final Map<Specification.Type, Class> TYPE_TO_CLASS = new HashMap<>();
    private static final Map<Class<?>, Specification.Type> CLASS_TO_TYPE = StrictMap.with(ConcurrentHashMap::new).withErrorFormat("%s is not an allowed parameter type");
    static {
        TYPE_TO_CLASS.put(Specification.Type.INTEGER, Long.class);
        TYPE_TO_CLASS.put(Specification.Type.FLOAT, Double.class);

        CLASS_TO_TYPE.put(Boolean.class, Specification.Type.BOOLEAN);
        CLASS_TO_TYPE.put(Long.class, Specification.Type.INTEGER);
        CLASS_TO_TYPE.put(Double.class, Specification.Type.FLOAT);
        CLASS_TO_TYPE.put(String.class, Specification.Type.STRING);
        CLASS_TO_TYPE.put(byte[].class, Specification.Type.BYTES);
        CLASS_TO_TYPE.put(List.class, Specification.Type.LIST);
        CLASS_TO_TYPE.put(Map.class, Specification.Type.MAP);
        CLASS_TO_TYPE.put(BinaryBuffer.class, Specification.Type.BYTES);
    }

    private final Specification specification;
    private final TypeConverter typeConverter = new TypeConverter();
    private final StrictMap<String, Parameter> parameters = StrictMap.with(HashMap::new).withErrorFormat("%s: No such parameter");
    private int specIndex = 0;

    @Override
    public @Nonnull Iterator<Object> iterator() {
        return new Iterator<Object>() {
            private final Iterator<Specification.ParameterSpecification> iter = specification.iterator();
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Object next() {
                return parameters.get(iter.next().name).value;
            }
        };
    }

    public @Nonnull Specification getSpecification() {
        return specification;
    }

    public void verifyCompleteness() {
        StringBuilder builder = new StringBuilder();
        for(Specification.ParameterSpecification specParam: specification) {
            try {
                parameters.get(specParam.name);
            } catch(NotFoundException e) {
                builder.append(" ");
                builder.append(specParam.name);
            }
        }
        if(builder.length() > 0) {
            throw new ValidationException("Missing parameters:" + builder);
        }
    }

    public static class Parameter {
        public final Specification.Type type;
        public final Object value;

        Parameter(@Nonnull Specification.Type type, @Nullable Object value) {
            this.type = type;
            this.value = value;
        }
    }

    public Message(@Nonnull Specification specification) {
        this.specification = specification;
        for(Specification.ParameterSpecification paramSpec: specification) {
            if(paramSpec.isOptional()) {
                set(paramSpec.name, null);
            }
        }
    }

    private String getNextSpecificationName() {
        return specification.getByIndex(specIndex++).name;
    }

    public @Nonnull
    Message add(@Nullable Object value) {
        // TODO: What to do when the client adds too many parameters?
        // Throw an exception?
        // Silently ignore?
        // Which is better for forward compatibility?
        return set(getNextSpecificationName(), value);
    }

    private Object convert(@Nullable Object value, @Nonnull Specification.Type type) {
        Class destClass = TYPE_TO_CLASS.get(type);
        if(destClass != null) {
            value = typeConverter.convert(value, destClass);
        }
        return value;
    }

    public @Nonnull
    Message set(@Nonnull String name, @Nullable Object value) {
        Specification.ParameterSpecification paramSpec = specification.getByName(name);
        value = typeConverter.promote(value);
        value = convert(value, paramSpec.type);
        Specification.Type type = getEffectiveType(paramSpec, value);
        return setUnchecked(name, type, value);
    }

    public @Nonnull
    Message addUnchecked(@Nonnull Specification.Type type, @Nullable Object value) {
        return setUnchecked(getNextSpecificationName(), type, value);
    }

    public @Nonnull
    Message setUnchecked(@Nonnull String name, Specification.Type type, @Nullable Object value) {
        parameters.put(name, new Parameter(type, value));
        return this;

    }

    public boolean isPresent(@Nonnull String parameterName) {
        return parameters.get(parameterName).value != null;
    }

    public boolean isStream(@Nonnull String parameterName) {
        return parameters.get(parameterName).type.equals(Specification.Type.STREAM);
    }

    public @Nonnull <T> T get(@Nonnull String parameterName, @Nonnull Class<T> cls) {
        T value = (T)parameters.get(parameterName).value;
        if(value == null) {
            throw new NotFoundException("Parameter " + parameterName + " not found");
        }
        return value;
    }

    public @Nonnull Object getObject(@Nonnull String parameterName) {
        return get(parameterName, Object.class);
    }

    public boolean getBoolean(@Nonnull String parameterName) {
        return get(parameterName, Boolean.class);
    }

    public long getLong(@Nonnull String parameterName) {
        return get(parameterName, Long.class);
    }

    public double getDouble(@Nonnull String parameterName) {
        return get(parameterName, Double.class);
    }

    public @Nonnull String getString(@Nonnull String parameterName) {
        return get(parameterName, String.class);
    }

    public @Nonnull byte[] getBytes(@Nonnull String parameterName) {
        return get(parameterName, byte[].class);
    }

    public @Nonnull
    List getList(@Nonnull String parameterName) {
        return get(parameterName, List.class);
    }

    public @Nonnull Map getMap(@Nonnull String parameterName) {
        return get(parameterName, Map.class);
    }

    private @Nonnull
    Specification.Type getEffectiveType(@Nonnull Specification.ParameterSpecification paramSpec, @Nullable Object value) {
        if(value == null) {
            if(paramSpec.isOptional()) {
                return Specification.Type.NULL;
            }
            throw new ValidationException(paramSpec.name + " is a required parameter");
        }

        Specification.Type actualType;
        try {
            actualType = getBaseType(value.getClass());
        } catch(NotFoundException e) {
            throw new ValidationException(paramSpec.name + ": Could not get type for value " + value + ": " + e.getMessage());
        }

        if (actualType.equals(paramSpec.type)) {
            return paramSpec.type;
        }

        if(paramSpec.isStreamType(actualType)) {
            return Specification.Type.STREAM;
        }

        if(paramSpec.type == Specification.Type.ANY) {
            return actualType;
        }

        throw new ValidationException(paramSpec.name + ": Value has type " + actualType + " but spec requires " + paramSpec.type);
    }

    static class ValidationException extends IllegalArgumentException {
        ValidationException(String message) {
            super(message);
        }
    }

    private static Specification.Type getBaseType(Class javaClass) {
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
}
