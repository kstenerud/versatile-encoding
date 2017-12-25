package org.stenerud.remotefs.utility;

import org.junit.Test;
import org.stenerud.remotefs.utility.TypeConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TypeConverterTest {

    @Test
    public void testPromote() {
        TypeConverter converter = new TypeConverter();
        assertEquals(10L, converter.promote((byte)10));
        assertEquals(10L, converter.promote((short)10));
        assertEquals(10L, converter.promote((int)10));
        assertEquals(10L, converter.promote((long)10));

        assertEquals(10.4d, (double)converter.promote(10.4f), 0.01);
        assertEquals(10.4d, (double)converter.promote(10.4d), 0.01);
    }

    @Test
    public void testConvert() {
        TypeConverter converter = new TypeConverter();
        assertEquals(10L, converter.convert((byte)10, Long.class));
        assertEquals(10L, converter.convert((short)10, Long.class));
        assertEquals(10L, converter.convert((int)10, Long.class));
        assertEquals(10L, converter.convert((long)10, Long.class));
        assertEquals(10L, converter.convert((float)10, Long.class));
        assertEquals(10L, converter.convert((double)10, Long.class));
        assertNull(converter.convert(null, Long.class));

        assertEquals(10.0, converter.convert((byte)10, Double.class));
        assertEquals(10.0, converter.convert((short)10, Double.class));
        assertEquals(10.0, converter.convert((int)10, Double.class));
        assertEquals(10.0, converter.convert((long)10, Double.class));
        assertEquals(10.0, converter.convert((float)10, Double.class));
        assertEquals(10.0, converter.convert((double)10, Double.class));
        assertNull(converter.convert(null, Double.class));
    }

    @Test
    public void testNonConversion() {
        TypeConverter converter = new TypeConverter();
        Object o = new Object();
        assertEquals(o, converter.convert(o, Long.class));
        assertEquals(o, converter.convert(o, Double.class));
        assertEquals((int)10, converter.convert((int)10, byte[].class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailedFloatConversion() {
        TypeConverter converter = new TypeConverter();
        converter.convert(10.4f, Long.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailedDoubleConversion() {
        TypeConverter converter = new TypeConverter();
        converter.convert(10.4d, Long.class);
    }
}
