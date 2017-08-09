/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.jna;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.CollectionMath;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingResponseCallback;

import com.sun.jna.Platform;

public class JnaPingIT {

    static private JnaPinger s_jnaPinger = new JnaPinger();

    private InetAddress m_goodHost = InetAddressUtils.getLocalHostAddress();
    private InetAddress m_ipv6goodHost = InetAddressUtils.addr("::1");

    // 192.0.2.0/24 is reserved for documentation purposes
    private InetAddress m_badHost = InetAddressUtils.addr("192.0.2.123");

    // Originally we used the 2001:db8 prefix, which is reserved for documentation purposes
    // (suffix is 'BadAddr!' as ascii), but some networks actually return "no route to host"
    // rather than just timing out, which throws off these tests.
    private InetAddress m_ipv6badHost = InetAddressUtils.addr("2600:5800:f2a2:ffff:ffff:ffff:dead:beef");

    @Before
    public void setUp() throws Exception {
        assumeTrue(Boolean.getBoolean("runPingTests"));
        MockLogAppender.setupLogging();
    }

    @Test
    public void testSinglePingIPv4() throws Exception {
        singlePingGood(m_goodHost);
    }

    @Test
    public void testSinglePingIPv6() throws Exception {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        singlePingGood(m_ipv6goodHost);
    }

    @Test
    public void testPingCallbackTimeoutIPv4() throws Exception {
        pingCallbackTimeout(m_badHost);
    }

    @Test
    public void testPingCallbackTimeoutIPv6() throws Exception {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        assumeTrue(!Platform.isMac()); // mac throws "no route to host" for a bad host
        pingCallbackTimeout(m_ipv6badHost);
    }

    @Test
    public void testSinglePingFailureIPv4() throws Exception {
        assertNull(s_jnaPinger.ping(m_badHost));
    }

    @Test
    public void testSinglePingFailureIPv6() throws Exception {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        assumeTrue(!Platform.isMac()); // mac throws "no route to host" for a bad host
        assertNull(s_jnaPinger.ping(m_ipv6badHost));
    }

    @Test
    public void testParallelPingIPv4() throws Exception {
        parallelPingGood(m_goodHost);
    }

    @Test
    public void testParallelPingIPv6() throws Exception {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        parallelPingGood(m_ipv6goodHost);
    }

    @Test
    public void testParallelPingFailureIPv4() throws Exception {
        parallelPingFailure(m_badHost);
    }

    @Test
    public void testParallelPingFailureIPv6() throws Exception {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        parallelPingFailure(m_ipv6badHost);
    }

    private void singlePingGood(final InetAddress addr) throws Exception {
        final Number rtt = s_jnaPinger.ping(addr);
        assertNotNull("No RTT value returned from ping, looks like the ping failed", rtt);
        assertTrue("Negative RTT value returned from ping", rtt.doubleValue() > 0);
    }

    private void pingCallbackTimeout(final InetAddress addr) throws Exception {
        final TestPingResponseCallback cb = new TestPingResponseCallback();
        s_jnaPinger.ping(addr, PingConstants.DEFAULT_TIMEOUT, PingConstants.DEFAULT_RETRIES, PingConstants.DEFAULT_PACKET_SIZE,1, cb);

        cb.await();

        assertTrue("Unexpected Error sending ping to " + addr + ": " + cb.getThrowable(), 
                   cb.getThrowable() == null || cb.getThrowable() instanceof NoRouteToHostException);
        assertTrue(cb.isTimeout());
        assertNotNull(cb.getPacket());
        assertNotNull(cb.getAddress());
    }

    private void parallelPingGood(final InetAddress addr) throws Exception {
        final List<Number> items = s_jnaPinger.parallelPing(addr, 20, PingConstants.DEFAULT_TIMEOUT, 50);
        printResponse(items);
        assertTrue("Collection contained all null values, all parallel pings failed", CollectionMath.countNotNull(items) > 0);
        for (final Number item : items) {
            assertNotNull("Found a null reponse time in the response", item);
            assertTrue("Negative RTT value returned from ping", item.floatValue() > 0);
        }
    }

    private void parallelPingFailure(InetAddress addr) throws Exception {
        final List<Number> items = s_jnaPinger.parallelPing(addr, 20, PingConstants.DEFAULT_TIMEOUT, 50);
        printResponse(items);
        assertTrue("Collection contained some numeric values when all parallel pings should have failed", CollectionMath.countNotNull(items) == 0);
    }

    private void printResponse(final List<Number> items) {
        final Long passed = CollectionMath.countNotNull(items);
        final Long failed = CollectionMath.countNull(items);
        Number passedPercent = CollectionMath.percentNotNull(items);
        Number failedPercent = CollectionMath.percentNull(items);
        Number average = CollectionMath.average(items);
        Number median = CollectionMath.median(items);

        if (passedPercent == null) passedPercent = Long.valueOf(0);
        if (failedPercent == null) failedPercent = Long.valueOf(100);
        if (median        == null) median        = Double.valueOf(0);

        if (average == null) {
            average = new Double(0);
        } else {
            average = new Double(average.doubleValue() / 1000.0);
        }

        final StringBuffer sb = new StringBuffer();
        sb.append("response times = ").append(items);
        sb.append("\n");

        sb.append("pings = ").append(items.size());
        sb.append(", passed = ").append(passed).append(" (").append(passedPercent).append("%)");
        sb.append(", failed = ").append(failed).append(" (").append(failedPercent).append("%)");
        sb.append(", median = ").append(median);
        sb.append(", average = ").append(average).append("ms");
        sb.append("\n");
        System.out.print(sb);
    }

    private static class TestPingResponseCallback implements PingResponseCallback {
        private final CountDownLatch m_latch = new CountDownLatch(1);
        private InetAddress m_address;
        private EchoPacket m_packet;
        private Throwable m_throwable;
        private boolean m_timeout = false;

        @Override
        public void handleResponse(final InetAddress address, final EchoPacket response) {
            m_address = address;
            m_packet = response;
            m_latch.countDown();
            System.err.println("RESPONSE COUNTED DOWN");
        }

        @Override
        public void handleTimeout(final InetAddress address, final EchoPacket request) {
            m_timeout = true;
            m_address = address;
            m_packet = request;
            m_latch.countDown();
            System.err.println("TIMEOUT COUNTED DOWN");
        }

        @Override
        public void handleError(final InetAddress address, final EchoPacket request, final Throwable t) {
            m_address = address;
            m_packet = request;
            m_throwable = t;
            m_latch.countDown();
            System.err.println("ERROR COUNTED DOWN");
            t.printStackTrace();
        }

        public void await() throws InterruptedException {
            m_latch.await();
        }

        public InetAddress getAddress() {
            return m_address;
        }

        public EchoPacket getPacket() {
            return m_packet;
        }

        public Throwable getThrowable() {
            return m_throwable;
        }

        public boolean isTimeout() {
            return m_timeout;
        }

    }
}
