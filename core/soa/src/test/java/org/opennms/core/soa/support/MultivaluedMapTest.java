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
package org.opennms.core.soa.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.opennms.core.soa.support.MultivaluedMap;
import org.opennms.core.soa.support.MultivaluedMapImpl;


/**
 * MultivaluedMapTest
 *
 * @author brozow
 */
public class MultivaluedMapTest {
    
    private MultivaluedMap<String, String> map = new MultivaluedMapImpl<String, String>();
    
    @Test
    public void testAddRemove() {
        
        String key = "key";
        String value1 = "value1";
        String value2 = "value2";
        
        assertNull(map.get(key));

        map.add(key, value1);
        map.add(key, value2);
        
        assertNotNull(map.get(key));
        
        assertEquals(2, map.get(key).size());
        
        assertTrue(map.get(key).contains(value1));
        assertTrue(map.get(key).contains(value2));
        
        assertTrue(map.remove(key, value1));
        
        assertEquals(1, map.get(key).size());
        
        assertFalse(map.get(key).contains(value1));
        assertTrue(map.get(key).contains(value2));
        
        assertTrue(map.remove(key, value2));

        assertNull(map.get(key));
        
        
    }
    
    @Test
    public void testGetCopy() {
        
        String key = "key";
        String value1 = "value1";
        String value2 = "value2";
        
        assertNull(map.get(key));

        map.add(key, value1);
        map.add(key, value2);

        Set<String> copy = map.getCopy(key);
        
        assertNotSame(copy, map.get(key));
        assertEquals(copy, map.get(key));
        
        
    }

}
