package org.stenerud.remotefs.utility;

import org.stenerud.remotefs.NotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Message and function parameters.
 */
public class Parameters implements Iterable<Object> {
    private static final Map<Specification.Type, Class> TYPE_TO_JAVA_CLASS = new HashMap<>();
    static {
        TYPE_TO_JAVA_CLASS.put(Specification.Type.INTEGER, Long.class);
        TYPE_TO_JAVA_CLASS.put(Specification.Type.FLOAT, Double.class);
    }

    private final Specification specification;
    private final TypeConverter typeConverter = new TypeConverter();
    private final StrictMap<String, Parameter> parameters = new StrictMap<>(HashMap::new);
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

    public Parameters(@Nonnull Specification specification) {
        this.specification = specification;
        for(Specification.ParameterSpecification paramSpec: specification) {
            if(paramSpec.isOptional()) {
                set(paramSpec.name, null);
            }
        }
    }

    public @Nonnull Parameters add(@Nullable Object value) {
        // TODO: What to do when the client adds too many parameters?
        // Throw an exception?
        // Silently ignore?
        // Which is better for forward compatibility?
        String name = specification.getByIndex(specIndex++).name;
        return set(name, value);
    }

    private Object convert(@Nullable Object value, @Nonnull Specification.Type type) {
        Class destClass = TYPE_TO_JAVA_CLASS.get(type);
        if(destClass != null) {
            value = typeConverter.convert(value, destClass);
        }
        return value;
    }

    public @Nonnull Parameters set(@Nonnull String name, @Nullable Object value) {
        Specification.ParameterSpecification paramSpec = specification.getByName(name);
        value = typeConverter.promote(value);
        value = convert(value, paramSpec.type);
        Specification.Type type = getEffectiveType(paramSpec, value);
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
            actualType = TypeMappings.getType(value.getClass());
        } catch(NotFoundException e) {
            throw new ValidationException(paramSpec.name + ": Could not get type for value " + value + ": " + e.getMessage());
        }

        if (actualType.equals(paramSpec.type)) {
            return paramSpec.type;
        }

        if(paramSpec.isStreamType(actualType)) {
            return Specification.Type.STREAM;
        }

        throw new ValidationException(paramSpec.name + ": Value has type " + actualType + " but spec requires " + paramSpec.type);
    }

    static class ValidationException extends IllegalArgumentException {
        ValidationException(String message) {
            super(message);
        }
    }
}
