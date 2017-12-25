package org.stenerud.remotefs.message;

import org.junit.Test;
import org.stenerud.remotefs.NotFoundException;
import org.stenerud.remotefs.message.Parameters;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.DeepEquality;

import static org.junit.Assert.*;

import java.util.*;

public class ParametersTest {
    private Parameters getBasicParameters() {
        return new Parameters(new Specification("test", "a test spec",
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

        Parameters parameters = new Parameters(new Specification("test", "a test spec",
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
        assertEquals((long)expectedByte, parameters.getObject("byte"));
        assertEquals((long)expectedShort, parameters.getObject("short"));
        assertEquals((long)expectedInt, parameters.getObject("int"));
        assertEquals((long)expectedLong, parameters.getObject("long"));
        assertEquals((long)expectedFloat, parameters.getObject("float"));
        assertEquals((long)expectedDouble, parameters.getObject("double"));
    }

    @Test
    public void testFloatCasting() {
        byte expectedByte = 10;
        short expectedShort = 100;
        int expectedInt = 1000;
        long expectedLong = 10000;
        float expectedFloat = 10.1f;
        double expectedDouble = 100.1;

        Parameters parameters = new Parameters(new Specification("test", "a test spec",
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
        assertEquals((double)expectedByte, parameters.getObject("byte"));
        assertEquals((double)expectedShort, parameters.getObject("short"));
        assertEquals((double)expectedInt, parameters.getObject("int"));
        assertEquals((double)expectedLong, parameters.getObject("long"));
        assertEquals((double)expectedFloat, parameters.getObject("float"));
        assertEquals((double)expectedDouble, parameters.getObject("double"));
    }

    @Test
    public void testIterator() {
        Parameters parameters = getBasicParameters()
                .set("boolean", true)
                .set("int", 100)
                .set("float", 10.5)
                .set("string", "testing")
                .set("bytes", null)
                .set("list", Arrays.asList(1, 2, 3, 4))
                .set("map", new HashMap<>());
        for(Object o: parameters) {
        }
    }

    @Test
    public void testGetSpecification() {
        assertNotNull(getBasicParameters().getSpecification());
    }

    @Test
    public void testAttributes() {
        Parameters parameters = getBasicParameters()
                .set("bytes", 1)
                .set("boolean", true)
                .set("int", 10)
                .set("string", "test")
                .set("list", new LinkedList<>())
                .set("map", new HashMap<>());
        parameters.verifyCompleteness();
        assertFalse(parameters.isPresent("float"));
        assertTrue(parameters.isPresent("bytes"));
        assertTrue(parameters.isStream("bytes"));
    }

    @Test(expected = Parameters.ValidationException.class)
    public void testIncorrectParameter() {
        Parameters parameters = getBasicParameters()
                .set("bytes", "test");
    }

    @Test(expected = Parameters.ValidationException.class)
    public void testBadParameterType() {
        Parameters parameters = getBasicParameters()
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

        Parameters parameters = getBasicParameters()
                .add(expectedBoolean)
                .add(expectedInteger)
                .add(expectedFloat)
                .add(expectedString)
                .add(expectedBytes)
                .add(expectedList)
                .add(expectedMap);
        parameters.verifyCompleteness();

        assertEquals(expectedBoolean, parameters.getBoolean("boolean"));
        assertEquals(expectedInteger, parameters.getLong("int"));
        assertEquals(expectedFloat, parameters.getDouble("float"), 0.0001);
        assertEquals(expectedString, parameters.getString("string"));
        assertEquals(expectedBytes, parameters.getBytes("bytes"));
        assertEquals(expectedList, parameters.getList("list"));
        assertEquals(expectedMap, parameters.getMap("map"));
        DeepEquality.assertEquals(expectedInteger, parameters.getObject("int"));
        assertEquals(expectedFloat, parameters.get("float", Double.class));
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

        Parameters parameters = getBasicParameters()
                .set("boolean", expectedBoolean)
                .set("int", expectedInteger)
                .set("float", expectedFloat)
                .set("string", expectedString)
                .set("bytes", expectedBytes)
                .set("list", expectedList)
                .set("map", expectedMap);
        parameters.verifyCompleteness();

        assertEquals(expectedBoolean, parameters.getBoolean("boolean"));
        assertEquals(expectedInteger, parameters.getLong("int"));
        assertEquals(expectedFloat, parameters.getDouble("float"), 0.0001);
        assertEquals(expectedString, parameters.getString("string"));
        assertEquals(expectedBytes, parameters.getBytes("bytes"));
        assertEquals(expectedList, parameters.getList("list"));
        assertEquals(expectedMap, parameters.getMap("map"));
        DeepEquality.assertEquals(expectedInteger, parameters.getObject("int"));
        assertEquals(expectedFloat, parameters.get("float", Double.class));
    }

    @Test
    public void testOptionalParameters() {
        boolean expectedBoolean = true;
        long expectedInteger = 42;
        String expectedString = "This is a test";
        List expectedList = Arrays.asList(1, 2, 3, 4);
        Map expectedMap = new HashMap();

        Parameters parameters = getBasicParameters()
                .set("boolean", expectedBoolean)
                .set("int", expectedInteger)
                .set("string", expectedString)
                .set("list", expectedList)
                .set("map", expectedMap);
        parameters.verifyCompleteness();

        assertEquals(expectedBoolean, parameters.getBoolean("boolean"));
        assertEquals(expectedInteger, parameters.getLong("int"));
        assertFalse(parameters.isPresent("float"));
        assertEquals(expectedString, parameters.getString("string"));
        assertFalse(parameters.isPresent("bytes"));
        assertEquals(expectedList, parameters.getList("list"));
        assertEquals(expectedMap, parameters.getMap("map"));
    }

    @Test
    public void testSetOptionalParameterToNull() {
        Parameters parameters = getBasicParameters()
                .set("boolean", true)
                .set("int", 100)
                .set("float", 10.5)
                .set("string", "testing")
                .set("bytes", null)
                .set("list", Arrays.asList(1, 2, 3, 4))
                .set("map", new HashMap<>());
        parameters.verifyCompleteness();
    }

    @Test(expected = Parameters.ValidationException.class)
    public void testSetRequiredParameterToNull() {
        Parameters parameters = getBasicParameters()
                .set("boolean", true)
                .set("int", null)
                .set("float", 10.5)
                .set("string", "testing")
                .set("bytes", "blah".getBytes())
                .set("list", Arrays.asList(1, 2, 3, 4))
                .set("map", new HashMap<>());
        parameters.verifyCompleteness();
    }

    @Test(expected = Parameters.ValidationException.class)
    public void testIncompleteParameters() {
        Parameters parameters = getBasicParameters()
                .set("boolean", true)
                .set("int", 100)
                .set("float", 10.5)
                .set("string", "testing")
                .set("bytes", "blah".getBytes())
                .set("map", new HashMap<>());
        parameters.verifyCompleteness();
    }

    @Test(expected = NotFoundException.class)
    public void testOptionalParameterNotFound() {
        Parameters parameters = getBasicParameters()
                .set("boolean", true)
                .set("int", 100)
                .set("string", "testing")
                .set("list", Arrays.asList(1, 2, 3, 4))
                .set("map", new HashMap());
        parameters.verifyCompleteness();

        parameters.getObject("float");
    }

    @Test(expected = NotFoundException.class)
    public void testRequiredParameterNotFound() {
        Parameters parameters = getBasicParameters();
        parameters.getObject("int");
    }
}
