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
 * Created: March 31, 2010
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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.model.discovery.IPPollAddress;

/**
 * 
 */
public class DiscoveryConfigFactoryTest {
    @Test
    public void testAddToSpecificsFromURLViaURL() throws Exception {
        final List<IPPollAddress> specifics = new ArrayList<IPPollAddress>();
        final URL in = Thread.currentThread().getContextClassLoader().getResource(
                "org/opennms/netmgt/config/validDiscoveryIncludeFile.txt"
        );
        final long timeout = 100;
        final int retries = 1;
        DiscoveryConfigFactory.addToSpecificsFromURL(specifics, in.toString(), timeout, retries);
        assertEquals(7, specifics.size());
        assertEquals("127.0.0.1", specifics.get(0).getAddress().getHostAddress());
        assertEquals("10.1.1.1", specifics.get(1).getAddress().getHostAddress());
        assertEquals("10.2.1.1", specifics.get(2).getAddress().getHostAddress());
        assertEquals("8.8.8.8", specifics.get(3).getAddress().getHostAddress());
        assertEquals("fe80:0:0:0:ffff:eeee:dddd:cccc", specifics.get(4).getAddress().getHostAddress());
        assertEquals("0:0:0:0:0:0:0:1", specifics.get(5).getAddress().getHostAddress());
        assertEquals("fe80:0:0:0:ffff:eeee:dddd:cccd", specifics.get(6).getAddress().getHostAddress());
    }
    
    @Test
    public void testAddToSpecificsFromURLViaStream() throws Exception {
        final List<IPPollAddress> specifics = new ArrayList<IPPollAddress>();
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "org/opennms/netmgt/config/validDiscoveryIncludeFile.txt"
        );
        final long timeout = 100;
        final int retries = 1;
        DiscoveryConfigFactory.addToSpecificsFromURL(specifics, in, timeout, retries);
        assertEquals(7, specifics.size());
        assertEquals("127.0.0.1", specifics.get(0).getAddress().getHostAddress());
        assertEquals("10.1.1.1", specifics.get(1).getAddress().getHostAddress());
        assertEquals("10.2.1.1", specifics.get(2).getAddress().getHostAddress());
        assertEquals("8.8.8.8", specifics.get(3).getAddress().getHostAddress());
        assertEquals("fe80:0:0:0:ffff:eeee:dddd:cccc", specifics.get(4).getAddress().getHostAddress());
        assertEquals("0:0:0:0:0:0:0:1", specifics.get(5).getAddress().getHostAddress());
        assertEquals("fe80:0:0:0:ffff:eeee:dddd:cccd", specifics.get(6).getAddress().getHostAddress());
    }
}
