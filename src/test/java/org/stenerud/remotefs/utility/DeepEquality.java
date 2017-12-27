package org.stenerud.remotefs.utility;

import junit.framework.AssertionFailedError;
import org.stenerud.remotefs.message.Parameters;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Compare deep container objects in unit tests.
 */
public class DeepEquality {
    public static void assertEquals(Object expected, Object actual) {
        List<Object> context = new LinkedList<>();
        try {
            assertEquals(context, expected, actual);
        } catch(AssertionFailedError e) {
            if(context.isEmpty()) {
                throw e;
            }
            e.printStackTrace();
            throw new AssertionFailedError("At " + getContextString(context) + ": " + e.getMessage());
        }
    }

    private static String getContextString(List<Object> context) {
        boolean isTopLevel = true;
        StringBuilder builder = new StringBuilder();
        for(Object entry: context) {
            if(!isTopLevel) {
                builder.append("/");
            }
            builder.append(entry);
            isTopLevel = false;
        }
        return builder.toString();
    }

    private static void failEqualityAssertion(Object expected, Object actual) {
        throw new AssertionFailedError("Expected |" + expected + "| but got |" + actual + "|");
    }

    private static void assertEquals(List<Object> context, Object expected, Object actual) {
        // Identity
        if(expected == actual) {
            return;
        }

        // Nulls & class compatibility
        if(expected == null || actual == null) {
            failEqualityAssertion(expected, actual);
        }
        assertClassCompatibility(expected, actual);

        // Containers
        if(expected instanceof List) {
            assertEquality(context, (List) expected, (List) actual);
            return;
        }
        if(expected instanceof Map) {
            assertEquality(context, (Map) expected, (Map) actual);
            return;
        }
        if(expected.getClass().isArray()) {
            assertArrayEquality(context, expected, actual);
            return;
        }

        // Numbers
        if(expected instanceof Number) {
            assertNumberEquality((Number)expected, (Number)actual);
            return;
        }

        // Dates
        if(expected instanceof Date && actual instanceof Instant) {
            expected = Instant.ofEpochMilli(((Date) expected).getTime());
        } else if(expected instanceof Instant && actual instanceof Date) {
            actual = Instant.ofEpochMilli(((Date) actual).getTime());
        }

        if(expected instanceof Parameters && actual instanceof Parameters) {
            assertParametersEquality((Parameters)expected, (Parameters)actual);
            return;
        }

        // Everything else
        if(!expected.equals(actual)) {
            failEqualityAssertion(expected, actual);
        }
    }

    private static void assertParametersEquality(Parameters expected, Parameters actual) {
        Iterator<Object> expectedIterator = expected.iterator();
        Iterator<Object> actualIterator = actual.iterator();
        while(expectedIterator.hasNext()) {
            Object expectedObject = expectedIterator.next();
            Object actualObject = actualIterator.next();
            assertEquals(expectedObject, actualObject);
        }
        if(actualIterator.hasNext()) {
            throw new AssertionFailedError("actual iterator has more entries");
        }
    }

    private static boolean isNan(Number value) {
        if(value instanceof Float) {
            return Float.isNaN((float)value);
        }
        if(value instanceof Double) {
            return Double.isNaN((double)value);
        }
        return false;
    }

    private static boolean isInfinite(Number value) {
        if(value instanceof Float) {
            return Float.isInfinite((float)value);
        }
        if(value instanceof Double) {
            return Double.isInfinite((double)value);
        }
        return false;
    }

    private static void assertNumberEquality(Number expected, Number actual) {
        // Consider NaNs equal for testing purposes.
        if(isNan(expected)) {
            if(!isNan(actual)) {
                failEqualityAssertion(expected, actual);
            }
            return;
        }

        // Consider infinities equal for testing purposes.
        if(isInfinite(expected)) {
            if(!isInfinite(actual)) {
                failEqualityAssertion(expected, actual);
            }
            return;
        }

        if(!toBigDecimal(expected).equals(toBigDecimal(actual))) {
            failEqualityAssertion(expected, actual);
        }
    }

