package org.opennms.netmgt.model.updates;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.mock.MockLogAppender;

public class IpInterfaceUpdateTest {

	@Before
	public void setUp() {
		MockLogAppender.setupLogging();
	}

	@Test(expected=IllegalStateException.class)
	public void testIncompleteNoNode() {
		OnmsIpInterface iface = new OnmsIpInterface("192.168.0.1", null);
		new IpInterfaceUpdate(iface);
	}

	@Test(expected=IllegalStateException.class)
	public void testIncompleteNoAddress() {
		OnmsNode node = new OnmsNode();
		node.setForeignSource("foreignSource");
		node.setForeignId("foreignId");
		OnmsIpInterface iface = new OnmsIpInterface((InetAddress)null, node);
		IpInterfaceUpdate update = new IpInterfaceUpdate(iface);
		LogUtils.debugf(this, "address = %s", update.getAddress());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testIncompleteNoIfaceId() {
		IpInterfaceUpdate update = new IpInterfaceUpdate((Integer)null);
		LogUtils.debugf(this, "address = %s", update.getAddress());
	}
	
	@Test
	public void testForceUnmanage() {
		OnmsNode node = new OnmsNode();
		node.setForeignSource("foreignSource");
		node.setForeignId("foreignId");
		OnmsIpInterface iface = new OnmsIpInterface("192.168.0.1", node);
		
		IpInterfaceUpdate update = new IpInterfaceUpdate(iface);
		update.setIsManaged("F");
		update.apply(iface);
		
		assertEquals("F", iface.getIsManaged());
	}
}
