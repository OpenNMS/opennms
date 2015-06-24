/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.network.IPAddress;
import org.opennms.core.network.IPAddressRange;

/**
 * IPAddressRangeTest
 *
 * @author brozow
 */
public class IPAddressRangeTest extends TestCase {

    private final IPAddress zero = new IPAddress("0.0.0.0");
    private final IPAddress one = new IPAddress("0.0.0.1");

    private final IPAddress maxOneOctet = new IPAddress("0.0.0.255");
    private final IPAddress maxTwoOctet = new IPAddress("0.0.255.0");
    private final IPAddress maxThreeOctet = new IPAddress("0.255.0.0");
    private final IPAddress thirtyBitNumber = new IPAddress("63.255.255.255");
    private final IPAddress thirtyOneBitNumber = new IPAddress("127.255.255.255");
    private final IPAddress thirtyTwoBit = new IPAddress("128.0.0.0");
    private final IPAddress maxFourOctet = new IPAddress("255.0.0.0");

    private final IPAddress maxIPv4 = new IPAddress("255.255.255.255");
    private final IPAddress maxIPv6 = new IPAddress("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
    private final IPAddress maxIPv6MinusFive = new IPAddress("ffff:ffff:ffff:ffff:ffff:ffff:ffff:fffa");
    private final IPAddress begin = new IPAddress("192.168.1.1");
    private final IPAddress addr2 = new IPAddress("192.168.1.3");
    private final IPAddress addr3 = new IPAddress("192.168.1.5");
    private final IPAddress end = new IPAddress("192.168.1.254");

    private final IPAddressRange normal;
    private final IPAddressRange singleton;
    private final IPAddressRange small;
    private final IPAddressRange highV6;

    public IPAddressRangeTest() {
        normal = new IPAddressRange(begin, end);
        small = new IPAddressRange(addr2, addr3);
        singleton = new IPAddressRange(addr2, addr2);
        highV6 = new IPAddressRange(maxIPv6MinusFive, maxIPv6);
    }

    public void testToBigInteger() {
        IPAddress startAtZero = new IPAddress("0.0.0.0");
        assertTrue(startAtZero.isPredecessorOf(one));
        assertEquals(0L, startAtZero.toBigInteger().longValue());
        startAtZero = startAtZero.incr();
        startAtZero = startAtZero.incr();
        startAtZero = startAtZero.incr();
        startAtZero = startAtZero.incr();
        assertEquals(4L, startAtZero.toBigInteger().longValue());
        startAtZero = startAtZero.decr();
        startAtZero = startAtZero.decr();
        assertEquals(2L, startAtZero.toBigInteger().longValue());
        assertTrue(startAtZero.isSuccessorOf(one));

        assertEquals(1L, one.toBigInteger().longValue());

        assertEquals((long)(Math.pow(2, 30) - 1.0), thirtyBitNumber.toBigInteger().longValue());

        assertEquals((long)(Math.pow(2, 31) - 1.0), thirtyOneBitNumber.toBigInteger().longValue());

        assertEquals((long)(Math.pow(2, 31)), thirtyTwoBit.toBigInteger().longValue());

        assertEquals((long)(Math.pow(2, 32) - 1.0), maxIPv4.toBigInteger().longValue());
        // assertEquals((long)(Math.pow(2, 16 * 8) - 1.0), maxIPv6.toBigInteger().longValue());
    }

    public void testConvertBigIntegerIntoInetAddress() throws UnknownHostException {
        assertEquals(0, new ByteArrayComparator().compare(zero.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(zero.toBigInteger()).getAddress()));
        assertEquals(0, new ByteArrayComparator().compare(one.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(zero.incr().toBigInteger()).getAddress()));
        assertEquals(0, new ByteArrayComparator().compare(zero.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(one.decr().toBigInteger()).getAddress()));
        assertEquals(0, new ByteArrayComparator().compare(one.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(one.toBigInteger()).getAddress()));

        assertEquals(0, new ByteArrayComparator().compare(one.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(one.toBigInteger()).getAddress()));

        assertEquals(0, new ByteArrayComparator().compare(maxOneOctet.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(maxOneOctet.toBigInteger()).getAddress()));
        assertEquals(0, new ByteArrayComparator().compare(maxTwoOctet.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(maxTwoOctet.toBigInteger()).getAddress()));
        assertEquals(0, new ByteArrayComparator().compare(maxThreeOctet.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(maxThreeOctet.toBigInteger()).getAddress()));
        assertEquals(0, new ByteArrayComparator().compare(thirtyBitNumber.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(thirtyBitNumber.toBigInteger()).getAddress()));
        assertEquals(0, new ByteArrayComparator().compare(thirtyOneBitNumber.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(thirtyOneBitNumber.toBigInteger()).getAddress()));
        assertEquals(0, new ByteArrayComparator().compare(thirtyTwoBit.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(thirtyTwoBit.toBigInteger()).getAddress()));
        assertEquals(0, new ByteArrayComparator().compare(maxFourOctet.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(maxFourOctet.toBigInteger()).getAddress()));

        InetAddress maxIPv4Addr = InetAddressUtils.convertBigIntegerIntoInetAddress(maxIPv4.toBigInteger());
        assertTrue(maxIPv4Addr instanceof Inet4Address);
        assertFalse(maxIPv4Addr instanceof Inet6Address);
        assertEquals(0, new ByteArrayComparator().compare(maxIPv4.toOctets(), maxIPv4Addr.getAddress()));

        InetAddress maxIPv6Addr = InetAddressUtils.convertBigIntegerIntoInetAddress(maxIPv6.toBigInteger());
        assertTrue(maxIPv6Addr instanceof Inet6Address);
        assertFalse(maxIPv6Addr instanceof Inet4Address);
        assertEquals(0, new ByteArrayComparator().compare(maxIPv6.toOctets(), maxIPv6Addr.getAddress()));

        assertEquals(0, new ByteArrayComparator().compare(maxIPv6MinusFive.toOctets(), InetAddressUtils.convertBigIntegerIntoInetAddress(maxIPv6MinusFive.toBigInteger()).getAddress()));

        try {
            InetAddressUtils.convertBigIntegerIntoInetAddress(new BigInteger("-1"));
            fail("Failed to catch exception for negative value.");
        } catch (IllegalArgumentException e) {
            // Expected case
        }

        try {
            InetAddressUtils.convertBigIntegerIntoInetAddress(maxIPv6.incr().toBigInteger());
            fail("Failed to catch exception for overflow value.");
        } catch (IllegalStateException e) {
            // Expected case
        }
    }

    public void testToIpAddrString() throws UnknownHostException {
        assertEquals("0.0.0.0", InetAddressUtils.toIpAddrString(zero.toOctets()));
        assertEquals("0.0.0.1", InetAddressUtils.toIpAddrString(one.toOctets()));

        assertEquals("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", InetAddressUtils.toIpAddrString(maxIPv6.toOctets()));
        assertEquals("ffff:ffff:ffff:ffff:ffff:ffff:ffff:fffa", InetAddressUtils.toIpAddrString(maxIPv6MinusFive.toOctets()));
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", InetAddressUtils.toIpAddrString(new IPAddress("::1").toOctets()));
        assertEquals("aaaa:0000:0000:0000:0000:0000:0000:0001", InetAddressUtils.toIpAddrString(new IPAddress("AAAA::1").toOctets()));
        assertEquals("aaaa:0000:0000:0000:0000:0000:0000:0000", InetAddressUtils.toIpAddrString(new IPAddress("AAAA::").toOctets()));
        assertEquals("00aa:0000:0000:0000:0000:0000:0000:0000", InetAddressUtils.toIpAddrString(new IPAddress("AA::").toOctets()));
        assertEquals("aaaa:0000:0000:0000:0000:0000:0000:0000%15", InetAddressUtils.toIpAddrString(InetAddressUtils.addr("AAAA::%15")));
        assertEquals("aaaa:0000:0000:0000:0000:0000:0000:0000", InetAddressUtils.toIpAddrString(InetAddressUtils.addr("AAAA::%0")));
    }

    public void testCreate() {
        assertEquals(begin, normal.getBegin());
        assertEquals(end, normal.getEnd());
        assertEquals(new BigInteger("254"), normal.size());

        assertEquals(maxIPv6MinusFive, highV6.getBegin());
        assertEquals(maxIPv6, highV6.getEnd());
        assertEquals(new BigInteger("6"), highV6.size());
    }

    public void testSingletonRange() {
        assertEquals(BigInteger.ONE, singleton.size());
    }

    public void testContains() {
        assertTrue(normal.contains(begin));
        assertTrue(normal.contains(begin.incr()));
        assertTrue(normal.contains(end.decr()));
        assertTrue(normal.contains(end));
    }

    public void testIterator() {
        assertEquals(new BigInteger("3"), small.size());
        Iterator<IPAddress> it = small.iterator();
        assertTrue(it.hasNext());
        assertEquals(addr2, it.next());
        assertTrue(it.hasNext());
        assertEquals(addr2.incr(), it.next());
        assertTrue(it.hasNext());
        assertEquals(addr3, it.next());
        assertFalse(it.hasNext());
    }

    public void testIterateSingleton() {
        Iterator<IPAddress> it = singleton.iterator();
        assertTrue(it.hasNext());
        assertEquals(addr2, it.next());
        assertFalse(it.hasNext());
    }

    public void testGetLowestInetAddress() throws UnknownHostException {
        assertNull(InetAddressUtils.getLowestInetAddress(Collections.<InetAddress>emptyList()));

        List<InetAddress> ips;
        ips = Arrays.asList(
                            InetAddressUtils.addr("0.0.0.0")
        );
        assertEquals("0.0.0.0", getLowestHostAddress(ips));

        ips = Arrays.asList(
                            InetAddressUtils.addr("0.0.0.0"),
                            InetAddressUtils.addr("abcd:ef00:0000:0000:0000:0000:0000:0001"),
                            InetAddressUtils.addr("127.0.0.1")
        );
        assertEquals("0.0.0.0", getLowestHostAddress(ips));

        ips = Arrays.asList(
                            InetAddressUtils.addr("255.255.255.255"),
                            InetAddressUtils.addr("abcd:ef00:0000:0000:0000:0000:0000:0001"),
                            InetAddressUtils.addr("127.0.0.1")
        );
        assertEquals("127.0.0.1", getLowestHostAddress(ips));

        ips = Arrays.asList(
                            InetAddressUtils.addr("8000:0000:0000:0000:0000:0000:0000:0001"),
                            InetAddressUtils.addr("ff00:0000:0000:0000:0000:0000:0000:0001"),
                            InetAddressUtils.addr("8000:0000:0000:0000:0000:0000:0000:0001"),
                            InetAddressUtils.addr("ff00:0000:0000:0000:0000:0000:0000:0001")
        );
        assertEquals("8000:0000:0000:0000:0000:0000:0000:0001", InetAddressUtils.toIpAddrString(InetAddressUtils.getLowestInetAddress(ips)));

        ips = Arrays.asList(
                            InetAddressUtils.addr("ff00:0000:0000:0000:0000:0000:0000:0001"),
                            InetAddressUtils.addr("8000:0000:0000:0000:0000:0000:0000:0001"),
                            InetAddressUtils.addr("ff00:0000:0000:0000:0000:0000:0000:0001"),
                            InetAddressUtils.addr("8000:0000:0000:0000:0000:0000:0000:0001")
        );
        assertEquals("8000:0000:0000:0000:0000:0000:0000:0001", InetAddressUtils.toIpAddrString(InetAddressUtils.getLowestInetAddress(ips)));

        // TODO: These tests produce strange results because it is unclear how this function should 
        // compare IPv6 addresses that include scope IDs

        ips = Arrays.asList(
                            InetAddressUtils.addr("ff00:0000:0000:0000:0000:0000:0000:0001%5"),
                            InetAddressUtils.addr("ff00:0000:0000:0000:0000:0000:0000:0001")
        );
        assertEquals("ff00:0000:0000:0000:0000:0000:0000:0001%5", InetAddressUtils.toIpAddrString(InetAddressUtils.getLowestInetAddress(ips)));

        ips = Arrays.asList(
                            InetAddressUtils.addr("ff00:0000:0000:0000:0000:0000:0000:0001"),
                            InetAddressUtils.addr("ff00:0000:0000:0000:0000:0000:0000:0001%5")
        );
        assertEquals("ff00:0000:0000:0000:0000:0000:0000:0001", InetAddressUtils.toIpAddrString(InetAddressUtils.getLowestInetAddress(ips)));

        ips = Arrays.asList(
                            InetAddressUtils.addr("ff00:0000:0000:0000:0000:0000:0000:0001%6"),
                            InetAddressUtils.addr("ff00:0000:0000:0000:0000:0000:0000:0001%5")
        );
        assertEquals("ff00:0000:0000:0000:0000:0000:0000:0001%6", InetAddressUtils.toIpAddrString(InetAddressUtils.getLowestInetAddress(ips)));
    }

	private String getLowestHostAddress(final List<InetAddress> ips) {
		return InetAddressUtils.str(InetAddressUtils.getLowestInetAddress(ips));
	}

}