    private static BigDecimal toBigDecimal(Number number) {
        if(number instanceof BigDecimal) {
            return (BigDecimal)number;
        }
        if(number instanceof Byte) {
            return new BigDecimal((Byte)number);
        }
        if(number instanceof Short) {
            return new BigDecimal((Short)number);
        }
        if(number instanceof Integer) {
            return new BigDecimal((Integer)number);
        }
        if(number instanceof Long) {
            return new BigDecimal((Long)number);
        }
        if(number instanceof Float) {
            return new BigDecimal((Float)number);
        }
        if(number instanceof Double) {
            return new BigDecimal((Double)number);
        }
        if(number instanceof BigInteger) {
            return new BigDecimal((BigInteger)number);
        }
        if(number instanceof AtomicInteger) {
            return new BigDecimal(((AtomicInteger)number).get());
        }
        if(number instanceof AtomicLong) {
            return new BigDecimal(((AtomicLong)number).get());
        }
        throw new IllegalArgumentException("Don't know how to convert numeric type " + number.getClass());
    }

    private static void assertArrayEquality(List<Object> context, Object expected, Object actual) {
        int expectedLength = Array.getLength(expected);
        int actualLength = Array.getLength(actual);
        if(expectedLength != actualLength) {
            throw new AssertionFailedError("Expected array size " + expectedLength + " but got " + actualLength);
        }

        context.add("(array)");
        for(int i = 0; i < expectedLength; i++) {
            assertEquals(context, Array.get(expected, i), Array.get(actual, i));
        }
        context.remove(context.size()-1);
    }

    private static void assertEquality(List<Object> context, List<Object> expected, List<Object> actual) {
        if(expected.size() != actual.size()) {
            throw new AssertionFailedError("Expected list size " + expected.size() + " but got " + actual.size());
        }
        ListIterator<Object> expectedIter = expected.listIterator();
        ListIterator<Object> actualIter = actual.listIterator();
        context.add("(list)");
        while(expectedIter.hasNext()) {
            assertEquals(context, expectedIter.next(), actualIter.next());
        }
        context.remove(context.size()-1);
    }

    private static void assertEquality(List<Object> context, Map<Object, Object> expected, Map<Object, Object> actual) {
        if(expected.size() != actual.size()) {
            throw new AssertionFailedError("Expected map size " + expected.size() + " but got " + actual.size());
        }
        for(Map.Entry<Object, Object> entry: expected.entrySet()) {
            Object key = entry.getKey();
            try {
                // BinaryCodec will use long for all integers
                key = ((Number)key).longValue();
            } catch(ClassCastException e) {
                // Ignore
            }
            Object expectedValue = entry.getValue();
            Object actualValue = actual.get(key);
            if(actualValue == null) {
                throw new AssertionFailedError("Actual map did not contain expected key " + key);
            }
            context.add(key);
            assertEquals(context, expectedValue, actualValue);
            context.remove(context.size()-1);
        }
    }

    private static void assertClassCompatibility(Object expected, Object actual) {
        Class expectedClass = expected.getClass();
        Class actualClass = actual.getClass();

        if(expectedClass == actualClass) {
            return;
        }

        // Comparing Object to another class is meaningless.
        if(expectedClass == Object.class ^ actualClass == Object.class) {
            throw new AssertionFailedError("Expected class " + expectedClass + " but got " + actualClass);
        }

        if(expected instanceof Map && actual instanceof  Map) {
            return;
        }
        if(expected instanceof List && actual instanceof  List) {
            return;
        }
        if(expected instanceof Set && actual instanceof  Set) {
            return;
        }
        if(expected instanceof Instant && actual instanceof Date) {
            return;
        }
        if(expected instanceof Date && actual instanceof Instant) {
            return;
        }

        // Number class hierarchy is messy in Java.
        if(expected instanceof Number && actual instanceof Number) {
            return;
        }

        // One should at least be a subclass of the other.
        if(!(expectedClass.isAssignableFrom(actualClass) ||
                actualClass.isAssignableFrom(expectedClass))) {
            throw new AssertionFailedError("Expected class " + expectedClass + " but got " + actualClass);
        }
    }
}
