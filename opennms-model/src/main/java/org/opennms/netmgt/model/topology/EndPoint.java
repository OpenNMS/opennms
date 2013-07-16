package org.opennms.netmgt.model.topology;


/**
 * This class represents a destination in the network such as
 * an IP address or a physical port.
 * Also can represent a TCP Port
 * 
 * @author Antonio
 *
 */
public abstract class EndPoint {
	
	/** 
	 * The Element to which the End Point 
	 * belongs
	 *  
	 */
	private Element m_device;

	/**
	 * Only one Link for End Point is allowed
	 * 
	 */	
	private Link m_link;
	
	public Element getDevice() {
		return m_device;
	}

	public void setDevice(Element device) {
		m_device = device;
	}
	
	public Link getLink() {
		return m_link;
	}

	public void setLink(Link link) {
		m_link = link	;
	}
	
	public boolean hasLink() {
		return m_link != null;
	}

	public boolean hasElement() {
		return m_device != null;
	}
}
