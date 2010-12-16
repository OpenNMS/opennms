/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.model.discovery;

import java.util.Iterator;

import junit.framework.TestCase;

public class IPAddressSetTest extends TestCase {
    
    private final IPAddress m_oneDot1 = new IPAddress("192.168.1.1");
    private final IPAddress m_oneDot2 = new IPAddress("192.168.1.2");
    private final IPAddress m_oneDot3 = new IPAddress("192.168.1.3");
    private final IPAddress m_oneDot4 = new IPAddress("192.168.1.4");
    private final IPAddress m_oneDot5 = new IPAddress("192.168.1.5");
    private final IPAddress m_oneDot6 = new IPAddress("192.168.1.6");
    private final IPAddress m_oneDot7 = new IPAddress("192.168.1.7");
    private final IPAddress m_oneDot8 = new IPAddress("192.168.1.8");
    private final IPAddress m_oneDot9 = new IPAddress("192.168.1.9");
    private final IPAddress m_oneDotA = new IPAddress("192.168.1.10");
    private final IPAddress m_oneDotB = new IPAddress("192.168.1.11");
    private final IPAddress m_oneDotC = new IPAddress("192.168.1.12");
    private final IPAddress m_oneDotD = new IPAddress("192.168.1.13");
    private final IPAddress m_oneDotE = new IPAddress("192.168.1.14");
    private final IPAddress m_oneDotF = new IPAddress("192.168.1.15");
    

    private final IPAddressRange m_small = new IPAddressRange(m_oneDot1, m_oneDot5);
    private final IPAddressRange m_smaller = new IPAddressRange(m_oneDot7, m_oneDot9);
    

    public void testIPAddressSet() {
        IPAddressSet set = new IPAddressSet();
        assertTrue(set.isEmpty());

        assertEquals(0, set.size());
        assertEquals(0, set.getRangeCount());
        assertEquals(set.size(), iterateAndCount(set));

        assertFalse(set.contains(m_oneDot1));
        assertFalse(set.iterator().hasNext());
    }

    public void testIPAddressSetIPAddress() {
        IPAddressSet set = new IPAddressSet(m_oneDot1);
        assertFalse(set.isEmpty());
        
        assertEquals(1, set.size());
        assertEquals(1, set.getRangeCount());
        assertEquals(set.size(), iterateAndCount(set));

        assertTrue(set.contains(m_oneDot1));
        assertFalse(set.contains(m_oneDot3));
        
        Iterator<IPAddress> it = set.iterator();
        assertTrue(it.hasNext());
        assertEquals(m_oneDot1, it.next());
        assertFalse(it.hasNext());
    }

    public void testIPAddressSetIPAddressRange() {
        IPAddressSet set = new IPAddressSet(m_small);
        
        assertFalse(set.isEmpty());
        assertEquals(5, set.size());
        assertEquals(1, set.getRangeCount());
        
        assertEquals(set.size(), iterateAndCount(set));

        assertTrue(set.contains(m_oneDot1));
        assertTrue(set.contains(m_oneDot3));
        assertTrue(set.contains(m_oneDot5));
        assertFalse(set.contains(m_oneDot9));
        
    }

    public void testUnionIPAddress() {
        IPAddressSet set = new IPAddressSet(m_small);
        set = set.union(m_oneDot7);
        
        assertEquals(6, set.size());
        assertEquals(2, set.getRangeCount());
        
        assertEquals(set.size(), iterateAndCount(set));

        assertTrue(set.contains(m_oneDot1));
        assertTrue(set.contains(m_oneDot3));
        assertTrue(set.contains(m_oneDot5));
        assertFalse(set.contains(m_oneDot6));
        assertTrue(set.contains(m_oneDot7));
        assertFalse(set.contains(m_oneDot9));
    }

    public void testUnionIPAddressRangeAddRangeWhollyBefore() {
        IPAddressSet set = new IPAddressSet(m_smaller);
        set = set.union(m_small);
        
        assertEquals(8, set.size());
        assertEquals(2, set.getRangeCount());
        
        assertEquals(set.size(), iterateAndCount(set));
        
        assertTrue(set.contains(m_oneDot3));
        assertFalse(set.contains(m_oneDot6));
        assertTrue(set.contains(m_oneDot8));
    }
    
