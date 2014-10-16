/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.ssh.internal.gwt.client.ui;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class KeyBufferTest {

    String testString = "test"; // The string value used to create Keys in the KeyBuffer
    String doubleTestString = testString + testString; // Double the string used in testing drain
    KeyBuffer k; // The KeyBuffer used for testing
    
    @Before
    public void setup () {
       k = new KeyBuffer();
    }
    
    @Test
    public void testAdd(){
        assertEquals(0, k.size()); // Empty KeyBuffer will have size zero
        k.add(testString);
        assertEquals(1,k.size()); // Size should be 1 since we just added an item
    }
    
    @Test
    public void testToString(){
        assertEquals(0, k.size()); // Make sure the list is empty before we start
        assertEquals("", k.toString());
        k.add(testString);
        assertEquals(testString, k.toString());
        }
    
    @Test 
    public void testFree(){
        assertEquals(0, k.size()); // Make sure the list is empty before we start
        k.add(testString); // Add the test string so we have data to free
        k.free(); // Clear the buffer
        assertEquals(0, k.size()); // Make sure the list is empty again after the drain
        
        assertEquals(0, k.size());
        k.add(testString);
        k.add(testString);
        k.free();
        assertEquals(0, k.size());
    }
    @Test
    public void testDrain(){
        assertEquals(0, k.size()); // Make sure the list is empty before we start
        k.add(doubleTestString); // Add the test string twice so we have some data to drain
        assertEquals(doubleTestString, k.drain()); // drain the list into a string
        assertEquals(0, k.size()); // Make sure the list is empty again after the drain
    }
    

}

