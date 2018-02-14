/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import java.net.InetSocketAddress;

import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.mock.OpenNMSITCase;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.support.TcpEventProxy;


/**
 * MemoryLeakTest
 *
 * @author brozow
 */
public class MemoryLeakIT {
    
    private static final long MINS = 8*60*60*1000L;

    @Test
    @Ignore
    public void testMemory() throws Exception {
        EventProxy proxy = new TcpEventProxy(new InetSocketAddress("127.0.0.1", OpenNMSITCase.PROXY_PORT));
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
            System.err.println(String.format("Expected Rate: %f Actual Rate: %f Events Sent: %d", eventRate, actualRate, count));
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
        
        if (pct < 0.01) {
            throw new IllegalStateException("Memory Used up!");
        }

    }
}
