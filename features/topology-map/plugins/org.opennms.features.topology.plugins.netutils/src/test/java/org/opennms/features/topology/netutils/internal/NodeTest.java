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
