/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
