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
 * Modifications:
 * 
 * Created: August 14, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ping;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.opennms.core.utils.CollectionMath;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.jicmp.JnaPinger;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org>Ben Reed</a>
 */
public class PingTest extends TestCase {
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
        m_goodHost = InetAddress.getLocalHost();
        m_badHost  = InetAddressUtils.UNPINGABLE_ADDRESS;
    }

    public void testSinglePingJni() throws Exception {
        singlePing(new JniPinger());
    }

    public void testSinglePingJna() throws Exception {
        singlePing(new JnaPinger());
    }

    protected void singlePing(Pinger pinger) throws Exception {
        Number rtt = pinger.ping(m_goodHost);
        assertNotNull("No RTT value returned from ping, looks like the ping failed", rtt);
        assertTrue("Negative RTT value returned from ping", rtt.doubleValue() > 0);
    }
    
    private class TestPingResponseCallback implements PingResponseCallback {
        public final CountDownLatch m_latch = new CountDownLatch(1);
        public InetAddress m_address;
        public EchoPacket m_packet;
        public Throwable m_throwable;
        public boolean m_timeout = false;
        @Override
        public void handleResponse(InetAddress address, EchoPacket response) {
            m_address = address;
            m_packet = response;
            m_latch.countDown();
        }
        @Override
        public void handleTimeout(InetAddress address, EchoPacket request) {
            m_timeout = true;
            m_address = address;
            m_packet = request;
            m_latch.countDown();        }
        @Override
        public void handleError(InetAddress address, EchoPacket request, Throwable t) {
            m_address = address;
            m_packet = request;
            m_throwable = t;
            m_latch.countDown();
        }
        
        public void await() throws InterruptedException {
            m_latch.await();
        }
        
    };

    protected void pingCallbackTimeout(Pinger pinger) throws Exception {
        TestPingResponseCallback cb = new TestPingResponseCallback();
        pinger.ping(m_badHost, PingConstants.DEFAULT_TIMEOUT, PingConstants.DEFAULT_RETRIES, 1, cb);
        cb.await();
        assertTrue(cb.m_timeout);
        assertNotNull(cb.m_packet);
        assertNotNull(cb.m_address);
        
    }

    public void testPingCallbackTimeoutJni() throws Exception {
        pingCallbackTimeout(new JniPinger());
    }

    public void testPingCallbackTimeoutJna() throws Exception {
        pingCallbackTimeout(new JnaPinger());
    }

    public void testSinglePingFailureJni() throws Exception {
        singlePingFailure(new JniPinger());
    }

    public void testSinglePingFailureJna() throws Exception {
        singlePingFailure(new JnaPinger());
    }

    protected void singlePingFailure(Pinger pinger) throws Exception {
        assertNull(pinger.ping(m_badHost));
    }

    public void testParallelPingJni() throws Exception {
        parallelPing(new JniPinger());
    }

    public void testParallelPingJna() throws Exception {
        parallelPing(new JnaPinger());
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
        parallelPingFailure(new JniPinger());
    }

    public void testParallelPingFailureJna() throws Exception {
        parallelPingFailure(new JnaPinger());
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
        
        if (passedPercent == null) passedPercent = new Long(0);
        if (failedPercent == null) failedPercent = new Long(100);
        if (median        == null) median        = new Double(0);

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
