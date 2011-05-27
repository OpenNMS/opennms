package org.opennms.netmgt.model.updates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;

public class NodeUpdateTest {

	private static final String DEFAULT_FOREIGN_ID = "foreignId";
	private static final String DEFAULT_FOREIGN_SOURCE = "foreignSource";

	@Test
	public void testFieldChange() {
		OnmsNode node = getNode();
		node.setSysDescription("Not descriptive.");
		
		NodeUpdate update = new NodeUpdate(DEFAULT_FOREIGN_SOURCE, DEFAULT_FOREIGN_ID);
		update.setSysContact("some guy").setType("A");
		update.setLabel("after").apply(node);
		
		assertEquals("after", node.getLabel());
		assertEquals(DEFAULT_FOREIGN_SOURCE, node.getForeignSource());
		assertEquals("some guy", node.getSysContact());
		assertEquals("A", node.getType());
		assertEquals("Not descriptive.", node.getSysDescription());
	}

	@Test(expected=IllegalStateException.class)
	public void testChangeUpdateAfterApplying() {
		OnmsNode node = getNode();
		
		NodeUpdate update = new NodeUpdate(DEFAULT_FOREIGN_SOURCE, DEFAULT_FOREIGN_ID);
		update.setLabel("after").apply(node);
		
		assertEquals("after", node.getLabel());
		
		update.setSysContact("some guy").setType("A");
		update.apply(node);
		
		assertEquals("monkey", node.getForeignSource());
		assertEquals("some guy", node.getSysContact());
		assertEquals("A", node.getType());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testIncompleteNoForeignSource() {
		new NodeUpdate(null, DEFAULT_FOREIGN_ID);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testIncompleteNoId() {
		new NodeUpdate((Integer)null);
	}
	
	@Test
	public void testSetToNull() {
		OnmsNode node = getNode();
		
		NodeUpdate update = new NodeUpdate(DEFAULT_FOREIGN_SOURCE, DEFAULT_FOREIGN_ID);
		update.setLabel(null);
		update.apply(node);
		
		assertEquals(null, node.getLabel());
	}

	private OnmsNode getNode() {
		OnmsNode node = new OnmsNode();
		node.setLabel("before");
		node.setForeignSource(DEFAULT_FOREIGN_SOURCE);
		node.setForeignId(DEFAULT_FOREIGN_ID);
		return node;
	}
	
	@Test
	public void testSetAssets() {
		OnmsNode node = getNode();
		
		NodeUpdate update = new NodeUpdate(DEFAULT_FOREIGN_SOURCE, DEFAULT_FOREIGN_ID);
		update.assetRecord().setAssetAttribute("admin", "admin value");
		update.assetRecord().setBuilding("this one, right here");
		update.assetRecord().setCity("The one built on rock and roll.");
		
		update.apply(node);
		
		assertEquals("admin value", node.getAssetRecord().getAdmin());
		assertEquals("this one, right here", node.getAssetRecord().getBuilding());
		assertEquals("The one built on rock and roll.", node.getAssetRecord().getCity());
	}

	@Test
	public void testAddInterface() {
		final OnmsNode node = new OnmsNode();
		node.setLabel("before");
		node.setForeignSource("foreignSource");
		node.setForeignId("foreignId");
		
		NodeUpdate update = new NodeUpdate("foreignSource", "foreignId");
		update.ipAddress(InetAddressUtils.addr("192.168.0.1")).setIsSnmpPrimary(PrimaryType.SECONDARY);
		
		update.apply(node);
		
		OnmsIpInterface iface = node.getIpInterfaceByIpAddress("192.168.0.1");
		assertNotNull(iface);
		assertTrue(iface.isManaged());
		assertFalse(iface.isPrimary());
	}

	@Test
	public void testAddUnmanagedInterface() {
		final OnmsNode node = new OnmsNode();
		node.setLabel("before");
		node.setForeignSource("foreignSource");
		node.setForeignId("foreignId");
		
		NodeUpdate update = new NodeUpdate("foreignSource", "foreignId");
		update.ipAddress(InetAddressUtils.addr("192.168.0.1")).setIsManaged("U").setIsSnmpPrimary(PrimaryType.SECONDARY);
		
		update.apply(node);
		
		OnmsIpInterface iface = node.getIpInterfaceByIpAddress("192.168.0.1");
		assertNotNull(iface);
		assertEquals("U", iface.getIsManaged());
		assertEquals("S", iface.getPrimaryString());
	}

	@Test
	public void testAddUnmanagedInterfaceWithServices() {
		final OnmsNode node = new OnmsNode();
		node.setLabel("before");
		node.setForeignSource("foreignSource");
		node.setForeignId("foreignId");
		
		NodeUpdate update = new NodeUpdate("foreignSource", "foreignId");
		IpInterfaceUpdate ipUpdater = update.ipAddress(InetAddressUtils.addr("192.168.0.1"));
		ipUpdater.setIsManaged("U").setIsSnmpPrimary(PrimaryType.SECONDARY);
		ipUpdater.addMonitoredService("HTTP").addMonitoredService("ICMP");
		update.apply(node);
		
		OnmsIpInterface iface = node.getIpInterfaceByIpAddress("192.168.0.1");
		assertNotNull(iface);
		assertEquals("U", iface.getIsManaged());
		assertEquals("S", iface.getPrimaryString());
		
		assertNotNull(iface.getMonitoredServiceByServiceType("HTTP"));
		assertNotNull(iface.getMonitoredServiceByServiceType("ICMP"));
		assertNull(iface.getMonitoredServiceByServiceType("SNMP"));
	}

	@Test
	public void testAddInterfaceWithServicesToExistingNodeWithOtherServices() {
		final InetAddress addr = InetAddressUtils.addr("192.168.0.1");

		final OnmsNode node = new OnmsNode();
		node.setLabel("before");
		node.setForeignSource("foreignSource");
		node.setForeignId("foreignId");
		OnmsIpInterface iface = new OnmsIpInterface(addr, node);
		iface.addMonitoredService(new OnmsMonitoredService(iface, new OnmsServiceType("SNMP")));
		
		NodeUpdate update = new NodeUpdate("foreignSource", "foreignId");
		IpInterfaceUpdate ipUpdater = update.ipAddress(addr);
		ipUpdater.addMonitoredService("HTTP").addMonitoredService("ICMP");
		update.apply(node);
		
		iface = node.getIpInterfaceByIpAddress("192.168.0.1");
		assertNotNull(iface);
		assertTrue(iface.isManaged());
		assertEquals("N", iface.getPrimaryString());
		
		assertNotNull(iface.getMonitoredServiceByServiceType("HTTP"));
		assertNotNull(iface.getMonitoredServiceByServiceType("ICMP"));
		assertNotNull(iface.getMonitoredServiceByServiceType("SNMP"));
	}

}
