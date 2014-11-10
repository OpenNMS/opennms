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

package org.opennms.netmgt.icmp.jni6;

import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.opennms.core.utils.CollectionMath;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.jni6.Jni6Pinger;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org>Ben Reed</a>
 */
public class Jni6PingTest extends TestCase {

    static private Jni6Pinger s_jniPinger = new Jni6Pinger();

    private InetAddress m_goodHost = null;
    private InetAddress m_badHost = null;

    /**
     * Don't run this test unless the runPingTests property
     * is set to "true".
     */
    @Override
    protected void runTest() throws Throwable {
        if (!isRunTest()) {
            System.err.println("Skipping test '" + getName() + "' because system property '" + getRunTestProperty() + "' is not set to 'true'");
            return;
        }
            
        try {
            System.err.println("------------------- begin "+getName()+" ---------------------");
            super.runTest();
        } finally {
            System.err.println("------------------- end "+getName()+" -----------------------");
        }
    }

    private boolean isRunTest() {
        return Boolean.getBoolean(getRunTestProperty());
    }

    private String getRunTestProperty() {
        return "runPingTests";
    }

    @Override
    protected void setUp() throws Exception {
        if (!isRunTest()) {
            return;
        }

        super.setUp();
        m_goodHost = InetAddress.getByName("::1");
        // 2001:db8 prefix is reserved for documentation purposes suffix is 'BadAddr!' as ascii
        m_badHost = InetAddress.getByName("2001:0db8::4261:6441:6464:7221");
        assertEquals(16, m_badHost.getAddress().length);
    }

    public void testSinglePingJni() throws Exception {
        singlePing(s_jniPinger);
    }

    protected void singlePing(Pinger pinger) throws Exception {
        Number rtt = pinger.ping(m_goodHost);
        assertNotNull("No RTT value returned from ping, looks like the ping failed", rtt);
        assertTrue("Negative RTT value returned from ping", rtt.doubleValue() > 0);
    }
    
    private static class TestPingResponseCallback implements PingResponseCallback {
        private final CountDownLatch m_latch = new CountDownLatch(1);
        private InetAddress m_address;
        private EchoPacket m_packet;
        private Throwable m_throwable;
        private boolean m_timeout = false;
        
        @Override
        public void handleResponse(InetAddress address, EchoPacket response) {
            m_address = address;
            m_packet = response;
            m_latch.countDown();
            System.err.println("RESPONSE COUNTED DOWN");
        }
        
        @Override
        public void handleTimeout(InetAddress address, EchoPacket request) {
            m_timeout = true;
            m_address = address;
            m_packet = request;
            m_latch.countDown();
            System.err.println("TIMEOUT COUNTED DOWN");
        }
        
        @Override
        public void handleError(InetAddress address, EchoPacket request, Throwable t) {
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
        
        /**
         * @return the address
         */
        public InetAddress getAddress() {
            return m_address;
        }
        
        /**
         * @return the packet
         */
        public EchoPacket getPacket() {
            return m_packet;
        }
        
        /**
         * @return the throwable
         */
        public Throwable getThrowable() {
            return m_throwable;
        }
        
        /**
         * @return the timeout
         */
        public boolean isTimeout() {
            return m_timeout;
        }
        
    };

    protected void pingCallbackTimeout(Pinger pinger) throws Exception {

        TestPingResponseCallback cb = new TestPingResponseCallback();
        
        pinger.ping(m_badHost, PingConstants.DEFAULT_TIMEOUT, PingConstants.DEFAULT_RETRIES, PingConstants.DEFAULT_PACKET_SIZE, 1, cb);
        
        cb.await();

        assertTrue("Unexpected Error sending ping to " + m_badHost + ": " + cb.getThrowable(), 
                cb.getThrowable() == null || cb.getThrowable() instanceof NoRouteToHostException);
        assertTrue(cb.isTimeout());
        assertNotNull(cb.getPacket());
        assertNotNull(cb.getAddress());
        
    }

    public void testPingCallbackTimeoutJni() throws Exception {
        pingCallbackTimeout(s_jniPinger);
    }

    public void testSinglePingFailureJni() throws Exception {
        try {
            singlePingFailure(s_jniPinger);
        } catch (NoRouteToHostException ex) {
            // this is a possible option if the OS knows that this is impossible
        }
    }

    protected void singlePingFailure(Pinger pinger) throws Exception {
        assertNull(pinger.ping(m_badHost));
    }

    public void testParallelPingJni() throws Exception {
        parallelPing(s_jniPinger);
    }

    protected void parallelPing(Pinger pinger) throws Exception {
        List<Number> items = pinger.parallelPing(m_goodHost, 20, PingConstants.DEFAULT_TIMEOUT, 50);
        Thread.sleep(1000);
        printResponse(items);
        assertTrue("Collection contained all null values, all parallel pings failed", CollectionMath.countNotNull(items) > 0);
        for (Number item : items) {
            assertNotNull("Found a null reponse time in the response", item);
            assertTrue("Negative RTT value returned from ping", item.floatValue() > 0);
        }
    }

    public void testParallelPingFailureJni() throws Exception {
        parallelPingFailure(s_jniPinger);
    }

    protected void parallelPingFailure(Pinger pinger) throws Exception {
        List<Number> items = pinger.parallelPing(m_badHost, 20, PingConstants.DEFAULT_TIMEOUT, 50);
        Thread.sleep(PingConstants.DEFAULT_TIMEOUT + 100);
        printResponse(items);
        assertTrue("Collection contained some numeric values when all parallel pings should have failed", CollectionMath.countNotNull(items) == 0);
    }
    
    private void printResponse(List<Number> items) {
        Long passed = CollectionMath.countNotNull(items);
        Long failed = CollectionMath.countNull(items);
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

        StringBuffer sb = new StringBuffer();
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
}
    