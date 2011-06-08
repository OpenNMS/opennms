/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.netmgt.eventd;

import java.net.InetSocketAddress;

import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.utils.TcpEventProxy;


/**
 * MemoryLeakTest
 *
 * @author brozow
 */
public class MemoryLeakTest extends OpenNMSTestCase {
    
    private static final long MINS = 8*60*60*1000L;

    public void XXXtestMemory() throws Exception {
        EventProxy proxy = new TcpEventProxy(new InetSocketAddress("127.0.0.1", OpenNMSTestCase.PROXY_PORT));
        double eventRate = 20.0 / 1000.0;
        
        long start = System.currentTimeMillis();
        long count = 0;
        while(System.currentTimeMillis() - start < MINS) {
            long now = Math.max(System.currentTimeMillis(), 1);
            double actualRate = ((double)count) / ((double)(now - start));
            if (actualRate < eventRate) {
                sendEvent(proxy, count);
                count++;
            }
            Thread.sleep(30);
            if (count % 10 == 0) {
                System.err.println(String.format("Expected Rate: %f Actual Rate: %f Events Sent: %d", eventRate, actualRate, count));
            }
        }
        
    }

    private void sendEvent(EventProxy proxy, long count) throws Exception {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/internal/authentication/successfulLogin", "MemoryLeakTest");
        bldr.addParam("user", "brozow");
        
        proxy.send(bldr.getEvent());

        long free = Runtime.getRuntime().freeMemory();
        long max = Runtime.getRuntime().maxMemory();
        
        double pct = ((double)free)/((double)max);
        System.err.println("% Free Memory is "+pct);
    }
}
