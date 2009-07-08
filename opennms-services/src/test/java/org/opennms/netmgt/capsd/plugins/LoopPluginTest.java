/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.capsd.Plugin;

import junit.framework.TestCase;
public class LoopPluginTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.opennms.netmgt.capsd.plugins.LoopPlugin.getProtocolName()'
     */
    public void testGetProtocolName() {
        Plugin plugin = new LoopPlugin();
        assertEquals("LOOP", plugin.getProtocolName());
    }

    /*
     * Test method for 'org.opennms.netmgt.capsd.plugins.LoopPlugin.isProtocolSupported(InetAddress)'
     */
    public void testIsProtocolSupportedInetAddress() throws UnknownHostException {
        Plugin plugin = new LoopPlugin();
        assertFalse(plugin.isProtocolSupported(InetAddress.getByName("127.0.0.1")));
    }

    /*
     * Test method for 'org.opennms.netmgt.capsd.plugins.LoopPlugin.isProtocolSupported(InetAddress, Map)'
     */
    public void testIsProtocolSupportedInetAddressMap() throws UnknownHostException {
        Map<String, Object> qualifiers = new HashMap<String, Object>();
        qualifiers.put("ip-match", "127.*.*.1-2");
        qualifiers.put("is-supported", "true");
        Plugin plugin = new LoopPlugin();
        assertTrue(plugin.isProtocolSupported(InetAddress.getByName("127.0.0.1"), qualifiers));
        assertFalse(plugin.isProtocolSupported(InetAddress.getByName("127.0.0.3"), qualifiers));

    }

}
