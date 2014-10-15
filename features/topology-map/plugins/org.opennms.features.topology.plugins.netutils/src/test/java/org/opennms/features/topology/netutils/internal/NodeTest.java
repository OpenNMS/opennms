/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.netutils.internal;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class NodeTest {

	Node node1, node2;
	@Before
	public void setUp() throws Exception {
		node1 = new Node(0, "", "");
		node2 = new Node(5, "127.0.0.1", "Leonardo");
	}

	@Test
	public void testGetLabel() {
		assertEquals("", node1.getLabel());
		assertEquals("Leonardo", node2.getLabel());
	}
	
	@Test
	public void testGetIPAddress() {
		assertEquals("", node1.getIPAddress());
		assertEquals("127.0.0.1", node2.getIPAddress());
	}
	
	@Test
	public void testGetNodeID() {
		assertEquals(0, node1.getNodeID());
		assertEquals(5, node2.getNodeID());
	}
	
	@Test
	public void testSetLabel() {
		node1.setLabel("Philip");
		assertEquals("Philip", node1.getLabel());
	}
	
	@Test
	public void testSetIPAddress() {
		node1.setIPAddress("1.1.1.1");
		assertEquals("1.1.1.1", node1.getIPAddress());
	}
	
	@Test
	public void testSetNodeID() {
		node1.setNodeID(99);
		assertEquals(99, node1.getNodeID());
	}

}
