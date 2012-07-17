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
