/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.model.discovery;

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * IPAddressRangeTest
 *
 * @author brozow
 */
public class IPAddressRangeTest extends TestCase {
    
    IPAddress begin = new IPAddress("192.168.1.1");
    IPAddress addr2 = new IPAddress("192.168.1.3");
    IPAddress addr3 = new IPAddress("192.168.1.5");
    IPAddress end = new IPAddress("192.168.1.254");
    
    IPAddressRange normal;
    IPAddressRange singleton;
    IPAddressRange small;

    @Override
    protected void setUp() throws Exception {
        normal = new IPAddressRange(begin, end);
        small = new IPAddressRange(addr2, addr3);
        singleton = new IPAddressRange(addr2, addr2);
    }

    public void testCreate() {
        assertEquals(begin, normal.getBegin());
        assertEquals(end, normal.getEnd());
        assertEquals(254, normal.size());
    }
    
    public void testSingletonRange() {
        assertEquals(1, singleton.size());
    }
    
    public void testIterator() {
        Iterator<IPAddress> it = small.iterator();
        assertTrue(it.hasNext());
        assertEquals(addr2, it.next());
        assertTrue(it.hasNext());
        assertEquals(addr2.incr(), it.next());
        assertTrue(it.hasNext());
        assertEquals(addr3, it.next());
        assertFalse(it.hasNext());
    }
    
    public void testIterateSingleTon() {
        Iterator<IPAddress> it = singleton.iterator();
        assertTrue(it.hasNext());
        assertEquals(addr2, it.next());
        assertFalse(it.hasNext());
    }

}
