/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;


/**
 * ParameterMapTest
 *
 * @author brozow
 */
public class ParameterMapTest {
    
    private static final int[] INT_ARRAY_VAL = new int[] { 1, 2, 3};
    private static final int[] DEFAULT_INT_ARRAY = new int[] { 4, 5, 6 };
    
    private Map<String, Object> m_map;
    
    @Before
    public void setUp() {
        m_map = new HashMap<String, Object>();
        
        m_map.put("keyedBoolean", true);
        m_map.put("keyedBooleanString", "true");
        m_map.put("keyedInteger", 7);
        m_map.put("keyedIntegerString", "7");
        m_map.put("keyedIntegerArray", new int[] { 1, 2, 3});
        m_map.put("keyedIntegerArrayStringWithColons", "1:2:3");
        m_map.put("keyedIntegerArrayStringWithSpaces", "1 2 3");
        m_map.put("keyedIntegerArrayStringWithSemis", "1;2;3");
        m_map.put("keyedIntegerArrayStringWithCommas", "1,2,3");
        m_map.put("keyedLong", 7L);
        m_map.put("keyedLongString", "7");
        m_map.put("keyedString", "keyedString");
    }
    
    @Test
    public void testGetKeyedString() {
        assertEquals("keyedString", ParameterMap.getKeyedString(m_map, "keyedString", "defaultValue"));
        assertEquals("defaultValue", ParameterMap.getKeyedString(m_map, "noSuchKey", "defaultValue"));
        assertEquals("defaultValue", ParameterMap.getKeyedString(null, "keyedString", "defaultValue"));
    }

    @Test
    public void testGetKeyedBoolean() {
        assertEquals(true, ParameterMap.getKeyedBoolean(m_map, "keyedBoolean", false));
        assertEquals(true, ParameterMap.getKeyedBoolean(m_map, "keyedBooleanString", false));
        assertEquals(false, ParameterMap.getKeyedBoolean(m_map, "noSuchKey", false));
        assertEquals(false, ParameterMap.getKeyedBoolean(null, "keyedBoolean", false));
    }

    @Test
    public void testGetKeyedInteger() {
        assertEquals(7, ParameterMap.getKeyedInteger(m_map, "keyedInteger", 3));
        assertEquals(7, ParameterMap.getKeyedInteger(m_map, "keyedIntegerString", 3));
        assertEquals(3, ParameterMap.getKeyedInteger(m_map, "noSuchKey", 3));
        assertEquals(3, ParameterMap.getKeyedInteger(null, "keyedInteger", 3));
    }

    @Test
    public void testGetKeyedIntegerArray() {
        assertArrayEquals(INT_ARRAY_VAL, ParameterMap.getKeyedIntegerArray(m_map, "keyedIntegerArray", DEFAULT_INT_ARRAY));
        assertArrayEquals(INT_ARRAY_VAL, ParameterMap.getKeyedIntegerArray(m_map, "keyedIntegerArrayStringWithColons", DEFAULT_INT_ARRAY));
        assertArrayEquals(INT_ARRAY_VAL, ParameterMap.getKeyedIntegerArray(m_map, "keyedIntegerArrayStringWithSpaces", DEFAULT_INT_ARRAY));
        assertArrayEquals(INT_ARRAY_VAL, ParameterMap.getKeyedIntegerArray(m_map, "keyedIntegerArrayStringWithSemis", DEFAULT_INT_ARRAY));
        assertArrayEquals(INT_ARRAY_VAL, ParameterMap.getKeyedIntegerArray(m_map, "keyedIntegerArrayStringWithCommas", DEFAULT_INT_ARRAY));
        assertArrayEquals(DEFAULT_INT_ARRAY, ParameterMap.getKeyedIntegerArray(m_map, "noSuchKey", DEFAULT_INT_ARRAY));
        assertArrayEquals(DEFAULT_INT_ARRAY, ParameterMap.getKeyedIntegerArray(null, "keyedIntegerArray", DEFAULT_INT_ARRAY));
        
    }

    @Test
    public void testGetKeyedLong() {
        assertEquals(7L, ParameterMap.getKeyedLong(m_map, "keyedLong", 3L));
        assertEquals(7L, ParameterMap.getKeyedLong(m_map, "keyedLongString", 3L));
        assertEquals(3L, ParameterMap.getKeyedLong(m_map, "noSuchKey", 3L));
        assertEquals(3L, ParameterMap.getKeyedLong(null, "keyedLong", 3L));
    }

}
