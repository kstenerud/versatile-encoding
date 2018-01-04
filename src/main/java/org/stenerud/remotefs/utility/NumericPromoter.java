package org.stenerud.remotefs.utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class NumericPromoter {
    private static final Logger LOG = Logger.getLogger(NumericPromoter.class.getName());
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

    private interface NumericConverter {
        Object convert(Object object);
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

        LONG_CONVERTERS.put(Byte.class, object -> (long) (byte) object);
        LONG_CONVERTERS.put(Short.class, object -> (long) (short) object);
        LONG_CONVERTERS.put(Integer.class, object -> (long) (int) object);
        LONG_CONVERTERS.put(Long.class, object -> object);
        LONG_CONVERTERS.put(Float.class, object -> {
            float asFloat = (float)object;
            long asLong = (long)asFloat;
            if(asLong != asFloat) {
                throw new IllegalArgumentException("Cannot convert " + object + " to type long");
            }
            return asLong;
        });
        LONG_CONVERTERS.put(Double.class, object -> {
            double asDouble = (double)object;
            long asLong = (long)asDouble;
            if(asLong != asDouble) {
                throw new IllegalArgumentException("Cannot convert " + object + " to type long");
            }
            return asLong;
        });

        DOUBLE_CONVERTERS.put(Byte.class, object -> (double) (byte) object);
        DOUBLE_CONVERTERS.put(Short.class, object -> (double) (short) object);
        DOUBLE_CONVERTERS.put(Integer.class, object -> (double) (int) object);
        DOUBLE_CONVERTERS.put(Long.class, object -> (double) (long) object);
        DOUBLE_CONVERTERS.put(Float.class, object -> (double) (float) object);
        DOUBLE_CONVERTERS.put(Double.class, object -> object);
    }
}
