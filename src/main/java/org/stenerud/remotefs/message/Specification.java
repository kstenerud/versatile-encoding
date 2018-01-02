package org.stenerud.remotefs.message;

import org.stenerud.remotefs.NotFoundException;
import org.stenerud.remotefs.utility.Named;
import org.stenerud.remotefs.utility.StrictMap;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Specifies and validates message parameter types and values.
 */
public class Specification implements Iterable<Specification.ParameterSpecification> {
    public final String name;
    public final String description;
    private final List<ParameterSpecification> parameterSpecificationsByIndex;
    private final StrictMap<String, ParameterSpecification> parameterSpecificationsByName;

    public Specification(@Nonnull String name, @Nonnull String description, @Nonnull ParameterSpecification ... specifications) {
        this.name = name;
        this.description = description;
        parameterSpecificationsByName = StrictMap.with(HashMap::new).withErrorFormat("%s: No such parameter in specification " + name);
        parameterSpecificationsByIndex = new ArrayList<>(specifications.length);
        for(ParameterSpecification spec: specifications) {
            this.parameterSpecificationsByIndex.add(spec);
            this.parameterSpecificationsByName.put(spec.name, spec);
        }
    }

    public @Nonnull ParameterSpecification getByName(@Nonnull String name) {
        return parameterSpecificationsByName.get(name);
    }

    public @Nonnull ParameterSpecification getByIndex(int index) {
        try {
            return parameterSpecificationsByIndex.get(index);
        } catch(IndexOutOfBoundsException e) {
            throw new NotFoundException(e);
        }
    }

    @Override
    public @Nonnull
    Iterator<ParameterSpecification> iterator() {
        return parameterSpecificationsByIndex.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Specification that = (Specification) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Specification{" +
                "name='" + name + '\'' +
                '}';
    }

    public static class Attribute extends Named {
        private Attribute(@Nonnull String name) {
            super(name);
        }
        public static final Attribute OPTIONAL = new Attribute("optional");
        public static final Attribute STREAMABLE = new Attribute("streamable");
    }

    public static class Type extends Named {
        private Type(@Nonnull String name) {
            super(name);
        }
        // TODO: More granular types for what the msg receiver is expecting to use.
        // TODO: How does this affect free-form maps & lists & ANY?
        // TODO: int8, int16, int32, int64, float32, float64, date, others as-is
        public static final Type BOOLEAN = new Type("boolean");
        public static final Type INTEGER = new Type("integer");
        public static final Type FLOAT = new Type("float");
        public static final Type STRING = new Type("string");
        public static final Type BYTES = new Type("bytes");
        public static final Type LIST = new Type("list");
        public static final Type MAP = new Type("map");
        public static final Type ANY = new Type("any");
        public static final Type STREAM = new Type("stream");
        public static final Type NULL = new Type("null");
    }
    private static final Type STREAM_TYPE_MARKER = Type.INTEGER;

    public static class ParameterSpecification {
        public final Type type;
        public final String name;
        public final String description;
        public final Set<Attribute> attributes = new HashSet<>();

        public ParameterSpecification(@Nonnull String name, @Nonnull Type type, @Nonnull String description, @Nonnull Attribute ... attributes) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.attributes.addAll(Arrays.asList(attributes));
        }

        public boolean isOptional() {
            return attributes.contains(Attribute.OPTIONAL);
        }

        public boolean isStreamable() {
            return attributes.contains(Attribute.STREAMABLE);
        }

        public boolean isStreamType(Type type) {
            return isStreamable() && type == STREAM_TYPE_MARKER;
        }
    }
}
