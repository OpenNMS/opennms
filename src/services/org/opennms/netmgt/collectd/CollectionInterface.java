package org.opennms.netmgt.collectd;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.IPv4NetworkInterface;

public class CollectionInterface extends IPv4NetworkInterface {

	private OnmsIpInterface m_iface;

	public CollectionInterface(OnmsIpInterface iface) {
		super(iface.getInetAddress());
		m_iface = iface;
	}
	
	public String getHostAddress() {
		return m_address.getHostAddress();
	}
	
	public OnmsIpInterface getIpInterface() {
		return m_iface;
	}
	
	public OnmsNode getNode() {
		return m_iface.getNode();
	}

}
