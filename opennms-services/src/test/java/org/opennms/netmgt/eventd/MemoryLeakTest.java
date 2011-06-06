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
import java.net.UnknownHostException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.utils.TcpEventProxy;


/**
 * MemoryLeakTest
 *
 * @author brozow
 */
public class MemoryLeakTest extends OpenNMSTestCase {

    public void xxxTestMemory() throws Exception {
        EventProxy proxy = new TcpEventProxy(new InetSocketAddress(InetAddressUtils.addr("127.0.0.1"), 5837));
        double eventRate = 10.0 / 1000.0;
        
        long start = System.currentTimeMillis();
        long count = 0;
        while(true) {
            long now = Math.max(System.currentTimeMillis(), 1);
            double actualRate = ((double)count) / ((double)(now - start));
            if (actualRate < eventRate) {
                sendEvent(proxy, count);
                count++;
            }
            Thread.sleep(10);
            System.err.println(String.format("Expected Rate: %f Actual Rate: %f Events Sent: %d", eventRate, actualRate, count));
        }
        
    }

    private void sendEvent(EventProxy proxy, long count) throws Exception {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/internal/authentication/successfulLogin", "MemoryLeakTest");
        bldr.addParam("user", "brozow");
        
        proxy.send(bldr.getEvent());
    }
    


}
