package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;

public class IPv6NetworkInterface implements NetworkInterface {

	Map<String, Object> m_properties = new TreeMap<String, Object>();
	
	Object m_lock = new Object();
	
	protected InetAddress m_address;

	public IPv6NetworkInterface(String ipAddr) throws UnknownHostException {
		m_address = InetAddress.getByName(ipAddr);
	}
	
	public IPv6NetworkInterface(InetAddress inetAddress) {
		m_address = inetAddress;
	}

	public InetAddress getInetAddress() {
		return m_address;
	}
	public Object getAddress() {
		return m_address;
	}

	public Object getAttribute(String key) {
		synchronized (m_lock) {
			return m_properties.get(key);
		}
	}

	public int getType() {
		return TYPE_IPV6;
	}

	public Object setAttribute(String property, Object value) {
		synchronized (m_lock) {
			return m_properties.put(property, value);
		}
	}

}
