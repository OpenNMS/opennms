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

import junit.framework.TestCase;

import org.opennms.core.utils.CollectionMath;

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
        m_goodHost = InetAddress.getByName("www.google.com");
        m_badHost  = InetAddress.getByName("1.1.1.1");
    }

    public void testSinglePing() throws Exception {
        assertTrue(Pinger.ping(m_goodHost) > 0);
    }

    public void testSinglePingFailure() throws Exception {
        assertNull(Pinger.ping(m_badHost));
    }

    public void testParallelPing() throws Exception {
        List<Number> items = Pinger.parallelPing(m_goodHost, 20, PingConstants.DEFAULT_TIMEOUT, 50);
        Thread.sleep(1000);
        printResponse(items);
        assertTrue(CollectionMath.countNotNull(items) > 0);
    }

    public void testParallelPingFailure() throws Exception {
        List<Number> items = Pinger.parallelPing(m_badHost, 20, PingConstants.DEFAULT_TIMEOUT, 50);
        Thread.sleep(PingConstants.DEFAULT_TIMEOUT + 100);
        printResponse(items);
        assertTrue(CollectionMath.countNotNull(items) == 0);
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
