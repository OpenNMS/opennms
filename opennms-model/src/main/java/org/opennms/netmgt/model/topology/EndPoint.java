package org.opennms.netmgt.model.topology;

import java.util.List;


/**
 * This class represents a destination in the network such as
 * an IP address or a physical port.
 * 
 * @author Antonio
 *
 */
public abstract class EndPoint {
	
	private Element m_device;

	private List<Link> m_links;
	
	public Element getDevice() {
		return m_device;
	}

	public void setDevice(Element m_device) {
		this.m_device = m_device;
	}
	
	public List<Link> getLinks() {
		return m_links;
	}
	
	public void setLinks(List<Link> links) {
		m_links = links	;
	}
	
}
