package org.stenerud.remotefs.message;

import org.junit.Test;
import org.stenerud.remotefs.exception.NotFoundException;
import org.stenerud.remotefs.utility.DeepEquality;

import static org.junit.Assert.*;

import java.util.*;

public class MessageTest {
    private Message getBasicMessage() {
        return new Message(new Specification("test", "a test spec",
                new Specification.ParameterSpecification("boolean", Specification.Type.BOOLEAN, "The boolean parameter"),
                new Specification.ParameterSpecification("int", Specification.Type.INTEGER, "The int parameter"),
                new Specification.ParameterSpecification("float", Specification.Type.FLOAT, "The float parameter", Specification.Attribute.OPTIONAL),
                new Specification.ParameterSpecification("string", Specification.Type.STRING, "The string parameter", Specification.Attribute.STREAMABLE),
                new Specification.ParameterSpecification("bytes", Specification.Type.BYTES, "The bytes parameter", Specification.Attribute.OPTIONAL, Specification.Attribute.STREAMABLE),
                new Specification.ParameterSpecification("list", Specification.Type.LIST, "The list parameter"),
                new Specification.ParameterSpecification("map", Specification.Type.MAP, "The map parameter")
        ));
    }

    @Test
    public void testIntegerCasting() {
        byte expectedByte = 10;
        short expectedShort = 100;
        int expectedInt = 1000;
        long expectedLong = 10000;
        float expectedFloat = 100000;
        double expectedDouble = 1000000;

        Message message = new Message(new Specification("test", "a test spec",
                new Specification.ParameterSpecification("byte", Specification.Type.INTEGER, "description"),
                new Specification.ParameterSpecification("short", Specification.Type.INTEGER, "description"),
                new Specification.ParameterSpecification("int", Specification.Type.INTEGER, "description"),
                new Specification.ParameterSpecification("long", Specification.Type.INTEGER, "description"),
                new Specification.ParameterSpecification("float", Specification.Type.INTEGER, "description"),
                new Specification.ParameterSpecification("double", Specification.Type.INTEGER, "description")
        ))
                .set("byte", expectedByte)
                .set("short", expectedShort)
                .set("int", expectedInt)
                .set("long", expectedLong)
                .set("float", expectedFloat)
                .set("double", expectedDouble);
        assertEquals((long)expectedByte, message.getObject("byte"));
        assertEquals((long)expectedShort, message.getObject("short"));
        assertEquals((long)expectedInt, message.getObject("int"));
        assertEquals((long)expectedLong, message.getObject("long"));
        assertEquals((long)expectedFloat, message.getObject("float"));
        assertEquals((long)expectedDouble, message.getObject("double"));
    }

    @Test
    public void testFloatCasting() {
        byte expectedByte = 10;
        short expectedShort = 100;
        int expectedInt = 1000;
        long expectedLong = 10000;
        float expectedFloat = 10.1f;
        double expectedDouble = 100.1;

        Message message = new Message(new Specification("test", "a test spec",
                new Specification.ParameterSpecification("byte", Specification.Type.FLOAT, "description"),
                new Specification.ParameterSpecification("short", Specification.Type.FLOAT, "description"),
                new Specification.ParameterSpecification("int", Specification.Type.FLOAT, "description"),
                new Specification.ParameterSpecification("long", Specification.Type.FLOAT, "description"),
                new Specification.ParameterSpecification("float", Specification.Type.FLOAT, "description"),
                new Specification.ParameterSpecification("double", Specification.Type.FLOAT, "description")
        ))
                .set("byte", expectedByte)
                .set("short", expectedShort)
                .set("int", expectedInt)
                .set("long", expectedLong)
                .set("float", expectedFloat)
                .set("double", expectedDouble);
        assertEquals((double)expectedByte, message.getObject("byte"));
        assertEquals((double)expectedShort, message.getObject("short"));
        assertEquals((double)expectedInt, message.getObject("int"));
        assertEquals((double)expectedLong, message.getObject("long"));
        assertEquals((double)expectedFloat, message.getObject("float"));
        assertEquals((double)expectedDouble, message.getObject("double"));
    }

    @Test
    public void testIterator() {
        Message message = getBasicMessage()
                .set("boolean", true)
                .set("int", 100)
                .set("float", 10.5)
                .set("string", "testing")
                .set("bytes", null)
                .set("list", Arrays.asList(1, 2, 3, 4))
                .set("map", new HashMap<>());
        for(Object o: message) {
        }
    }

    @Test
    public void testGetSpecification() {
        assertNotNull(getBasicMessage().getSpecification());
    }

    @Test
    public void testAttributes() {
        Message message = getBasicMessage()
                .set("bytes", 1)
                .set("boolean", true)
                .set("int", 10)
                .set("string", "test")
                .set("list", new LinkedList<>())
                .set("map", new HashMap<>());
        message.verifyCompleteness();
        assertFalse(message.isPresent("float"));
        assertTrue(message.isPresent("bytes"));
        assertTrue(message.isStream("bytes"));
    }

    @Test(expected = Message.ValidationException.class)
    public void testIncorrectParameter() {
        Message message = getBasicMessage()
                .set("bytes", "test");
    }

