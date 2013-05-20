/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.capsd.plugins;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.capsd.Plugin;
public class LoopPluginTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
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
        assertFalse(plugin.isProtocolSupported(InetAddressUtils.addr("127.0.0.1")));
    }

    /*
     * Test method for 'org.opennms.netmgt.capsd.plugins.LoopPlugin.isProtocolSupported(InetAddress, Map)'
     */
    public void testIsProtocolSupportedInetAddressMap() throws UnknownHostException {
        Map<String, Object> qualifiers = new HashMap<String, Object>();
        qualifiers.put("ip-match", "127.*.*.1-2");
        qualifiers.put("is-supported", "true");
        Plugin plugin = new LoopPlugin();
        assertTrue(plugin.isProtocolSupported(InetAddressUtils.addr("127.0.0.1"), qualifiers));
        assertFalse(plugin.isProtocolSupported(InetAddressUtils.addr("127.0.0.3"), qualifiers));

    }

}
