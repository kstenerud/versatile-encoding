package org.stenerud.remotefs.message;

import org.junit.Test;
import org.stenerud.remotefs.NotFoundException;
import org.stenerud.remotefs.message.Specification;
import org.stenerud.remotefs.utility.DeepEquality;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SpecificationTest {

    private Specification getBasicSpecification() {
        return new Specification("test", "a test spec",
                new Specification.ParameterSpecification("boolean", Specification.Type.BOOLEAN, "The boolean parameter"),
                new Specification.ParameterSpecification("int", Specification.Type.INTEGER, "The int parameter"),
                new Specification.ParameterSpecification("float", Specification.Type.FLOAT, "The float parameter", Specification.Attribute.OPTIONAL),
                new Specification.ParameterSpecification("string", Specification.Type.STRING, "The string parameter", Specification.Attribute.STREAMABLE),
                new Specification.ParameterSpecification("bytes", Specification.Type.BYTES, "The bytes parameter", Specification.Attribute.OPTIONAL, Specification.Attribute.STREAMABLE),
                new Specification.ParameterSpecification("list", Specification.Type.LIST, "The list parameter"),
                new Specification.ParameterSpecification("map", Specification.Type.MAP, "The map parameter")
        );
    }

    private void assertParameterSpec(Specification.ParameterSpecification spec, String expectedName, Specification.Type expectedType, Specification.Attribute ... attributes) {
        Set<Specification.Attribute> expectedAttributes = new HashSet<>();
        for(Specification.Attribute paramSpec: attributes) {
            expectedAttributes.add(paramSpec);
        }
        assertEquals(expectedName, spec.name);
        assertEquals(expectedType, spec.type);
        DeepEquality.assertEquals(expectedAttributes, spec.attributes);
    }

    @Test
    public void testGetByName() {
        Specification spec = getBasicSpecification();
        assertParameterSpec(spec.getByName("boolean"), "boolean", Specification.Type.BOOLEAN);
        assertParameterSpec(spec.getByName("int"), "int", Specification.Type.INTEGER);
        assertParameterSpec(spec.getByName("float"), "float", Specification.Type.FLOAT, Specification.Attribute.OPTIONAL);
        assertParameterSpec(spec.getByName("string"), "string", Specification.Type.STRING, Specification.Attribute.STREAMABLE);
        assertParameterSpec(spec.getByName("bytes"), "bytes", Specification.Type.BYTES, Specification.Attribute.OPTIONAL, Specification.Attribute.STREAMABLE);
        assertParameterSpec(spec.getByName("list"), "list", Specification.Type.LIST);
        assertParameterSpec(spec.getByName("map"), "map", Specification.Type.MAP);
    }

    @Test
    public void testGetByIndex() {
        Specification spec = getBasicSpecification();
        assertParameterSpec(spec.getByIndex(0), "boolean", Specification.Type.BOOLEAN);
        assertParameterSpec(spec.getByIndex(1), "int", Specification.Type.INTEGER);
        assertParameterSpec(spec.getByIndex(2), "float", Specification.Type.FLOAT, Specification.Attribute.OPTIONAL);
        assertParameterSpec(spec.getByIndex(3), "string", Specification.Type.STRING, Specification.Attribute.STREAMABLE);
        assertParameterSpec(spec.getByIndex(4), "bytes", Specification.Type.BYTES, Specification.Attribute.OPTIONAL, Specification.Attribute.STREAMABLE);
        assertParameterSpec(spec.getByIndex(5), "list", Specification.Type.LIST);
        assertParameterSpec(spec.getByIndex(6), "map", Specification.Type.MAP);
    }

    @Test(expected = NotFoundException.class)
    public void testGetByUnknownName() {
        Specification spec = getBasicSpecification();
        spec.getByName("abcdefg");
    }

    @Test(expected = NotFoundException.class)
    public void testgetByInvalidIndex() {
        Specification spec = getBasicSpecification();
        spec.getByIndex(10000);
    }

    @Test
    public void testStreamTypeIsInteger() {
        Specification spec = getBasicSpecification();
        assertTrue(spec.getByName("string").isStreamType(Specification.Type.INTEGER));
    }



//    @Test
//    public void testBasic() throws Exception {
//        Specification spec = new Specification("test", "a test spec",
//                new Specification.ParameterSpecification("a", Specification.Type.STRING, "The A parameter"),
//                new Specification.ParameterSpecification("b", Specification.Type.INTEGER, "The B parameter")
//                );
//        Message message = new Message(spec)
//                .set("a", "a value")
//                .set("b", 100l);
//    }
//
//    @Test
//    public void testOptionalMissing() throws Exception {
//        Specification spec = new Specification("test", "a test spec",
//                new Specification.ParameterSpecification("a", Specification.Type.STRING, "The A parameter"),
//                new Specification.ParameterSpecification("b", Specification.Type.INTEGER, "The B parameter", Specification.Attribute.OPTIONAL)
//        );
//
//        Message message = new Message(spec)
//                .set("a", "a value");
//
//        message.verifyCompleteness();
//    }

//    @Test(expected = Specification.ValidationException.class)
//    public void testRequiredMissing() throws Exception {
//
//        Specification spec = new Specification(name) {
//            @Override
//            public SpecParameters getParameterSpecifications() {
//                SpecParameters message = new SpecParameters();
//                message.addParameter("a", Type.STRING, "The A parameter");
//                message.addOptionalParameter("b", Type.INTEGER, "The B parameter");
//                return message;
//            }
//        };
//        Message message = new Message();
//        Map<Specification.Type, Class> allowedSubstitutions = new HashMap<>();
//        spec.validate(message, allowedSubstitutions);
//    }
//
//    @Test
//    public void testAllTypes() throws Exception {
//
//        Specification spec = new Specification(name) {
//            @Override
//            public SpecParameters getParameterSpecifications() {
//                SpecParameters parameters = new SpecParameters();
//                parameters.addParameter("BOOLEAN", Type.BOOLEAN, "The boolean parameter");
//                parameters.addParameter("INTEGER", Type.INTEGER, "The integer parameter");
//                parameters.addParameter("FLOAT", Type.FLOAT, "The float parameter");
//                parameters.addParameter("STRING", Type.STRING, "The string parameter");
//                parameters.addParameter("BYTES", Type.BYTES, "The bytes parameter");
//                parameters.addParameter("LIST", Type.LIST, "The list parameter");
//                parameters.addParameter("MAP", Type.MAP, "The map parameter");
//                return parameters;
//            }
//        };
//        Message message = new Message()
//                .withParameter("BOOLEAN", true)
//                .withParameter("INTEGER", 100l)
//                .withParameter("FLOAT", 10.5d)
//                .withParameter("STRING", "something")
//                .withParameter("BYTES", "blah".getBytes())
//                .withParameter("LIST", new LinkedList<>())
//                .withParameter("MAP", new HashMap<>());
//        Map<Specification.Type, Class> allowedSubstitutions = new HashMap<>();
//        spec.validate(message, allowedSubstitutions);
//    }
//
//    @Test
//    public void testSubstitution() throws Exception {
//
//        Specification spec = new Specification(name) {
//            @Override
//            public SpecParameters getParameterSpecifications() {
//                SpecParameters parameters = new SpecParameters();
//                parameters.addParameter("a", Type.STRING, "The A parameter");
//                return parameters;
//            }
//        };
//        Message message = new Message()
//                .withParameter("a", new File("/"));
//        Map<Specification.Type, Class> allowedSubstitutions = new HashMap<>();
//        allowedSubstitutions.put(Specification.Type.STRING, File.class);
//        spec.validate(message, allowedSubstitutions);
//    }
//
//    @Test(expected = Specification.ValidationException.class)
//    public void testUnknownType() throws Exception {
//
//        Specification spec = new Specification(name) {
//            @Override
//            public SpecParameters getParameterSpecifications() {
//                SpecParameters parameters = new SpecParameters();
//                parameters.addParameter("a", Type.STRING, "The A parameter");
//                return parameters;
//            }
//        };
//        Message message = new Message()
//                .withParameter("a", new File("/"));
//        Map<Specification.Type, Class> allowedSubstitutions = new HashMap<>();
//        spec.validate(message, allowedSubstitutions);
//    }
}