    @Test(expected = Message.ValidationException.class)
    public void testBadParameterType() {
        Message message = getBasicMessage()
                .set("bytes", new HashSet());
    }

    @Test
    public void testAddParameters() {
        boolean expectedBoolean = true;
        long expectedInteger = 42;
        Double expectedFloat = 10.5;
        String expectedString = "This is a test";
        byte[] expectedBytes = "blah".getBytes();
        List expectedList = Arrays.asList(1, 2, 3, 4);
        Map expectedMap = new HashMap();

        Message message = getBasicMessage()
                .add(expectedBoolean)
                .add(expectedInteger)
                .add(expectedFloat)
                .add(expectedString)
                .add(expectedBytes)
                .add(expectedList)
                .add(expectedMap);
        message.verifyCompleteness();

        assertEquals(expectedBoolean, message.getBoolean("boolean"));
        assertEquals(expectedInteger, message.getLong("int"));
        assertEquals(expectedFloat, message.getDouble("float"), 0.0001);
        assertEquals(expectedString, message.getString("string"));
        assertEquals(expectedBytes, message.getBytes("bytes"));
        assertEquals(expectedList, message.getList("list"));
        assertEquals(expectedMap, message.getMap("map"));
        DeepEquality.assertEquals(expectedInteger, message.getObject("int"));
        assertEquals(expectedFloat, message.get("float", Double.class));
    }

    @Test
    public void testSetParameters() {
        boolean expectedBoolean = true;
        long expectedInteger = 42;
        Double expectedFloat = 10.5;
        String expectedString = "This is a test";
        byte[] expectedBytes = "blah".getBytes();
        List expectedList = Arrays.asList(1, 2, 3, 4);
        Map expectedMap = new HashMap();

        Message message = getBasicMessage()
                .set("boolean", expectedBoolean)
                .set("int", expectedInteger)
                .set("float", expectedFloat)
                .set("string", expectedString)
                .set("bytes", expectedBytes)
                .set("list", expectedList)
                .set("map", expectedMap);
        message.verifyCompleteness();

        assertEquals(expectedBoolean, message.getBoolean("boolean"));
        assertEquals(expectedInteger, message.getLong("int"));
        assertEquals(expectedFloat, message.getDouble("float"), 0.0001);
        assertEquals(expectedString, message.getString("string"));
        assertEquals(expectedBytes, message.getBytes("bytes"));
        assertEquals(expectedList, message.getList("list"));
        assertEquals(expectedMap, message.getMap("map"));
        DeepEquality.assertEquals(expectedInteger, message.getObject("int"));
        assertEquals(expectedFloat, message.get("float", Double.class));
    }

    @Test
    public void testOptionalParameters() {
        boolean expectedBoolean = true;
        long expectedInteger = 42;
        String expectedString = "This is a test";
        List expectedList = Arrays.asList(1, 2, 3, 4);
        Map expectedMap = new HashMap();

        Message message = getBasicMessage()
                .set("boolean", expectedBoolean)
                .set("int", expectedInteger)
                .set("string", expectedString)
                .set("list", expectedList)
                .set("map", expectedMap);
        message.verifyCompleteness();

        assertEquals(expectedBoolean, message.getBoolean("boolean"));
        assertEquals(expectedInteger, message.getLong("int"));
        assertFalse(message.isPresent("float"));
        assertEquals(expectedString, message.getString("string"));
        assertFalse(message.isPresent("bytes"));
        assertEquals(expectedList, message.getList("list"));
        assertEquals(expectedMap, message.getMap("map"));
    }

    @Test
    public void testSetOptionalParameterToNull() {
        Message message = getBasicMessage()
                .set("boolean", true)
                .set("int", 100)
                .set("float", 10.5)
                .set("string", "testing")
                .set("bytes", null)
                .set("list", Arrays.asList(1, 2, 3, 4))
                .set("map", new HashMap<>());
        message.verifyCompleteness();
    }

    @Test(expected = Message.ValidationException.class)
    public void testSetRequiredParameterToNull() {
        Message message = getBasicMessage()
                .set("boolean", true)
                .set("int", null)
                .set("float", 10.5)
                .set("string", "testing")
                .set("bytes", "blah".getBytes())
                .set("list", Arrays.asList(1, 2, 3, 4))
                .set("map", new HashMap<>());
        message.verifyCompleteness();
    }

    @Test(expected = Message.ValidationException.class)
    public void testIncompleteParameters() {
        Message message = getBasicMessage()
                .set("boolean", true)
                .set("int", 100)
                .set("float", 10.5)
                .set("string", "testing")
                .set("bytes", "blah".getBytes())
                .set("map", new HashMap<>());
        message.verifyCompleteness();
    }

    @Test(expected = NotFoundException.class)
    public void testOptionalParameterNotFound() {
        Message message = getBasicMessage()
                .set("boolean", true)
                .set("int", 100)
                .set("string", "testing")
                .set("list", Arrays.asList(1, 2, 3, 4))
                .set("map", new HashMap());
        message.verifyCompleteness();

        message.getObject("float");
    }

    @Test(expected = NotFoundException.class)
    public void testRequiredParameterNotFound() {
        Message message = getBasicMessage();
        message.getObject("int");
    }
}
