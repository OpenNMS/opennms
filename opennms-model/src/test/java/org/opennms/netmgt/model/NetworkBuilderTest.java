/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

public class NetworkBuilderTest {

	@Test
	public void testMultipleIpInterfacesWithOneIfIndex() {
		final NetworkBuilder nb = new NetworkBuilder();
		nb.addNode("test").setForeignId("foo").setForeignSource("bar");
		final OnmsSnmpInterface iface = nb.addSnmpInterface(1).getSnmpInterface();
		nb.addInterface("192.168.0.1", iface).setIsManaged("M").setIsSnmpPrimary("P");
		nb.addInterface("192.168.1.1", iface).setIsManaged("M").setIsSnmpPrimary("S");
		
		final OnmsNode node = nb.getCurrentNode();
		
		assertEquals("test", node.getLabel());
		assertEquals("foo", node.getForeignId());
		assertEquals(2, node.getIpInterfaces().size());
		assertEquals(1, node.getSnmpInterfaces().size());
	}

	@Test
	@Ignore
	public void testAddSnmpToMultipleIp() {
		final NetworkBuilder builder = new NetworkBuilder();
		builder.addNode("foo");
        builder.addInterface("192.168.1.2").setIsManaged("M").setIsSnmpPrimary("S").addSnmpInterface(2)
	        .setCollectionEnabled(true)
	        .setIfOperStatus(1)
	        .setIfSpeed(10000000)
	        .setIfName("eth0")
	        .setIfType(6);
	    builder.addInterface("192.168.1.3").setIsManaged("M").setIsSnmpPrimary("N").addSnmpInterface(2)
	        .setCollectionEnabled(true)
	        .setIfOperStatus(1)
	        .setIfSpeed(10000000)
	        .setIfName("eth0")
	        .setIfType(6);
	    final OnmsNode node = builder.getCurrentNode();
	    assertEquals("foo", node.getLabel());
	    assertEquals(2, node.getIpInterfaces().size());
	    assertEquals(1, node.getSnmpInterfaces().size());
	}
}
