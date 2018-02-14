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

package org.opennms.netmgt.model.discovery;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import junit.framework.TestCase;

import org.opennms.core.network.IPAddress;

/**
 * IPAddressRangeTest
 *
 * @author brozow
 */
public class IPAddrRangeTest extends TestCase {

    private final IPAddress addr2 = new IPAddress("192.168.1.3");
    private final IPAddress addr3 = new IPAddress("192.168.1.5");
    private final IPAddress v6low = new IPAddress("ffdd:0000:0000:0000:0000:0000:0000:0000");
    private final IPAddress v6high = new IPAddress("ffff:0000:0000:0000:0000:0000:0000:0000");

    private final IPAddrRange singleton;
    private final IPAddrRange v6singleton;
    private final IPAddrRange small;
    private final IPAddrRange v6large;

    public IPAddrRangeTest() throws UnknownHostException {
        small = new IPAddrRange(addr2.toString(), addr3.toString());
        singleton = new IPAddrRange(addr2.toString(), addr2.toString());
        v6large = new IPAddrRange(v6low.toString(), v6high.toString());
        v6singleton = new IPAddrRange(v6low.toString(), v6low.toString());
    }

    public void testIterator() {
        // assertEquals(3, small.size());
        Iterator<InetAddress> it = small.iterator();
        assertTrue(it.hasNext());
        assertEquals(addr2.toInetAddress(), it.next());
        assertTrue(it.hasNext());
        assertEquals(addr2.incr().toInetAddress(), it.next());
        assertTrue(it.hasNext());
        assertEquals(addr3.toInetAddress(), it.next());
        assertFalse(it.hasNext());
    }

    public void testIterateSingleton() {
        Iterator<InetAddress> it = singleton.iterator();
        assertTrue(it.hasNext());
        assertEquals(addr2.toInetAddress(), it.next());
        assertFalse(it.hasNext());
    }

    public void testGetSizeOfIpAddrRange() {
        assertEquals(BigInteger.valueOf(3), small.size()); 
        assertEquals(BigInteger.ONE, singleton.size());
    }

    public void testSizeLargerThanIntegerMaxValue() {
        assertEquals(new BigInteger("220000000000000000000000000000", 16).add(BigInteger.ONE), v6large.size()); 
        assertEquals(BigInteger.ONE, v6singleton.size());
    }
}
