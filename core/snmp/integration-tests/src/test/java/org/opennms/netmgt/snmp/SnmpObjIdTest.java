/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.util.Arrays;

import junit.framework.TestCase;

public class SnmpObjIdTest extends TestCase {

    private void assertArrayEquals(int[] a, int[] b) {
        if (a == null) {
            assertNull("expected value is null but actual value is "+Arrays.toString(b), b);
        } else {
            if (b == null) {
                fail("Expected value is "+Arrays.toString(a)+" but actual value is null");
            } 
            assertEquals("arrays have different length", a.length, b.length);
            for(int i = 0; i < a.length; i++) {
                assertEquals("array differ at index "+i+" expected: "+Arrays.toString(a)+", actual: "+Arrays.toString(b), a[i], b[i]);
            }
        }
    }
    
    public void testInvalidOids() {
        try {
            SnmpObjId.get(".1.3.5.x.9");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        
        try {
            SnmpObjId.get(".1.3.-5.7");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
    }
    
    public void testLargeSubId() {
        long subid = ((long)Integer.MAX_VALUE) + 10L;
        String oidStr = ".1.3.5." + subid + ".9";
        SnmpObjId oid = SnmpObjId.get(oidStr);
        assertEquals(oidStr, oid.toString());
    }
    
    public void testCompareWithLargeSubid() {
        long subid = ((long)Integer.MAX_VALUE) + 10L;
        String oidStr = ".1.3.5." + subid + ".9";
        SnmpObjId oid = SnmpObjId.get(oidStr);

        long subid2 = ((long)Integer.MAX_VALUE) + 20L;
        String oidStr2 = ".1.3.5." + subid2 + ".9";
        SnmpObjId oid2 = SnmpObjId.get(oidStr2);
        
        SnmpObjId oid3 = SnmpObjId.get(".1.3.5.7.9");
        
        assertTrue(oid.compareTo(oid2) < 0);
        
        assertTrue(oid3.compareTo(oid) < 0);

    }

    
    public void testDecrementWithLargeSubid() {
        long subid = ((long)Integer.MAX_VALUE) + 10L;
        String oidStr = ".1.3.5." + subid;
        SnmpObjId oid = SnmpObjId.get(oidStr);
        String oidStr2 = ".1.3.5." +(subid-1); 
        
        assertEquals(oidStr2, oid.decrement().toString());

    }

    public void testSnmpOidCompare() {
        SnmpObjId oid1 = SnmpObjId.get("1.3.5.7");
        SnmpObjId oid1b = SnmpObjId.get(".1.3.5.7");
        SnmpObjId oid2 = SnmpObjId.get(new int[] {1, 3, 5, 7});
        SnmpObjId oid3 = SnmpObjId.get(".1.3.5.8");
        SnmpObjId oid4 = SnmpObjId.get(".1.3.5.7.0");
        SnmpObjId oid5 = SnmpObjId.get(oid4);
        
        assertArrayEquals(oid1.getIds(), oid2.getIds());
        
        assertEquals(oid1, oid1b);
        
        assertEquals(".1.3.5.7", oid1.toString());
        
        assertEquals(oid1, oid2);
        assertEquals(oid5, oid4);
        
        assertFalse(oid1.equals(oid3));
        assertFalse(oid1.equals(oid4));
        assertFalse(oid1.equals(null));
        assertFalse(oid1.equals(".1.3.5.7"));
        
        assertTrue(oid1.compareTo(oid3) < 0);
        assertTrue(oid3.compareTo(oid1) > 0);
        assertTrue(oid1.compareTo(oid4) < 0);
        assertTrue(oid4.compareTo(oid1) > 0);
        
        try {
            oid1.compareTo(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            
        }
        
        SnmpInstId inst1 = new SnmpInstId("1");
        SnmpInstId inst2 = new SnmpInstId(inst1);
        SnmpInstId inst3 = new SnmpInstId(1);
        
        assertEquals(inst1, inst2);
        assertEquals(inst2, inst3);
        assertEquals(inst1, inst3);
        
    }

    public void testOidAppendPrefixInstance() {
        SnmpObjId base = SnmpObjId.get(".1.3.5.7");
        SnmpObjId result = SnmpObjId.get(".1.3.5.7.9.8.7.6");
        SnmpInstId inst = new SnmpInstId("9.8.7.6");

        assertEquals(result, base.append(new int[] {9,8,7,6}));
        assertEquals(result, base.append("9.8.7.6"));
        assertEquals(result, base.append(inst));
        
        assertTrue(base.isPrefixOf(base));
        assertTrue(base.isPrefixOf(result));
        assertFalse(result.isPrefixOf(base));
        
        SnmpInstId instance = result.getInstance(base);
        assertEquals(SnmpObjId.get(".9.8.7.6"), instance);
        assertEquals("9.8.7.6", instance.toString());
    }
    
    public void testDecrement() {
        SnmpObjId oid = SnmpObjId.get(".1.3.5.7");
        assertEquals(SnmpObjId.get(".1.3.5.6"), oid.decrement());
        
        SnmpObjId oid2 = SnmpObjId.get(".1.3.5.7.0");
        assertEquals(oid, oid2.decrement());
    }
    
    

}
