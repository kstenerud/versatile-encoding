package org.stenerud.remotefs.message;

import org.stenerud.remotefs.exception.NotFoundException;
import org.stenerud.remotefs.utility.BinaryBuffer;
import org.stenerud.remotefs.utility.Decimal128Holder;
import org.stenerud.remotefs.utility.NumericPromoter;
import org.stenerud.remotefs.utility.StrictMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Message.
 */
public class Message implements Iterable<Object> {
    private static final Logger LOG = Logger.getLogger(Message.class.getName());
    private static final Map<Specification.Type, Class> TYPE_TO_CLASS = new HashMap<>();
    private static final Map<Class<?>, Specification.Type> CLASS_TO_TYPE = StrictMap.withImplementation(ConcurrentHashMap::new).withErrorFormat("%s is not an allowed parameter type");
    static {
        TYPE_TO_CLASS.put(Specification.Type.INTEGER, Long.class);
        TYPE_TO_CLASS.put(Specification.Type.FLOAT, Double.class);

        CLASS_TO_TYPE.put(Boolean.class, Specification.Type.BOOLEAN);
        CLASS_TO_TYPE.put(Long.class, Specification.Type.INTEGER);
        CLASS_TO_TYPE.put(Double.class, Specification.Type.FLOAT);
        CLASS_TO_TYPE.put(String.class, Specification.Type.STRING);
        CLASS_TO_TYPE.put(Date.class, Specification.Type.DATE);
        CLASS_TO_TYPE.put(Instant.class, Specification.Type.DATE);
        CLASS_TO_TYPE.put(Decimal128Holder.class, Specification.Type.DECIMAL);
        CLASS_TO_TYPE.put(byte[].class, Specification.Type.BYTES);
        CLASS_TO_TYPE.put(List.class, Specification.Type.LIST);
        CLASS_TO_TYPE.put(Map.class, Specification.Type.MAP);
        CLASS_TO_TYPE.put(BinaryBuffer.class, Specification.Type.BYTES);
    }

    private final Specification specification;
    private final NumericPromoter numericPromoter = new NumericPromoter();
    private final StrictMap<String, Parameter> parameters = StrictMap.withImplementation(HashMap::new).withErrorFormat("%s: No such parameter");
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

    // For unit testing only
    @Nonnull Specification getSpecification() {
        return specification;
    }

    public @Nonnull String getIdentifier() {
        return specification.name;
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

    public int getParameterCount() {
        return parameters.size();
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
        return set(getNextSpecificationName(), value);
    }

    private Object convert(@Nullable Object value, @Nonnull Specification.Type type) {
        Class destClass = TYPE_TO_CLASS.get(type);
        if(destClass != null) {
            value = numericPromoter.convert(value, destClass);
        }
        return value;
    }

    public @Nonnull
    Message set(@Nonnull String name, @Nullable Object value) {
        Specification.ParameterSpecification paramSpec = specification.getByName(name);
        value = numericPromoter.promote(value);
        value = convert(value, paramSpec.type);
        Specification.Type type = getEffectiveType(paramSpec, value);
        return setUnchecked(name, type, value);
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
