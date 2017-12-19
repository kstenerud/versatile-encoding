package org.stenerud.remotefs.utility;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class TypeMappingsTest {

    @Test
    public void testGetClass() {
        assertEquals(Specification.Type.BOOLEAN, TypeMappings.getType(Boolean.class));
        assertEquals(Specification.Type.INTEGER, TypeMappings.getType(Long.class));
        assertEquals(Specification.Type.FLOAT, TypeMappings.getType(Double.class));
        assertEquals(Specification.Type.STRING, TypeMappings.getType(String.class));
        assertEquals(Specification.Type.BYTES, TypeMappings.getType(byte[].class));
        assertEquals(Specification.Type.LIST, TypeMappings.getType(List.class));
        assertEquals(Specification.Type.MAP, TypeMappings.getType(Map.class));

        assertEquals(Specification.Type.LIST, TypeMappings.getType(LinkedList.class));
        assertEquals(Specification.Type.LIST, TypeMappings.getType(ArrayList.class));

        assertEquals(Specification.Type.MAP, TypeMappings.getType(HashMap.class));
        assertEquals(Specification.Type.MAP, TypeMappings.getType(TreeMap.class));
    }

    @Test
    public void testGetType() {
        assertEquals(Boolean.class, TypeMappings.getClass(Specification.Type.BOOLEAN));
        assertEquals(Long.class, TypeMappings.getClass(Specification.Type.INTEGER));
        assertEquals(Double.class, TypeMappings.getClass(Specification.Type.FLOAT));
        assertEquals(String.class, TypeMappings.getClass(Specification.Type.STRING));
        assertEquals(byte[].class, TypeMappings.getClass(Specification.Type.BYTES));
        assertEquals(List.class, TypeMappings.getClass(Specification.Type.LIST));
        assertEquals(Map.class, TypeMappings.getClass(Specification.Type.MAP));
    }
}