    public void testUnionIPAddressRangeAddRangeOverlaps() {
        IPAddressSet set = new IPAddressSet();
        set = set.union(m_small);
        set = set.union(m_smaller);
        set = set.union(new IPAddressRange(m_oneDot3, m_oneDot8));
        
        
        assertEquals(9, set.size());
        assertEquals(1, set.getRangeCount());
        assertEquals(set.size(), iterateAndCount(set));
        
    }
    
    public void testUnionIPAddressRangeAddRangeAdjacent() {
        IPAddressSet set = new IPAddressSet();
        set = set.union(m_small);
        set = set.union(m_smaller);
        set = set.union(new IPAddressRange(m_oneDotA, m_oneDotC));
        
        assertEquals(11, set.size());
        assertEquals(2, set.getRangeCount());
        assertEquals(set.size(), iterateAndCount(set));
        
    }
    
    public void testUnionIPAddressRangeAddRangeFollowing() {
        IPAddressSet set = new IPAddressSet(m_small);
        set = set.union(m_smaller);
        set = set.union(new IPAddressRange(m_oneDotB, m_oneDotD));
        
        assertEquals(11, set.size());
        assertEquals(3, set.getRangeCount());
        assertEquals(set.size(), iterateAndCount(set));
        
    }
    
    private static long iterateAndCount(IPAddressSet set) {
        int count = 0;
        Iterator<IPAddress> it = set.iterator();
        while(it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    public void testUnionIPAddressSet() {
        IPAddressSet setA = new IPAddressSet(m_small).union(m_smaller);
        IPAddressSet setB = new IPAddressSet(new IPAddressRange(m_oneDotB, m_oneDotD)).union(m_oneDot6);
        IPAddressSet set = setA.union(setB);
        
        assertEquals(12, set.size());
        assertEquals(2, set.getRangeCount());
        assertEquals(set.size(), iterateAndCount(set));

    }

    public void testContainsAll() {
        //fail("Not yet implemented");
    }

    public void testMinusIPAddress() {
        IPAddressSet set = new IPAddressSet(new IPAddressRange(m_oneDot1, m_oneDot9));
        set = set.minus(new IPAddress(m_oneDot6));

        assertEquals(8, set.size());
        assertEquals(2, set.getRangeCount());
        
        assertEquals(set.size(), iterateAndCount(set));
        
        assertTrue(set.contains(m_oneDot4));
        assertFalse(set.contains(m_oneDot6));
        assertTrue(set.contains(m_oneDot8));
       
    }
    
    public void testMinusIPRange() {
        IPAddressSet set = new IPAddressSet(new IPAddressRange(m_oneDot1, m_oneDot9));
        set = set.minus(m_small);
        set = set.minus(m_smaller);

        assertEquals(1, set.size());
        assertEquals(1, set.getRangeCount());
        
        assertEquals(set.size(), iterateAndCount(set));
        
        assertFalse(set.contains(m_oneDot3));
        assertTrue(set.contains(m_oneDot6));
        assertFalse(set.contains(m_oneDot8));
        
    }

    public void testMinusIPAddressSet() {
        IPAddressSet set = new IPAddressSet(new IPAddressRange(m_oneDot1, m_oneDot9)).union(new IPAddressRange(m_oneDotB, m_oneDotF));
        
        IPAddressSet set2 = new IPAddressSet(m_small).union(m_smaller);
        
        set = set.minus(set2);

        assertEquals(6, set.size());
        assertEquals(2, set.getRangeCount());
        
        assertEquals(set.size(), iterateAndCount(set));
        
        assertFalse(set.contains(m_oneDot2));
        assertTrue(set.contains(m_oneDot6));
        assertFalse(set.contains(m_oneDot8));
        assertTrue(set.contains(m_oneDotE));
        
    }

    public void testIntersect() {
        
    }

}
