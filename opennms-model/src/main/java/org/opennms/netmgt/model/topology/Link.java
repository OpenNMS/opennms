package org.opennms.netmgt.model.topology;


/**
 * This class represents a physical link between 2 network end points
 * such as an Ethernet connection or a virtual link between 2 end points
 * such as an IP address connection to a subnetwork.  Can also be used
 * represent a network service between to service end points.
 *  
 * @author antonio
 *
 */
public abstract class Link {

	private EndPoint m_a;
	
	private EndPoint m_b;
	
	public EndPoint getA() {
		return m_a;
	}

	public void setA(EndPoint a) {
		this.m_a = a;
	}

	public EndPoint getB() {
		return m_b;
	}

	public void setB(EndPoint b) {
		this.m_b = b;
	}

}
