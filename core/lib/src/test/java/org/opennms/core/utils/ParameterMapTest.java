/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
