package org.stenerud.remotefs.utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TypeConverter {
    public Object promote(@Nullable Object value) {
        if(value == null) {
            return null;
        }
        Class destType = PROMOTIONS.get(value.getClass());
        if(destType != null) {
            return convert(value, destType);
        }
        return value;
    }

    public Object convert(@Nullable Object value, @Nonnull Class destType) {
        if(value == null) {
            return null;
        }

        NumericConverter converter = getConverter(value.getClass(), destType);
        if(converter != null) {
            return converter.convert(value);
        }

        return value;
    }

    private NumericConverter getConverter(Class sourceType, Class destType) {
        Map<Class, NumericConverter> converters = CONVERTERS_BY_DEST_TYPE.get(destType);
        if(converters != null) {
            return converters.get(sourceType);
        }
        return null;
    }

    private static interface NumericConverter {
        public Object convert(Object object);
    }
    private static final Map<Class, Class> PROMOTIONS = new HashMap<>();
    private static final Map<Class, NumericConverter> LONG_CONVERTERS = new HashMap<>();
    private static final Map<Class, NumericConverter> DOUBLE_CONVERTERS = new HashMap<>();
    private static final Map<Class, Map<Class, NumericConverter>> CONVERTERS_BY_DEST_TYPE = new HashMap<>();
    static {
        PROMOTIONS.put(Byte.class, Long.class);
        PROMOTIONS.put(Short.class, Long.class);
        PROMOTIONS.put(Integer.class, Long.class);
        PROMOTIONS.put(Float.class, Double.class);

        CONVERTERS_BY_DEST_TYPE.put(Long.class, LONG_CONVERTERS);
        CONVERTERS_BY_DEST_TYPE.put(Double.class, DOUBLE_CONVERTERS);

        LONG_CONVERTERS.put(Byte.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                return (long) (byte) object;
            }
        });
        LONG_CONVERTERS.put(Short.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                return (long) (short) object;
            }
        });
        LONG_CONVERTERS.put(Integer.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                return (long) (int) object;
            }
        });
        LONG_CONVERTERS.put(Long.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                return object;
            }
        });
        LONG_CONVERTERS.put(Float.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                float asFloat = (float)object;
                long asLong = (long)asFloat;
                if(asLong != asFloat) {
                    throw new IllegalArgumentException("Cannot convert " + object + " to type long");
                }
                return asLong;
            }
        });
        LONG_CONVERTERS.put(Double.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                double asDouble = (double)object;
                long asLong = (long)asDouble;
                if(asLong != asDouble) {
                    throw new IllegalArgumentException("Cannot convert " + object + " to type long");
                }
                return asLong;
            }
        });

        DOUBLE_CONVERTERS.put(Byte.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                return (double) (byte) object;
            }
        });
        DOUBLE_CONVERTERS.put(Short.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                return (double) (short) object;
            }
        });
        DOUBLE_CONVERTERS.put(Integer.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                return (double) (int) object;
            }
        });
        DOUBLE_CONVERTERS.put(Long.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                return (double) (long) object;
            }
        });
        DOUBLE_CONVERTERS.put(Float.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                return (double) (float) object;
            }
        });
        DOUBLE_CONVERTERS.put(Double.class, new NumericConverter() {
            @Override
            public Object convert(Object object) {
                return object;
            }
        });
    }
}
